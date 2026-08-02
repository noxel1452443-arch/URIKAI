"""Add calendar + anchor band columns to content/stages_meta.csv."""
from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
META = ROOT / "content" / "stages_meta.csv"

# calendar_start, calendar_end, anchor_when, band_start, band_end
EXTRA: dict[str, tuple[str, str, str, int, int]] = {
    "v01": ("2020年2月", "2020年4月", "2020年3月19日", 22, 36),
    "v02": ("2008年10月", "2008年12月", "2008年11月21日", 22, 36),
    "v03": ("2017年12月", "2018年2月", "2018年2月6日", 22, 36),
    "v04": ("2011年3月", "2011年4月", "2011年3月15日", 7, 20),
    "v05": ("2022年9月", "2022年10月", "2022年9月28日", 22, 36),
    "v06": ("1998年8月", "1998年10月", "1998年10月8日", 14, 26),
    "v07": ("2015年6月", "2015年8月", "2015年8月26日", 36, 48),
    "v08": ("2020年4月", "2020年5月", "2020年4月20日", 8, 20),
    "v09": ("2001年9月", "2001年10月", "2001年9月17日", 6, 20),
    "v10": ("2023年3月", "2023年4月", "2023年3月13日", 55, 63),
    "z01": ("2015年7月", "2015年8月", "2015年8月11日〜24日", 20, 44),
    "z02": ("2018年9月", "2018年11月", "2018年10月〜11月", 20, 44),
    "z03": ("2022年4月", "2022年6月", "2022年5月4日〜18日", 26, 44),
    "z04": ("2013年5月", "2013年7月", "2013年6月19日", 8, 28),
    "z05": ("2021年1月", "2021年2月", "2021年1月25日〜28日", 50, 62),
    "z06": ("2016年6月", "2016年7月", "2016年6月24日", 20, 44),
    "z07": ("2000年2月", "2000年4月", "2000年3月10日〜24日", 26, 44),
    "z08": ("2024年2月", "2024年4月", "2024年3月19日〜29日", 38, 58),
    "z09": ("2011年7月", "2011年9月", "2011年8月8日〜18日", 50, 62),
    "z10": ("2019年6月", "2019年9月", "2019年8月1日〜23日", 8, 28),
    "b01": ("1989年12月", "1990年3月", "1990年1月4日", 33, 45),
    "b02": ("2000年2月", "2000年4月", "2000年3月10日", 28, 44),
    "b03": ("2017年11月", "2018年2月", "2017年12月17日", 43, 55),
    "b04": ("2021年1月", "2021年3月", "2021年2月12日", 34, 52),
    "b05": ("1989年11月", "1990年1月", "1989年12月29日", 34, 46),
    "b06": ("2021年1月", "2021年2月", "2021年1月28日", 14, 30),
    "b07": ("2015年5月", "2015年7月", "2015年6月29日", 46, 68),
    "b08": ("2007年9月", "2007年11月", "2007年10月9日", 34, 46),
    "b09": ("2020年11月", "2020年12月", "2020年12月21日", 34, 52),
    "b10": ("2022年4月", "2022年6月", "2022年5月5日", 34, 56),
    "e01": ("2021年1月", "2021年2月", "2021年1月28日", 52, 62),
    "e02": ("2011年7月", "2011年9月", "2011年8月8日", 52, 62),
    "e03": ("2024年6月", "2024年8月", "2024年7月31日", 52, 64),
    "e04": ("2018年12月", "2019年1月", "2019年1月4日", 50, 64),
    "e05": ("2020年4月", "2020年5月", "2020年4月28日", 52, 64),
    "e06": ("1998年9月", "1998年10月", "1998年10月15日", 52, 64),
    "e07": ("2016年6月", "2016年6月", "2016年6月24日", 52, 62),
    "e08": ("2021年2月", "2021年2月", "2021年2月25日", 52, 64),
    "e09": ("2021年1月", "2021年2月", "2021年1月28日", 50, 62),
    "e10": ("2010年5月", "2010年5月", "2010年5月6日", 34, 36),
    "e11": ("1989年12月", "1990年1月", "1989年12月29日", 44, 58),
    "e12": ("2000年3月", "2000年4月", "2000年3月14日", 50, 68),
    "e13": ("2023年4月", "2023年5月", "2023年5月25日", 46, 58),
    "e14": ("2017年11月", "2018年1月", "2017年12月17日", 44, 58),
    "e15": ("2021年1月", "2021年1月", "2021年1月27日", 46, 58),
}

NEW_COLS = [
    "calendar_start",
    "calendar_end",
    "anchor_when",
    "anchor_band_start",
    "anchor_band_end",
]


def main() -> None:
    with META.open(encoding="utf-8-sig", newline="") as f:
        rows = list(csv.DictReader(f))
        old_fields = list(rows[0].keys()) if rows else []

    if "calendar_start" in old_fields:
        print("already patched")
        return

    out_fields = []
    for col in old_fields:
        out_fields.append(col)
        if col == "market_kind":
            out_fields.extend(NEW_COLS)

    patched: list[dict[str, str]] = []
    for row in rows:
        sid = row["stage_id"].strip()
        if not sid:
            continue
        if sid not in EXTRA:
            raise SystemExit(f"missing calendar for {sid}")
        cal_start, cal_end, anchor_when, band_start, band_end = EXTRA[sid]
        row["calendar_start"] = cal_start
        row["calendar_end"] = cal_end
        row["anchor_when"] = anchor_when
        row["anchor_band_start"] = str(band_start)
        row["anchor_band_end"] = str(band_end)
        patched.append(row)

    with META.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=out_fields, lineterminator="\n")
        w.writeheader()
        for row in patched:
            w.writerow({k: row.get(k, "") for k in out_fields})
        f.write("\n")

    print(f"patched {len(patched)} stages -> {META}")


if __name__ == "__main__":
    main()
