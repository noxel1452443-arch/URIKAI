"""
content/ の編集用CSVから、アプリ用 charts.csv を生成する。

正本（編集する場所）:
  content/stages_meta.csv   … 表示名・史実コメント・ヒント等（1ステージ1行）
  content/keyframes.csv     … 形状の制御点（stage_id, second, price_ratio）

成果物（手編集しない）:
  app/src/main/assets/charts.csv  … ランタイム用・70秒展開済み

使い方:
  py -3 tools/generate_charts_csv.py
"""
from __future__ import annotations

import csv
import random
import re
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONTENT = ROOT / "content"
META_PATH = CONTENT / "stages_meta.csv"
KF_PATH = CONTENT / "keyframes.csv"
OUT_PATH = ROOT / "app" / "src" / "main" / "assets" / "charts.csv"

POINT_COUNT = 70
INITIAL_ASSET = 10_000
BASE_RATIO = 100.0
INITIAL_DISPLAY = 1_000
TARGET_THEO_MIN = 15_000
TARGET_THEO_MAX = 22_000
TARGET_THEO_MID = 18_500

ALLOWED_TIERS = {"STANDARD", "EXTREME"}

# 内部タグ（ユーザー向け UI には出さない）
ALLOWED_TYPES = {"DROP_REBOUND", "WHIPSAW", "RUNAWAY"}
LEGACY_TYPE_MAP = {
    "V_SHAPE": "DROP_REBOUND",
    "ZIGZAG": "WHIPSAW",
    "BUBBLE": "RUNAWAY",
}

ALLOWED_MACRO_SHAPES = {
    "V_RECOVER",
    "W_DOUBLE",
    "INVERSE_V",
    "N_RALLY",
    "INV_N",
    "RANGE",
    "BUBBLE",
    "GRIND_DOWN",
    "GRIND_UP",
    "LATE_SPIKE",
    "EARLY_SHOCK",
}
ALLOWED_PIVOT_TIMING = {"EARLY", "MID", "LATE"}
ALLOWED_MARKET_KINDS = {"INDEX", "FX", "CRYPTO", "OIL", "STOCK"}
ALLOWED_TRICKS = {
    "",
    "BEAR_TRAP",
    "BULL_TRAP",
    "DEAD_CAT",
    "SECOND_WAVE",
}
MACRO_TO_SCENARIO = {
    "V_RECOVER": "DROP_REBOUND",
    "W_DOUBLE": "DROP_REBOUND",
    "EARLY_SHOCK": "DROP_REBOUND",
    "LATE_SPIKE": "DROP_REBOUND",
    "RANGE": "WHIPSAW",
    "N_RALLY": "WHIPSAW",
    "INV_N": "WHIPSAW",
    "BUBBLE": "RUNAWAY",
    "INVERSE_V": "RUNAWAY",
    "GRIND_DOWN": "RUNAWAY",
    "GRIND_UP": "RUNAWAY",
}


@dataclass
class StageDef:
    stage_id: str
    brand_name: str
    brand_name_en: str
    period: str
    period_en: str
    market_kind: str
    instrument_name: str
    instrument_name_en: str
    calendar_start: str
    calendar_start_en: str
    calendar_end: str
    calendar_end_en: str
    anchor_when: str
    anchor_when_en: str
    anchor_band_start: int
    anchor_band_end: int
    scenario_type: str
    macro_shape: str
    pivot_timing: str
    trick: str
    hint_text: str
    hint_text_en: str
    anchor_label: str
    anchor_label_en: str
    history_comment: str
    history_comment_en: str
    seed: int
    tier: str
    keyframes: list[tuple[int, float]]


def normalize_scenario_type(raw: str) -> str:
    stype = raw.strip()
    stype = LEGACY_TYPE_MAP.get(stype, stype)
    if stype not in ALLOWED_TYPES:
        raise ValueError(f"scenario_type must be one of {ALLOWED_TYPES} (legacy ok), got {raw!r}")
    return stype


def normalize_macro_shape(raw: str) -> str:
    shape = raw.strip()
    if shape not in ALLOWED_MACRO_SHAPES:
        raise ValueError(f"macro_shape must be one of {ALLOWED_MACRO_SHAPES}, got {raw!r}")
    return shape


def normalize_market_kind(raw: str) -> str:
    kind = raw.strip().upper()
    if kind not in ALLOWED_MARKET_KINDS:
        raise ValueError(f"market_kind must be one of {ALLOWED_MARKET_KINDS}, got {raw!r}")
    return kind


