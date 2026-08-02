"""
content/keyframes.csv を再生成する。

設計方針:
  - マクロ: 骨格アンカー + PUNCH + CLIFF（だましの大変動）
  - 設計ミクロ: 脚内の 1〜数秒 ripple（densify 相当）— マクロ秒はロック
  - セッション・ミクロ: アプリ SessionMicroOverlay（プレイごと）

使い方:
  py -3 tools/rewrite_keyframes.py
  py -3 tools/generate_charts_csv.py
"""
from __future__ import annotations

import csv
import math
import random
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
META_PATH = ROOT / "content" / "stages_meta.csv"
OUT = ROOT / "content" / "keyframes.csv"

# stage_id -> (cliff_at_second, delta_ratio)  1秒で垂直に動かす
CLIFFS: dict[str, tuple[int, float]] = {
    "v03": (20, -14.0),
    "v05": (33, +11.0),
    "v08": (18, -20.0),
    "v10": (59, -24.0),
    "z03": (24, -12.0),
    "z05": (12, +16.0),
    "z07": (26, -14.0),
    "z09": (57, -22.0),
    "b02": (41, -24.0),
    "b03": (37, -28.0),
    "b04": (32, -17.0),
    "b05": (20, -19.0),
    "b06": (35, -38.0),
    "b08": (26, -16.0),
    "b09": (34, -15.0),
}

# 秒単位の強制上書き（アドバイス反映: 押し目/二番天井/垂直トラップ）
PUNCHES: dict[str, list[tuple[int, float]]] = {
    "v01": [(11, 84), (12, 90), (13, 82), (47, 94), (48, 84), (50, 92), (55, 104), (56, 98), (57, 108)],
    "v02": [(10, 88), (11, 94), (12, 80), (43, 92), (44, 74), (46, 86), (52, 102), (53, 90), (54, 106)],
    "v06": [(8, 86), (9, 92), (10, 80), (36, 90), (37, 76), (39, 86)],
    "b01": [(11, 108), (12, 118), (13, 110), (24, 128), (25, 138), (26, 130), (37, 142), (38, 148), (39, 138), (40, 145), (41, 128)],
    "b05": [(13, 106), (14, 118), (15, 108), (22, 120), (23, 128), (24, 118), (34, 136), (35, 144), (36, 134), (37, 143), (38, 130)],
    "b08": [(15, 112), (16, 122), (17, 114), (28, 128), (29, 136), (30, 126), (38, 140), (39, 146), (40, 136), (41, 142), (42, 128)],
    "v10": [(52, 88), (53, 92), (54, 96), (55, 102), (56, 112), (57, 118), (58, 120), (60, 94), (61, 90)],
    "z09": [(48, 104), (49, 108), (50, 112), (54, 118), (55, 122), (56, 124), (58, 98), (59, 94)],
    "b03": [(28, 118), (29, 128), (30, 118), (36, 132), (37, 104), (38, 112)],
    "b04": [(30, 112), (31, 118), (32, 108), (38, 122), (39, 124), (40, 116), (41, 108)],
    "z02": [(18, 108), (19, 96), (20, 104), (42, 106), (43, 92), (44, 100), (55, 112), (56, 98)],
    "z06": [(12, 104), (13, 88), (14, 96), (38, 102), (39, 86), (40, 94)],
    "z10": [(8, 96), (9, 108), (10, 98), (48, 100), (49, 88), (50, 96), (58, 98), (59, 86)],
}

HAND_TUNED: dict[str, list[tuple[int, float]]] = {}

