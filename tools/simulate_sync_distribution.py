#!/usr/bin/env python3
"""SYNC 到達率・最終資産の分布をモンテカルロ推定（SyncCalculator 相当ロジック）。"""
from __future__ import annotations

import csv
import math
import random
import statistics
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CHARTS = ROOT / "app" / "src" / "main" / "assets" / "charts.csv"
META = ROOT / "content" / "stages_meta.csv"

INITIAL_ASSET = 10_000
DURATION = 60
BASE_RATIO = 100.0
INITIAL_DISPLAY = 1_000
TOTAL_RATE = 0.045
ORDER_LAG_SEC = 1
ORDER_COOLDOWN_SEC = 1

# ★: 資産<10000→1 / 10000〜理論を等分で2-4 / ≥理論→5 / >理論→5+
STAR2_FLOOR = 10_000


def stars(asset: float, theo: float) -> int:
    if asset < STAR2_FLOOR:
        return 1
    if theo <= STAR2_FLOOR:
        return 5 if asset >= theo else 2
    span = theo - STAR2_FLOOR
    t3 = STAR2_FLOOR + span / 3
    t4 = STAR2_FLOOR + (span * 2) / 3
    if asset >= theo:
        return 5
    if asset >= t4:
        return 4
    if asset >= t3:
        return 3
    return 2


def stars_from_sync(sync: float, theo: float) -> int:
    if theo <= 0:
        return 1
    return stars(sync / 100.0 * theo, theo)


def stars_text(asset: float, theo: float) -> str:
    filled = stars(asset, theo)
    base = "★" * filled + "☆" * (5 - filled)
    return base + ("+" if asset > theo > 0 else "")


from simulate_human_max import human_max_asset as _human_max_asset  # noqa: E402


def theoretical_max_asset(
    ratios: list[float],
    offset: int,
    **_kwargs,
) -> int:
    """SyncCalculator HUMAN MAX 相当。"""
    value, _ = _human_max_asset(ratios, offset)
    return value


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


def simulate_trades(
    ratios: list[float],
    offset: int,
    trade_secs: list[tuple[int, bool]],
    lag_sec: float = ORDER_LAG_SEC,
    cooldown_sec: float = ORDER_COOLDOWN_SEC,
) -> int:
    """trade_secs: (submit_sec, is_buy). 約定は submit + lag。クールダウン中は次の submit 不可。"""
    cash = INITIAL_ASSET
    shares = 0.0
    holding = False
    interest_principal = INITIAL_ASSET

    fills: dict[float, bool] = {}
    last_fill = -999.0
    for submit, is_buy in trade_secs:
        if cooldown_sec > 0 and submit < last_fill + cooldown_sec:
            continue
        fill = min(float(DURATION), submit + lag_sec)
        if fill >= DURATION:
            continue
        if is_buy and not holding:
            fills[fill] = True
            last_fill = fill
        elif not is_buy and holding:
            fills[fill] = False
            last_fill = fill

    fill_events = sorted(fills.items())
    fill_idx = 0
    for sec in range(DURATION):
        if not holding:
            cash += per_second_interest(interest_principal)
        while fill_idx < len(fill_events) and fill_events[fill_idx][0] <= sec + 0.001:
            fill_t, is_buy = fill_events[fill_idx]
            price = display_price(ratio_at(ratios, offset + fill_t))
            if is_buy:
                shares = cash / price
                cash = 0
                holding = True
            else:
                cash = round(shares * price)
                shares = 0.0
                holding = False
                interest_principal = cash
            fill_idx += 1

    end_price = display_price(ratio_at(ratios, offset + DURATION))
    if holding:
        cash = round(shares * end_price)
    return max(0, cash)


def sync_percent(player: int, theoretical: int) -> float:
    if theoretical <= 0:
        return 0.0
    return max(0.0, player / theoretical * 100.0)


def cash_only_asset() -> int:
    cash = INITIAL_ASSET
    principal = INITIAL_ASSET
    for _ in range(DURATION):
        cash += per_second_interest(principal)
    return cash