def normalize_pivot_timing(raw: str) -> str:
    timing = raw.strip()
    if timing not in ALLOWED_PIVOT_TIMING:
        raise ValueError(f"pivot_timing must be one of {ALLOWED_PIVOT_TIMING}, got {raw!r}")
    return timing


def normalize_trick(raw: str | None) -> str:
    trick = (raw or "").strip()
    if trick not in ALLOWED_TRICKS:
        raise ValueError(f"trick must be one of {ALLOWED_TRICKS - {''}}, or empty, got {raw!r}")
    return trick


def normalize_tier(raw: str | None) -> str:
    tier = (raw or "").strip().upper() or "STANDARD"
    if tier not in ALLOWED_TIERS:
        raise ValueError(f"tier must be one of {ALLOWED_TIERS} or empty, got {raw!r}")
    return tier


def normalize_calendar_range(start: str, end: str, stage_id: str) -> tuple[str, str]:
    cal_start = start.strip()
    cal_end = end.strip()
    if not cal_start or not cal_end:
        raise ValueError(f"{stage_id}: calendar_start and calendar_end are required")
    return cal_start, cal_end


def normalize_anchor_when(raw: str, stage_id: str) -> str:
    """peek 用: 特定日1つ（範囲・月のみ不可）。"""
    when = raw.strip()
    if not when:
        raise ValueError(f"{stage_id}: anchor_when is required")
    if "〜" in when or "～" in when or "−" in when or "~" in when.lower():
        raise ValueError(
            f"{stage_id}: anchor_when must be a single day (no range): {when!r}"
        )
    if re.fullmatch(r"\d{4}年\d{1,2}月", when):
        raise ValueError(
            f"{stage_id}: anchor_when must include day, not month-only: {when!r}"
        )
    if not re.search(r"\d{1,2}日", when):
        raise ValueError(
            f"{stage_id}: anchor_when must include day (e.g. 2020年3月19日): {when!r}"
        )
    return when


def is_extreme_stage_id(stage_id: str) -> bool:
    return (
        len(stage_id) == 3
        and stage_id.startswith("e")
        and stage_id[1:].isdigit()
    )


def assert_tier_matches_stage_id(stage_id: str, tier: str) -> None:
    extreme = is_extreme_stage_id(stage_id)
    if extreme and tier != "EXTREME":
        raise ValueError(f"{stage_id}: extreme stage_id requires tier=EXTREME, got {tier!r}")
    if not extreme and tier == "EXTREME":
        raise ValueError(f"{stage_id}: tier=EXTREME requires stage_id e01..e99")


def assert_scenario_matches_macro(stage_id: str, scenario_type: str, macro_shape: str) -> None:
    expected = MACRO_TO_SCENARIO[macro_shape]
    if scenario_type != expected:
        raise ValueError(
            f"{stage_id}: scenario_type={scenario_type!r} does not match "
            f"macro_shape={macro_shape!r} (expected {expected!r})"
        )


