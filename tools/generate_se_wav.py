"""Generate short punchy SE wav files for URIKAI (Game Boy–like light/dark contrast)."""
from __future__ import annotations

import math
import struct
import wave
from pathlib import Path

OUT = Path(r"C:\Users\reo\Documents\URIKAI\app\src\main\res\raw")
SAMPLE_RATE = 44100


def write_wav(path: Path, samples: list[float]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "w") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SAMPLE_RATE)
        frames = bytearray()
        for s in samples:
            v = max(-1.0, min(1.0, s))
            frames += struct.pack("<h", int(v * 32767))
        w.writeframes(frames)


def envelope(t: float, attack: float, decay: float) -> float:
    if t < attack:
        return t / attack if attack > 0 else 1.0
    return max(0.0, 1.0 - (t - attack) / decay) if decay > 0 else 0.0


def tone(freq: float, t: float) -> float:
    return math.sin(2 * math.pi * freq * t)


def square(freq: float, t: float) -> float:
    return 1.0 if math.sin(2 * math.pi * freq * t) >= 0 else -1.0


def noise(i: int) -> float:
    # cheap deterministic noise
    x = math.sin(i * 12.9898) * 43758.5453
    return (x - math.floor(x)) * 2.0 - 1.0


def gen_buy() -> list[float]:
    """Bright rising blip (buy / lag OK+)."""
    dur = 0.16
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.004, 0.14)
        f = 520 + 1600 * (t / dur)
        s = 0.55 * square(f, t) + 0.25 * tone(f * 2, t)
        out.append(s * env * 0.85)
    return out


def gen_sell() -> list[float]:
    """Base sell thump (overridden by quality SEs in player)."""
    dur = 0.18
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.003, 0.16)
        s = 0.55 * tone(180 * (1 - 0.4 * t / dur), t)
        if t < 0.03:
            s += 0.4 * square(1400, t) * (1 - t / 0.03)
        out.append(s * env)
    return out


def gen_miss() -> list[float]:
    """ドゥン↓ — dull falling miss."""
    dur = 0.28
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.008, 0.26)
        f = 140 * (1.0 - 0.55 * t / dur)
        s = 0.7 * tone(f, t) + 0.25 * tone(f * 0.5, t)
        s += 0.12 * noise(i) * max(0.0, 1.0 - t / 0.08)
        out.append(s * env)
    return out


def gen_ok() -> list[float]:
    """Neutral short confirm."""
    dur = 0.10
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.003, 0.09)
        s = 0.5 * square(660, t) + 0.2 * tone(990, t)
        out.append(s * env)
    return out


def gen_great() -> list[float]:
    """Happy two-note up."""
    dur = 0.22
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    notes = [(0.00, 523.25), (0.08, 783.99)]
    for i in range(n):
        t = i / SAMPLE_RATE
        s = 0.0
        for start, freq in notes:
            local = t - start
            if 0 <= local <= 0.12:
                env = envelope(local, 0.004, 0.10)
                s += 0.45 * square(freq, local) * env
                s += 0.2 * tone(freq * 2, local) * env
        out.append(max(-1.0, min(1.0, s)))
    return out


def gen_perfect() -> list[float]:
    """Bright arpeggio up."""
    dur = 0.30
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    notes = [(0.00, 523.25), (0.07, 659.25), (0.14, 987.77)]
    for i in range(n):
        t = i / SAMPLE_RATE
        s = 0.0
        for start, freq in notes:
            local = t - start
            if 0 <= local <= 0.14:
                env = envelope(local, 0.003, 0.12)
                s += 0.42 * square(freq, local) * env
                s += 0.22 * tone(freq * 2, local) * env
        out.append(max(-1.0, min(1.0, s)))
    return out


def gen_fantastic() -> list[float]:
    """Big joyful fanfare."""
    dur = 0.42
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    notes = [(0.00, 392.0), (0.08, 523.25), (0.16, 659.25), (0.24, 880.0), (0.30, 1174.7)]
    for i in range(n):
        t = i / SAMPLE_RATE
        s = 0.0
        for start, freq in notes:
            local = t - start
            if 0 <= local <= 0.14:
                env = envelope(local, 0.003, 0.12)
                s += 0.38 * square(freq, local) * env
                s += 0.22 * tone(freq * 2, local) * env
        if 0.28 <= t <= 0.40:
            local = t - 0.28
            env = envelope(local, 0.002, 0.10)
            s += 0.2 * noise(i) * env * 0.35
        out.append(max(-1.0, min(1.0, s)))
    return out


