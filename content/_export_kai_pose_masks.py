"""Convert kai pose JPG → solid sil + ink masks (160x160). sil は外形を塗りつぶし透け防止。"""
from collections import deque
from pathlib import Path

try:
    from PIL import Image, ImageFilter
except ImportError:
    import subprocess
    import sys

    subprocess.check_call([sys.executable, "-m", "pip", "install", "pillow", "-q"])
    from PIL import Image, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
assets = Path(r"C:\Users\reo\.cursor\projects\c-Users-reo-Documents-URIKAI\assets")
out = ROOT / "shared" / "src" / "commonMain" / "composeResources" / "drawable"
docs = ROOT / "docs" / "kai-concepts"

jobs = [
    ("kai_pose_sit.jpg", "kai_pose_sit"),
    ("kai_pose_sleep.jpg", "kai_pose_sleep"),
]


def near_bg(r, g, b, bg, tol=42):
    return abs(r - bg[0]) <= tol and abs(g - bg[1]) <= tol and abs(b - bg[2]) <= tol


def flood_exterior(is_char, w, h):
    """キャラでない画素を辺から塗り、外側背景を True に。"""
    exterior = [[False] * w for _ in range(h)]
    q = deque()
    for x in range(w):
        for y in (0, h - 1):
            if not is_char[y][x]:
                exterior[y][x] = True
                q.append((x, y))
    for y in range(h):
        for x in (0, w - 1):
            if not is_char[y][x] and not exterior[y][x]:
                exterior[y][x] = True
                q.append((x, y))
    while q:
        x, y = q.popleft()
        for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
            if 0 <= nx < w and 0 <= ny < h and not exterior[ny][nx] and not is_char[ny][nx]:
                exterior[ny][nx] = True
                q.append((nx, ny))
    return exterior


def export_one(src: Path, key: str) -> None:
    im = Image.open(src).convert("RGBA")
    w, h = im.size
    samples = [
        im.getpixel((2, 2))[:3],
        im.getpixel((w - 3, 2))[:3],
        im.getpixel((2, h - 3))[:3],
        im.getpixel((w - 3, h - 3))[:3],
    ]
    bg = (
        sorted(c[0] for c in samples)[1],
        sorted(c[1] for c in samples)[1],
        sorted(c[2] for c in samples)[1],
    )
    # 顔/wave と同じ3段階（tools/cut_kai_sprites.py）
    INK_BODY = 0
    INK_MID = 110
    INK_LINE = 255

    px = im.load()
    is_char = [[False] * w for _ in range(h)]
    ink_a = [[0] * w for _ in range(h)]
    for y in range(h):
        for x in range(w):
            r, g, b, _a = px[x, y]
            if near_bg(r, g, b, bg, 42) or near_bg(r, g, b, (0xC3, 0xCB, 0xA0), 34):
                continue
            is_char[y][x] = True
            lum = (r + g + b) / 3.0
            if lum >= 230 and min(r, g, b) > 200:
                ink_a[y][x] = INK_BODY
            elif lum < 70:
                ink_a[y][x] = INK_LINE
            elif lum < 130:
                ink_a[y][x] = INK_MID
            else:
                ink_a[y][x] = INK_BODY

    # 小さな穴・途切れを埋めるため一度ぼかして二値化
    char_img = Image.new("L", (w, h), 0)
    cp = char_img.load()
    for y in range(h):
        for x in range(w):
            if is_char[y][x]:
                cp[x, y] = 255
    char_img = char_img.filter(ImageFilter.MaxFilter(3)).filter(ImageFilter.MinFilter(3))
    cp = char_img.load()
    for y in range(h):
        for x in range(w):
            is_char[y][x] = cp[x, y] > 127

    exterior = flood_exterior(is_char, w, h)
    ink = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    sil = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    ip = ink.load()
    sp = sil.load()
    for y in range(h):
        for x in range(w):
            # 外側でない = キャラ本体（内部の抜けも塗りつぶし）
            if not exterior[y][x]:
                sp[x, y] = (0, 0, 0, 255)
            a = ink_a[y][x]
            if a > 0:
                ip[x, y] = (0, 0, 0, a)

    bbox = sil.getbbox()
    if bbox is None:
        raise SystemExit(f"empty mask: {key}")
    pad = 6
    l, t, r, b = bbox
    l, t = max(0, l - pad), max(0, t - pad)
    r, b = min(w, r + pad), min(h, b + pad)
    ink_c = ink.crop((l, t, r, b))
    sil_c = sil.crop((l, t, r, b))
    cw, ch = ink_c.size
    scale = min(144 / cw, 144 / ch)
    nw, nh = max(1, int(cw * scale)), max(1, int(ch * scale))
    ink_r = ink_c.resize((nw, nh), Image.NEAREST)
    sil_r = sil_c.resize((nw, nh), Image.NEAREST)
    ink_o = Image.new("RGBA", (160, 160), (0, 0, 0, 0))
    sil_o = Image.new("RGBA", (160, 160), (0, 0, 0, 0))
    ox, oy = (160 - nw) // 2, (160 - nh) // 2
    # mask 付き paste はアルファを乗算して薄く／変質させるので直置き
    ink_o.paste(ink_r, (ox, oy))
    sil_o.paste(sil_r, (ox, oy))
    ink_o.save(out / f"{key}.png")
    sil_o.save(out / f"{key}_sil.png")

    # LCD 合成プレビュー（巣の上でも透けないか確認用）
    lcd = (198, 206, 160, 255)
    ink_c = (40, 48, 36, 255)
    prev = Image.new("RGBA", (160, 160), (160, 168, 140, 255))  # 巣っぽい地
    for y in range(160):
        for x in range(160):
            if sil_o.getpixel((x, y))[3] > 20:
                prev.putpixel((x, y), lcd)
            if ink_o.getpixel((x, y))[3] > 20:
                prev.putpixel((x, y), ink_c)
    prev.resize((320, 320), Image.NEAREST).save(docs / f"{key}_lcd_preview.png")
    print("wrote", key, "sil_opa", sum(1 for p in sil_o.getdata() if p[3] > 200))


for src_name, key in jobs:
    export_one(assets / src_name, key)
print("done")
