#!/usr/bin/env python3
"""
案C: HUMAN MAX — 平滑化したマクロ山・谷をラグ/CD付きで完璧に取った理論値。

  py -3 tools/simulate_human_max.py

出力:
  tools/human_max_report.txt
  content/chart_overview_human_max.png
"""
from __future__ import annotations

import csv
import statistics
from collections import defaultdict
from pathlib import Path

import matplotlib.pyplot as plt
from matplotlib import rcParams

ROOT = Path(__file__).resolve().parents[1]
CHARTS = ROOT / "app" / "src" / "main" / "assets" / "charts.csv"
META = ROOT / "content" / "stages_meta.csv"
REPORT = ROOT / "tools" / "human_max_report.txt"
OUT_PNG = ROOT / "content" / "chart_overview_human_max.png"

INITIAL_ASSET = 10_000
DURATION = 60
PLAY_SECONDS = 60
BASE_RATIO = 100.0
INITIAL_DISPLAY = 1_000
TOTAL_RATE = 0.045
ORDER_LAG_SEC = 1
ORDER_COOLDOWN_SEC = 1
MA_SEC = 3
MIN_SWING_FRAC = 0.02  # ピボット間の最小相対振幅（ノイズ山を落とす）


def ratio_at(ratios: list[float], absolute_seconds: float) -> float:
    if absolute_seconds <= 0:
        return ratios[0]
    last = len(ratios) - 1
    if absolute_seconds >= last:
        return ratios[-1]
    i0 = int(absolute_seconds)
    frac = absolute_seconds - i0
    return ratios[i0] + (ratios[i0 + 1] - ratios[i0]) * frac


def display_price(ratio: float) -> int:
    return max(1, int(ratio / BASE_RATIO * INITIAL_DISPLAY))


def per_second_interest(principal: int) -> int:
    if principal <= 0:
        return 0
    return max(1, round(principal * TOTAL_RATE / DURATION))


def cash_only_asset() -> int:
    cash = INITIAL_ASSET
    principal = INITIAL_ASSET
    for _ in range(DURATION):
        cash += per_second_interest(principal)
    return cash


def moving_average(prices: list[int], ma_sec: int) -> list[float]:
    out: list[float] = []
    for i in range(len(prices)):
        a = max(0, i - ma_sec + 1)
        chunk = prices[a : i + 1]
        out.append(sum(chunk) / len(chunk))
    return out


def find_macro_pivots(
    mas: list[float],
    min_swing_frac: float = MIN_SWING_FRAC,
) -> list[tuple[int, str, float]]:
    """平滑系列の局所山・谷を取り、振幅の小さいノイズを間引きする。"""
    n = len(mas)
    raw: list[tuple[int, str, float]] = []
    for i in range(1, n - 1):
        if mas[i] <= mas[i - 1] and mas[i] <= mas[i + 1]:
            raw.append((i, "lo", mas[i]))
        elif mas[i] >= mas[i - 1] and mas[i] >= mas[i + 1]:
            raw.append((i, "hi", mas[i]))

    if not raw:
        return []

    filtered: list[tuple[int, str, float]] = [raw[0]]
    for t, kind, val in raw[1:]:
        lt, lk, lv = filtered[-1]
        if kind == lk:
            # 同種はより極端な方を残す
            if kind == "lo" and val < lv:
                filtered[-1] = (t, kind, val)
            elif kind == "hi" and val > lv:
                filtered[-1] = (t, kind, val)
            continue
        if lv <= 0:
            continue
        if abs(val - lv) / lv >= min_swing_frac:
            filtered.append((t, kind, val))
    return filtered


