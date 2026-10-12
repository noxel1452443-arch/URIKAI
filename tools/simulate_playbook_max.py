#!/usr/bin/env python3
"""
理論値の試算（改善プレイブック max / マクロ min→max 窓）。

  py -3 tools/simulate_playbook_max.py

出力:
  tools/playbook_max_report.txt
  content/chart_overview_playbooks.png
  tools/macro_swing_report.txt
  content/chart_overview_macro_swing.png
"""
from __future__ import annotations

import csv
import statistics
from collections import defaultdict
from pathlib import Path

import matplotlib.pyplot as plt
from matplotlib import rcParams

ROOT = Path(__file__).resolve().parents[1]
CHARTS = ROOT / "shared" / "src" / "commonMain" / "composeResources" / "files" / "charts.csv"
META = ROOT / "content" / "stages_meta.csv"
REPORT_PB = ROOT / "tools" / "playbook_max_report.txt"
OUT_PNG_PB = ROOT / "content" / "chart_overview_playbooks.png"
REPORT_SW = ROOT / "tools" / "macro_swing_report.txt"
OUT_PNG_SW = ROOT / "content" / "chart_overview_macro_swing.png"

INITIAL_ASSET = 10_000
DURATION = 60
PLAY_SECONDS = 60
BASE_RATIO = 100.0
INITIAL_DISPLAY = 1_000
TOTAL_RATE = 0.045
ORDER_LAG_SEC = 1
ORDER_COOLDOWN_SEC = 1
LOOKBACK_SEC = 5
# 改善: 各状態（保有/現金）に最低滞在秒 = lookback。往復しすぎを止める。
MIN_DWELL_SEC = LOOKBACK_SEC
# マクロ swing: 移動平均窓（瞬間スパイクを低/高から外す）
SWING_MA_SEC = 3


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


def playbook_asset(
    ratios: list[float],
    offset: int,
    mode: str,
    lookback: int = LOOKBACK_SEC,
    lag_sec: int = ORDER_LAG_SEC,
    cooldown_sec: int = ORDER_COOLDOWN_SEC,
    min_dwell: int = MIN_DWELL_SEC,
) -> int:
    """
    mode='tf': 上がり続き→買い、下がり続き→売り
    mode='mr': 下がり続き→買い、上がり続き→売り
    改善: 約定後は max(cool, min_dwell) 秒は次の提出不可（状態に滞在）。
    """
    assert mode in ("tf", "mr")
    cash = float(INITIAL_ASSET)
    shares = 0.0
    holding = False
    interest_principal = INITIAL_ASSET
    pending_fill: int | None = None
    pending_buy = False
    ready_at = 0
    dwell = max(cooldown_sec, min_dwell)

    for sec in range(DURATION):
        if not holding:
            cash += per_second_interest(interest_principal)

        if pending_fill is not None and pending_fill == sec:
            price = display_price(ratio_at(ratios, offset + sec))
            if pending_buy:
                shares = cash / price
                cash = 0.0
                holding = True
            else:
                cash = shares * price
                shares = 0.0
                holding = False
                interest_principal = max(0, round(cash))
            pending_fill = None
            ready_at = sec + dwell

        if pending_fill is None and sec >= ready_at:
            fill_sec = sec + lag_sec
            if fill_sec <= DURATION and fill_sec >= lookback:
                fill_p = display_price(ratio_at(ratios, offset + fill_sec))
                past_p = display_price(ratio_at(ratios, offset + fill_sec - lookback))
                rising = fill_p > past_p
                falling = fill_p < past_p
                want_buy = rising if mode == "tf" else falling
                want_sell = falling if mode == "tf" else rising
                if not holding and want_buy:
                    pending_fill = fill_sec
                    pending_buy = True
                elif holding and want_sell:
                    pending_fill = fill_sec
                    pending_buy = False

    last = display_price(ratio_at(ratios, offset + DURATION))
    if holding:
        cash = shares * last
    return max(0, round(cash))


