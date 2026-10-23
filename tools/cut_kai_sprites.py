"""Kai JPGs → 図鑑と同じインクマスクPNG（テーマの Ink で着色）。

- 紙背景だけ flood で抜く
- 輝度→アルファ3段階（make_stage_art と同じ発想）
- 全差分を同じセル（高さ・足元・重心X）に収めて差し替えジャンプを抑える
"""
from __future__ import annotations

import math
from collections import deque
from pathlib import Path

from PIL import Image

SRC = Path(r"C:\Users\reo\.cursor\projects\c-Users-reo-Documents-URIKAI\assets")
ROOT = Path(r"C:\Users\reo\Documents\URIKAI")
DST = ROOT / "shared" / "src" / "commonMain" / "composeResources" / "drawable"
DOCS = ROOT / "docs" / "kai-concepts"
PREVIEW = ROOT / "app" / "build" / "art_preview"
DOCS.mkdir(parents=True, exist_ok=True)
PREVIEW.mkdir(parents=True, exist_ok=True)

NAMES = [
    "kai_face_talk",
    "kai_face_smile",
    "kai_face_think",
    "kai_face_wink",
    "kai_face_ouch",
    "kai_face_cry",
    "kai_pose_wave",
    "kai_pose_point_up",
]

# 表示 ~92dp に対して余裕を持たせ、1024→小さすぎる縮小でのガビつきを抑える
OUT = 160
# 本体の高さ上限・胴体赤道幅（体以外の浮遊要素はスケール基準に入れない）
TARGET_H = 128
TARGET_BODY_W = 88
FOOT_PAD = 10

LCD_BG = (0xC3, 0xCB, 0xA0)
LCD_INK = (0x1F, 0x24, 0x1A)
EPAPER_BG = (0xE6, 0xE4, 0xDC)
EPAPER_INK = (0x2A, 0x2A, 0x28)


