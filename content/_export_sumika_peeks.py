"""g4/g5 のぞき穴: 内観は grade 固定、sit/sleep はカイだけ差し替え・サイズ調整。

透過は一切出さない（輪郭も不透明）。内観は BOX 縮小で粒状ノイズを抑え、
カイは元 160px から整数寄りに縮小してガビを避ける。
"""
from __future__ import annotations

from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageOps

ROOT = Path(__file__).resolve().parents[1]
docs = ROOT / "docs/kai-concepts/sumika-final"
out = ROOT / "shared/src/commonMain/composeResources/drawable"
dbg = docs / "_peek_debug"
dbg.mkdir(exist_ok=True)

# のぞき穴 UI は ~136dp。2x でほぼ整数倍になるよう 256px。
OUT = 256
# 内観の切り出しは grade 共通（sit/sleep で変えない）
ROOM_ZOOM = {"g4": 1.06, "g5": 1.06}
# カイ。元 160px → NEAREST。sit 基準に sleep を揃え、grade ごとに微調整。
KAI_SIZE = {
    ("g4", "sit"): 138,
    ("g4", "sleep"): 152,
    ("g5", "sit"): 128,  # ほんの少し小さく
    ("g5", "sleep"): 154,
}
# 足元余白（下からの px）。大きいほど画面上へ。
KAI_BOTTOM = {
    ("g4", "sit"): 30,
    ("g4", "sleep"): 12,  # あとほんの少し下げ
    ("g5", "sit"): 56,  # もう少し上＝布団の上
    ("g5", "sleep"): 34,
}
OUTLINE = (40, 40, 40, 255)


def find_room_radius(a: np.ndarray) -> tuple[float, float, float]:
    h, w = a.shape
    cy, cx = (h - 1) / 2.0, (w - 1) / 2.0
    radii = []
    for ang in np.linspace(0, 2 * np.pi, 720, endpoint=False):
        best = 0
        for r in range(int(min(cx, cy)) - 6, 40, -1):
            x = int(round(cx + r * np.cos(ang)))
            y = int(round(cy + r * np.sin(ang)))
            if not (2 <= x < w - 2 and 2 <= y < h - 2):
                continue
            if a[y, x] < 190:
                best = r
                break
        if best:
            radii.append(best)
    r = float(np.percentile(radii, 50))
    r = min(r, min(cx, cy) * 0.92)
    return cx, cy, r


def soft_quantize(g: Image.Image) -> Image.Image:
    """8階調。4階調だと壁の粒が「まばら透過」に見えやすい。"""
    a = np.array(g, dtype=np.float32)
    a = np.clip((a - 12) * 1.06, 0, 255)
    levels = np.array([36.0, 72.0, 104.0, 132.0, 156.0, 176.0, 196.0, 220.0], dtype=np.float32)
    idx = np.abs(a[..., None] - levels).argmin(axis=-1)
    return Image.fromarray(levels[idx].astype(np.uint8), mode="L")