@dataclass
class Stage:
    stage_id: str
    tier: str
    ratios: list[float]


def load_stages() -> list[Stage]:
    tiers: dict[str, str] = {}
    with META.open(encoding="utf-8-sig") as f:
        for row in csv.DictReader(f):
            tiers[row["stage_id"]] = row.get("tier") or "STANDARD"

    by_id: dict[str, list[float]] = defaultdict(list)
    with CHARTS.open(encoding="utf-8") as f:
        for row in csv.DictReader(f):
            sid = row["stage_id"]
            by_id[sid].append(float(row["price_ratio"]))

    stages = []
    for sid, ratios in sorted(by_id.items()):
        stages.append(Stage(sid, tiers.get(sid, "STANDARD"), ratios))
    return stages


def percentile(values: list[float], p: float) -> float:
    if not values:
        return 0.0
    xs = sorted(values)
    k = (len(xs) - 1) * p / 100.0
    f = math.floor(k)
    c = math.ceil(k)
    if f == c:
        return xs[int(k)]
    return xs[f] * (c - k) + xs[c] * (k - f)


def summarize(name: str, assets: list[int], syncs: list[float], theos: list[int]) -> None:
    star_counts = defaultdict(int)
    for a, t in zip(assets, theos):
        star_counts[stars(a, t)] += 1
    n = len(assets)
    print(f"\n=== {name} (n={n}) ===")
    print(
        f"最終資産: mean={statistics.mean(assets):,.0f}  "
        f"p10={percentile(assets, 10):,.0f}  p50={percentile(assets, 50):,.0f}  "
        f"p90={percentile(assets, 90):,.0f}  min={min(assets):,}  max={max(assets):,}"
    )
    print(
        f"到達率%: mean={statistics.mean(syncs):.1f}  "
        f"p10={percentile(syncs, 10):.1f}  p50={percentile(syncs, 50):.1f}  "
        f"p90={percentile(syncs, 90):.1f}"
    )
    print("★分布:", end=" ")
    for star in range(5, 0, -1):
        pct = star_counts[star] / n * 100
        print(f"★{star}={pct:.0f}%", end="  ")
    print()
    print(f"  理論最大 mean={statistics.mean(theos):,.0f}")

def random_stage_offset(rng: random.Random, stages: list[Stage]) -> tuple[Stage, int]:
    stage = rng.choice(stages)
    offset = rng.randint(0, 10)
    return stage, offset


def gen_casual_trades(rng: random.Random, ratios: list[float], offset: int) -> list[tuple[int, bool]]:
    """見た目の勢いに反応するカジュアルプレイヤー（2〜5回程度の売買）。"""
    trades: list[tuple[int, bool]] = []
    holding = False
    last_action = -999
    for sec in range(3, DURATION - 3):
        if sec - last_action < 4:
            continue
        p0 = display_price(ratio_at(ratios, offset + sec - 2))
        p1 = display_price(ratio_at(ratios, offset + sec))
        if not holding and p1 > p0 * 1.008 and rng.random() < 0.55:
            trades.append((sec, True))
            holding = True
            last_action = sec
        elif holding and p1 < p0 * 0.992 and rng.random() < 0.55:
            trades.append((sec, False))
            holding = False
            last_action = sec
    return trades


def gen_panic_trades(rng: random.Random) -> list[tuple[int, bool]]:
    """初動で買い、下落で狼狽売り、または逆。"""
    buy = rng.randint(2, 12)
    sell = rng.randint(buy + 4, min(DURATION - 2, buy + 25))
    return [(buy, True), (sell, False)]


def gen_random_trades(rng: random.Random) -> list[tuple[int, bool]]:
    n = rng.randint(1, 4)
    trades: list[tuple[int, bool]] = []
    holding = False
    t = rng.randint(2, 10)
    for _ in range(n):
        if t >= DURATION - 2:
            break
        if not holding:
            trades.append((t, True))
            holding = True
        else:
            trades.append((t, False))
            holding = False
        t += rng.randint(3, 12)
    return trades


