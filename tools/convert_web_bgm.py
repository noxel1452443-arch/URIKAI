"""Android 用 BGM (.ogg) をブラウザ版用の .m4a (AAC) に変換する。Safari は Ogg を再生できない場合があるため。

  py -3 tools/convert_web_bgm.py

要 ffmpeg（PATH 上）。出力: shared/src/wasmJsMain/resources/audio/bgm_*.m4a
"""
import pathlib
import shutil
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
SRC = ROOT / "app" / "src" / "main" / "res" / "raw"
OUT = ROOT / "shared" / "src" / "wasmJsMain" / "resources" / "audio"


def main() -> None:
    ffmpeg = shutil.which("ffmpeg")
    if not ffmpeg:
        sys.exit("ffmpeg not found on PATH")
    OUT.mkdir(parents=True, exist_ok=True)
    for ogg in sorted(SRC.glob("bgm_*.ogg")):
        dst = OUT / (ogg.stem + ".m4a")
        subprocess.run(
            [ffmpeg, "-y", "-loglevel", "error", "-i", str(ogg),
             "-c:a", "aac", "-b:a", "64k", "-movflags", "+faststart", str(dst)],
            check=True,
        )
        print(f"{dst.relative_to(ROOT)}  {dst.stat().st_size // 1024} KB")


if __name__ == "__main__":
    main()
