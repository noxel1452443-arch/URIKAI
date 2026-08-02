#!/usr/bin/env python3
"""チャートごとの HUMAN 到達しやすさ・とりうる幅を調べる。"""
from __future__ import annotations

import math
import random
import statistics
from pathlib import Path

from simulate_human_max import (
    cash_only_asset,
    display_price,
    human_max_asset,
    load_meta,
    load_series,
    ratio_at,
)
from simulate_sync_distribution import (
    gen_casual_trades,
    load_stages,
    percentile,
    simulate_trades,
)

OUT = Path(__file__).resolve().parents[1] / "tools" / "sync_feel_diagnosis.txt"


def sync(player: float, theo: float) -> float:
    return 0.0 if theo <= 0 else player / theo * 100.0


def corr(a: list[float], b: list[float]) -> float:
    n = len(a)
    if n < 2:
        return 0.0
    ma = sum(a) / n
    mb = sum(b) / n
    num = sum((x - ma) * (y - mb) for x, y in zip(a, b))
    da = math.sqrt(sum((x - ma) ** 2 for x in a))
    db = math.sqrt(sum((y - mb) ** 2 for y in b))
    return num / (da * db) if da and db else 0.0


def main() -> None:
    series = load_series()
    meta = load_meta()
    stages = load_stages()
    cash = cash_only_asset()
    rng = random.Random(0)
    lines: list[str] = []

    rows: list[dict] = []
    for s in stages:
        humans = [human_max_asset(s.ratios, o)[0] for o in range(11)]
        h_mean = statistics.mean(humans)
        h_std = statistics.pstdev(humans)
        ratios = s.ratios[:61]
        rmin, rmax = min(ratios), max(ratios)
        amp = (rmax - rmin) / ((rmin + rmax) / 2) if (rmin + rmax) > 0 else 0.0

        buckets = {
            "cash": [],
            "rand1": [],
            "swing": [],
            "casual": [],
            "assets_rand": [],
            "assets_swing": [],
        }
        for _ in range(220):
            off = rng.randint(0, 10)
            theo, _ = human_max_asset(s.ratios, off)
            buckets["cash"].append(sync(cash, theo))

            t0 = rng.randint(3, 30)
            t1 = rng.randint(t0 + 5, 55)
            p_rand = simulate_trades(s.ratios, off, [(t0, True), (t1, False)])
            buckets["rand1"].append(sync(p_rand, theo))
            buckets["assets_rand"].append(p_rand)

            seg = [display_price(ratio_at(s.ratios, off + i)) for i in range(60)]
            bi = min(range(5, 30), key=lambda i: seg[i])
            si = max(range(30, 55), key=lambda i: seg[i])
            if si > bi + 3:
                p_sw = simulate_trades(
                    s.ratios,
                    off,
                    [(max(2, bi - 1), True), (max(bi + 4, si - 1), False)],
                )
            else:
                p_sw = cash
            buckets["swing"].append(sync(p_sw, theo))
            buckets["assets_swing"].append(p_sw)

            p_cas = simulate_trades(s.ratios, off, gen_casual_trades(rng, s.ratios, off))
            buckets["casual"].append(sync(p_cas, theo))

        def spr(xs: list[float]) -> float:
            return percentile(xs, 90) - percentile(xs, 10)

        ruin = sum(1 for a in buckets["assets_rand"] if a < cash * 0.7) / len(
            buckets["assets_rand"]
        )
        below = sum(1 for a in buckets["assets_rand"] if a < cash) / len(
            buckets["assets_rand"]
        )
        # 同じ +1487 円が何星か（実感衝突の代理）
        asset_1487 = 10_000 + 1487
        sync_1487 = [sync(asset_1487, h) for h in humans]
        # 同じ +1000
        asset_1000 = 11_000
        sync_1000 = [sync(asset_1000, h) for h in humans]

        rows.append(
            {
                "sid": s.stage_id,
                "tier": s.tier,
                "shape": meta.get(s.stage_id, {}).get("macro_shape", "?"),
                "brand": meta.get(s.stage_id, {}).get("instrument", ""),
                "h": h_mean,
                "h_cv": (h_std / h_mean) if h_mean else 0.0,
                "amp": amp,
                "cash_s": statistics.mean(buckets["cash"]),
                "swing_s": statistics.mean(buckets["swing"]),
                "rand_s": statistics.mean(buckets["rand1"]),
                "rand_spr": spr(buckets["rand1"]),
                "swing_spr": spr(buckets["swing"]),
                "cas_s": statistics.mean(buckets["casual"]),
                "ruin": ruin,
                "below": below,
                "s1000": statistics.mean(sync_1000),
                "s1487": statistics.mean(sync_1487),
            }
        )

    rows.sort(key=lambda r: r["h"])
    lines.append(
        "sid  tier shape          HUMAN cashS swingS randS rSpr sSpr ruin belC  +1kS +1487S amp"
    )
    lines.append("-" * 110)
    for r in rows:
        lines.append(
            f"{r['sid']:4} {r['tier'][:3]:3} {r['shape'][:12]:12} "
            f"{r['h']:6.0f} {r['cash_s']:5.1f} {r['swing_s']:6.1f} {r['rand_s']:5.1f} "
            f"{r['rand_spr']:4.0f} {r['swing_spr']:4.0f} "
            f"{r['ruin']*100:4.0f}% {r['below']*100:4.0f}% "
            f"{r['s1000']:5.1f} {r['s1487']:6.1f} {r['amp']*100:5.1f}%"
        )

    H = [r["h"] for r in rows]
    lines.append("")
    lines.append(f"corr(HUMAN, cashSYNC)   = {corr(H, [r['cash_s'] for r in rows]):+.3f}")
    lines.append(f"corr(HUMAN, swingSYNC)  = {corr(H, [r['swing_s'] for r in rows]):+.3f}")
    lines.append(f"corr(HUMAN, ruinRate)   = {corr(H, [r['ruin'] for r in rows]):+.3f}")
    lines.append(f"corr(HUMAN, amp)        = {corr(H, [r['amp'] for r in rows]):+.3f}")
    lines.append(f"corr(HUMAN, randSpread) = {corr(H, [r['rand_spr'] for r in rows]):+.3f}")
    lines.append(f"corr(amp, cashSYNC)     = {corr([r['amp'] for r in rows], [r['cash_s'] for r in rows]):+.3f}")
    lines.append(f"corr(amp, swingSYNC)    = {corr([r['amp'] for r in rows], [r['swing_s'] for r in rows]):+.3f}")
    lines.append(f"corr(amp, ruinRate)     = {corr([r['amp'] for r in rows], [r['ruin'] for r in rows]):+.3f}")

    # 到達ギャップ: cash→swing の伸び（上手くなる余地の見え方）
    gaps = [r["swing_s"] - r["cash_s"] for r in rows]
    lines.append("")
    lines.append(
        f"swing-cash gap: mean={statistics.mean(gaps):.1f}  "
        f"min={min(gaps):.1f}  max={max(gaps):.1f}"
    )
    # 同じ円の星ブレ
    lines.append("")
    lines.append("同じ +1000円 の SYNC 分布（チャート間）:")
    s1 = [r["s1000"] for r in rows]
    lines.append(
        f"  mean={statistics.mean(s1):.1f}  p10={percentile(s1,10):.1f}  "
        f"p50={percentile(s1,50):.1f}  p90={percentile(s1,90):.1f}  "
        f"min={min(s1):.1f}  max={max(s1):.1f}"
    )
    s2 = [r["s1487"] for r in rows]
    lines.append("同じ +1487円 の SYNC 分布（チャート間）:")
    lines.append(
        f"  mean={statistics.mean(s2):.1f}  p10={percentile(s2,10):.1f}  "
        f"p50={percentile(s2,50):.1f}  p90={percentile(s2,90):.1f}  "
        f"min={min(s2):.1f}  max={max(s2):.1f}"
    )

    lines.append("")
    lines.append("FOCUS z06 / v07")
    for sid in ("z06", "v07"):
        r = next(x for x in rows if x["sid"] == sid)
        lines.append(
            f"  {sid} HUMAN={r['h']:.0f} cashS={r['cash_s']:.1f} swingS={r['swing_s']:.1f} "
            f"randSpr={r['rand_spr']:.0f} ruin={r['ruin']*100:.0f}% "
            f"+1k={r['s1000']:.1f}% +1487={r['s1487']:.1f}% amp={r['amp']*100:.1f}%"
        )

    # 仮説: HUMANが高いほど「同じ円のSYNC」が下がるのは定義どおり。
    # 加えて swing到達がHUMANと逆相関なら「高い理論ほど取りにくい」=別軸。
    lines.append("")
    lines.append("解釈メモ用: swingSYNC が低い=理論に対し上手い近似でも届きにくい")
    hard = sorted(rows, key=lambda r: r["swing_s"])[:8]
    easy = sorted(rows, key=lambda r: r["swing_s"], reverse=True)[:8]
    lines.append("最も swingSYNC が低い（到達しにくい代理）:")
    for r in hard:
        lines.append(f"  {r['sid']} {r['shape']:12} H={r['h']:.0f} swing={r['swing_s']:.1f} cash={r['cash_s']:.1f}")
    lines.append("最も swingSYNC が高い（到達しやすい代理）:")
    for r in easy:
        lines.append(f"  {r['sid']} {r['shape']:12} H={r['h']:.0f} swing={r['swing_s']:.1f} cash={r['cash_s']:.1f}")

    text = "\n".join(lines) + "\n"
    OUT.write_text(text, encoding="utf-8")
    print(text)
    print(f"wrote {OUT}")


if __name__ == "__main__":
    main()
