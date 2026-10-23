"""Export sumika-final JPG concepts to composeResources PNG sprites."""
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    import subprocess
    import sys

    subprocess.check_call([sys.executable, "-m", "pip", "install", "pillow", "-q"])
    from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
final = ROOT / "docs" / "kai-concepts" / "sumika-final"
assets = Path(r"C:\Users\reo\.cursor\projects\c-Users-reo-Documents-URIKAI\assets")
out = ROOT / "shared" / "src" / "commonMain" / "composeResources" / "drawable"

for src, dst in [
    (assets / "sumika_g2_shikinuno_pillow.jpg", final / "g2_shikinuno_pillow.jpg"),
    (assets / "sumika_g5_yukimajo.jpg", final / "g5_yukimajo.jpg"),
]:
    if src.exists():
        Image.open(src).convert("RGB").save(dst, quality=95)

jobs = [
    (final / "g1_hoshigusa.jpg", "sumika_g1.png", (240, 120)),
    (final / "g2_shikinuno_pillow.jpg", "sumika_g2.png", (240, 120)),
    (final / "g3_cushion_pillow.jpg", "sumika_g3.png", (240, 120)),
    (final / "g4_usagi_dome.jpg", "sumika_g4.png", (240, 120)),
    (final / "g5_yukimajo.jpg", "sumika_g5.png", (240, 120)),
    (final / "g4_inside.jpg", "sumika_g4_inside.png", (120, 120)),
    (final / "g5_inside.jpg", "sumika_g5_inside.png", (120, 120)),
]

FALLBACK_BG = (0xC3, 0xCB, 0xA0)


def sample_bg(im: Image.Image) -> tuple[int, int, int]:
    w, h = im.size
    samples = [
        im.getpixel((2, 2)),
        im.getpixel((w - 3, 2)),
        im.getpixel((2, h - 3)),
        im.getpixel((w - 3, h - 3)),
        im.getpixel((w // 2, 2)),
        im.getpixel((2, h // 2)),
    ]
    # median-ish average of corner samples
    rs = sorted(s[0] for s in samples)
    gs = sorted(s[1] for s in samples)
    bs = sorted(s[2] for s in samples)
    mid = len(samples) // 2
    return (rs[mid], gs[mid], bs[mid])


def near(c: tuple[int, int, int], bg: tuple[int, int, int], tol: int) -> bool:
    return (
        abs(c[0] - bg[0]) <= tol
        and abs(c[1] - bg[1]) <= tol
        and abs(c[2] - bg[2]) <= tol
    )


def chroma_key(im: Image.Image) -> Image.Image:
    im = im.convert("RGBA")
    bg = sample_bg(im.convert("RGB"))
    # also accept classic paper color
    px = im.load()
    w, h = im.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if near((r, g, b), bg, 32) or near((r, g, b), FALLBACK_BG, 28):
                px[x, y] = (r, g, b, 0)
    # flood-fill from corners for stubborn near-bg
    stack = [(0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)]
    seen = set()
    while stack:
        x, y = stack.pop()
        if (x, y) in seen or not (0 <= x < w and 0 <= y < h):
            continue
        seen.add((x, y))
        r, g, b, a = px[x, y]
        if a == 0:
            pass
        elif near((r, g, b), bg, 40) or near((r, g, b), FALLBACK_BG, 36):
            px[x, y] = (r, g, b, 0)
        else:
            continue
        stack.extend([(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)])
    return im


for src, name, size in jobs:
    im = chroma_key(Image.open(src))
    im = im.resize(size, Image.NEAREST)
    dest = out / name
    im.save(dest)
    print("wrote", dest.name, im.size, "bg~", sample_bg(Image.open(src).convert("RGB")))

print("done")
