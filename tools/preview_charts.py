"""
全ステージのチャート形状＋ラグ付き理論最大を一覧 PNG で出力する。

  py -3 tools/preview_charts.py

入力: shared/src/commonMain/composeResources/files/charts.csv
出力: content/chart_overview.png
"""
from __future__ import annotations

import csv
from collections import defaultdict
from pathlib import Path

import matplotlib.pyplot as plt
from matplotlib import rcParams

ROOT = Path(__file__).resolve().parents[1]
CHARTS = ROOT / "shared" / "src" / "commonMain" / "composeResources" / "files" / "charts.csv"
META = ROOT / "content" / "stages_meta.csv"
OUT = ROOT / "content" / "chart_overview.png"

PLAY_SECONDS = 60
INITIAL_ASSET = 10_000
DURATION = 60
BASE_RATIO = 100.0
INITIAL_DISPLAY = 1_000
TOTAL_RATE = 0.045
ORDER_LAG_SEC = 1
ORDER_COOLDOWN_SEC = 1

from simulate_human_max import human_max_asset as _human_max_asset  # noqa: E402


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


def theoretical_max_asset(ratios: list[float], offset: int = 0) -> int:
    """SyncCalculator HUMAN MAX 相当。"""
    value, _ = _human_max_asset(ratios, offset)
    return value


def theo_summary(ratios: list[float]) -> tuple[int, int, int]:
    """offset 0..10 の mean / min / max（整数）。"""
    vals = [theoretical_max_asset(ratios, off) for off in range(0, 11)]
    return round(sum(vals) / len(vals)), min(vals), max(vals)


def main() -> None:
    if not CHARTS.exists():
        raise FileNotFoundError(f"Missing {CHARTS}. Run generate_charts_csv.py first.")

    series = load_series()
    meta = load_meta()
    stage_ids = sorted(series.keys(), key=lambda s: (s[0], int(s[1:])))

    rcParams["font.family"] = "Meiryo"
    rcParams["axes.unicode_minus"] = False

    cols, rows = 9, 5
    fig, axes = plt.subplots(rows, cols, figsize=(22, 17), facecolor="#0a0a0a")
    fig.suptitle(
        "URIKAI — chart shapes + HUMAN MAX "
        f"(offset0–10 mean · cash-only≈Y{10_480:,})",
        color="#e0e0e0",
        fontsize=13,
        y=0.985,
    )

    for i, ax in enumerate(axes.flat):
        if i >= len(stage_ids):
            ax.axis("off")
            continue
        sid = stage_ids[i]
        ratios = series[sid]
        play = ratios[: PLAY_SECONDS + 1]
        xs = list(range(len(play)))
        ys = play
        m = meta.get(sid, {})
        shape = m.get("macro_shape", "?")
        instrument = m.get("instrument", "")
        tier = m.get("tier", "")
        mean_t, min_t, max_t = theo_summary(ratios)
        ext = tier == "EXTREME"
        line = "#ff7a59" if ext else "#00e5cc"
        tag = " EXT" if ext else ""

        ax.set_facecolor("#111111")
        ax.plot(xs, ys, color=line, linewidth=1.35)
        ax.axhline(100, color="#444444", linewidth=0.55, linestyle="--")
        ax.set_xlim(0, PLAY_SECONDS)
        lo = min(ys)
        hi = max(ys)
        pad = max(3.0, (hi - lo) * 0.08)
        ax.set_ylim(lo - pad, hi + pad)

        title = f"{sid}{tag}  {shape}"
        if instrument:
            title += f"\n{instrument}"
        title += f"\nHUMAN Y{mean_t:,}  ({min_t:,}–{max_t:,})"
        ax.set_title(title, color="#cccccc", fontsize=7.2, pad=3, linespacing=1.15)
        ax.tick_params(colors="#666666", labelsize=5.5)
        for spine in ax.spines.values():
            spine.set_color("#333333")

    fig.tight_layout(rect=[0, 0, 1, 0.97])
    OUT.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(OUT, dpi=150, facecolor=fig.get_facecolor())
    plt.close(fig)
    print(f"wrote {OUT.relative_to(ROOT)} ({len(stage_ids)} charts)")


if __name__ == "__main__":
    main()