def gen_judge_great() -> list[float]:
    """Enhanced GREAT — sparkle layer."""
    base = gen_great()
    out: list[float] = []
    for i, b in enumerate(base):
        t = i / SAMPLE_RATE
        spark = 0.22 * tone(1760 + 900 * t, t) * envelope(t, 0.002, 0.18)
        out.append(max(-1.0, min(1.0, b * 0.85 + spark)))
    return out


def gen_judge_perfect() -> list[float]:
    base = gen_perfect()
    out: list[float] = []
    for i, b in enumerate(base):
        t = i / SAMPLE_RATE
        spark = 0.28 * square(2093, t) * envelope(t, 0.002, 0.22) * (0.5 + 0.5 * math.sin(40 * t))
        out.append(max(-1.0, min(1.0, b * 0.8 + spark)))
    return out


def gen_judge_fantastic() -> list[float]:
    base = gen_fantastic()
    # extend with shimmer tail
    extra = int(SAMPLE_RATE * 0.12)
    out: list[float] = []
    for i, b in enumerate(base):
        t = i / SAMPLE_RATE
        shimmer = 0.18 * tone(2349 + 400 * math.sin(30 * t), t) * envelope(t, 0.01, 0.35)
        out.append(max(-1.0, min(1.0, b * 0.78 + shimmer)))
    for j in range(extra):
        t = j / SAMPLE_RATE
        env = envelope(t, 0.002, 0.10)
        out.append(0.2 * tone(2637, t) * env)
    return out


def gen_tick() -> list[float]:
    dur = 0.035
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.001, 0.032)
        s = 0.55 * square(1400, t) + 0.2 * tone(2800, t)
        out.append(s * env)
    return out


def gen_charge_i() -> list[float]:
    """Rising pitch ladder — low → high."""
    dur = 0.28
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.01, 0.24)
        # 5 stepped pitches
        step = min(4, int(t / dur * 5))
        f = 220 * (1.35 ** step)
        s = 0.5 * square(f, t) + 0.2 * tone(f * 2, t)
        out.append(s * env)
    return out


def gen_charge_ii() -> list[float]:
    """Heavy pocket-watch chime (luxury)."""
    dur = 0.55
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    # deep tick + resonant bell
    for i in range(n):
        t = i / SAMPLE_RATE
        s = 0.0
        if t < 0.06:
            env = envelope(t, 0.002, 0.055)
            s += 0.55 * tone(90, t) * env
            s += 0.25 * noise(i) * env
        for start, freq, amp in [(0.05, 440.0, 0.4), (0.12, 554.37, 0.35), (0.22, 659.25, 0.45)]:
            local = t - start
            if 0 <= local <= 0.32:
                env = envelope(local, 0.008, 0.28)
                # inharmonic partials like a clock bell
                s += amp * tone(freq, local) * env
                s += amp * 0.35 * tone(freq * 2.01, local) * env
                s += amp * 0.18 * tone(freq * 3.05, local) * env
        out.append(max(-1.0, min(1.0, s)))
    return out


def gen_payout_count() -> list[float]:
    """Short とぅる — counting blip."""
    dur = 0.055
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.002, 0.048)
        s = 0.5 * square(880, t) + 0.2 * tone(1760, t)
        out.append(s * env)
    return out


def gen_payout_clang() -> list[float]:
    """チャキーン! cash register."""
    dur = 0.32
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        s = 0.0
        if t < 0.04:
            s += 0.55 * noise(i) * (1 - t / 0.04)
        for start, freq in [(0.02, 1200.0), (0.05, 1800.0), (0.08, 2400.0)]:
            local = t - start
            if 0 <= local <= 0.20:
                env = envelope(local, 0.002, 0.18)
                s += 0.35 * tone(freq, local) * env
                s += 0.15 * square(freq * 0.5, local) * env
        # metallic ring
        if t > 0.06:
            local = t - 0.06
            env = max(0.0, 1.0 - local / 0.24)
            s += 0.2 * tone(3100, t) * env * math.sin(55 * local)
        out.append(max(-1.0, min(1.0, s)))
    return out


def gen_type() -> list[float]:
    """Typewriter key."""
    dur = 0.04
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.001, 0.035)
        s = 0.45 * noise(i) + 0.25 * square(2200, t)
        out.append(s * env)
    return out


def gen_star() -> list[float]:
    """Twinkle for ★."""
    dur = 0.14
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.003, 0.12)
        f = 1200 + 1800 * (t / dur)
        s = 0.45 * tone(f, t) + 0.25 * tone(f * 1.5, t)
        out.append(s * env)
    return out