# Extreme tier（内部のみ）: 50%級 ~12本 + 数倍級 3本（e13–e15）
EXTREME_ANCHORS: dict[str, list[tuple[int, float]]] = {
    "e01": [
        (0, 100), (8, 108), (15, 118), (22, 105), (30, 112), (38, 108), (45, 115),
        (52, 108), (56, 102), (58, 48), (62, 46), (69, 45),
    ],
    "e02": [
        (0, 100), (10, 92), (18, 98), (28, 108), (38, 112), (48, 118), (54, 122),
        (57, 58), (60, 52), (65, 50), (69, 48),
    ],
    "e03": [
        (0, 100), (12, 104), (24, 108), (36, 112), (44, 116), (50, 118), (54, 115),
        (57, 50), (60, 46), (69, 42),
    ],
    "e04": [
        (0, 100), (12, 98), (24, 96), (36, 97), (45, 98), (48, 102), (50, 108),
        (52, 112), (54, 138), (56, 162), (58, 175), (62, 180), (69, 185),
    ],
    "e05": [
        (0, 100), (6, 75), (8, 72), (14, 85), (22, 92), (30, 88), (40, 95),
        (50, 98), (55, 88), (57, 42), (62, 40), (69, 38),
    ],
    "e06": [
        (0, 100), (8, 88), (16, 76), (24, 82), (32, 90), (40, 98), (48, 104),
        (52, 108), (55, 112), (57, 138), (59, 152), (62, 156), (69, 158),
    ],
    "e07": [
        (0, 100), (6, 85), (10, 82), (18, 88), (26, 92), (34, 88), (42, 94),
        (50, 98), (54, 102), (56, 118), (58, 132), (62, 138), (69, 142),
    ],
    "e08": [
        (0, 100), (12, 108), (20, 115), (28, 122), (36, 118), (44, 125), (50, 118),
        (56, 115), (57, 50), (60, 48), (69, 46),
    ],
    "e09": [
        (0, 100), (10, 105), (18, 112), (26, 118), (34, 125), (42, 128), (48, 122),
        (54, 125), (55, 58), (58, 52), (69, 50),
    ],
    "e10": [
        (0, 100), (18, 99), (28, 97), (34, 55), (35, 48), (36, 52), (40, 68),
        (45, 82), (50, 95), (54, 108), (58, 128), (62, 142), (69, 148),
    ],
    "e11": [
        (0, 100), (8, 110), (16, 125), (24, 142), (32, 158), (38, 148), (44, 155),
        (48, 95), (52, 78), (58, 72), (69, 68),
    ],
    "e12": [
        (0, 100), (6, 108), (12, 118), (18, 128), (24, 132), (32, 118), (42, 108),
        (50, 98), (54, 88), (58, 78), (62, 68), (65, 62), (69, 58),
    ],
    "e13": [
        (0, 100), (15, 110), (28, 120), (38, 132), (45, 148), (50, 168),
        (52, 220), (54, 265), (56, 245), (58, 225), (62, 215), (69, 205),
    ],
    "e14": [
        (0, 100), (12, 115), (22, 135), (32, 165), (40, 210), (46, 280),
        (48, 310), (50, 270), (54, 220), (58, 180), (62, 155), (69, 140),
    ],
    "e15": [
        (0, 100), (12, 102), (22, 105), (32, 108), (40, 112), (44, 118),
        (48, 125), (50, 280), (51, 265), (52, 272), (54, 268), (58, 262),
        (62, 255), (66, 250), (69, 248),
    ],
}

EXTREME_CLIFFS: dict[str, tuple[int, float]] = {
    "e01": (57, -54),
    "e02": (57, -64),
    "e03": (57, -65),
    "e05": (57, -46),
    "e08": (57, -65),
    "e09": (55, -67),
}

# 終盤の垂直上昇（期待裏切りのリスクオン）
EXTREME_SURGES: dict[str, tuple[int, float]] = {
    "e04": (54, +42),
    "e06": (57, +42),
    "e07": (56, +32),
    "e10": (40, +32),
    "e15": (50, +155),
}

EXTREME_PUNCHES: dict[str, list[tuple[int, float]]] = {
    "e01": [(57, 52), (58, 48)],
    "e02": [(56, 122), (57, 58)],
    "e04": [(53, 112), (54, 138)],
    "e06": [(56, 112), (57, 138)],
    "e07": [(55, 102), (56, 118)],
    "e10": [(34, 55), (35, 48), (36, 52), (39, 68)],
    "e13": [(53, 265), (54, 250)],
    "e14": [(47, 310), (48, 295)],
    "e15": [(49, 125), (50, 280), (52, 272)],
}