def load_stages_from_content() -> list[StageDef]:
    if not META_PATH.exists():
        raise FileNotFoundError(f"Missing {META_PATH}")
    if not KF_PATH.exists():
        raise FileNotFoundError(f"Missing {KF_PATH}")

    with META_PATH.open(encoding="utf-8-sig", newline="") as f:
        meta_rows = list(csv.DictReader(f))

    kf_by_stage: dict[str, list[tuple[int, float]]] = {}
    with KF_PATH.open(encoding="utf-8-sig", newline="") as f:
        for row in csv.DictReader(f):
            sid = row["stage_id"].strip()
            sec = int(row["second"])
            ratio = float(row["price_ratio"])
            kf_by_stage.setdefault(sid, []).append((sec, ratio))

    stages: list[StageDef] = []
    seen: set[str] = set()
    for row in meta_rows:
        sid = row["stage_id"].strip()
        if not sid:
            continue
        if sid in seen:
            raise ValueError(f"Duplicate stage_id in stages_meta.csv: {sid}")
        seen.add(sid)
        stype = normalize_scenario_type(row["scenario_type"])
        macro_shape = normalize_macro_shape(row["macro_shape"])
        pivot_timing = normalize_pivot_timing(row["pivot_timing"])
        trick = normalize_trick(row.get("trick"))
        tier = normalize_tier(row.get("tier"))
        assert_tier_matches_stage_id(sid, tier)
        assert_scenario_matches_macro(sid, stype, macro_shape)
        kfs = sorted(kf_by_stage.get(sid, []), key=lambda x: x[0])
        if not kfs:
            raise ValueError(f"{sid}: no keyframes in keyframes.csv")
        if kfs[0][0] != 0:
            raise ValueError(f"{sid}: keyframes must start at second=0")
        if kfs[-1][0] != POINT_COUNT - 1:
            raise ValueError(f"{sid}: keyframes must end at second={POINT_COUNT - 1}")
        secs = [s for s, _ in kfs]
        if len(secs) != len(set(secs)):
            raise ValueError(f"{sid}: duplicate second in keyframes")
        if not row["anchor_label"].strip():
            raise ValueError(f"{sid}: anchor_label is required")
        en_fields = {
            "brand_name_en": row.get("brand_name_en", "").strip(),
            "period_en": row.get("period_en", "").strip(),
            "instrument_name_en": row.get("instrument_name_en", "").strip(),
            "calendar_start_en": row.get("calendar_start_en", "").strip(),
            "calendar_end_en": row.get("calendar_end_en", "").strip(),
            "anchor_when_en": row.get("anchor_when_en", "").strip(),
            "hint_text_en": row.get("hint_text_en", "").strip(),
            "anchor_label_en": row.get("anchor_label_en", "").strip(),
            "history_comment_en": row.get("history_comment_en", "").strip(),
        }
        for key, val in en_fields.items():
            if not val:
                raise ValueError(f"{sid}: {key} is required")
        instrument_name = row.get("instrument_name", "").strip()
        if not instrument_name:
            raise ValueError(f"{sid}: instrument_name is required")
        calendar_start, calendar_end = normalize_calendar_range(
            row["calendar_start"], row["calendar_end"], sid
        )
        anchor_when = normalize_anchor_when(row["anchor_when"], sid)
        try:
            band_start = int(row["anchor_band_start"])
            band_end = int(row["anchor_band_end"])
        except (KeyError, ValueError) as exc:
            raise ValueError(f"{sid}: anchor_band_start/end must be integers") from exc
        if not (0 <= band_start <= band_end <= POINT_COUNT - 1):
            raise ValueError(
                f"{sid}: anchor_band must satisfy 0 <= start <= end <= {POINT_COUNT - 1}, "
                f"got {band_start}..{band_end}"
            )
        market_kind = normalize_market_kind(row["market_kind"])
        stages.append(
            StageDef(
                stage_id=sid,
                brand_name=row["brand_name"].strip(),
                brand_name_en=en_fields["brand_name_en"],
                period=row["period"].strip(),
                period_en=en_fields["period_en"],
                market_kind=market_kind,
                instrument_name=instrument_name,
                instrument_name_en=en_fields["instrument_name_en"],
                calendar_start=calendar_start,
                calendar_start_en=en_fields["calendar_start_en"],
                calendar_end=calendar_end,
                calendar_end_en=en_fields["calendar_end_en"],
                anchor_when=anchor_when,
                anchor_when_en=en_fields["anchor_when_en"],
                anchor_band_start=band_start,
                anchor_band_end=band_end,
                scenario_type=stype,
                macro_shape=macro_shape,
                pivot_timing=pivot_timing,
                trick=trick,
                hint_text=row["hint_text"].strip(),
                hint_text_en=en_fields["hint_text_en"],
                anchor_label=row["anchor_label"].strip(),
                anchor_label_en=en_fields["anchor_label_en"],
                history_comment=row["history_comment"].strip(),
                history_comment_en=en_fields["history_comment_en"],
                seed=int(row["seed"]),
                tier=tier,
                keyframes=kfs,
            )
        )

    orphan = set(kf_by_stage) - seen
    if orphan:
        raise ValueError(f"keyframes.csv has unknown stage_id(s): {sorted(orphan)}")

    standard = [s for s in stages if s.tier == "STANDARD"]
    extreme = [s for s in stages if s.tier == "EXTREME"]
    if len(standard) != 30:
        raise ValueError(f"expected 30 standard stages, got {len(standard)}")
    if len(extreme) != 15:
        raise ValueError(f"expected 15 extreme stages, got {len(extreme)}")

    if not stages:
        raise ValueError("No stages loaded from content/")
    return stages


def lerp(a: float, b: float, t: float) -> float:
    return a + (b - a) * t


