import cv2
import numpy as np

MAX_SIDE = 1600  # cạnh dài tối đa (px)


def preprocess_image(raw_bytes: bytes) -> bytes:
   
    np_arr = np.frombuffer(raw_bytes, np.uint8)
    img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
    if img is None:
        return raw_bytes

    # Thu nhỏ ảnh quá lớn (ảnh chụp điện thoại): upload nhanh hơn, ít token hơn
    h, w = img.shape[:2]
    longest = max(h, w)
    if longest > MAX_SIDE:
        scale = MAX_SIDE / longest
        img = cv2.resize(
            img, (int(w * scale), int(h * scale)), interpolation=cv2.INTER_AREA
        )

    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    processed = cv2.adaptiveThreshold(
        gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 31, 10
    )

    ok, buffer = cv2.imencode(".png", processed)
    if not ok:
        return raw_bytes
    return buffer.tobytes()