def macro_swing_asset(
    ratios: list[float],
    offset: int,
    ma_sec: int = SWING_MA_SEC,
    lag_sec: int = ORDER_LAG_SEC,
) -> int:
    """
    プレイ区間の移動平均上で、時系列順の最良 (低→高) を1往復。
    約定価格は実価格。提出は fill - lag（負ならその秒はスキップし次善を探さない＝単純化）。
    利子: 買い前・売り後の現金秒のみ。
    """
    prices = [display_price(ratio_at(ratios, offset + s)) for s in range(DURATION + 1)]

    def ma_at(i: int) -> float:
        a = max(0, i - ma_sec + 1)
        chunk = prices[a : i + 1]
        return sum(chunk) / len(chunk)

    mas = [ma_at(i) for i in range(DURATION + 1)]

    best_ratio = 1.0
    best_ij = (0, 0)
    for i in range(DURATION + 1):
        for j in range(i + 1, DURATION + 1):
            if mas[i] <= 0:
                continue
            r = mas[j] / mas[i]
            if r > best_ratio:
                best_ratio = r
                best_ij = (i, j)

    t_lo, t_hi = best_ij
    if best_ratio <= 1.0 or t_hi <= t_lo:
        return cash_only_asset()

    # ラグ: 約定を t_lo / t_hi に合わせるため提出は -lag。提出が負なら現金のみにフォールバック。
    buy_submit = t_lo - lag_sec
    sell_submit = t_hi - lag_sec
    if buy_submit < 0 or sell_submit <= buy_submit or t_hi > DURATION:
        return cash_only_asset()

    cash = float(INITIAL_ASSET)
    principal = INITIAL_ASSET
    shares = 0.0
    holding = False

    for sec in range(DURATION + 1):
        if not holding and sec < DURATION:
            cash += per_second_interest(principal)

        if sec == t_lo and not holding:
            px = prices[t_lo]
            if px > 0:
                shares = cash / px
                cash = 0.0
                holding = True

        if sec == t_hi and holding:
            cash = shares * prices[t_hi]
            shares = 0.0
            holding = False
            principal = max(0, round(cash))

    if holding:
        cash = shares * prices[DURATION]
    return max(0, round(cash))


def mean_over_offsets_pb(ratios: list[float], mode: str) -> tuple[float, int, int]:
    vals = [playbook_asset(ratios, off, mode) for off in range(0, 11)]
    return statistics.mean(vals), min(vals), max(vals)


def mean_over_offsets_sw(ratios: list[float]) -> tuple[float, int, int]:
    vals = [macro_swing_asset(ratios, off) for off in range(0, 11)]
    return statistics.mean(vals), min(vals), max(vals)


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


