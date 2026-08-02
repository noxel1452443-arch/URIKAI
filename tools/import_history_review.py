"""
history_review.csv の修正を stages_meta.csv に反映する。

  py -3 tools/import_history_review.py

反映列: brand_name, calendar_start, calendar_end, anchor_when, anchor_label, history_comment
（review_status / source_* 等は history_review.csv に残る）
"""
from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
META = ROOT / "content" / "stages_meta.csv"
REVIEW = ROOT / "content" / "history_review.csv"

IMPORT_FIELDS = [
    "brand_name",
    "calendar_start",
    "calendar_end",
    "anchor_when",
    "anchor_label",
    "history_comment",
]


def main() -> None:
    if not REVIEW.exists():
        raise SystemExit(f"Missing {REVIEW}. Run export_history_review.py first.")

    with REVIEW.open(encoding="utf-8-sig", newline="") as f:
        review_by_id = {
            r["stage_id"].strip(): r
            for r in csv.DictReader(f)
            if r.get("stage_id", "").strip()
        }

    with META.open(encoding="utf-8-sig", newline="") as f:
        meta_rows = list(csv.DictReader(f))
        fieldnames = list(meta_rows[0].keys()) if meta_rows else []

    updated = 0
    for row in meta_rows:
        sid = row["stage_id"].strip()
        if not sid:
            continue
        rev = review_by_id.get(sid)
        if not rev:
            continue
        changed = False
        for field in IMPORT_FIELDS:
            val = rev.get(field, "").strip()
            if val and row.get(field, "").strip() != val:
                row[field] = val
                changed = True
        if changed:
            updated += 1

    with META.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=fieldnames, lineterminator="\n")
        w.writeheader()
        w.writerows(meta_rows)
        f.write("\n")

    print(f"updated {updated} stage(s) in {META.relative_to(ROOT)}")
    print("Run: py -3 tools/generate_charts_csv.py")


if __name__ == "__main__":
    main()