def human_max_asset(
    ratios: list[float],
    offset: int,
    ma_sec: int = MA_SEC,
    min_swing_frac: float = MIN_SWING_FRAC,
    lag_sec: int = ORDER_LAG_SEC,
    cooldown_sec: int = ORDER_COOLDOWN_SEC,
) -> tuple[int, int]:
    """
    平滑マクロ山・谷をラグ/CD付きでロングのみ完璧実行。
    戻り値: (HUMAN_MAX, ピボット数)
    HUMAN_MAX = max(トレード結果, 現金放置)
    """
    cash_base = cash_only_asset()
    prices = [display_price(ratio_at(ratios, offset + s)) for s in range(DURATION + 1)]
    mas = moving_average(prices, ma_sec)
    pivots = find_macro_pivots(mas, min_swing_frac)

    cash = float(INITIAL_ASSET)
    shares = 0.0
    holding = False
    interest_principal = INITIAL_ASSET
    ready_at = 0
    pending_fill: int | None = None
    pending_buy = False
    pivot_i = 0

    for sec in range(DURATION + 1):
        if not holding and sec < DURATION:
            cash += per_second_interest(interest_principal)

        if pending_fill is not None and pending_fill == sec:
            px = prices[sec]
            if pending_buy:
                if px > 0:
                    shares = cash / px
                    cash = 0.0
                    holding = True
            else:
                cash = shares * px
                shares = 0.0
                holding = False
                interest_principal = max(0, round(cash))
            pending_fill = None
            ready_at = sec + cooldown_sec

        while pending_fill is None and sec >= ready_at and pivot_i < len(pivots):
            t, kind, _ = pivots[pivot_i]
            submit = t - lag_sec
            fill = t
            if fill > DURATION:
                pivot_i += 1
                continue
            if sec > submit:
                pivot_i += 1
                continue
            if sec < submit:
                break

            if kind == "lo" and not holding:
                pending_fill = fill
                pending_buy = True
                pivot_i += 1
                break
            if kind == "hi" and holding:
                pending_fill = fill
                pending_buy = False
                pivot_i += 1
                break
            pivot_i += 1

    if holding:
        cash = shares * prices[DURATION]

    traded = max(0, round(cash))
    return max(traded, cash_base), len(pivots)


def mean_human(ratios: list[float]) -> tuple[float, int, int, float]:
    vals: list[int] = []
    pivs: list[int] = []
    for off in range(0, 11):
        v, p = human_max_asset(ratios, off)
        vals.append(v)
        pivs.append(p)
    return statistics.mean(vals), min(vals), max(vals), statistics.mean(pivs)


def load_series() -> dict[str, list[float]]:
    by_stage: dict[str, list[tuple[int, float]]] = defaultdict(list)
    with CHARTS.open(encoding="utf-8-sig", newline="") as f:
        for row in csv.DictReader(f):
            sec = int(row["second"])
            by_stage[row["stage_id"].strip()].append((sec, float(row["price_ratio"])))
    out: dict[str, list[float]] = {}
    for sid, pts in by_stage.items():
        pts.sort(key=lambda x: x[0])
        out[sid] = [p for _, p in pts]
    return out


def load_meta() -> dict[str, dict[str, str]]:
    meta: dict[str, dict[str, str]] = {}
    with META.open(encoding="utf-8-sig", newline="") as f:
        for row in csv.DictReader(f):
            sid = row["stage_id"].strip()
            meta[sid] = {
                "macro_shape": row.get("macro_shape", "").strip(),
                "instrument": row.get("instrument_name", "").strip(),
                "tier": (row.get("tier") or "").strip() or "STANDARD",
            }
    return meta