ALL_CLIFFS = {**CLIFFS, **EXTREME_CLIFFS, **EXTREME_SURGES}
ALL_PUNCHES = {**PUNCHES, **EXTREME_PUNCHES}


@dataclass(frozen=True)
class StageMeta:
    stage_id: str
    macro_shape: str
    pivot_timing: str
    trick: str
    seed: int


def emit(stage_id: str, points: list[tuple[int, float]]) -> list[tuple[str, int, float]]:
    if len(points) != 70:
        raise ValueError(f"{stage_id}: expected 70 seconds, got {len(points)}")
    if points[0][0] != 0 or points[-1][0] != 69:
        raise ValueError(f"{stage_id}: must span 0..69")
    secs = [s for s, _ in points]
    if len(secs) != len(set(secs)):
        raise ValueError(f"{stage_id}: duplicate seconds")
    for i in range(len(points) - 1):
        if points[i][0] >= points[i + 1][0]:
            raise ValueError(f"{stage_id}: seconds must increase")
    return [(stage_id, s, round(p, 1)) for s, p in points]


def price_at(points: dict[int, float], sec: int) -> float:
    if sec in points:
        return points[sec]
    keys = sorted(points.keys())
    if sec <= keys[0]:
        return points[keys[0]]
    if sec >= keys[-1]:
        return points[keys[-1]]
    for i in range(len(keys) - 1):
        s0, s1 = keys[i], keys[i + 1]
        if s0 <= sec <= s1:
            t = 0.0 if s1 == s0 else (sec - s0) / (s1 - s0)
            return points[s0] + (points[s1] - points[s0]) * t
    return points[keys[-1]]


def fill_every_second(anchors: list[tuple[int, float]]) -> list[tuple[int, float]]:
    d = {int(s): float(p) for s, p in anchors}
    return [(sec, round(price_at(d, sec), 1)) for sec in range(70)]


def apply_punches(series: list[tuple[int, float]], punches: list[tuple[int, float]]) -> list[tuple[int, float]]:
    d = dict(series)
    for sec, price in punches:
        d[int(sec)] = float(price)
    return [(sec, d[sec]) for sec in range(70)]


def apply_cliff(series: list[tuple[int, float]], cliff_sec: int, delta: float) -> list[tuple[int, float]]:
    d = dict(series)
    prev = d.get(cliff_sec - 1, d[cliff_sec])
    d[cliff_sec] = round(prev + delta, 1)
    return [(sec, d[sec]) for sec in range(70)]


def macro_locked_seconds(series: list[tuple[int, float]], stage_id: str) -> set[int]:
    """PUNCH / CLIFF / 大きな1秒変化 — 設計ミクロで上書きしない。"""
    locked: set[int] = set()
    for sec, _ in ALL_PUNCHES.get(stage_id, []):
        locked.add(int(sec))
    if stage_id in ALL_CLIFFS:
        cliff_sec, _ = ALL_CLIFFS[stage_id]
        locked.add(cliff_sec)
        if cliff_sec > 0:
            locked.add(cliff_sec - 1)
    for i in range(1, len(series)):
        s0, p0 = series[i - 1]
        s1, p1 = series[i]
        if abs(p1 - p0) >= 8.0:
            locked.add(int(s0))
            locked.add(int(s1))
    return locked


def micro_amp_for(shape: str) -> float:
    return {
        "RANGE": 2.2,
        "N_RALLY": 2.3,
        "INV_N": 2.3,
        "V_RECOVER": 2.8,
        "W_DOUBLE": 2.7,
        "EARLY_SHOCK": 2.6,
        "LATE_SPIKE": 2.5,
        "BUBBLE": 2.4,
        "INVERSE_V": 2.5,
        "GRIND_UP": 2.3,
        "GRIND_DOWN": 2.3,
    }.get(shape, 2.5)