def write_playbook_report(series: dict[str, list[float]], meta: dict[str, dict[str, str]]) -> list[dict]:
    cash = cash_only_asset()
    stage_ids = sorted(series.keys(), key=lambda s: (s[0], int(s[1:])))
    rows: list[dict] = []
    lines: list[str] = []
    lines.append(
        f"改善プレイブック max  lookback={LOOKBACK_SEC}s  min_dwell={MIN_DWELL_SEC}s  "
        f"lag={ORDER_LAG_SEC}s  cd={ORDER_COOLDOWN_SEC}s"
    )
    lines.append(f"現金放置: Y{cash:,}")
    lines.append("TF / MR / MAX=max(TF,MR,現金)  ※約定後は min_dwell 秒は再提出不可")
    lines.append("")
    lines.append(
        f"{'id':4} {'tier':8} {'shape':18} "
        f"{'TF':>7} {'MR':>7} {'MAX':>7} {'win':3} {'cash%':>6}  instrument"
    )
    lines.append("-" * 100)

    tf_all: list[float] = []
    mr_all: list[float] = []
    max_all: list[float] = []
    win_tf = win_mr = win_cash = 0

    for sid in stage_ids:
        ratios = series[sid]
        m = meta.get(sid, {})
        tier = m.get("tier", "STANDARD")
        shape = m.get("macro_shape", "?")
        instrument = m.get("instrument", "")

        tf_mean, tf_lo, tf_hi = mean_over_offsets_pb(ratios, "tf")
        mr_mean, mr_lo, mr_hi = mean_over_offsets_pb(ratios, "mr")
        mx = max(tf_mean, mr_mean, float(cash))
        if tf_mean >= mr_mean and tf_mean >= cash:
            win = "TF"
            win_tf += 1
        elif mr_mean >= tf_mean and mr_mean >= cash:
            win = "MR"
            win_mr += 1
        else:
            win = "CASH"
            win_cash += 1

        cash_pct = cash / mx * 100.0 if mx > 0 else 0.0
        tf_all.append(tf_mean)
        mr_all.append(mr_mean)
        max_all.append(mx)
        rows.append(
            {
                "sid": sid,
                "tier": tier,
                "shape": shape,
                "instrument": instrument,
                "tf": tf_mean,
                "mr": mr_mean,
                "mx": mx,
                "win": win,
                "cash_pct": cash_pct,
            }
        )
        lines.append(
            f"{sid:4} {tier:8} {shape:18} "
            f"{tf_mean:7,.0f} {mr_mean:7,.0f} {mx:7,.0f} {win:3} {cash_pct:5.1f}%  {instrument}"
        )

    lines.append("-" * 100)
    lines.append(
        f"全体 mean  TF={statistics.mean(tf_all):,.0f}  "
        f"MR={statistics.mean(mr_all):,.0f}  MAX={statistics.mean(max_all):,.0f}"
    )
    std = [r for r in rows if r["tier"] != "EXTREME"]
    ext = [r for r in rows if r["tier"] == "EXTREME"]
    if std:
        lines.append(
            f"STANDARD mean  TF={statistics.mean(r['tf'] for r in std):,.0f}  "
            f"MR={statistics.mean(r['mr'] for r in std):,.0f}  "
            f"MAX={statistics.mean(r['mx'] for r in std):,.0f}"
        )
    if ext:
        lines.append(
            f"EXTREME  mean  TF={statistics.mean(r['tf'] for r in ext):,.0f}  "
            f"MR={statistics.mean(r['mr'] for r in ext):,.0f}  "
            f"MAX={statistics.mean(r['mx'] for r in ext):,.0f}"
        )
    lines.append(f"勝者カウント: TF={win_tf}  MR={win_mr}  CASH={win_cash}  (n={len(rows)})")

    REPORT_PB.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("\n".join(lines))
    print(f"\nwrote {REPORT_PB.relative_to(ROOT)}")
    return rows


def write_swing_report(series: dict[str, list[float]], meta: dict[str, dict[str, str]]) -> list[dict]:
    cash = cash_only_asset()
    stage_ids = sorted(series.keys(), key=lambda s: (s[0], int(s[1:])))
    rows: list[dict] = []
    lines: list[str] = []
    lines.append(
        f"マクロ swing (MA{SWING_MA_SEC}s 上の最良 低→高 1往復)  lag={ORDER_LAG_SEC}s"
    )
    lines.append(f"現金放置: Y{cash:,}")
    lines.append("SWING=窓付きマクロ1往復 / MAX=max(SWING,現金)")
    lines.append("")
    lines.append(
        f"{'id':4} {'tier':8} {'shape':18} "
        f"{'SWING':>7} {'MAX':>7} {'vsC':>6}  instrument"
    )
    lines.append("-" * 90)

    sw_all: list[float] = []
    mx_all: list[float] = []

    for sid in stage_ids:
        ratios = series[sid]
        m = meta.get(sid, {})
        tier = m.get("tier", "STANDARD")
        shape = m.get("macro_shape", "?")
        instrument = m.get("instrument", "")
        sw_mean, sw_lo, sw_hi = mean_over_offsets_sw(ratios)
        mx = max(sw_mean, float(cash))
        vs = sw_mean / cash * 100.0 if cash else 0.0
        sw_all.append(sw_mean)
        mx_all.append(mx)
        rows.append(
            {
                "sid": sid,
                "tier": tier,
                "shape": shape,
                "instrument": instrument,
                "sw": sw_mean,
                "sw_lo": sw_lo,
                "sw_hi": sw_hi,
                "mx": mx,
                "vs": vs,
            }
        )
        lines.append(
            f"{sid:4} {tier:8} {shape:18} "
            f"{sw_mean:7,.0f} {mx:7,.0f} {vs:5.1f}%  {instrument}"
        )

    lines.append("-" * 90)
    lines.append(f"全体 mean  SWING={statistics.mean(sw_all):,.0f}  MAX={statistics.mean(mx_all):,.0f}")
    std = [r for r in rows if r["tier"] != "EXTREME"]
    ext = [r for r in rows if r["tier"] == "EXTREME"]
    if std:
        lines.append(
            f"STANDARD mean  SWING={statistics.mean(r['sw'] for r in std):,.0f}  "
            f"MAX={statistics.mean(r['mx'] for r in std):,.0f}"
        )
    if ext:
        lines.append(
            f"EXTREME  mean  SWING={statistics.mean(r['sw'] for r in ext):,.0f}  "
            f"MAX={statistics.mean(r['mx'] for r in ext):,.0f}"
        )

    REPORT_SW.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print("\n".join(lines))
    print(f"\nwrote {REPORT_SW.relative_to(ROOT)}")
    return rows


