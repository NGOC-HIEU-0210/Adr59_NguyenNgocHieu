import asyncio
import hashlib
import json
import random
import re
from collections import OrderedDict

from fastapi import HTTPException
from google import genai
from google.genai import errors, types

from app.config import settings

PROMPT = """Bạn là công cụ nhận diện công thức toán học viết tay.
Hãy nhìn vào ảnh và tìm TẤT CẢ công thức/phương trình toán học xuất hiện trong ảnh.

Yêu cầu:
- Chuyển mỗi công thức thành dạng văn bản chuẩn, có thể tính toán được
  (dùng ^ cho lũy thừa, * cho phép nhân nếu cần, sin/cos/tan/log/sqrt viết thường).
- Nếu công thức là một hàm số, hãy giữ nguyên dạng "y = ..." hoặc "f(x) = ...".
- CHỈ trả về một JSON array (list) các chuỗi công thức, không thêm giải thích,
  không thêm markdown code fence.

Ví dụ output đúng:
["y = x^2 - 2x", "y = sin(x)"]

Nếu không tìm thấy công thức nào, trả về: []
"""

_client: genai.Client | None = None


def _get_client() -> genai.Client:
    global _client
    if _client is None:
        if not settings.GEMINI_API_KEY:
            raise HTTPException(
                status_code=500,
                detail="Thiếu GEMINI_API_KEY trong biến môi trường.",
            )
        _client = genai.Client(
            api_key=settings.GEMINI_API_KEY,
            http_options=types.HttpOptions(timeout=30_000),  # 30s mỗi request
        )
    return _client


def _parse_equations(text: str) -> list[str]:
    """Gemini đôi khi vẫn bọc JSON trong ```json ... ``` dù đã bị cấm trong prompt.
    Cố gắng bóc tách và parse cho chắc."""
    cleaned = text.strip()
    fence_match = re.search(r"```(?:json)?\s*(.*?)\s*```", cleaned, re.DOTALL)
    if fence_match:
        cleaned = fence_match.group(1).strip()

    try:
        data = json.loads(cleaned)
    except json.JSONDecodeError:
        array_match = re.search(r"\[.*\]", cleaned, re.DOTALL)
        if not array_match:
            raise
        data = json.loads(array_match.group(0))

    if not isinstance(data, list):
        raise ValueError("Gemini không trả về một JSON array")
    return [str(item) for item in data]


RETRYABLE_CODES = {429, 500, 502, 503, 504}

# Cache kết quả theo hash ảnh (LRU nhỏ, nằm trong RAM)
_CACHE_MAX = 128
_cache: "OrderedDict[str, list[str]]" = OrderedDict()


async def _generate(client: genai.Client, model: str, image_bytes: bytes):
    # client.aio = bản async, không chặn event loop
    return await client.aio.models.generate_content(
        model=model,
        contents=[
            PROMPT,
            types.Part.from_bytes(data=image_bytes, mime_type="image/png"),
        ],
        config=types.GenerateContentConfig(
            thinking_config=types.ThinkingConfig(thinking_level="minimal"),
            response_mime_type="application/json",
            automatic_function_calling=types.AutomaticFunctionCallingConfig(
                disable=True
            ),
        ),
    )


async def recognize_formulas(image_bytes: bytes) -> list[str]:
    """Gửi ảnh tới Gemini: cache -> retry có jitter -> chuyển model nhanh."""
    key = hashlib.sha256(image_bytes).hexdigest()
    if key in _cache:
        _cache.move_to_end(key)
        return _cache[key]

    client = _get_client()

    models = [settings.GEMINI_MODEL_NAME]
    if settings.GEMINI_FALLBACK_MODEL:
        models.append(settings.GEMINI_FALLBACK_MODEL)
    # Có model dự phòng: mỗi model thử 2 lần rồi đổi. Không có: thử 4 lần.
    tries_per_model = 2 if len(models) > 1 else 4

    last_err: Exception | None = None

    for model in models:
        for attempt in range(tries_per_model):
            try:
                print(f"[DEBUG] {model} lần {attempt + 1}")
                response = await _generate(client, model, image_bytes)
                result = _parse_equations(response.text or "[]")

                _cache[key] = result
                if len(_cache) > _CACHE_MAX:
                    _cache.popitem(last=False)
                return result

            except errors.APIError as e:
                last_err = e
                print(f"[DEBUG]   lỗi {e.code} {e.status}")
                if e.code not in RETRYABLE_CODES:
                    raise HTTPException(
                        status_code=502,
                        detail=f"Gemini lỗi {e.code}: {e.message}",
                    ) from e
            except (json.JSONDecodeError, ValueError) as e:
                # Model trả JSON hỏng -> thử lại coi như lỗi tạm thời
                last_err = e
                print(f"[DEBUG]   JSON hỏng: {e}")
            except Exception as e:  # timeout mạng, đứt kết nối...
                last_err = e
                print(f"[DEBUG]   lỗi mạng/timeout: {type(e).__name__}")

            # Backoff có jitter (0.5-1.0 x), không chặn server vì dùng asyncio.sleep
            await asyncio.sleep((2**attempt) * random.uniform(0.5, 1.0))

    raise HTTPException(
        status_code=503,
        detail="Gemini đang quá tải, vui lòng thử lại sau ít phút.",
    ) from last_err