def interpolate_keyframes(keyframes: list[tuple[int, float]]) -> list[float]:
    """案B: smoothstep をやめ、線形補間のみ（折返し直前の減速サインを弱める）。"""
    kf = sorted(keyframes, key=lambda x: x[0])
    out: list[float] = []
    for sec in range(POINT_COUNT):
        for i in range(len(kf) - 1):
            s0, p0 = kf[i]
            s1, p1 = kf[i + 1]
            if s0 <= sec <= s1:
                t = 0.0 if s1 == s0 else (sec - s0) / (s1 - s0)
                out.append(lerp(p0, p1, t))
                break
    return out


def inflection_window_seconds(keyframes: list[tuple[int, float]], radius: int = 2) -> set[int]:
    """キーフレーム折返し付近（フェイク底/天井が起きやすい帯）。"""
    kf = sorted(keyframes, key=lambda x: x[0])
    windows: set[int] = set()
    for i in range(1, len(kf) - 1):
        _, p0 = kf[i - 1]
        s1, p1 = kf[i]
        _, p2 = kf[i + 1]
        if (p1 - p0) * (p2 - p1) < 0:
            for s in range(max(0, s1 - radius), min(POINT_COUNT, s1 + radius + 1)):
                windows.add(s)
    return windows


def sharp_move_seconds(keyframes: list[tuple[int, float]], threshold: float = 7.0) -> set[int]:
    """1秒変化が大きい秒（punch / cliff）— ノイズで潰さない。"""
    kf = sorted(keyframes, key=lambda x: x[0])
    sharp: set[int] = set()
    for i in range(1, len(kf)):
        s0, p0 = kf[i - 1]
        s1, p1 = kf[i]
        if s1 == s0:
            continue
        per_sec = abs(p1 - p0) / (s1 - s0)
        if per_sec >= threshold or abs(p1 - p0) >= threshold:
            sharp.add(s1)
            if s1 > 0:
                sharp.add(s1 - 1)
    return sharp


def add_microstructure(
    series: list[float],
    seed: int,
    intensity: float,
    trap_seconds: set[int],
    sharp_seconds: set[int],
) -> list[float]:
    """生成パイプラインのベースノイズ。設計ミクロの上に薄く載せる。"""
    del sharp_seconds
    rng = random.Random(seed)
    out = [series[0]]
    for i in range(1, len(series)):
        local_intensity = intensity * (1.35 if i in trap_seconds else 1.0)
        noise = rng.uniform(-local_intensity, local_intensity)
        spike_chance = 0.14 if i in trap_seconds else 0.08
        if rng.random() < spike_chance:
            noise += rng.choice([-1, 1]) * local_intensity * rng.uniform(1.6, 3.2)
        out.append(series[i] + noise)
    scale = BASE_RATIO / out[0]
    return [round(p * scale, 2) for p in out]


def ratio_at(ratios: list[float], absolute_seconds: float) -> float:
    if absolute_seconds <= 0:
        return ratios[0]
    last = len(ratios) - 1
    if absolute_seconds >= last:
        return ratios[-1]
    i0 = int(absolute_seconds)
    frac = absolute_seconds - i0
    return lerp(ratios[i0], ratios[i0 + 1], frac)


def display_price(ratio: float) -> float:
    return max(1.0, ratio / BASE_RATIO * INITIAL_DISPLAY)


def theoretical_max(ratios: list[float], offset: int, duration: int = 60) -> float:
    step = 0.1
    count = int(duration / step) + 1
    prices = [display_price(ratio_at(ratios, offset + i * step)) for i in range(count)]
    cash = float(INITIAL_ASSET)
    shares = 0.0
    holding = False
    for i in range(len(prices) - 1):
        now, nxt = prices[i], prices[i + 1]
        if not holding and nxt > now:
            shares = cash / now
            cash = 0.0
            holding = True
        elif holding and nxt < now:
            cash = shares * now
            shares = 0.0
            holding = False
    if holding:
        cash = shares * prices[-1]
    return cash


def avg_theoretical(ratios: list[float]) -> float:
    return sum(theoretical_max(ratios, off) for off in range(0, 11)) / 11


def scale_around_base(ratios: list[float], scale: float) -> list[float]:
    return [round(BASE_RATIO + (r - BASE_RATIO) * scale, 2) for r in ratios]


def clamp_series(ratios: list[float], min_ratio: float = 18.0) -> list[float]:
    clamped = [max(min_ratio, r) for r in ratios]
    scale0 = BASE_RATIO / clamped[0]
    return [round(p * scale0, 2) for p in clamped]


