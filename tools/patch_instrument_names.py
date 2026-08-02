"""stages_meta.csv に instrument_name / instrument_name_en を追加・更新する。"""
from __future__ import annotations

import csv
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
META = ROOT / "content" / "stages_meta.csv"

# 史実コメント・イベントと照合した銘柄（抽象カテゴリではなく具体名）
INSTRUMENTS: dict[str, tuple[str, str]] = {
    "v01": ("日経平均", "Nikkei 225"),
    "v02": ("S&P500", "S&P 500"),
    "v03": ("ビットコイン", "Bitcoin"),
    "v04": ("日経平均", "Nikkei 225"),
    "v05": ("英ポンド/米ドル", "GBP/USD"),
    "v06": ("S&P500", "S&P 500"),
    "v07": ("上海総合", "Shanghai Composite"),
    "v08": ("WTI原油", "WTI Crude Oil"),
    "v09": ("ダウ平均", "Dow Jones"),
    "v10": ("ナスダック", "Nasdaq"),
    "z01": ("上海総合", "Shanghai Composite"),
    "z02": ("日経平均", "Nikkei 225"),
    "z03": ("S&P500", "S&P 500"),
    "z04": ("S&P500", "S&P 500"),
    "z05": ("GameStop", "GameStop"),
    "z06": ("英ポンド/米ドル", "GBP/USD"),
    "z07": ("ナスダック", "Nasdaq"),
    "z08": ("米ドル/円", "USD/JPY"),
    "z09": ("EURO STOXX 50", "EURO STOXX 50"),
    "z10": ("S&P500", "S&P 500"),
    "b01": ("日経平均", "Nikkei 225"),
    "b02": ("ナスダック", "Nasdaq"),
    "b03": ("ビットコイン", "Bitcoin"),
    "b04": ("ナスダック", "Nasdaq"),
    "b05": ("日経平均", "Nikkei 225"),
    "b06": ("GameStop", "GameStop"),
    "b07": ("上海総合", "Shanghai Composite"),
    "b08": ("S&P500", "S&P 500"),
    "b09": ("テスラ", "Tesla"),
    "b10": ("ナスダック", "Nasdaq"),
    "e01": ("GameStop", "GameStop"),
    "e02": ("EURO STOXX 50", "EURO STOXX 50"),
    "e03": ("日経平均", "Nikkei 225"),
    "e04": ("S&P500", "S&P 500"),
    "e05": ("WTI原油", "WTI Crude Oil"),
    "e06": ("S&P500", "S&P 500"),
    "e07": ("FTSE100", "FTSE 100"),
    "e08": ("ナスダック", "Nasdaq"),
    "e09": ("GameStop", "GameStop"),
    "e10": ("ダウ平均", "Dow Jones"),
    "e11": ("日経平均", "Nikkei 225"),
    "e12": ("ナスダック", "Nasdaq"),
    "e13": ("ナスダック", "Nasdaq"),
    "e14": ("ビットコイン", "Bitcoin"),
    "e15": ("GameStop", "GameStop"),
}


def main() -> None:
    with META.open(encoding="utf-8", newline="") as f:
        rows = list(csv.DictReader(f))
        fieldnames = list(rows[0].keys()) if rows else []

    if "instrument_name" not in fieldnames:
        # market_kind の直後に挿入
        i = fieldnames.index("market_kind") + 1
        fieldnames[i:i] = ["instrument_name", "instrument_name_en"]
    elif "instrument_name_en" not in fieldnames:
        i = fieldnames.index("instrument_name") + 1
        fieldnames.insert(i, "instrument_name_en")

    missing = []
    for row in rows:
        sid = row["stage_id"].strip()
        if sid not in INSTRUMENTS:
            missing.append(sid)
            continue
        jp, en = INSTRUMENTS[sid]
        row["instrument_name"] = jp
        row["instrument_name_en"] = en

    if missing:
        raise SystemExit(f"No instrument mapping for: {missing}")

    with META.open("w", encoding="utf-8", newline="") as f:
        w = csv.DictWriter(f, fieldnames=fieldnames, quoting=csv.QUOTE_MINIMAL)
        w.writeheader()
        w.writerows(rows)

    print(f"Patched {len(rows)} stages in {META}")


if __name__ == "__main__":
    main()
