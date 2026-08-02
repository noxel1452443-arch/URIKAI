#!/usr/bin/env python3
"""ラグ・クールダウンが SYNC 分布に与える影響を比較。"""
from __future__ import annotations

import random
import statistics
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools"))

from simulate_sync_distribution import (  # noqa: E402
    DURATION,
    display_price,
    gen_decent_swing,
    gen_random_trades,
    load_stages,
    percentile,
    per_second_interest,
    ratio_at,
    sync_percent,
    theoretical_max_asset,
)

PROPOSED_STARS = [(67, 5), (60, 4), (52, 3), (40, 2), (0, 1)]


def stars_proposed(sync: float) -> int:
    for threshold, count in PROPOSED_STARS:
        if sync >= threshold:
            return count
    return 1


def star_distribution(syncs: list[float]) -> dict[int, float]:
    counts = {i: 0 for i in range(1, 6)}
    for sync in syncs:
        counts[stars_proposed(sync)] += 1
    n = len(syncs)
    return {k: v / n * 100 for k, v in counts.items()}


def simulate_with_mechanics(
    ratios: list[float],
    offset: int,
    trades: list[tuple[int, bool]],
    lag_sec: float,
    cooldown_sec: float = 0.0,
) -> int:
    cash = 10_000
    shares = 0.0
    holding = False
    interest_principal = 10_000

    fills: list[tuple[float, bool]] = []
    last_fill = -999.0
    for submit, is_buy in sorted(trades):
        if cooldown_sec > 0 and submit < last_fill + cooldown_sec:
            continue
        fill = min(float(DURATION), submit + lag_sec)
        if fill >= DURATION:
            continue
        if is_buy and not holding:
            fills.append((fill, True))
            last_fill = fill
        elif not is_buy and holding:
            fills.append((fill, False))
            last_fill = fill

    fills.sort(key=lambda x: x[0])
    fill_idx = 0
    for sec in range(DURATION):
        if not holding:
            cash += per_second_interest(interest_principal)
        while fill_idx < len(fills) and fills[fill_idx][0] <= sec + 0.001:
            _, is_buy = fills[fill_idx]
            price = display_price(ratio_at(ratios, offset + fills[fill_idx][0]))
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


def run_trials(
    name: str,
    gen_trades,
    lag_sec: float,
    cooldown_sec: float,
    trials: int,
    pick_stage,
    rng: random.Random,
) -> None:
    syncs: list[float] = []
    assets: list[int] = []
    for _ in range(trials):
        stage, offset = pick_stage()
        theo = theoretical_max_asset(stage.ratios, offset)
        trades = gen_trades(rng, stage.ratios, offset)
        player = simulate_with_mechanics(stage.ratios, offset, trades, lag_sec, cooldown_sec)
        assets.append(player)
        syncs.append(sync_percent(player, theo))

    sd = star_distribution(syncs)
    print(f"=== {name} ===")
    print(
        f"  資産 p50={percentile(assets, 50):,.0f}  p90={percentile(assets, 90):,.0f}  "
        f"p10={percentile(assets, 10):,.0f}"
    )
    print(
        f"  sync p50={percentile(syncs, 50):.1f}  p90={percentile(syncs, 90):.1f}  "
        f"p10={percentile(syncs, 10):.1f}"
    )
    print("  星(再較正案 67/60/52/40):", ", ".join(f"★{k}={sd[k]:.0f}%" for k in [5, 4, 3, 2, 1]))


def main() -> None:
    stages = load_stages()
    std = [s for s in stages if s.tier != "EXTREME"]
    ext = [s for s in stages if s.tier == "EXTREME"]
    rng = random.Random(42)
    trials = 5000

    def pick_stage():
        stage = rng.choice(ext) if ext and rng.random() < 0.08 else rng.choice(std)
        return stage, rng.randint(0, 10)

    print("底->天井狙い（比較的上手）")
    for lag, cd, label in [
        (1.0, 0.0, "lag=1.0s"),
        (0.5, 0.0, "lag=0.5s"),
        (0.0, 0.0, "lag=0s (理想)"),
        (0.0, 1.0, "cd=1.0s 即約定"),
        (1.0, 1.0, "lag=1.0 + cd=1.0"),
        (0.5, 0.5, "lag=0.5 + cd=0.5"),
    ]:
        run_trials(label, gen_decent_swing, lag, cd, trials, pick_stage, rng)

    print("\nランダム売買1-4回")
    for lag in (1.0, 0.5, 0.0):
        run_trials(
            f"lag={lag}s",
            lambda r, ratios, offset: gen_random_trades(r),
            lag,
            0.0,
            trials,
            pick_stage,
            rng,
        )

    print("\n完了")


if __name__ == "__main__":
    main()
