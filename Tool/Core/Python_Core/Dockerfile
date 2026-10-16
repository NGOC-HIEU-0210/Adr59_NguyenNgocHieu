FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

# Đổi mirror khi cần: docker compose build --build-arg PIP_INDEX_URL=<url> python-core
ARG PIP_INDEX_URL=https://pypi.org/simple
ENV PIP_INDEX_URL=${PIP_INDEX_URL} \
    PIP_DEFAULT_TIMEOUT=200 \
    PIP_RETRIES=10

WORKDIR /app

# Tải file nặng nhất trước, tách layer riêng. Cache mount lưu wheel đã tải xong,
# nên build lại sau khi bị ngắt sẽ không phải tải lại từ đầu.
RUN --mount=type=cache,target=/root/.cache/pip \
    pip install opencv-python-headless numpy

COPY requirements.txt .
RUN --mount=type=cache,target=/root/.cache/pip \
    pip install -r requirements.txt

COPY main.py .
COPY app ./app

RUN useradd -r -u 1001 appuser
USER 1001

EXPOSE 8000
# KHÔNG dùng reload trong container
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]