def gen_decent_swing(rng: random.Random, ratios: list[float], offset: int) -> list[tuple[int, bool]]:
    """底付近で1回買い、高値付近で1回売り（数秒ズレ）。"""
    segment = ratios[offset : offset + DURATION + 1]
    if len(segment) < 10:
        return []
    min_i = min(range(len(segment)), key=lambda i: segment[i])
    tail = range(min(min_i + 3, len(segment) - 1), len(segment))
    if not tail:
        return []
    max_i = max(tail, key=lambda i: segment[i])
    buy_submit = max(2, min_i + rng.randint(-3, 3))
    sell_submit = max(buy_submit + 5, max_i + rng.randint(-4, 4))
    if sell_submit >= DURATION - 1:
        return []
    return [(buy_submit, True), (sell_submit, False)]


def run_monte_carlo(stages: list[Stage], trials: int, seed: int = 42) -> None:
    rng = random.Random(seed)
    cash_only = cash_only_asset()

    std_stages = [s for s in stages if (s.tier or "STANDARD") != "EXTREME"]
    ext_stages = [s for s in stages if s.tier == "EXTREME"]

    def pick_stage() -> Stage:
        if ext_stages and rng.random() < 0.08:
            return rng.choice(ext_stages)
        return rng.choice(std_stages)

    theo_std: list[int] = []
    theo_ext: list[int] = []
    for stage in stages:
        for off in range(0, 11):
            t = theoretical_max_asset(stage.ratios, off)
            if stage.tier == "EXTREME":
                theo_ext.append(t)
            else:
                theo_std.append(t)

    print("=== 理論最大資産（HUMAN MAX / offset 0..10）===")
    print(
        f"STANDARD: mean={statistics.mean(theo_std):,.0f}  "
        f"p10={percentile(theo_std, 10):,.0f}  p50={percentile(theo_std, 50):,.0f}  "
        f"p90={percentile(theo_std, 90):,.0f}"
    )
    if theo_ext:
        print(
            f"EXTREME:  mean={statistics.mean(theo_ext):,.0f}  "
            f"p10={percentile(theo_ext, 10):,.0f}  p50={percentile(theo_ext, 50):,.0f}  "
            f"p90={percentile(theo_ext, 90):,.0f}  max={max(theo_ext):,}"
        )
    mix_theo = 0.92 * statistics.mean(theo_std) + 0.08 * statistics.mean(theo_ext)
    print(f"混合抽選(92/8)の理論最大 mean={mix_theo:,.0f}")
    print(f"現金放置(利息): {cash_only:,}円")
    std_sync = sync_percent(cash_only, int(statistics.mean(theo_std)))
    print(f"  STANDARD理論比 {std_sync:.1f}% -> 星{stars(cash_only, statistics.mean(theo_std))}")
    if theo_ext:
        ext_sync = sync_percent(cash_only, int(statistics.mean(theo_ext)))
        print(f"  EXTREME理論比  {ext_sync:.1f}% -> 星{stars(cash_only, statistics.mean(theo_ext))}")

    scenarios = {
        "現金放置": "cash",
        "1往復ランダム": "panic",
        "1往復(底->天井狙い)": "swing",
        "ランダム売買1-4回": "random",
        "カジュアル(勢い追随)": "casual",
        "理想(ラグ付き理論=100%)": "perfect",
    }

    for name, kind in scenarios.items():
        assets: list[int] = []
        syncs: list[float] = []
        theos: list[int] = []
        for _ in range(trials):
            stage = pick_stage()
            offset = rng.randint(0, 10)
            theo = theoretical_max_asset(stage.ratios, offset)
            if kind == "cash":
                player = cash_only
            elif kind == "panic":
                player = simulate_trades(stage.ratios, offset, gen_panic_trades(rng))
            elif kind == "swing":
                player = simulate_trades(
                    stage.ratios, offset, gen_decent_swing(rng, stage.ratios, offset)
                )
            elif kind == "random":
                player = simulate_trades(stage.ratios, offset, gen_random_trades(rng))
            elif kind == "casual":
                player = simulate_trades(
                    stage.ratios, offset, gen_casual_trades(rng, stage.ratios, offset)
                )
            else:
                player = theo
            assets.append(player)
            syncs.append(sync_percent(player, theo))
            theos.append(theo)
        summarize(name, assets, syncs, theos)

    print("\n=== 最終資産の帯分布（1往復 底->天井, n=trials）===")
    buckets = [
        (0, 5000),
        (5000, 9000),
        (9000, 11000),
        (11000, 15000),
        (15000, 20000),
        (20000, 999999),
    ]
    assets_b: list[int] = []
    syncs_b: list[float] = []
    theos_b: list[int] = []
    for _ in range(trials):
        stage = pick_stage()
        offset = rng.randint(0, 10)
        theo = theoretical_max_asset(stage.ratios, offset)
        player = simulate_trades(
            stage.ratios, offset, gen_decent_swing(rng, stage.ratios, offset)
        )
        assets_b.append(player)
        syncs_b.append(sync_percent(player, theo))
        theos_b.append(theo)
    for lo, hi in buckets:
        label = f"{lo:,}-{hi:,}" if hi < 999999 else f"{lo:,}+"
        idx = [i for i, a in enumerate(assets_b) if lo <= a < hi]
        if not idx:
            continue
        sub_sync = [syncs_b[i] for i in idx]
        sub_stars = [stars(assets_b[i], theos_b[i]) for i in idx]
        print(
            f"  {label:>12}円: {len(idx)/trials*100:5.1f}%  "
            f"sync mean={statistics.mean(sub_sync):.0f}%  "
            f"星mean={statistics.mean(sub_stars):.1f}"
        )

    print("\n=== 星閾値再較正の参考パーセンタイル ===")
    for label, kind in [
        ("底->天井", "swing"),
        ("カジュアル", "casual"),
        ("現金放置", "cash"),
    ]:
        xs: list[float] = []
        for _ in range(trials):
            stage = pick_stage()
            offset = rng.randint(0, 10)
            theo = theoretical_max_asset(stage.ratios, offset)
            if kind == "cash":
                player = cash_only
            elif kind == "casual":
                player = simulate_trades(
                    stage.ratios, offset, gen_casual_trades(rng, stage.ratios, offset)
                )
            else:
                player = simulate_trades(
                    stage.ratios, offset, gen_decent_swing(rng, stage.ratios, offset)
                )
            xs.append(sync_percent(player, theo))
        print(
            f"{label}: p10={percentile(xs,10):.0f}% p25={percentile(xs,25):.0f}% "
            f"p50={percentile(xs,50):.0f}% p75={percentile(xs,75):.0f}% "
            f"p90={percentile(xs,90):.0f}% p97={percentile(xs,97):.0f}%"
        )
    print(
        f"現行星閾値: ★1=<¥{STAR2_FLOOR:,}  ★2床=¥{STAR2_FLOOR:,}  "
        f"★3/★4=床〜理論の等分  ★5≥理論  OVER=>理論は★5+"
    )
    print(f"Extreme分母キャップ: なし（HUMAN MAX）")
    print(f"注文ラグ/クールダウン: {ORDER_LAG_SEC}s / {ORDER_COOLDOWN_SEC}s（マクロ山・谷）")


def main() -> None:
    stages = load_stages()
    report = ROOT / "tools" / "sync_distribution_report.txt"
    with report.open("w", encoding="utf-8") as f:
        import contextlib
        import io

        buf = io.StringIO()

        class Tee:
            def write(self, s: str) -> int:
                buf.write(s)
                f.write(s)
                return len(s)

            def flush(self) -> None:
                pass

        import sys

        old = sys.stdout
        sys.stdout = Tee()
        try:
            print(f"ステージ数: {len(stages)} (STANDARD 30 + EXTREME 15)")
            run_monte_carlo(stages, trials=8000)
        finally:
            sys.stdout = old
    print(f"wrote {report}")


if __name__ == "__main__":
    main()
