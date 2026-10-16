import os
from pathlib import Path

from dotenv import load_dotenv

# app/config.py -> app/ -> Python_Core/  (project root, nơi chứa main.py và .env)
BASE_DIR = Path(__file__).resolve().parent.parent
ENV_PATH = BASE_DIR / ".env"

# Chỉ định rõ đường dẫn .env tuyệt đối, không phụ thuộc thư mục bạn đứng khi
# gõ lệnh (cwd). Chỉ có tác dụng khi có file .env (local dev). Trên production,
# biến môi trường do hạ tầng (Docker/CI/secret manager) set thì load_dotenv()
# không override.
load_dotenv(dotenv_path=ENV_PATH)


class Settings:
    """Cấu hình đọc từ biến môi trường. Không hardcode key vào code."""

    GEMINI_API_KEY: str | None = os.environ.get("GEMINI_API_KEY")

    # gemini-2.5-flash đã bị Google dừng cấp cho user mới (lỗi 404 NOT_FOUND).
    # Đổi sang model Google khuyến nghị thay thế.
    GEMINI_MODEL_NAME: str = os.environ.get("GEMINI_MODEL_NAME", "gemini-3.6-flash")

    # Model dự phòng khi model chính quá tải (tùy chọn, để trống = không dùng).
    # Đặt GEMINI_FALLBACK_MODEL trong .env, nhớ chọn model còn hoạt động với key của bạn.
    GEMINI_FALLBACK_MODEL: str | None = os.environ.get("GEMINI_FALLBACK_MODEL") or None


settings = Settings()