def make_room(grade: str) -> Image.Image:
    src = docs / f"{grade}_inside.jpg"
    g = ImageOps.grayscale(Image.open(src).convert("RGB"))
    g = ImageEnhance.Contrast(g).enhance(1.05)
    a = np.array(g, dtype=np.float32)
    cx, cy, r = find_room_radius(a)
    yy, xx = np.ogrid[: a.shape[0], : a.shape[1]]
    rm = (xx - cx) ** 2 + (yy - cy) ** 2 <= (r * 0.98) ** 2
    wall = int(np.median(a[rm & (a > 130) & (a < 220)])) if rm.any() else 168
    paper = int(np.median(a[~rm])) if (~rm).any() else 210
    zoom = ROOM_ZOOM[grade]
    view_r = r / zoom
    side = max(8, int(np.ceil(view_r * 2)))
    left = int(round(cx - side / 2))
    top = int(round(cy - side / 2))
    pad = ImageOps.expand(g, border=side, fill=int(paper))
    crop = pad.crop((left + side, top + side, left + 2 * side, top + 2 * side))
    # BOX 平均縮小＋弱いメディアンで壁の粒（透過に見えるノイズ）を抑える
    crop = crop.resize((OUT, OUT), Image.BOX)
    crop = crop.filter(ImageFilter.MedianFilter(size=3))
    crop = soft_quantize(crop)
    ca = np.array(crop, dtype=np.uint8)
    jj, ii = np.meshgrid(np.arange(OUT), np.arange(OUT))
    ocx = ocy = (OUT - 1) / 2.0
    rad = OUT / 2.0 - 2.0
    in_circ = (jj - ocx) ** 2 + (ii - ocy) ** 2 <= rad**2
    out_a = np.full((OUT, OUT), wall, dtype=np.uint8)
    out_a[in_circ] = ca[in_circ]
    rgb = np.stack([out_a, out_a, out_a], axis=-1)
    rgba = np.dstack([rgb, np.full((OUT, OUT), 255, dtype=np.uint8)])
    img = Image.fromarray(rgba, mode="RGBA")
    ImageDraw.Draw(img).ellipse(
        [1.5, 1.5, OUT - 2.5, OUT - 2.5],
        outline=OUTLINE,
        width=2,
    )
    # 輪郭描画後も全面不透明を保証
    arr = np.array(img)
    arr[:, :, 3] = 255
    return Image.fromarray(arr, mode="RGBA")


def make_kai(pose: str, size: int) -> Image.Image:
    """顔・wave と同系の明るい本体＋薄いインク。"""
    sil = np.array(Image.open(out / f"kai_pose_{pose}_sil.png").convert("RGBA"))
    ink = np.array(Image.open(out / f"kai_pose_{pose}.png").convert("RGBA"))
    kai = np.zeros((160, 160, 4), dtype=np.uint8)
    body = sil[:, :, 3] > 128
    kai[body, 0] = 208
    kai[body, 1] = 208
    kai[body, 2] = 208
    kai[body, 3] = 255
    ia = ink[:, :, 3]
    hard = ia >= 200
    mid = (ia > 0) & (ia < 200)
    kai[hard, 0:3] = 36
    kai[hard, 3] = 255
    kai[mid, 0:3] = 72
    kai[mid, 3] = 255
    return Image.fromarray(kai, "RGBA").resize((size, size), Image.NEAREST)


def export(grade: str, pose: str) -> None:
    room = make_room(grade)
    size = KAI_SIZE[(grade, pose)]
    kai = make_kai(pose, size)
    canvas = room.copy()
    kx = (OUT - size) // 2
    ky = OUT - size - KAI_BOTTOM[(grade, pose)]
    canvas.alpha_composite(kai, (kx, ky))
    arr = np.array(canvas)
    jj, ii = np.meshgrid(np.arange(OUT), np.arange(OUT))
    ocx = ocy = (OUT - 1) / 2.0
    in_circ = (jj - ocx) ** 2 + (ii - ocy) ** 2 <= (OUT / 2.0 - 2.0) ** 2
    wall = int(np.median(arr[in_circ, 0])) if in_circ.any() else 168
    arr[~in_circ, 0:3] = wall
    arr[:, :, 3] = 255
    img = Image.fromarray(arr, mode="RGBA")
    ImageDraw.Draw(img).ellipse(
        [1.5, 1.5, OUT - 2.5, OUT - 2.5],
        outline=OUTLINE,
        width=2,
    )
    arr = np.array(img)
    arr[:, :, 3] = 255
    img = Image.fromarray(arr, mode="RGBA")
    name = f"sumika_{grade}_peek_{pose}.png"
    img.save(out / name)
    img.save(dbg / name)
    print(
        name,
        "kai",
        size,
        "bottom",
        KAI_BOTTOM[(grade, pose)],
        "out",
        OUT,
    )


def main() -> None:
    for grade in ("g4", "g5"):
        for pose in ("sit", "sleep"):
            export(grade, pose)
    print("ok")


if __name__ == "__main__":
    main()