def add_design_micro(
    series: list[tuple[int, float]],
    seed: int,
    macro_shape: str,
    locked: set[int],
) -> list[tuple[int, float]]:
    """脚内の設計ミクロ（旧 densify）。マクロロック秒は触らない。"""
    amp = micro_amp_for(macro_shape)
    rng = random.Random(seed)
    out: list[tuple[int, float]] = []
    leg = 0
    prev_sec = -1
    for sec, price in series:
        if sec in locked or abs(price - (out[-1][1] if out else price)) >= 8.0:
            out.append((sec, price))
            if sec != prev_sec:
                leg += 1
            prev_sec = sec
            continue
        freq = rng.uniform(1.3, 2.4)
        wiggle = math.sin(sec * freq + leg * 0.9) * amp
        if sec % 2 == 0:
            wiggle += rng.uniform(-1.2, 1.2)
        if rng.random() < 0.08:
            wiggle += rng.choice([-1, 1]) * amp * rng.uniform(1.5, 3.0)
        out.append((sec, round(price + wiggle, 1)))
        prev_sec = sec
    return out


def light_noise(series: list[tuple[int, float]], seed: int, amp: float = 0.12) -> list[tuple[int, float]]:
    rng = random.Random(seed + 911)
    out: list[tuple[int, float]] = []
    prev = series[0][1]
    for sec, price in series:
        if abs(price - prev) > 6.0:
            out.append((sec, price))
        else:
            n = rng.uniform(-amp, amp) if sec % 2 == 0 else 0.0
            out.append((sec, round(price + n, 1)))
        prev = price
    return out


def load_meta() -> list[StageMeta]:
    rows: list[StageMeta] = []
    with META_PATH.open(encoding="utf-8-sig", newline="") as f:
        for row in csv.DictReader(f):
            sid = row["stage_id"].strip()
            if not sid:
                continue
            rows.append(
                StageMeta(
                    stage_id=sid,
                    macro_shape=row["macro_shape"].strip(),
                    pivot_timing=row["pivot_timing"].strip(),
                    trick=(row.get("trick") or "").strip(),
                    seed=int(row["seed"]),
                )
            )
    standard = [r for r in rows if not r.stage_id.startswith("e")]
    extreme = [r for r in rows if r.stage_id.startswith("e")]
    if len(standard) != 30:
        raise ValueError(f"expected 30 standard stages in {META_PATH}, got {len(standard)}")
    if len(extreme) != 15:
        raise ValueError(f"expected 15 extreme stages in {META_PATH}, got {len(extreme)}")
    return rows


def stage_idx(stage_id: str) -> int:
    return int(stage_id[1:])


def is_extreme_stage(stage_id: str) -> bool:
    return stage_id.startswith("e") and len(stage_id) == 3 and stage_id[1:].isdigit()


def pivot_band(timing: str) -> tuple[int, int, int]:
    if timing == "EARLY":
        return (14, 22, 26)
    if timing == "LATE":
        return (36, 44, 48)
    return (22, 30, 36)


def merge_anchors(points: list[tuple[int, float]]) -> list[tuple[int, float]]:
    d: dict[int, float] = {}
    for sec, price in points:
        d[sec] = price
    return sorted(d.items())


def apply_trick(
    anchors: list[tuple[int, float]],
    trick: str,
    shape: str,
    idx: int,
) -> list[tuple[int, float]]:
    if not trick:
        return anchors
    d = dict(anchors)
    drop_end, bottom, rally = pivot_band("MID")

    if trick == "BEAR_TRAP" and shape in {"V_RECOVER", "LATE_SPIKE", "EARLY_SHOCK"}:
        trap = rally + 6 + (idx % 3)
        d[trap] = d.get(bottom, 72) + 14
        d[trap + 1] = d.get(bottom, 72) + 4
        d[trap + 3] = d.get(bottom, 72) + 10
    elif trick == "BULL_TRAP" and shape in {"RANGE", "N_RALLY", "GRIND_UP", "INV_N"}:
        trap = drop_end + 10 + (idx % 4)
        d[trap] = d.get(trap - 2, 100) + 16 + (idx % 3)
        d[trap + 1] = d.get(trap - 2, 100) - 4
        d[trap + 2] = d.get(trap - 2, 100) - 10
    elif trick == "DEAD_CAT" and shape == "W_DOUBLE":
        d[drop_end + 4] = d.get(drop_end, 82) + 10
        d[drop_end + 6] = d.get(drop_end, 82) - 6
    elif trick == "SECOND_WAVE" and shape == "W_DOUBLE":
        mid = rally + 14
        d[mid] = d.get(rally + 6, 98) - 10
        d[mid + 4] = d.get(rally + 6, 98) + 2
    return merge_anchors(list(d.items()))


