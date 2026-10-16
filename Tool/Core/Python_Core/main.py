import asyncio

import uvicorn
from fastapi import FastAPI, File, HTTPException, UploadFile

from app.gemini_service import recognize_formulas
from app.image_processing import preprocess_image
from app.schemas import RecognizeResponse

app = FastAPI(title="Math Graph AI Service")


@app.post("/api/v1/extract-math", response_model=RecognizeResponse)
async def extract_math(image: UploadFile = File(...)):
    raw_bytes = await image.read()

    try:
        # cv2 là tác vụ CPU -> chạy ở thread riêng để không chặn server
        processed_bytes = await asyncio.to_thread(preprocess_image, raw_bytes)
        equations = await recognize_formulas(processed_bytes)

        return RecognizeResponse(
            status="success",
            equations=equations,
            filename=image.filename,
        )

    except HTTPException:
        raise
    except Exception as e:
        # Lỗi thật sự (ảnh hỏng, parse JSON lỗi...) -> trả 500 thay vì 200
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)