def balance_series(ratios: list[float]) -> list[float]:
    lo, hi = 0.35, 2.8
    best = ratios
    best_diff = float("inf")
    for _ in range(28):
        mid = (lo + hi) / 2
        candidate = clamp_series(scale_around_base(ratios, mid))
        avg = avg_theoretical(candidate)
        diff = abs(avg - TARGET_THEO_MID)
        if diff < best_diff:
            best_diff = diff
            best = candidate
        if avg < TARGET_THEO_MID:
            lo = mid
        else:
            hi = mid
    avg = avg_theoretical(best)
    if avg < TARGET_THEO_MIN:
        best = clamp_series(scale_around_base(ratios, hi))
    elif avg > TARGET_THEO_MAX:
        best = clamp_series(scale_around_base(ratios, lo))
    return clamp_series(best)


def intensity_for(stype: str, tier: str) -> float:
    """ベースノイズは控えめ（ミクロの主役はアプリ側 SessionMicroOverlay）。"""
    base = 0.40
    if stype == "WHIPSAW":
        base = 0.45
    elif stype == "RUNAWAY":
        base = 0.42
    if tier == "EXTREME":
        base *= 1.15
    return base


def finalize_series(ratios: list[float], tier: str) -> list[float]:
    if tier == "EXTREME":
        return clamp_series(ratios)
    return balance_series(ratios)


def main() -> None:
    stages = load_stages_from_content()
    rows: list[list[object]] = []
    report: list[str] = []

    for stage in stages:
        base = interpolate_keyframes(stage.keyframes)
        trap_seconds = inflection_window_seconds(stage.keyframes)
        sharp_seconds = sharp_move_seconds(stage.keyframes)
        noisy = add_microstructure(
            base,
            stage.seed,
            intensity_for(stage.scenario_type, stage.tier),
            trap_seconds,
            sharp_seconds,
        )
        balanced = finalize_series(noisy, stage.tier)
        balanced = clamp_series(balanced)
        avg = avg_theoretical(balanced)
        start = balanced[0]
        mn, mx = min(balanced), max(balanced)
        range_pct = (mx / mn - 1.0) * 100.0 if mn > 0 else 0.0
        tier_tag = " EXT" if stage.tier == "EXTREME" else ""
        report.append(
            f"{stage.stage_id}{tier_tag} {stage.macro_shape} {stage.scenario_type} avgTheo={avg:.0f} "
            f"traps={len(trap_seconds)} min={mn:.1f} max={mx:.1f} range={range_pct:.0f}%"
        )
        for sec, p in enumerate(balanced):
            rows.append(
                [
                    stage.stage_id,
                    stage.brand_name,
                    stage.brand_name_en,
                    stage.period,
                    stage.period_en,
                    stage.market_kind,
                    stage.instrument_name,
                    stage.instrument_name_en,
                    stage.calendar_start,
                    stage.calendar_start_en,
                    stage.calendar_end,
                    stage.calendar_end_en,
                    stage.anchor_when,
                    stage.anchor_when_en,
                    stage.anchor_band_start,
                    stage.anchor_band_end,
                    stage.scenario_type,
                    stage.hint_text,
                    stage.hint_text_en,
                    stage.history_comment,
                    stage.history_comment_en,
                    stage.anchor_label,
                    stage.anchor_label_en,
                    stage.pivot_timing,
                    stage.macro_shape,
                    sec,
                    f"{p:.2f}",
                ]
            )

    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    with OUT_PATH.open("w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, quoting=csv.QUOTE_MINIMAL)
        w.writerow(
            [
                "stage_id",
                "brand_name",
                "brand_name_en",
                "period",
                "period_en",
                "market_kind",
                "instrument_name",
                "instrument_name_en",
                "calendar_start",
                "calendar_start_en",
                "calendar_end",
                "calendar_end_en",
                "anchor_when",
                "anchor_when_en",
                "anchor_band_start",
                "anchor_band_end",
                "scenario_type",
                "hint_text",
                "hint_text_en",
                "history_comment",
                "history_comment_en",
                "anchor_label",
                "anchor_label_en",
                "pivot_timing",
                "macro_shape",
                "second",
                "price_ratio",
            ]
        )
        w.writerows(rows)

    print(f"stages {len(stages)} rows {len(rows)}")
    print(f"wrote {OUT_PATH.relative_to(ROOT)}")
    for line in report:
        print(line)
    avgs = [float(x.split("avgTheo=")[1].split()[0]) for x in report]
    print(f"theo band: min={min(avgs):.0f} max={max(avgs):.0f} mean={sum(avgs)/len(avgs):.0f}")


if __name__ == "__main__":
    main()
