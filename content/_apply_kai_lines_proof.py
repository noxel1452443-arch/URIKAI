# -*- coding: utf-8 -*-
"""Apply content/KAI_LINES_PROOF.txt JP lines into HomeKaiLines.kt."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROOF = ROOT / "content/KAI_LINES_PROOF.txt"
SRC = ROOT / "shared/src/commonMain/kotlin/com/urikai/app/game/HomeKaiLines.kt"

LINE_PAT = re.compile(
    r'^([gpi]\d+)\t(.*)$'
)
CALL_PAT = re.compile(
    r'(([gpi])\(\s*")([^"]+)("\s*,\s*\d+\s*,\s*[\d_]+\s*,\s*")((?:\\.|[^"\\])*)("\s*,\s*")((?:\\.|[^"\\])*)("\s*\))'
)


def escape_kt(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def main() -> None:
    updates: dict[str, str] = {}
    for raw in PROOF.read_text(encoding="utf-8").splitlines():
        if not raw or raw.startswith("#"):
            continue
        m = LINE_PAT.match(raw)
        if not m:
            continue
        updates[m.group(1)] = m.group(2)

    text = SRC.read_text(encoding="utf-8")
    changed = 0
    missing: list[str] = []

    def repl(m: re.Match[str]) -> str:
        nonlocal changed
        line_id = m.group(3)
        if line_id not in updates:
            missing.append(line_id)
            return m.group(0)
        new_jp = escape_kt(updates[line_id])
        old_jp = m.group(5)
        if old_jp == new_jp:
            return m.group(0)
        changed += 1
        return f'{m.group(1)}{line_id}{m.group(4)}{new_jp}{m.group(6)}{m.group(7)}{m.group(8)}'

    new_text = CALL_PAT.sub(repl, text)
    unused = sorted(set(updates) - set(re.findall(r'[gpi]\(\s*"([^"]+)"', text)))
    SRC.write_text(new_text, encoding="utf-8")
    print(f"updated {changed} JP lines")
    if unused:
        print("unused proof ids:", ", ".join(unused))
    if missing:
        print("ids in code without proof row:", len(missing))


if __name__ == "__main__":
    main()
