import csv
from pathlib import Path

rows = list(csv.DictReader(Path("stages_meta.csv").open(encoding="utf-8-sig")))
out: list[str] = []
out.append("URIKAI チャート出来事テキスト一覧（推敲用）")
out.append("出典: content/stages_meta.csv")
out.append(
    "編集対象JP: brand_name / instrument_name / anchor_when / "
    "flavor_text / history_comment / anchor_label"
)
out.append("英語も併記。推敲後は stages_meta.csv に戻し、py -3 tools/generate_charts_csv.py を実行。")
out.append("=" * 72)
out.append("")

for i, r in enumerate(rows, 1):
    sid = r["stage_id"]
    ep = r.get("saga_ep", "")
    tier = r.get("tier", "") or "-"
    out.append(f"### [{i:02d}] {sid}  saga_ep={ep}  tier={tier}")
    out.append(f"period: {r['period']} / {r['period_en']}")
    out.append(f"instrument: {r['instrument_name']} / {r['instrument_name_en']}")
    out.append("")
    out.append("【見出し brand_name】")
    out.append(r["brand_name"])
    out.append(r["brand_name_en"])
    out.append("")
    out.append("【出来事（転換点） anchor_when】")
    out.append(r["anchor_when"])
    out.append(r["anchor_when_en"])
    out.append("")
    out.append("【フレーバー（図鑑掘り下げ） flavor_text】")
    out.append(r["flavor_text"])
    out.append(r["flavor_text_en"])
    out.append("")
    out.append("【波のコツ（短いラベル） anchor_label】")
    out.append(r["anchor_label"])
    out.append(r["anchor_label_en"])
    out.append("")
    out.append("【締めのひとこと（予備・画面非表示） history_comment】")
    out.append(r["history_comment"])
    out.append(r["history_comment_en"])
    out.append("")
    out.append("-" * 72)
    out.append("")

text = "\n".join(out)
Path("FLAVOR_COPY_FOR_EDIT.txt").write_text(text, encoding="utf-8")
print(f"written FLAVOR_COPY_FOR_EDIT.txt chars={len(text)} stages={len(rows)}")