def build_v_recover(timing: str, idx: int) -> list[tuple[int, float]]:
    drop_end, bottom, rally = pivot_band(timing)
    low = 64 + (idx % 5)
    high = 110 + (idx % 4)
    return [
        (0, 100),
        (max(6, drop_end - 12), 92),
        (max(10, drop_end - 6), 96),
        (drop_end, 82),
        (drop_end + 3, 88),
        (bottom, low),
        (bottom + 5, low + 6),
        (rally - 4, low + 12),
        (rally + 4, low + 18),
        (rally + 14, 94),
        (min(58, rally + 24), high - 4),
        (69, high),
    ]


def build_w_double(timing: str, idx: int) -> list[tuple[int, float]]:
    drop_end, bottom, rally = pivot_band(timing)
    first_low = 76 + (idx % 3)
    true_low = 62 + (idx % 4)
    high = 108 + (idx % 5)
    return [
        (0, 100),
        (max(8, drop_end - 8), first_low),
        (drop_end, first_low + 8),
        (drop_end + 5, first_low - 4),
        (bottom, true_low),
        (bottom + 6, true_low + 8),
        (rally, true_low + 18),
        (rally + 16, 96),
        (min(64, rally + 28), high),
        (69, high + 2),
    ]


def build_early_shock(timing: str, idx: int) -> list[tuple[int, float]]:
    del timing
    shock = 6 + (idx % 3)
    low = 72 + (idx % 4)
    settle = 106 + (idx % 4)
    return [
        (0, 100),
        (shock, low),
        (shock + 3, low - 6),
        (shock + 7, low + 8),
        (20, low + 14),
        (32, 88 + (idx % 3)),
        (46, 98 + (idx % 3)),
        (58, settle),
        (69, settle + 2),
    ]


def build_late_spike(timing: str, idx: int) -> list[tuple[int, float]]:
    del timing
    base = 84 + (idx % 3)
    spike = 120 + (idx % 4)
    crash = spike - 26
    return [
        (0, 100),
        (14, 94),
        (28, 90),
        (40, base),
        (48, base - 2),
        (52, base + 2),
        (55, base + 8),
        (57, spike - 8),
        (58, spike - 2),
        (59, spike),
        (60, crash),
        (63, crash + 6),
        (69, crash + 4),
    ]


def build_range_macro(timing: str, idx: int) -> list[tuple[int, float]]:
    """レンジのマクロ骨格のみ（不規則な振幅推移）。ミクロは add_design_micro へ。"""
    rng = random.Random(300 + idx * 17)
    vol_mode = {2: "expand", 4: "expand", 6: "expand", 8: "contract", 10: "contract"}.get(
        idx, "irregular"
    )
    if timing == "EARLY":
        vol_mode = "contract" if vol_mode == "irregular" else vol_mode
    if timing == "LATE":
        vol_mode = "expand" if vol_mode == "irregular" else vol_mode

    anchors: list[tuple[int, float]] = [(0, 100.0)]
    sec = 0
    price = 100.0
    while sec < 69:
        step = rng.randint(4, 9)
        sec = min(sec + step, 69)
        t = sec / 60.0
        if vol_mode == "expand":
            band = 3.0 + t * 12.0
        elif vol_mode == "contract":
            band = 13.0 - t * 9.0
        else:
            band = 4.0 + rng.uniform(0.0, 8.0)
        price = max(88.0, min(112.0, price + rng.uniform(-band, band)))
        anchors.append((sec, round(price, 1)))
    anchors[-1] = (69, round(100.0 + (idx % 5) - 2.0, 1))
    return anchors


