"""ブラウザ版をビルドして GitHub Pages 用フォルダ docs/play/ へコピーする。

  py -3 tools/publish_web.py          # ビルド + コピー
  py -3 tools/publish_web.py --skip-build

コピー後に docs/play/ をコミット・プッシュすると
https://<user>.github.io/URIKAI/play/ で遊べる（Pages の公開元が /docs の場合）。
"""
import pathlib
import shutil
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
DIST = ROOT / "shared" / "build" / "dist" / "wasmJs" / "productionExecutable"
OUT = ROOT / "docs" / "play"


def main() -> None:
    if "--skip-build" not in sys.argv:
        gradlew = ROOT / ("gradlew.bat" if sys.platform == "win32" else "gradlew")
        subprocess.run([str(gradlew), ":shared:wasmJsBrowserDistribution"], cwd=ROOT, check=True)
    if not (DIST / "index.html").exists():
        sys.exit(f"build output not found: {DIST}")

    if OUT.exists():
        shutil.rmtree(OUT)
    shutil.copytree(DIST, OUT, ignore=shutil.ignore_patterns("*.map", "*.LICENSE.txt"))
    # Pages の Jekyll 処理で先頭 _ のファイル等が落ちないようにする
    (ROOT / "docs" / ".nojekyll").touch()

    total = sum(p.stat().st_size for p in OUT.rglob("*") if p.is_file())
    print(f"copied to {OUT.relative_to(ROOT)}  ({total / 1024 / 1024:.1f} MB)")


if __name__ == "__main__":
    main()
