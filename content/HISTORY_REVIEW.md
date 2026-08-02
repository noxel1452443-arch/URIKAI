# 史実レビュー手順

45ステージの**タイトル・説明・全体期間・peek特定日**を史実として確認するためのワークフローです。  
**グラフ形状**は抽象チャートのため、このレビュー対象外です（`chart_note` 参照）。

## ファイル

| ファイル | 役割 |
| --- | --- |
| `content/history_review.csv` | **レビュー用シート**（人間 or AI が編集） |
| `content/stages_meta.csv` | 正本（import 後に反映） |
| `content/stages_meta.csv` の `anchor_band_*` | チャート上の転換点目印（日数比例ではない） |

## 使い方

### 1. レビュー表を出力（最新化）

```bat
py -3 tools/export_history_review.py
```

`content/history_review.csv` を Excel / Google スプレッドシート / VS Code で開く。

### 2. 各ステージを確認

| 列 | 確認内容 |
| --- | --- |
| `brand_name` | 事件名・年代が正しいか |
| `calendar_start` / `calendar_end` | 60秒チャートが表す**史実の全体期間** |
| `anchor_when` | peek 用**特定日1つ**（範囲不可） |
| `anchor_label` | その日の出来事を平易語で |
| `history_comment` | 図鑑・リザルトの1行説明 |
| `title_ok` 等 | `Y` / `N` / 空 |
| `review_status` | `pending` → `ok` または `needs_fix` |
| `source_url` | Wikipedia / 日銀 / FRB / 主要報道など |
| `source_note` | 何を根拠にしたか（例: 「日経平均 2020/3底」） |
| `fix_notes` | 修正提案・メモ |

### 3. AI にレビューさせる場合

プロンプト例:

```
content/history_review.csv の45行について、
brand_name / calendar_* / anchor_when / anchor_label / history_comment の史実性を検証してください。
各行に source_url, source_note, title_ok/period_ok/peek_date_ok/comment_ok (Y/N),
review_status (ok/needs_fix), fix_notes を埋めてください。
グラフ形状は対象外。anchor_when は特定日1つのまま。
```

### 4. 修正を正本へ反映

```bat
py -3 tools/import_history_review.py
py -3 tools/generate_charts_csv.py
```

### 5. ビルド確認

```bat
gradlew.bat assembleDebug
```

## 表示の意味（アプリ）

| UI | データ | 意味 |
| --- | --- | --- |
| リザルト1行目 | `brand_name` | 史実タイトル |
| リザルト2行目 | `market_kind` + `calendar_*` | 種類と**全体期間** |
| peek 帯上 | `anchor_when` + `anchor_label` | **特定日** + 出来事名 |
| peek フッター | （固定文） | 帯＝圧縮チャート上の転換点 |

## 自動チェック

`generate_charts_csv.py` 実行時:

- `anchor_when` に `〜` や月のみ表記がないこと
- `anchor_band_*` が 0〜69 の範囲内であること

史実の正しさそのものは**人間レビュー**が必要です。