def build_n_rally(timing: str, idx: int) -> list[tuple[int, float]]:
    _, _, rally = pivot_band(timing)
    peak1 = 112 + (idx % 4)
    trough = 88 + (idx % 3)
    peak2 = 118 + (idx % 4)
    crash = peak2 - 24
    return [
        (0, 100),
        (max(12, rally - 16), peak1),
        (max(20, rally - 6), peak1 - 6),
        (rally, trough),
        (rally + 8, trough + 6),
        (min(52, rally + 18), peak2 - 12),
        (min(56, rally + 22), peak2 - 4),
        (min(58, rally + 24), peak2 + 2),
        (min(59, rally + 25), peak2 + 4),
        (min(60, rally + 26), crash),
        (69, crash + 4),
    ]


def build_inv_n(timing: str, idx: int) -> list[tuple[int, float]]:
    drop_end, bottom, rally = pivot_band(timing)
    low = 84 + (idx % 3)
    bounce = 112 + (idx % 4)
    return [
        (0, 100),
        (max(8, drop_end - 4), low),
        (bottom, low - 6),
        (rally, bounce),
        (rally + 8, bounce - 10),
        (min(58, rally + 22), low + 2),
        (69, low + 6),
    ]


def build_bubble(timing: str, idx: int) -> list[tuple[int, float]]:
    peak = 138 + (idx % 5) * 3
    if timing == "EARLY":
        peak_sec = 26 + (idx % 3)
    elif timing == "LATE":
        peak_sec = 42 + (idx % 4)
    else:
        peak_sec = 34 + (idx % 3)
    first_top = peak_sec - 8
    return [
        (0, 100),
        (6, 108),
        (10, 118),
        (14, 110),
        (first_top, peak - 12),
        (first_top + 2, peak - 24),
        (first_top + 5, peak - 14),
        (peak_sec - 1, peak - 4),
        (peak_sec, peak),
        (peak_sec + 1, peak - 12),
        (peak_sec + 6, peak - 26),
        (peak_sec + 14, peak - 40),
        (peak_sec + 22, peak - 52),
        (69, peak - 58),
    ]


def build_inverse_v(timing: str, idx: int) -> list[tuple[int, float]]:
    peak = 126 + (idx % 5) * 2
    if timing == "EARLY":
        peak_sec = 16 + (idx % 3)
    elif timing == "LATE":
        peak_sec = 38 + (idx % 3)
    else:
        peak_sec = 28 + (idx % 3)
    return [
        (0, 100),
        (max(6, peak_sec - 12), 110),
        (peak_sec - 2, peak - 4),
        (peak_sec, peak),
        (peak_sec + 3, peak - 14),
        (peak_sec + 10, peak - 28),
        (peak_sec + 18, peak - 40),
        (69, peak - 48),
    ]


def build_grind_up(timing: str, idx: int) -> list[tuple[int, float]]:
    high = 122 + (idx % 4) * 2
    turn = {"EARLY": 28, "MID": 38, "LATE": 48}[timing]
    return [
        (0, 100),
        (8, 104),
        (14, 108),
        (20, 106),
        (26, 112),
        (32, 116),
        (turn, high),
        (min(turn + 3, 54), high - 10),
        (min(turn + 6, 58), high - 4),
        (min(turn + 9, 61), high + 4),
        (min(turn + 12, 64), high - 16),
        (69, high - 32),
    ]


def build_grind_down(timing: str, idx: int) -> list[tuple[int, float]]:
    low = 84 + (idx % 3)
    accel = {"EARLY": 22, "MID": 34, "LATE": 46}[timing]
    return [
        (0, 100),
        (12, 96),
        (22, 92),
        (accel, 88),
        (min(accel + 10, 58), low + 12),
        (min(accel + 18, 64), low + 6),
        (min(accel + 24, 67), low),
        (69, low + 2),
    ]


