# -*- coding: utf-8 -*-
"""Export HomeKaiLines JP copy for proofreading."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "shared/src/commonMain/kotlin/com/urikai/app/game/HomeKaiLines.kt"
OUT = ROOT / "content/KAI_LINES_PROOF.txt"

PAT = re.compile(
    r'([gpi])\(\s*"([^"]+)"\s*,\s*\d+\s*,\s*[\d_]+\s*,\s*"((?:\\.|[^"\\])*)"\s*,\s*"((?:\\.|[^"\\])*)"\s*\)'
)


def main() -> None:
    text = SRC.read_text(encoding="utf-8")
    rows = PAT.findall(text)
    lines = [
        "# カイ一言 校正用（日本語）",
        "# 各行: id<TAB>日本語セリフ",
        "# 直したい行だけ書き換えて保存し、反映を依頼してください。",
        "# EN は別途合わせます。",
        "",
    ]
    for _kind, line_id, jp, _en in rows:
        jp = jp.encode("utf-8").decode("unicode_escape") if "\\" in jp else jp
        # keep simple: only unescape common sequences
        jp = jp.replace(r"\n", "\n").replace(r"\"", '"')
        lines.append(f"{line_id}\t{jp}")
    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"wrote {len(rows)} lines -> {OUT}")


if __name__ == "__main__":
    main()
