# チャートコンテンツの編集ガイド

アプリが読むのは `shared/src/commonMain/composeResources/files/charts.csv` ですが、**手で編集するのはこの `content/` 配下だけ**にしてください。

## ファイル構成

| ファイル | 役割 | 行数の目安 |
| --- | --- | --- |
| `content/stages_meta.csv` | 架空銘柄・見出し・出来事・ヒント・波のコツ・乱数シード | **1ステージ = 1行** |
| `content/keyframes.csv` | チャート形状の制御点 | 1ステージあたり数〜十数行 |
| `shared/src/commonMain/composeResources/files/charts.csv` | 実行用（70秒に展開済み） | **生成物・直接編集しない** |

## データの流れ

```
content/stages_meta.csv  ─┐
                          ├─► py -3 tools/generate_charts_csv.py ─► files/charts.csv ─► アプリ
content/keyframes.csv    ─┘
```

## 表示名やコメントだけ変えたい

1. Excel / VS Code で `content/stages_meta.csv` を開く
2. `brand_name` / `instrument_name` / `anchor_when` / `flavor_text` / `history_comment` を編集
3. ターミナルで:

```bat
py -3 tools/generate_charts_csv.py
```

4. アプリを再ビルド / 再実行

## チャートの形を変えたい

### 一括再生成（推奨）

```bat
py -3 tools/rewrite_keyframes.py
py -3 tools/generate_charts_csv.py
```

`rewrite_keyframes.py` は **マクロ + 設計ミクロ + CLIFF** を keyframes に書き出します。

### 手編集

1. `content/keyframes.csv` で対象 `stage_id` の行を編集
2. 必ず **second=0** と **second=69** の点を含める（開始100付近・終了）
3. `price_ratio` は開始時を 100 前後とした相対値
4. `py -3 tools/generate_charts_csv.py` を実行

### 形状設計の3層

| 層 | 編集場所 | 内容 |
| --- | --- | --- |
| **マクロ** | keyframes | 大きな下降・上昇・WHIPSAW 全体 |
| **設計ミクロ** | keyframes（densify） | 脚内の小さな往復・底フェイク |
| **CLIFF** | keyframes（`CLIFFS` in rewrite script） | **1秒で大暴落/大高騰** — 全30本中 ~10本のみ |
| **セッション・ミクロ** | アプリ `SessionMicroOverlay` | プレイごと RNG（脚スタイル・周期1〜4秒） |

**CLIFF の書き方（例）**

```
second 34 → 108
second 35 → 92   ← 1秒で -16（大暴落）
```

`tools/rewrite_keyframes.py` の `CLIFFS` 辞書に `stage_id: (秒, 差分)` で追加。  
頻繁に入れないこと（ジェットコースター感は「たまに」が効く）。

生成時にノイズ付与とシンクロ平準化（理論最大の平均を揃える）が自動で入ります。  
**ミクロ swing のプレイごとのランダム化**はアプリ側 `SessionMicroOverlay` が `startGame()` 時にマクロ列へ重ねます（図鑑・CSV の形はマクロ基準）。

## ステージを追加したい

1. `stages_meta.csv` に1行追加（`stage_id` はユニーク、例: `v11`）
2. `scenario_type` は内部タグ `DROP_REBOUND` / `WHIPSAW` / `RUNAWAY` のいずれか（画面には表示しない）
3. `seed` は整数（ノイズ再現用。ステージごとに違う値を推奨）
4. `keyframes.csv` に同じ `stage_id` の制御点を追加（0 と 69 必須）
5. 生成スクリプトを実行

## ステージを削除したい

1. 両CSVから該当 `stage_id` の行を消す
2. 生成スクリプトを実行

## 列の意味（stages_meta.csv）

| 列 | 説明 |
| --- | --- |
| `stage_id` | 内部ID（図鑑の解放キーにも使用） |
| `brand_name` | クリア後に出る見出し（架空ニュース） |
| `instrument_name` | 架空の銘柄名（同じ銘柄を複数ステージで使い回してよい） |
| `base_price` | 開始時点の株価（円、100〜99,999の整数）。ゲーム中の株価表示に使う（売買の計算には影響しない）。同じ銘柄は同じ値にする（違うと生成時エラー） |
| `period` / `calendar_start` / `calendar_end` | ジャンル（例: 食品）。3列とも同じ値にする。リザルト・図鑑で「銘柄名（ジャンル）」と表示 |
| `anchor_when` | 転換点の架空の出来事（14文字以内。実在の日付は不可）。リザルトのチャートと図鑑詳細に表示。タイトルなしでも分かるよう「誰が・何をした」を1文で書く。良い知らせは上昇の起点、悪い知らせは下落の起点に置く |
| `anchor_label` | 値動きの説明（現在は画面に表示していない） |
| `anchor_band_start` / `anchor_band_end` | 出来事の秒（charts.csv 絶対秒）。実際の転換点（底・天井・急変の始まり）に合わせ、通常は同じ値にする |
| `market_kind` | 内部タグ（UI非表示）。通常は `STOCK`、コインは `CRYPTO`、通貨は `FX` |
| `scenario_type` | 内部タグ: `DROP_REBOUND` / `WHIPSAW` / `RUNAWAY`（ノイズ強度用。UI非表示） |
| `macro_shape` | マクロ形状タグ（keyframes 生成用。UI非表示） |
| `pivot_timing` | 転換タイミング: `EARLY` / `MID` / `LATE` |
| `trick` | だまし（任意）: `BEAR_TRAP` / `BULL_TRAP` / `DEAD_CAT` / `SECOND_WAVE` または空 |
| `flavor_text` | 図鑑詳細の出来事の掘り下げ（2〜3文）。イラストは `app/src/main/res/drawable/stage_<stage_id>.png` に置くと図鑑に表示（無ければシルエット） |
| `history_comment` | 物語の締めのひとこと（現在は画面に表示していない予備テキスト） |
| `seed` | 微細ノイズの乱数シード |

## 注意

- CSVは **UTF-8（Excelなら UTF-8 BOM 付きで保存可）**
- コメント内にカンマがある場合は `"..."` で囲む
- `stage_id` を変えると、既存ユーザーの図鑑解放状態はそのIDでは引き継がれない
- 生成スクリプトは内容を検証し、エラー時は `files/charts.csv` を更新しない

## macro_shape タグ一覧（30本配分用）

| タグ | 形 | 目安本数 |
| --- | --- | --- |
| `V_RECOVER` | 下落→回復 | 3 |
| `W_DOUBLE` | 二番底 | 2 |
| `EARLY_SHOCK` | 序盤ショック→止血 | 3 |
| `LATE_SPIKE` | 後半急変 | 1 |
| `RANGE` | 横ばい往復 | 4 |
| `N_RALLY` | 上→下→上 | 2 |
| `INV_N` | 下→上→下 | 2 |
| `BUBBLE` | 過熱→崩落 | 3 |
| `INVERSE_V` | 上昇→失速 | 2 |
| `GRIND_UP` | 漸進上昇→調整 | 2 |
| `GRIND_DOWN` | 漸進下落 | 2 |

`scenario_type` は `macro_shape` と対応関係が決まっている（不一致は生成時エラー）。