BUILDERS = {
    "V_RECOVER": build_v_recover,
    "W_DOUBLE": build_w_double,
    "EARLY_SHOCK": build_early_shock,
    "LATE_SPIKE": build_late_spike,
    "RANGE": build_range_macro,
    "N_RALLY": build_n_rally,
    "INV_N": build_inv_n,
    "BUBBLE": build_bubble,
    "INVERSE_V": build_inverse_v,
    "GRIND_UP": build_grind_up,
    "GRIND_DOWN": build_grind_down,
}


def build_stage(meta: StageMeta) -> list[tuple[int, float]]:
    idx = stage_idx(meta.stage_id)
    builder = BUILDERS.get(meta.macro_shape)
    if builder is None:
        raise ValueError(f"{meta.stage_id}: unknown macro_shape {meta.macro_shape!r}")

    anchors = builder(meta.pivot_timing, idx)
    anchors = apply_trick(anchors, meta.trick, meta.macro_shape, idx)
    series = fill_every_second(anchors)

    if meta.stage_id in ALL_PUNCHES:
        series = apply_punches(series, ALL_PUNCHES[meta.stage_id])

    if meta.stage_id in ALL_CLIFFS:
        cliff_sec, delta = ALL_CLIFFS[meta.stage_id]
        series = apply_cliff(series, cliff_sec, delta)

    locked = macro_locked_seconds(series, meta.stage_id)
    series = add_design_micro(series, meta.seed, meta.macro_shape, locked)
    series = light_noise(series, meta.seed + 7, amp=0.15)
    return series


def build_extreme_stage(meta: StageMeta) -> list[tuple[int, float]]:
    anchors = EXTREME_ANCHORS.get(meta.stage_id)
    if anchors is None:
        raise ValueError(f"{meta.stage_id}: missing EXTREME_ANCHORS entry")
    anchors = apply_trick(anchors, meta.trick, meta.macro_shape, stage_idx(meta.stage_id))
    series = fill_every_second(anchors)

    if meta.stage_id in EXTREME_PUNCHES:
        series = apply_punches(series, EXTREME_PUNCHES[meta.stage_id])

    if meta.stage_id in EXTREME_CLIFFS:
        cliff_sec, delta = EXTREME_CLIFFS[meta.stage_id]
        series = apply_cliff(series, cliff_sec, delta)

    if meta.stage_id in EXTREME_SURGES:
        surge_sec, delta = EXTREME_SURGES[meta.stage_id]
        series = apply_cliff(series, surge_sec, delta)

    locked = macro_locked_seconds(series, meta.stage_id)
    series = add_design_micro(series, meta.seed, meta.macro_shape, locked)
    series = light_noise(series, meta.seed + 7, amp=0.22)
    return series


def main() -> None:
    metas = load_meta()
    rows: list[tuple[str, int, float]] = []

    for meta in metas:
        if is_extreme_stage(meta.stage_id):
            points = build_extreme_stage(meta)
        else:
            points = build_stage(meta)
        rows.extend(emit(meta.stage_id, points))

    OUT.parent.mkdir(parents=True, exist_ok=True)
    with OUT.open("w", encoding="utf-8", newline="") as f:
        w = csv.writer(f)
        w.writerow(["stage_id", "second", "price_ratio"])
        for sid, sec, pr in rows:
            w.writerow([sid, sec, f"{pr:.1f}"])

    n_stages = len(metas)
    n_extreme = sum(1 for m in metas if is_extreme_stage(m.stage_id))
    print(
        f"wrote {OUT.relative_to(ROOT)} ({len(rows)} keyframes = 70/sec × {n_stages}, "
        f"{len(ALL_CLIFFS)} cliffs, {len(ALL_PUNCHES)} punch sets, {n_extreme} extreme)"
    )


if __name__ == "__main__":
    main()