def corner_bg(im: Image.Image) -> tuple[float, float, float]:
    w, h = im.size
    samples = [
        im.getpixel((1, 1)),
        im.getpixel((w - 2, 1)),
        im.getpixel((1, h - 2)),
        im.getpixel((w - 2, h - 2)),
        im.getpixel((w // 2, 1)),
        im.getpixel((1, h // 2)),
    ]
    return (
        sum(s[0] for s in samples) / len(samples),
        sum(s[1] for s in samples) / len(samples),
        sum(s[2] for s in samples) / len(samples),
    )


def bg_dist(r: int, g: int, b: int, br: float, bg: float, bb: float) -> float:
    return math.sqrt((r - br) ** 2 + (g - bg) ** 2 + (b - bb) ** 2)


def is_bg(r: int, g: int, b: int, br: float, bg: float, bb: float) -> bool:
    d = bg_dist(r, g, b, br, bg, bb)
    if d < 28:
        return True
    lum = (r + g + b) / 3
    if lum > 205 and g >= r - 8 and g > b and b > 140 and d < 45:
        return True
    if r > 235 and g > 235 and b > 230:
        return True
    return False


def flood_bg_mask(im: Image.Image, br: float, bg: float, bb: float) -> list[list[bool]]:
    w, h = im.size
    px = im.load()
    vis = [[False] * w for _ in range(h)]
    q: deque[tuple[int, int]] = deque()
    for x, y in (
        (0, 0),
        (w - 1, 0),
        (0, h - 1),
        (w - 1, h - 1),
        (w // 2, 0),
        (0, h // 2),
        (w - 1, h // 2),
        (w // 2, h - 1),
    ):
        if is_bg(*px[x, y][:3], br, bg, bb):
            q.append((x, y))
            vis[y][x] = True
    while q:
        x, y = q.popleft()
        for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
            if 0 <= nx < w and 0 <= ny < h and not vis[ny][nx]:
                if is_bg(*px[nx, ny][:3], br, bg, bb):
                    vis[ny][nx] = True
                    q.append((nx, ny))
    return vis


def cut_rgba(im: Image.Image) -> Image.Image:
    br, bgc, bb = corner_bg(im)
    mask = flood_bg_mask(im, br, bgc, bb)
    w, h = im.size
    px = im.load()
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    op = out.load()
    for y in range(h):
        for x in range(w):
            if mask[y][x]:
                continue
            r, g, b, _ = px[x, y]
            op[x, y] = (r, g, b, 255)
    return out


# 本体フィルはインク0＝液晶下地そのもの（ガイド枠と同じ明るさ）。
# 影・線・目だけインク（グラデーション感は残す）
INK_BODY = 0
INK_MID = 110
INK_LINE = 255


def to_packed_mask(rgb_cut: Image.Image) -> Image.Image:
    """作業用: A=255 シルエット、R=インク量。"""
    w, h = rgb_cut.size
    px = rgb_cut.load()
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    op = out.load()
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a < 16:
                continue
            lum = (r + g + b) / 3
            if lum >= 230 and min(r, g, b) > 200:
                ink = 0
            elif lum < 70:
                ink = INK_LINE
            elif lum < 130:
                ink = INK_MID
            else:
                ink = INK_BODY
            op[x, y] = (ink, ink, ink, 255)
    return out


def quantize_packed(im: Image.Image) -> Image.Image:
    px = im.load()
    w, h = im.size
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    op = out.load()
    for y in range(h):
        for x in range(w):
            r, _g, _b, a = px[x, y]
            if a < 40:
                continue
            v = r
            if v < 24:
                ink = 0
            elif v < 80:
                ink = INK_BODY
            elif v < 180:
                ink = INK_MID
            else:
                ink = INK_LINE
            op[x, y] = (ink, ink, ink, 255)
    return out


def split_sil_and_ink(packed: Image.Image) -> tuple[Image.Image, Image.Image]:
    """アプリ用2枚: sil=不透明輪郭 / ink=図鑑と同じアルファマスク（薄い本体）。"""
    w, h = packed.size
    px = packed.load()
    sil = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    ink = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    sp = sil.load()
    ip = ink.load()
    for y in range(h):
        for x in range(w):
            r, _g, _b, a = px[x, y]
            if a < 128:
                continue
            sp[x, y] = (0, 0, 0, 255)
            if r > 0:
                ip[x, y] = (0, 0, 0, r)
    return sil, ink


def connected_components(mask: Image.Image) -> list[tuple[int, Image.Image, tuple[int, int, int, int]]]:
    """[(面積, 成分RGBA, bbox), ...] 面積降順。"""
    w, h = mask.size
    a = mask.load()
    vis = [[False] * w for _ in range(h)]
    comps: list[tuple[int, Image.Image, tuple[int, int, int, int]]] = []
    for y in range(h):
        for x in range(w):
            if vis[y][x] or a[x, y][3] <= 16:
                continue
            q: deque[tuple[int, int]] = deque([(x, y)])
            vis[y][x] = True
            pts: list[tuple[int, int]] = []
            while q:
                cx, cy = q.popleft()
                pts.append((cx, cy))
                for nx, ny in ((cx + 1, cy), (cx - 1, cy), (cx, cy + 1), (cx, cy - 1)):
                    if 0 <= nx < w and 0 <= ny < h and not vis[ny][nx] and a[nx, ny][3] > 16:
                        vis[ny][nx] = True
                        q.append((nx, ny))
            xs = [p[0] for p in pts]
            ys = [p[1] for p in pts]
            bbox = (min(xs), min(ys), max(xs) + 1, max(ys) + 1)
            layer = Image.new("RGBA", (w, h), (0, 0, 0, 0))
            lp = layer.load()
            for px, py in pts:
                lp[px, py] = a[px, py]
            comps.append((len(pts), layer, bbox))
    comps.sort(key=lambda t: t[0], reverse=True)
    return comps


def body_equator_width(mask: Image.Image, bbox: tuple[int, int, int, int]) -> int:
    """胴体の赤道幅（耳・浮遊ドットを除いた帯の最大横幅）。"""
    x0, y0, x1, y1 = bbox
    a = mask.load()
    h = y1 - y0
    # 足元側〜中腹（耳より下）
    band0 = y0 + int(h * 0.40)
    band1 = y0 + int(h * 0.82)
    best = 1
    for y in range(band0, max(band0 + 1, band1)):
        left, right = None, None
        for x in range(x0, x1):
            if a[x, y][3] > 16:
                if left is None:
                    left = x
                right = x
        if left is not None and right is not None:
            best = max(best, right - left + 1)
    return best


def place_unified(mask: Image.Image) -> Image.Image:
    """本体（最大連結成分）の胴体幅でスケール。考えごとドット等は同じ倍率で添える。"""
    comps = connected_components(mask)
    if not comps:
        return Image.new("RGBA", (OUT, OUT), (0, 0, 0, 0))

    main_area, main_layer, main_bbox = comps[0]
    # 本体の 2% 未満は「体以外」（think の点々など）
    extras = [c for c in comps[1:] if c[0] < main_area * 0.02]

    body_w = body_equator_width(main_layer, main_bbox)
    mx0, my0, mx1, my1 = main_bbox
    main_h = my1 - my0
    # 胴体幅を基準。高さ上限も守りセルに収める
    scale = TARGET_BODY_W / body_w
    if main_h * scale > TARGET_H:
        scale = TARGET_H / main_h

    def scale_layer(layer: Image.Image, bbox: tuple[int, int, int, int]) -> tuple[Image.Image, tuple[int, int, int, int]]:
        x0, y0, x1, y1 = bbox
        cropped = layer.crop(bbox)
        nw = max(1, int(round((x1 - x0) * scale)))
        nh = max(1, int(round((y1 - y0) * scale)))
        # 縮小は BOX（平均）→ あとで量子化。NEAREST だと生成ディテールがガビる
        return cropped.resize((nw, nh), Image.Resampling.BOX), (x0, y0, x1, y1)

    main_r, _ = scale_layer(main_layer, main_bbox)
    # 元座標での本体足元・重心（スケール前）→ 配置基準
    ma = main_layer.load()
    mxs, mys = [], []
    for y in range(my0, my1):
        for x in range(mx0, mx1):
            if ma[x, y][3] > 16:
                mxs.append(x)
                mys.append(y)
    main_cx = sum(mxs) / len(mxs)
    main_foot = max(mys)

    # キャンバス上の本体位置（足元・重心X固定）
    main_a = main_r.getchannel("A")
    rxs, rys = [], []
    for y in range(main_r.size[1]):
        for x in range(main_r.size[0]):
            if main_a.getpixel((x, y)) > 16:
                rxs.append(x)
                rys.append(y)
    r_cx = sum(rxs) / len(rxs)
    r_foot = max(rys)

    canvas = Image.new("RGBA", (OUT, OUT), (0, 0, 0, 0))
    paste_x = int(round(OUT / 2 - r_cx))
    paste_y = OUT - FOOT_PAD - r_foot - 1
    paste_x = max(-main_r.size[0] // 4, min(OUT - main_r.size[0] + main_r.size[0] // 4, paste_x))
    paste_y = max(0, min(OUT - main_r.size[1], paste_y))
    canvas.paste(main_r, (paste_x, paste_y), main_r)

    # 余分な小要素：本体と同じ倍率・相対位置で添える
    for _area, layer, bbox in extras:
        er, (ex0, ey0, _ex1, _ey1) = scale_layer(layer, bbox)
        px = paste_x + int(round((ex0 - mx0) * scale))
        py = paste_y + int(round((ey0 - my0) * scale))
        canvas.paste(er, (px, py), er)

    return quantize_packed(canvas)


def preview_on(sil: Image.Image, ink: Image.Image, bg: tuple[int, int, int], ink_rgb: tuple[int, int, int]) -> Image.Image:
    """sil を下地色で塗り、ink アルファでインクを重ねる（アプリと同じ）。"""
    canvas = Image.new("RGBA", sil.size, (0, 0, 0, 0))
    plate = Image.new("RGBA", sil.size, bg + (255,))
    plate.putalpha(sil.getchannel("A"))
    canvas.alpha_composite(plate)
    layer = Image.new("RGBA", ink.size, ink_rgb + (255,))
    layer.putalpha(ink.getchannel("A"))
    canvas.alpha_composite(layer)
    # プレビュー用に紙色背景
    paper = Image.new("RGBA", sil.size, bg + (255,))
    paper.alpha_composite(canvas)
    return paper.resize((OUT * 4, OUT * 4), Image.NEAREST)


def main() -> None:
    for n in NAMES:
        im = Image.open(SRC / f"{n}.jpg").convert("RGBA")
        packed = place_unified(to_packed_mask(cut_rgba(im)))
        sil, ink = split_sil_and_ink(packed)
        ink.save(DST / f"{n}.png", optimize=True)
        sil.save(DST / f"{n}_sil.png", optimize=True)
        ink.save(DOCS / f"{n}_cut.png", optimize=True)
        sil.save(DOCS / f"{n}_sil.png", optimize=True)
        preview_on(sil, ink, LCD_BG, LCD_INK).save(PREVIEW / f"{n}_gb.png")
        preview_on(sil, ink, EPAPER_BG, EPAPER_INK).save(PREVIEW / f"{n}_epaper.png")
        preview_on(sil, ink, LCD_BG, LCD_INK).save(DOCS / f"{n}_preview.png")

        a = sil.getchannel("A")
        xs, ys = [], []
        for y in range(OUT):
            for x in range(OUT):
                if a.getpixel((x, y)) > 16:
                    xs.append(x)
                    ys.append(y)
        ink_a = ink.getchannel("A")
        levels = {}
        for v in ink_a.get_flattened_data():
            if v:
                levels[v] = levels.get(v, 0) + 1
        print(
            f"{n}: foot={max(ys)} cx={sum(xs)/len(xs):.1f} "
            f"sil={sum(1 for v in a.get_flattened_data() if v>0)} ink_levels={levels}"
        )


if __name__ == "__main__":
    main()