def gen_stamp() -> list[float]:
    """BEST stamp thump + click."""
    dur = 0.18
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.002, 0.16)
        s = 0.55 * tone(110 * (1 - 0.3 * t / dur), t)
        if t < 0.025:
            s += 0.5 * noise(i) * (1 - t / 0.025)
        if 0.03 <= t <= 0.08:
            local = t - 0.03
            s += 0.35 * square(900, local) * (1 - local / 0.05)
        out.append(s * env)
    return out


def gen_timeup() -> list[float]:
    """Short double-hit fanfare / low sync."""
    dur = 0.40
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    notes = [(0.00, 392.0), (0.12, 349.23), (0.24, 293.66)]
    for i in range(n):
        t = i / SAMPLE_RATE
        s = 0.0
        for start, freq in notes:
            local = t - start
            if 0 <= local <= 0.14:
                env = envelope(local, 0.006, 0.12)
                s += 0.4 * square(freq, local) * env
        out.append(max(-1.0, min(1.0, s)))
    return out


def gen_finish() -> list[float]:
    """FINISH whistle-ish square sweep."""
    dur = 0.32
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.004, 0.28)
        f = 740 + 520 * (t / dur)
        s = 0.45 * square(f, t) + 0.25 * square(f * 1.01, t)
        out.append(max(-1.0, min(1.0, s * env)))
    return out


def gen_countdown() -> list[float]:
    dur = 0.12
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.003, 0.10)
        s = 0.5 * square(740, t) + 0.25 * tone(1480, t)
        out.append(s * env)
    return out


def gen_panel() -> list[float]:
    """Result panel whoosh-in."""
    dur = 0.12
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.01, 0.10)
        f = 200 + 600 * (t / dur)
        s = 0.4 * tone(f, t) + 0.15 * noise(i) * (1 - t / dur)
        out.append(s * env)
    return out


def gen_ui_select() -> list[float]:
    """KH-like cursor select — soft short blip."""
    dur = 0.07
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    for i in range(n):
        t = i / SAMPLE_RATE
        env = envelope(t, 0.002, 0.055)
        # soft square + gentle high partial
        s = 0.42 * square(740, t) + 0.18 * tone(1480, t)
        out.append(s * env)
    return out


def gen_ui_confirm() -> list[float]:
    """KH-like decide — bright two-step rising confirm."""
    dur = 0.18
    n = int(SAMPLE_RATE * dur)
    out: list[float] = []
    # ピロリン: low then high with sparkle
    notes = [(0.00, 587.33, 0.12), (0.055, 880.0, 0.14)]
    for i in range(n):
        t = i / SAMPLE_RATE
        s = 0.0
        for start, freq, hold in notes:
            local = t - start
            if 0 <= local <= hold:
                env = envelope(local, 0.003, hold - 0.003)
                s += 0.48 * square(freq, local) * env
                s += 0.22 * tone(freq * 2, local) * env
        if 0.05 <= t <= 0.16:
            local = t - 0.05
            env = envelope(local, 0.002, 0.10)
            s += 0.16 * tone(1760 + 600 * local, local) * env
        out.append(max(-1.0, min(1.0, s)))
    return out


def main() -> None:
    files = {
        "se_buy.wav": gen_buy(),
        "se_sell.wav": gen_sell(),
        "se_miss.wav": gen_miss(),
        "se_ok.wav": gen_ok(),
        "se_great.wav": gen_great(),
        "se_perfect.wav": gen_perfect(),
        "se_fantastic.wav": gen_fantastic(),
        "se_judge_great.wav": gen_judge_great(),
        "se_judge_perfect.wav": gen_judge_perfect(),
        "se_judge_fantastic.wav": gen_judge_fantastic(),
        "se_tick.wav": gen_tick(),
        "se_charge_i.wav": gen_charge_i(),
        "se_charge_ii.wav": gen_charge_ii(),
        "se_payout_count.wav": gen_payout_count(),
        "se_payout_clang.wav": gen_payout_clang(),
        "se_type.wav": gen_type(),
        "se_star.wav": gen_star(),
        "se_stamp.wav": gen_stamp(),
        "se_timeup.wav": gen_timeup(),
        "se_finish.wav": gen_finish(),
        "se_countdown.wav": gen_countdown(),
        "se_panel.wav": gen_panel(),
        "se_ui_select.wav": gen_ui_select(),
        "se_ui_confirm.wav": gen_ui_confirm(),
    }
    for name, samples in files.items():
        write_wav(OUT / name, samples)
    print("wrote", len(files), "files to", OUT)


if __name__ == "__main__":
    main()