def _setup_font() -> None:
    rcParams["font.family"] = "Meiryo"
    rcParams["axes.unicode_minus"] = False


def write_playbook_png(series: dict[str, list[float]], rows: list[dict]) -> None:
    by_id = {r["sid"]: r for r in rows}
    stage_ids = [r["sid"] for r in rows]
    _setup_font()
    cols, rows_n = 9, 5
    fig, axes = plt.subplots(rows_n, cols, figsize=(22, 17), facecolor="#0a0a0a")
    fig.suptitle(
        f"URIKAI — playbook max(TF,MR,cash) + min_dwell={MIN_DWELL_SEC}s  "
        f"lookback={LOOKBACK_SEC}s  (cash≈Y{cash_only_asset():,})",
        color="#e0e0e0",
        fontsize=12,
        y=0.985,
    )
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
            f"\nTF Y{r['tf']:,.0f}  MR Y{r['mr']:,.0f}\n"
            f"MAX Y{r['mx']:,.0f} ({r['win']})"
        )
        ax.set_title(title, color="#cccccc", fontsize=6.8, pad=3, linespacing=1.1)
        ax.tick_params(colors="#666666", labelsize=5.5)
        for spine in ax.spines.values():
            spine.set_color("#333333")
    fig.tight_layout(rect=[0, 0, 1, 0.97])
    OUT_PNG_PB.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(OUT_PNG_PB, dpi=150, facecolor=fig.get_facecolor())
    plt.close(fig)
    print(f"wrote {OUT_PNG_PB.relative_to(ROOT)}")


def write_swing_png(series: dict[str, list[float]], rows: list[dict]) -> None:
    by_id = {r["sid"]: r for r in rows}
    stage_ids = [r["sid"] for r in rows]
    _setup_font()
    cols, rows_n = 9, 5
    fig, axes = plt.subplots(rows_n, cols, figsize=(22, 17), facecolor="#0a0a0a")
    fig.suptitle(
        f"URIKAI — macro swing (MA{SWING_MA_SEC}s best low→high once)  "
        f"(cash≈Y{cash_only_asset():,})",
        color="#e0e0e0",
        fontsize=12,
        y=0.985,
    )
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
            f"\nSWING Y{r['sw']:,.0f} ({r['sw_lo']:,}–{r['sw_hi']:,})\n"
            f"MAX Y{r['mx']:,.0f}  vsCash {r['vs']:.0f}%"
        )
        ax.set_title(title, color="#cccccc", fontsize=6.8, pad=3, linespacing=1.1)
        ax.tick_params(colors="#666666", labelsize=5.5)
        for spine in ax.spines.values():
            spine.set_color("#333333")
    fig.tight_layout(rect=[0, 0, 1, 0.97])
    OUT_PNG_SW.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(OUT_PNG_SW, dpi=150, facecolor=fig.get_facecolor())
    plt.close(fig)
    print(f"wrote {OUT_PNG_SW.relative_to(ROOT)}")


def main() -> None:
    series = load_series()
    meta = load_meta()
    print("=== A: improved playbooks ===\n")
    pb_rows = write_playbook_report(series, meta)
    write_playbook_png(series, pb_rows)
    print("\n=== B: macro swing ===\n")
    sw_rows = write_swing_report(series, meta)
    write_swing_png(series, sw_rows)


if __name__ == "__main__":
    main()
