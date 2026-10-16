from pydantic import BaseModel


class RecognizeResponse(BaseModel):
    status: str  # "success" | "error"
    equations: list[str] | None = None
    filename: str | None = None
    message: str | None = None  
