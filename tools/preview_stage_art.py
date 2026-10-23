"""既存の stage_*.png（アルファマスク）を液晶色でプレビュー化する。

  py -3 tools/preview_stage_art.py
  py -3 tools/preview_stage_art.py v01 z05

出力: app/build/art_preview/stage_<id>.png（4倍・緑液晶）
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
MASK_DIR = ROOT / "shared" / "src" / "commonMain" / "composeResources" / "drawable"
PREVIEW_DIR = ROOT / "app" / "build" / "art_preview"

LCD_BG = (0xC3, 0xCB, 0xA0)
LCD_INK = (0x1F, 0x24, 0x1A)
SCALE = 4


def preview_mask(mask: Image.Image) -> Image.Image:
    mask = mask.convert("RGBA")
    bg = Image.new("RGBA", mask.size, LCD_BG + (255,))
    ink = Image.new("RGBA", mask.size, LCD_INK + (255,))
    ink.putalpha(mask.getchannel("A"))
    bg.alpha_composite(ink)
    w, h = bg.size
    return bg.resize((w * SCALE, h * SCALE), Image.NEAREST)


def main() -> None:
    ids = [a for a in sys.argv[1:] if not a.startswith("-")]
    paths = sorted(MASK_DIR.glob("stage_*.png"))
    if ids:
        want = {f"stage_{i}.png" for i in ids}
        paths = [p for p in paths if p.name in want]
        missing = want - {p.name for p in paths}
        if missing:
            raise SystemExit(f"missing masks: {sorted(missing)}")

    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    for path in paths:
        out = PREVIEW_DIR / path.name
        preview_mask(Image.open(path)).save(out)
        print(f"wrote {out.relative_to(ROOT)}")
    print(f"{len(paths)} preview(s) in {PREVIEW_DIR.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
