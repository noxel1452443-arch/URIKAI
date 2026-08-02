"""
stages_meta.csv から史実レビュー用 CSV を出力する。

  py -3 tools/export_history_review.py

出力: content/history_review.csv
既存ファイルがある場合、review_* / source_* / fix_notes 列は stage_id でマージして保持する。
"""
from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
META = ROOT / "content" / "stages_meta.csv"
OUT = ROOT / "content" / "history_review.csv"

META_FIELDS = [
    "stage_id",
    "tier",
    "market_kind",
    "brand_name",
    "calendar_start",
    "calendar_end",
    "anchor_when",
    "anchor_label",
    "history_comment",
    "macro_shape",
    "anchor_band_start",
    "anchor_band_end",
]

REVIEW_FIELDS = [
    "review_status",
    "title_ok",
    "period_ok",
    "peek_date_ok",
    "comment_ok",
    "source_url",
    "source_note",
    "reviewer",
    "reviewed_date",
    "fix_notes",
]

CHART_NOTE = (
    "グラフは60秒に圧縮した抽象チャート。"
    "anchor_band_* は転換点の目印（カレンダー日数比例ではない）。"
)

STATUS_PENDING = "pending"


def load_existing_reviews() -> dict[str, dict[str, str]]:
    if not OUT.exists():
        return {}
    with OUT.open(encoding="utf-8-sig", newline="") as f:
        rows = list(csv.DictReader(f))
    return {r["stage_id"].strip(): r for r in rows if r.get("stage_id", "").strip()}


def main() -> None:
    existing = load_existing_reviews()

    with META.open(encoding="utf-8-sig", newline="") as f:
        meta_rows = list(csv.DictReader(f))

    out_fields = META_FIELDS + ["chart_note"] + REVIEW_FIELDS
    out_rows: list[dict[str, str]] = []

    for row in meta_rows:
        sid = row["stage_id"].strip()
        if not sid:
            continue
        prev = existing.get(sid, {})
        out: dict[str, str] = {
            "stage_id": sid,
            "tier": (row.get("tier") or "").strip() or "STANDARD",
            "market_kind": row["market_kind"].strip(),
            "brand_name": row["brand_name"].strip(),
            "calendar_start": row["calendar_start"].strip(),
            "calendar_end": row["calendar_end"].strip(),
            "anchor_when": row["anchor_when"].strip(),
            "anchor_label": row["anchor_label"].strip(),
            "history_comment": row["history_comment"].strip(),
            "macro_shape": row["macro_shape"].strip(),
            "anchor_band_start": row["anchor_band_start"].strip(),
            "anchor_band_end": row["anchor_band_end"].strip(),
            "chart_note": CHART_NOTE,
        }
        for field in REVIEW_FIELDS:
            out[field] = prev.get(field, "").strip() if field != "review_status" else (
                prev.get("review_status", "").strip() or STATUS_PENDING
            )
        out_rows.append(out)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    with OUT.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=out_fields, lineterminator="\n")
        w.writeheader()
        w.writerows(out_rows)
        f.write("\n")

    pending = sum(1 for r in out_rows if r["review_status"] == STATUS_PENDING)
    print(f"wrote {OUT.relative_to(ROOT)} ({len(out_rows)} stages, {pending} pending)")


if __name__ == "__main__":
    main()
