# -*- coding: utf-8 -*-
"""Replace bumper-crop mandarin saga with melt / 'won't melt' failure."""
from __future__ import annotations

import csv
from pathlib import Path

META = Path(__file__).resolve().parent / "stages_meta.csv"

PATCH: dict[str, dict[str, str]] = {
    "v08": {
        "brand_name": "冷凍ミカン、庫内で溶ける",
        "brand_name_en": "Frozen mandarins melt in the freezer",
        "anchor_when": "緊急冷凍で出荷再開",
        "anchor_when_en": "Emergency freeze, shipping resumes",
        "flavor_text": (
            "真夏の停電で冷凍庫が止まり、ミカンがシロップになった。"
            "廃棄が続いたが、町の氷屋と組んだ緊急冷凍で出荷が再開すると、株もじわじわ戻った。"
        ),
        "flavor_text_en": (
            "A midsummer outage stopped the freezers, and the mandarins turned to syrup. "
            "Dumping followed—then a tie-up with the town ice shop restarted shipping, "
            "and the stock edged back."
        ),
        "anchor_label": "事故のあと戻る",
        "anchor_label_en": "Recovers after the accident",
        "history_comment": "大ショックのあとは、ゆっくり戻ることが多い。",
        "history_comment_en": "After a big shock, recovery often comes slowly.",
    },
    "e05": {
        "brand_name": "冷凍ミカン、「溶けない」が溶ける",
        "brand_name_en": "'Won't melt' mandarins melt anyway",
        "anchor_when": "玄関先でまた溶解炎上",
        "anchor_when_en": "Melted again on doorsteps",
        "flavor_text": (
            "前回の停電を反省し、「絶対溶けない配送」を打ち出した。"
            "翌週の猛暑で受け取り遅れが続き、玄関先のシロップ写真がまた拡散。"
            "戻ったばかりの信頼が、もう一度沈んだ。"
        ),
        "flavor_text_en": (
            "After the outage, they promised delivery that would never melt. "
            "A heat wave and late doorstep pickups followed—syrup photos spread again, "
            "and hard-won trust sank a second time."
        ),
        "anchor_label": "戻ったあと再び崩れる",
        "anchor_label_en": "Bounce, then breaks again",
        "history_comment": "戻ったあとに、同じ弱点でまた崩れることがある。",
        "history_comment_en": "It can bounce, then break again on the same weak point.",
    },
}


def main() -> None:
    with META.open(encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)
        fields = list(reader.fieldnames or [])
        rows = list(reader)

    for row in rows:
        sid = row["stage_id"]
        if sid not in PATCH:
            continue
        row.update(PATCH[sid])
        aw = row["anchor_when"]
        if len(aw) > 14:
            raise SystemExit(f"{sid}: anchor_when too long ({len(aw)}): {aw}")

    with META.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=fields, lineterminator="\n")
        w.writeheader()
        w.writerows(rows)

    print("patched", sorted(PATCH))


if __name__ == "__main__":
    main()
