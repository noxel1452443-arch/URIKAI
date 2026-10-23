"""Remove Kai pay/allowance from session PnL history (keep balance-chart KAI events)."""
from __future__ import annotations

import re
from pathlib import Path

p = Path(__file__).resolve().parents[1] / "shared/src/commonMain/kotlin/com/urikai/app/data/LifetimePnlRepository.kt"
text = p.read_text(encoding="utf-8")

text, n1 = re.subn(
    r"\s*recordSessionPnlHistory\(\s*brandNameJp = \"[^\"]*\"\s*,\s*brandNameEn = \"Kai's pay\"\s*,\s*pnl = amount,\s*\)\s*",
    "\n",
    text,
    count=1,
)
text, n2 = re.subn(
    r"\s*recordSessionPnlHistory\(\s*brandNameJp = \"[^\"]*\"\s*,\s*brandNameEn = \"Kai's allowance\"\s*,\s*pnl = amount,\s*\)\s*",
    "\n",
    text,
    count=1,
)

old_get = """    fun getSessionPnlHistory(): List<SessionPnlHistoryEntry> {
        return sessionHistoryRawLines().mapNotNull { line ->
            val parts = line.split(SESSION_FIELD_SEP)
            if (parts.size < 3) return@mapNotNull null
            val pnl = parts[2].toLongOrNull() ?: return@mapNotNull null
            SessionPnlHistoryEntry(
                brandNameJp = parts[0],
                brandNameEn = parts[1],
                pnl = pnl
            )
        }.asReversed()
    }"""

new_get = """    fun getSessionPnlHistory(): List<SessionPnlHistoryEntry> {
        // お取引明細はプレイ結果中心。カイ給料／お小遣いは残高グラフ側のみ
        return sessionHistoryRawLines().mapNotNull { line ->
            val parts = line.split(SESSION_FIELD_SEP)
            if (parts.size < 3) return@mapNotNull null
            val en = parts[1]
            if (en == "Kai's pay" || en == "Kai's allowance") return@mapNotNull null
            val pnl = parts[2].toLongOrNull() ?: return@mapNotNull null
            SessionPnlHistoryEntry(
                brandNameJp = parts[0],
                brandNameEn = en,
                pnl = pnl
            )
        }.asReversed()
    }"""

if old_get not in text:
    raise SystemExit("getSessionPnlHistory block not found")
text = text.replace(old_get, new_get)
p.write_text(text, encoding="utf-8")
print(f"ok removed pay={n1} allowance={n2} filtered_get=1")
