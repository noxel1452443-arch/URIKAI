"""
元絵（生成画像など）を図鑑用の液晶ドット絵に変換する。

出力: app/src/main/res/drawable-nodpi/stage_<stage_id>.png
  - 160x120 ドット（4:3）
  - 背景は透明、インクは濃淡3段階のアルファ（色はアプリ側でテーマのインク色に着色）
  - プレビュー: app/build/art_preview/stage_<stage_id>.png（緑液晶で合成、4倍）

使い方:
  py -3 tools/make_stage_art.py v01 path/to/source.png
  py -3 tools/make_stage_art.py v01 path/to/source.png --levels 215,165,105
"""
from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
PREVIEW_DIR = ROOT / "app" / "build" / "art_preview"

WIDTH, HEIGHT = 160, 120
# 明るい→暗い の閾値（輝度）。これより明るいと透明。
DEFAULT_LEVELS = (215, 165, 105)
ALPHAS = (0, 85, 170, 255)

LCD_BG = (0xC3, 0xCB, 0xA0)
LCD_INK = (0x1F, 0x24, 0x1A)


def to_lcd_mask(src: Image.Image, levels: tuple[int, int, int]) -> Image.Image:
    img = ImageOps.fit(src.convert("RGB"), (WIDTH * 4, HEIGHT * 4), Image.LANCZOS)
    gray = ImageOps.autocontrast(img.convert("L"), cutoff=1)
    gray = gray.resize((WIDTH, HEIGHT), Image.BOX)
    t_light, t_mid, t_dark = levels

    def quantize(v: int) -> int:
        if v >= t_light:
            return ALPHAS[0]
        if v >= t_mid:
            return ALPHAS[1]
        if v >= t_dark:
            return ALPHAS[2]
        return ALPHAS[3]

    alpha = gray.point(quantize)
    out = Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 0))
    out.putalpha(alpha)
    return out


def preview(mask: Image.Image) -> Image.Image:
    bg = Image.new("RGBA", mask.size, LCD_BG + (255,))
    ink = Image.new("RGBA", mask.size, LCD_INK + (255,))
    ink.putalpha(mask.getchannel("A"))
    bg.alpha_composite(ink)
    return bg.resize((WIDTH * 4, HEIGHT * 4), Image.NEAREST)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("stage_id")
    parser.add_argument("source", type=Path)
    parser.add_argument("--levels", default=",".join(map(str, DEFAULT_LEVELS)))
    args = parser.parse_args()

    levels = tuple(int(x) for x in args.levels.split(","))
    if len(levels) != 3:
        raise SystemExit("--levels needs 3 values (light,mid,dark)")

    mask = to_lcd_mask(Image.open(args.source), levels)  # type: ignore[arg-type]
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    out_path = OUT_DIR / f"stage_{args.stage_id}.png"
    mask.save(out_path, optimize=True)
    preview(mask).save(PREVIEW_DIR / f"stage_{args.stage_id}.png")
    print(f"wrote {out_path.relative_to(ROOT)} ({out_path.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
