"""ブラウザ版のローカル確認用サーバー。

  py -3 tools/serve_web.py            # shared/build/dist/wasmJs/productionExecutable を配信
  py -3 tools/serve_web.py docs/play  # 任意のフォルダを配信
"""
import functools
import http.server
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
DEFAULT_DIR = ROOT / "shared" / "build" / "dist" / "wasmJs" / "productionExecutable"


class Handler(http.server.SimpleHTTPRequestHandler):
    extensions_map = {
        **http.server.SimpleHTTPRequestHandler.extensions_map,
        ".wasm": "application/wasm",
        ".js": "text/javascript",
        ".mjs": "text/javascript",
    }

    def end_headers(self):
        self.send_header("Cache-Control", "no-store")
        super().end_headers()


def main() -> None:
    directory = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_DIR
    port = 8765
    handler = functools.partial(Handler, directory=str(directory.resolve()))
    print(f"serving {directory} on http://localhost:{port}/")
    http.server.ThreadingHTTPServer(("127.0.0.1", port), handler).serve_forever()


if __name__ == "__main__":
    main()