def main() -> None:
    series = load_series()
    meta = load_meta()
    cash = cash_only_asset()
    stage_ids = sorted(series.keys(), key=lambda s: (s[0], int(s[1:])))

    rows: list[dict] = []
    lines: list[str] = []
    lines.append(
        f"案C HUMAN MAX  MA={MA_SEC}s  min_swing={MIN_SWING_FRAC*100:.0f}%  "
        f"lag={ORDER_LAG_SEC}s  cd={ORDER_COOLDOWN_SEC}s"
    )
    lines.append(f"現金放置: Y{cash:,}  (下落見送りの100%基準 / max(trade, cash))")
    lines.append("")
    lines.append(
        f"{'id':4} {'tier':8} {'shape':18} "
        f"{'HUMAN':>7} {'lo':>6} {'hi':>6} {'piv':>4} {'vsC':>6}  instrument"
    )
    lines.append("-" * 100)

    all_h: list[float] = []
    for sid in stage_ids:
        ratios = series[sid]
        m = meta.get(sid, {})
        tier = m.get("tier", "STANDARD")
        shape = m.get("macro_shape", "?")
        instrument = m.get("instrument", "")
        mean_v, lo, hi, piv = mean_human(ratios)
        vs = mean_v / cash * 100.0
        all_h.append(mean_v)
        rows.append(
            {
                "sid": sid,
                "tier": tier,
                "shape": shape,
                "instrument": instrument,
                "mean": mean_v,
                "lo": lo,
                "hi": hi,
                "piv": piv,
                "vs": vs,
            }
        )
        lines.append(
            f"{sid:4} {tier:8} {shape:18} "
            f"{mean_v:7,.0f} {lo:6,} {hi:6,} {piv:4.1f} {vs:5.1f}%  {instrument}"
        )

    lines.append("-" * 100)
    lines.append(f"全体 mean HUMAN={statistics.mean(all_h):,.0f}")
    std = [r for r in rows if r["tier"] != "EXTREME"]
    ext = [r for r in rows if r["tier"] == "EXTREME"]
    if std:
        lines.append(f"STANDARD mean HUMAN={statistics.mean(r['mean'] for r in std):,.0f}")
    if ext:
        lines.append(f"EXTREME  mean HUMAN={statistics.mean(r['mean'] for r in ext):,.0f}")
    at_cash = sum(1 for r in rows if abs(r["mean"] - cash) < 1)
    lines.append(f"現金と同額（見送りが最適）: {at_cash} / {len(rows)}")

    REPORT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("\n".join(lines))
    print(f"\nwrote {REPORT.relative_to(ROOT)}")

    rcParams["font.family"] = "Meiryo"
    rcParams["axes.unicode_minus"] = False
    cols, rows_n = 9, 5
    fig, axes = plt.subplots(rows_n, cols, figsize=(22, 17), facecolor="#0a0a0a")
    fig.suptitle(
        f"URIKAI — HUMAN MAX (MA{MA_SEC}s macro pivots, lag+CD, max with cash)  "
        f"cash≈Y{cash:,}",
        color="#e0e0e0",
        fontsize=12,
        y=0.985,
    )
    by_id = {r["sid"]: r for r in rows}
    for i, ax in enumerate(axes.flat):
        if i >= len(stage_ids):
            ax.axis("off")
            continue
        sid = stage_ids[i]
        play = series[sid][: PLAY_SECONDS + 1]
        r = by_id[sid]
        ext = r["tier"] == "EXTREME"
        ax.set_facecolor("#111111")
        ax.plot(list(range(len(play))), play, color="#ff7a59" if ext else "#00e5cc", linewidth=1.35)
        # 平滑線（offset0）
        prices0 = [display_price(ratio_at(series[sid], s)) for s in range(DURATION + 1)]
        mas0 = moving_average(prices0, MA_SEC)
        # ratio scale for plot: display/10 roughly matches ratio if base 100->1000
        ax.plot(
            list(range(len(mas0))),
            [p / 10.0 for p in mas0],
            color="#888888",
            linewidth=0.9,
            alpha=0.85,
        )
        ax.axhline(100, color="#444444", linewidth=0.55, linestyle="--")
        ax.set_xlim(0, PLAY_SECONDS)
        lo, hi = min(play), max(play)
        pad = max(3.0, (hi - lo) * 0.08)
        ax.set_ylim(lo - pad, hi + pad)
        tag = " EXT" if ext else ""
        title = f"{sid}{tag}  {r['shape']}"
        if r["instrument"]:
            title += f"\n{r['instrument']}"
        title += (
            f"\nHUMAN Y{r['mean']:,.0f} ({r['lo']:,}–{r['hi']:,})\n"
            f"vsCash {r['vs']:.0f}%  piv~{r['piv']:.0f}"
        )
        ax.set_title(title, color="#cccccc", fontsize=6.6, pad=3, linespacing=1.1)
        ax.tick_params(colors="#666666", labelsize=5.5)
        for spine in ax.spines.values():
            spine.set_color("#333333")

    fig.tight_layout(rect=[0, 0, 1, 0.97])
    OUT_PNG.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(OUT_PNG, dpi=150, facecolor=fig.get_facecolor())
    plt.close(fig)
    print(f"wrote {OUT_PNG.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
