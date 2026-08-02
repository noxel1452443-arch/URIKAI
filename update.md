# URIKAI 変更履歴（update.md）

当初の設計（`goal.md` / `readme.md`）からの実装・修正を時系列で記録する。  
**以降、コードを変更したら必ず本ファイルへ追記すること。**

---

## 2026-07-20 — プロジェクト開始 / Phase 1（骨組み）

### 目的
1画面・1ボタン・60秒のステートマシンだけを先に完成させる（チャートなし）。

### 追加・作成
- Android / Kotlin / Jetpack Compose プロジェクト一式
  - `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, Gradle Wrapper
  - `app/` モジュール（applicationId: `com.urikai.app`）
- 仕様どおりの状態定義
  - `GameState`（START / PLAYING / RESULT）
  - `GameUiState`（**6変数のみ**。以降も拡張禁止）
- `GameViewModel`
  - START → PLAYING → 60秒で RESULT → RETRY
  - 1巨大ボタン（BUY / SELL / 注文中...）
  - 注文中は 0.5秒無効化（価格変動なしのトグルのみ）
- `GameScreen` / `MainActivity` / 最低限テーマ色
- ドキュメント整形: `goal.md`, `readme.md`（見出し・表・コードブロック整備）

### 意図的に入れなかったもの
- チャート描画、CSV、シンクロ率、図鑑、SE、本格サイバーパンク装飾

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — Phase 2（手触り：CSV / 補間 / チャート）

### 目的
1本のCSVを読み、線形補間で滑らかに伸びるチャートと 0.5秒ラグの手触りを実装する。

### 追加・変更
- `app/src/main/assets/charts.csv`（デモ1ステージ・70秒・V_SHAPE）
- `ChartCsvParser` / `StageChart`
- `PriceMath`（ratio↔表示株価、線形補間）
- `ChartFrame`（描画用。`GameUiState` は拡張しない）
- `GameViewModel` を `AndroidViewModel` 化
  - 16ms ティックで価格・資産更新
  - 開始オフセット 0〜10秒ランダム
  - 押した瞬間の価格で約定、0.5秒後に保有トグル
  - 全力BUY/SELLで `cash` / `shares` を内部管理
- `PriceChart`（過去20秒窓、Yズーム0.2秒、現在点）
- `GameScreen` にチャート領域を組み込み

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — Phase 2 手触り修正（優先度順）

ユーザー指摘およびレビューで挙がった問題を、優先度順に修正。

### 1. グラフ見切れ対策
**問題:** 急騰・急落時に線・現在点が上下にはみ出す。Yズームアニメ中は古いレンジのまま描画されていた。

**対応 (`PriceChart.kt`, `GameViewModel` 定数):**
- 上下パディングを強化（比率約22%、かつ最小パディング）
- Canvas 上下に描画マージン（6%）を追加
- **レンジ拡大は `snapTo` で即時追従**、縮小のみ 0.2秒アニメ（見切れ優先）
- 売買マーカーの価格も Y レンジ計算に含める
- `clipRect` による現在点クリップをやめた

### 2. 現在点の位置・見た目
**問題:** 現在値が右端すぎて視線が端に張り付く。点も小さく主役になっていない。

**対応:**
- 現在Xを右端ではなく **幅の約78%**（`NOW_X_FRACTION`）に配置し、右側に「未来の空白」を確保
- 縦ガイド線＋大きめのグロー円＋白芯で視線のアンカー化
- 保有状態で色分け（現金時=BUY赤、保有時=SELL青）

### 3. 売買価格の表示（案A）
**問題:** いくらで買った／売ったが画面に残らない。

**対応:**
- `TradeMarker` 追加（`ChartFrame.markers` で保持。`GameUiState` は未拡張）
- 注文中: 半透明の ▲BUY / ▼SELL（pending）
- 約定後: 確定マーカー＋直近1件を約1.5秒 `BUY ¥xxx` / `SELL ¥xxx` ラベル表示
- 窓外に流れたマーカーは描画スキップ

### 4. サンプルCSVの実相場寄り化
**問題:** 滑らかすぎて実株のイメージと違う。

**対応 (`charts.csv` 再生成):**
- パニック売り・デッドキャット・踏み上げ・押し目を混ぜたノイズ付き V 字
- 1秒ごとの微細な上下を残し、数学カーブ感を低減
- シナリオタイプは従来どおり `V_SHAPE`（デモ1本）

### その他のついで修正
- 開始3秒ヒントをチャート上から **HUD下** へ移動（初動値動きを隠さない）
- 売却時の資産計算を `roundToInt` に変更（切り捨て誤差の緩和）

### 検証
- `assembleDebug` 成功（本修正反映後）

---

## 2026-07-20 — Phase 3（ゲーム性：30銘柄 / 大引け / シンクロ率）

### 目的
ゲームとして遊べる状態にする。評価軸を金額からシンクロ率へ移す。

### 追加・変更
- `charts.csv` を **30ステージ**に拡張（V_SHAPE / ZIGZAG / BUBBLE 各10）
  - 生成スクリプト: `tools/generate_charts_csv.py`
  - 値動きは歴史的事件名を冠した **合成 ratio**（実OHLCそのものではない）
- `SyncCalculator`
  - ラグなし・先読み可の全力ロングで理論最大資産を算出
  - `syncPercent = player / theoretical * 100`
- `ResultInfo`（`ChartFrame.resultInfo` で保持。`GameUiState` は未拡張）
- `GameViewModel`
  - 開始時に30本からランダム選出＋オフセット0〜10
  - 60秒終了時、保有株を終値で **強制全売却（大引け）**
  - リザルトで銘柄正体・期間・SYNC%・資産/理論最大を確定
- `GameScreen`
  - RESULT時に同一画面下部へオーバーレイ表示（新画面・新ボタンなし）
- versionName: `0.3.0-phase3`

### 意図的にやらなかったこと（Phase 4）
- 歴史スクラップ図鑑（永続ベスト記録UI）
- SE / 本格サイバーパンク装飾
- SNSシェア・ネタバレ防止画像
- 称号ランクの作り込み

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — Phase 4（装飾・完成：ビジュアル / SE / 図鑑 / シェア）

### 目的
製品として一通り遊べる完成形にする（ロードマップ最終Phase）。

### 追加・変更
- **ビジュアル**
  - `NeonBackdrop`：ピッチブラック＋シアン/マゼンタ格子と斜線
  - チャート線にネオングロー、枠線
  - 巨大ボタンにネオン枠、RESULT枠をマゼンタ縁取り
- **SE**（`GameSoundPlayer` / ToneGenerator）
  - BUY / SELL 約定時
  - TIME UP 時
  - 大引け強制売却ではSEなし（終了SEのみ）
- **歴史スクラップ図鑑**（1画面）
  - `AppLayer.SCRAPBOOK`（`GameUiState` / `GameState` は未拡張）
  - START / RESULT から `SCRAP` で開く。巨大ボタンは `BACK`
  - 未解放は `????`、クリア後に銘柄名・期間・BEST SYNC% をカラー表示
  - `ScrapbookRepository`（SharedPreferences）で永続化
- **SNSシェア**
  - RESULT の `SHARE` → テキストのみ共有（チャート形状なし）
  - 銘柄・期間・SYNC%・称号・資産・`#URIKAI`
  - 称号: ORACLE / SNIPER / TRADER / NOISE
- versionName: `1.0.0-phase4`

### 意図的にやらなかったこと
- OGP画像生成（テキストシェアのみ）
- カスタムWAVアセット（端末 Tone で代替）
- 図鑑以外の追加画面・育成要素

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — 背景簡素化 / SE差し替え

### 問題
- 全画面の格子・斜線がチャート領域とずれ、ノイズになっていた
- `ToneGenerator` 依存のSEが Android Studio エミュで無音だった

### 対応
- `NeonBackdrop`：格子・斜線を削除。ピッチブラック＋ごく薄い上下グラデのみ
- SEを **WAV + SoundPool** に変更
  - `res/raw/se_buy.wav`（上昇コイン系）
  - `res/raw/se_sell.wav`（決済ヒット系）
  - `res/raw/se_timeup.wav`（短いファンファーレ）
  - 生成脚本: `tools/generate_se_wav.py`
- `GameSoundPlayer` を ToneGenerator から SoundPool 実装へ置換

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — Step A（本番化：史実データ・図鑑コメント・シンクロ平準化）

### 目的
MVPの合成CSVを、史実イベントの「形状・値幅」を再現した30本に差し替え、クリア後の正体明かしを刺さる体験にする。

### 対応
- **CSVスキーマ拡張**
  - `history_comment` 列を追加（史実1行コメント）
  - `ChartCsvParser` を引用符付きCSV対応に更新
- **30本チャート再生成**（`tools/generate_charts_csv.py`）
  - V_SHAPE / ZIGZAG / BUBBLE 各10本
  - 各ステージに史実イベント名・期間・1行コメント
  - キーフレーム＋微細ノイズで「実相場っぽい」形状を再現
  - **シンクロ平準化**: オフセット0〜10の平均理論最大を約18,500円帯に調整
  - 注: 再配布可能な特徴再現データ（商用OHLCの再配布ではない）
- **UI**
  - RESULTオーバーレイに `historyComment` を表示
  - 図鑑（解放後）に史実コメント1行を表示
- `StageChart` / `ScrapbookEntry` / `ResultInfo` に `historyComment` 追加
- versionName: `1.1.0-stepA`

### 意図的にやらなかったこと
- 外部APIからの実OHLC自動取得（ライセンス・パイプラインは別タスク）
- 図鑑のコンプリート率UI（Step B候補）
- `GameUiState` の拡張

### 検証
- `assembleDebug` 成功
- 生成時理論最大: 全30本平均 ≈18,373円（目標帯 15,000〜22,000）

---

## 2026-07-20 — コンテンツ編集の正本を content/ に分離

### 問題
チャート名・史実コメント・形状が `tools/generate_charts_csv.py` 内の巨大タプルに埋もれており、編集・追加が困難だった。実行用 `assets/charts.csv` は70秒×N行で、テキスト変更だけでも全行更新が必要。

### 対応
- **正本を分離**
  - `content/stages_meta.csv` … 表示名・期間・ヒント・史実コメント・seed（1ステージ1行）
  - `content/keyframes.csv` … 形状制御点のみ
- `tools/generate_charts_csv.py` は content を読み、検証→展開→平準化→`assets/charts.csv` を生成
- 編集手順を `content/README.md` に記載
- `readme.md` のデータ仕様を「編集用 / 実行用」に更新
- ステージ数は content の行数に追従（アプリは1本以上あれば起動可）

### 運用
```bat
py -3 tools/generate_charts_csv.py
```

### 検証
- content から再生成成功（30ステージ / 2100行）
- `assembleDebug` 成功

---

## 2026-07-20 — Step B（快感の密度：称号 / ハプティクス / SE階層 / RESULT）

### 目的
新画面を増やさず、音・触覚・称号で「1プレイの手応え」を上げる。

### 対応
- **称号9段階**（`SyncTitle`）: ORACLE〜NOISE。RESULT / シェア / 図鑑BESTで共通
- **RESULT**
  - SYNC% の短いカウントアップ（0.7秒）
  - 称号帯で枠色を変化
  - 一言 verdict（例: 波と同調した / ノイズに飲まれた）
- **ハプティクス**（`HapticPlayer` + `VIBRATE` 権限）
  - BUY / SELL 約定時
  - TIME UP（高シンクロ時は強め）
- **SE階層**: 同一WAVのまま、TIME UP の再生レート/音量をシンクロ帯で変化
- versionName: `1.2.0-stepB`

### 意図的にやらなかったこと
- 新規SEファイル追加、設定画面、ミュートUI
- 称号を10超に増やさない（現在9）

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — UI引き算 + 注文ラグの約定タイミング修正

### 問題 / 相談反映
- RESULT が情報過多で、当初の「正体＋評価」より説明が厚い
- 注文ラグが「見た目の遅延」だけで、約定価格は押下瞬間にロックされていた（実注文の遅延感と不一致）
- 現在点の縦ガイド線がノイズ

### 対応
- **RESULT圧縮**: 正体（名称+時期）/ SYNC% / 称号 / SHARE。史実・verdict・PERFECT・TIME UP 文言を削除（史実は図鑑のみ、PERFECT計算は裏のまま）
- **注文ラグ**: 固定 **0.5秒**。方向は押下時、**約定価格・マーカー位置はラグ後の相場**で確定（買い時押し→約定時に崩れる体験）
  - 注文中マーカーは押下位置に半透明表示、確定マーカーは約定位置
- **チャート**: 現在点の縦線を削除（赤/青の点のみ）

### SHAREについて
- 引き続き `ACTION_SEND` + `text/plain` の**テキストのみ**（チャート画像なし）

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — 注文中タイマー / マーカー見た目

### 対応
- 約定マーカー（▲BUY / ▼SELL）をやや大きく（サイズ 14→20）
- 注文中は赤青を使わず、中立グレーの **円形タイマー**（扇形塗り＋針）を押下位置に表示
- タイマーは固定ラグ（0.5秒）で一周し、約定確定時に消える（確定マーカーへ置換）

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — タイマー大型化 / 評価表示の言い換え（星）

### 対応
- 注文中タイマー半径 12→22（視認性向上）
- RESULTの `SYNC` / 英語称号（NOISE等）を廃止
  - 表示: 「完璧な売買への到達率」+ 大きい％ + **★☆ 5段階**
  - 図鑑BEST・SHARE文面も同じ星表現に統一
- 開始画面のキャッチを「完璧な売買を目指せ」に変更

### 星の目安
- 5: 90%〜 / 4: 75%〜 / 3: 55%〜 / 2: 35%〜 / 1: それ未満

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — ビジュアルテーマ刷新（ローファイ液晶 / ゲームボーイ風）

### 目的
ネオン・黒背景の「AI量産型」サイバーパンクから脱却し、Kindle / 初代ゲームボーイのようなローファイ液晶の手触りへ。レイアウト構造は維持し、色・フォント・質感のみ差し替え。

### カラーパレット（`Theme.kt`）
| 用途 | 色 | 値 |
|------|-----|-----|
| 背景（液晶） | `LcdBackground` | `#C3CBA0` |
| 文字・線・ボタン塗り | `LcdInk` / `Ink` | `#1F241A` |
| 補助（電子ペーパー白） | `LcdPaper` | `#E5E5E5`（将来の反転パネル用に定義） |

旧 `AccentCyan` / `AccentMagenta` / `BuyRed` / `SellBlue` / `ScreenBlack` は廃止。

### フォント
- **DotGothic16**（`res/font/dotgothic16.ttf`）を全UIに適用
- `Type.kt` で `PixelFont` / `LcdTypography` を定義

### 強調表現（色の代わり）
- **二重線**（`lcdDoubleBorder`）: 高評価リザルト（★4以上）、図鑑ハイスコア行
- **白黒反転**（`LcdInvertedPanel`）: リザルトの到達率％
- **ドット網掛け**（`lcdHatch`）: 最高評価リザルト（★5）、注文中ボタン、図鑑ロック行

### 画面別の変更
- `NeonBackdrop` → `LcdBackdrop`: フラット液晶色＋ごく弱い走査線
- `PriceChart`: ネオングロー廃止。ステップ折れ線（水平→垂直）でドット絵風。現在点は■（保有=塗り、現金=枠）。売買マーカーは塗り三角（買）/ 枠三角（売）で色なし区別
- `PrimaryActionButton`: 常に濃色ベタ塗り＋液晶色のくり抜き文字。BUY/SELL/START も同色
- `ResultOverlay` / `ScrapbookPane` / `TopHud`: 単色インク基調に統一
- `themes.xml` / `colors.xml`: ステータスバー・ナビバー・ランチャー色を液晶パレットに

### 触ったファイル
- `ui/theme/Theme.kt`, `ui/theme/Type.kt`（新規）
- `ui/LcdBackdrop.kt`, `ui/LcdModifiers.kt`（新規）
- `ui/GameScreen.kt`, `ui/PriceChart.kt`, `ui/ScrapbookPane.kt`
- `res/font/dotgothic16.ttf`, `res/values/themes.xml`, `res/values/colors.xml`
- 削除: `ui/NeonBackdrop.kt`

### 意図的にやらなかったこと
- レイアウト構造の変更
- `GameUiState` の拡張
- 電子ペーパー白（`#E5E5E5`）背景への切り替えUI（現状はゲームボーイ黄緑を採用）

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — Step C（ラグ1秒 / 間に合わない× / 約定良否SE / 図鑑17/30 / CONFIG）

### 目的
デイリーは廃止。図鑑コンプリート1行＋「ラグと時間」を核にした手触り強化。`GameUiState` は6変数のまま。

### 対応

#### 1. 図鑑 `17 / 30`
- `CLEAR TO UNLOCK` → 解放数 / 全ステージ数の1行に置換

#### 2. 注文ラグ 0.5秒 → **1.0秒**
- `ORDER_LAG_MS = 1_000L`
- 最後1秒の注文は TIME UP で約定しないケースが増える

#### 3. 間に合わない（案F）
- TIME UP 時、未約定の pending 注文を **キャンセル**（`TradeMarker.isCancelled`）
- チャート上の押下位置に **×付き円**（タイマー2倍サイズと同位置）
- HUD・RESULT には文言を足さない

#### 4. 注文タイマー大型化 + 刻み音
- タイマー半径 22→**44**（2倍）
- 1秒ラグ中 **4回** の `se_tick.wav`（250ms間隔）

#### 5. 約定良否（案C縮小版・聴覚のみ）
- 押下時価格 vs 約定価格で `FillQuality`（GOOD / NEUTRAL / BAD）
  - 買い: 約定が安い→GOOD / 売り: 約定が高い→GOOD
- 既存 BUY/SELL SE の **ピッチ・音量** のみ変化（新UIなし）

#### 6. HUD FULL / FOCUS + CONFIG画面
- **CONF** ボタン（SCRAP横）→ `AppLayer.OPTION` の `OptionPane`
- FULL: TIME + 価格 + ASSET（デフォルト）
- FOCUS: TIME + 価格のみ
- `SettingsRepository` + SharedPreferences で永続化
- CONFIG に応答遅延 1.0秒を read-only 表示（ガジェット仕様書風）

### 却下・入れなかったもの
- デイリーチャレンジ
- 約定ズレの数字2行表示（案B）
- RETRY 1語診断（案E）
- 図鑑ノイズ記号（案G）
- 約定良否の視覚マーク（情報過多回避のため音のみ）

### 触ったファイル
- `game/GameViewModel.kt`, `TradeMarker.kt`, `AppLayer.kt`, `HudMode.kt`, `FillQuality.kt`
- `data/SettingsRepository.kt`
- `audio/GameSoundPlayer.kt`, `res/raw/se_tick.wav`
- `ui/GameScreen.kt`, `PriceChart.kt`, `ScrapbookPane.kt`, `OptionPane.kt`（新規）
- `MainActivity.kt`, `tools/generate_se_wav.py`, `app/build.gradle.kts`

### 検証
- `assembleDebug` 成功
- versionName: `1.4.0-stepC`

---

## 2026-07-20 — keyframes.csv 再設計（マクロ + メソ波）

### 問題
- キーフレームが粗く smoothstep 補間で緩やかすぎた
- 下降脚が長いと現金保有で操作不能な区間が発生
- ランダム `add_microstructure` だけでは「構造化された戻り」にならない

### 対応（`tools/rewrite_keyframes.py` → `content/keyframes.csv`）
- **V_SHAPE（10）**: 暴落脚に 3〜4% の小戻りを 3〜4秒間隔で挿入。底・回復脚にも軽い往復
- **ZIGZAG（10）**: 4秒周期の明確な往復（キーフレーム密度最大）
- **BUBBLE（10）**: 上昇脚の押し目、天井のもたつき、崩落脚の戻り売り
- キーフレーム数: 約273点 → **604点**（ステージあたり 17〜24点）
- `py -3 tools/generate_charts_csv.py` で `charts.csv` 再生成済み

### 検証（理論トレード回数・オフセット0〜10平均）
- v01〜v03: 約 8〜13 回/窓（以前より下降中の売買機会が増加）
- b01 崩落帯: 60秒窓内に複数の買い戻り・売り場面を確認
- 理論最大資産バンド: おおむね 18,500 前後（z05 のみやや高め ~20,700、許容帯内）

### 意図的にやらなかったこと
- アプリ側ロジック変更
- 1秒解像度以上のデータ密度（CSVは引き続き70点/ステージ）
- `add_microstructure` の削除（キーフレームで形を作り、ノイズは補助のまま）

### 触ったファイル
- `content/keyframes.csv`, `tools/rewrite_keyframes.py`（新規）
- `app/src/main/assets/charts.csv`（再生成）

---

## 2026-07-20 — 現金利子（単利9%）/ DEVICE画面 / RESULT簡素化

### 目的
下降トレンド中の現金保有に意味を持たせる。シンクロ率は1指標のまま、売買＋利子を理論値側も含めて統合。CONFIGはスコアに影響しない見た目・表示のみ。

### 対応

#### 1. 現金利子（単利・複利なし）
- `CashInterest.kt`: 60秒フル現金で **約9%**（¥10,000 → +¥900）
- 元本ベースの単利。株保有中・注文中は利子なし
- 毎秒 `perSecondAmount(principal)` を加算（最低¥1/秒）
- `SyncCalculator` も同ルールで理論最大を再計算

#### 2. 利子獲得の演出（FULL HUD）
- 現金保有中、毎秒 ASSET 横に `+¥XX` を約0.9秒表示（枠付き）
- `ChartFrame.interestTickAmount` / `interestTickUntilEpochMs`

#### 3. RESULT表示
- 「完璧な売買への到達率」ラベルを削除
- 反転パネルに `XX.X%  ★★★☆☆` を1行で表示
- `ASSET ¥XX,XXX` + `TRADE ¥XX,XXX   INT +¥XXX`（利子>0時）
- `ResultInfo` に `tradeAsset`, `totalInterest` 追加

#### 4. CONFIG → **DEVICE**（見た目のみ）
- **LCD**: GB（黄緑）/ PAPER（白）
- **HUD**: FULL / FOCUS
- **BTN**: EN / ▲▼ / 買売
- 刻み音ON/OFF・触覚ON/OFF・預金利率表示は入れない

#### 5. テーマシステム
- `LcdColors.kt` + `CompositionLocal` で GB/PAPER 動的切替
- `UriKaiTheme(lcdTheme = ...)` で全UIに反映

### 意図的にやらなかったこと
- 銀行預金の段階解放（成長性）は後回し
- 複利
- 刻み音・触覚のCONFIG化
- `GameUiState` のフィールド追加

### 触ったファイル
- `game/CashInterest.kt`（新規）, `SyncCalculator.kt`, `GameViewModel.kt`, `ResultInfo.kt`, `ChartFrame.kt`
- `game/LcdThemeMode.kt`, `ButtonLabelStyle.kt`（新規）
- `data/SettingsRepository.kt`
- `ui/theme/LcdColors.kt`, `Theme.kt`, `OptionPane.kt`, `GameScreen.kt`, `PriceChart.kt`, `LcdModifiers.kt`, `LcdBackdrop.kt`
- `MainActivity.kt`, `ShareTextBuilder.kt`, `app/build.gradle.kts`

### 検証
- `assembleDebug` 成功
- versionName: `1.5.0-interest`

---

## 2026-07-20 — DEVICE拡張（LANG / CHART / SCAN / CONTRAST / DARK）+ 図鑑ソート

### 目的
BTN表記を言語設定（EN/JPN）に統合。見た目系の自由度をDEVICEに集約。図鑑並びは図鑑画面内で切替。

### 対応

#### 1. LANG（旧 BTN）
- `DisplayLanguage`（EN / JPN）に置換。`▲▼` 表記は削除
- `UiStrings` で UI 文言を集中管理（ボタン・HUD・RESULT・DEVICE・図鑑ラベル等）
- **史実テキスト**（銘柄名・期間・ヒント・history_comment）は CSV のまま（現状は日本語）。EN 選択時もそのまま表示 — 英訳列は `content/` 側で後日追加可能

#### 2. DEVICE 新項目
- **CHART**: STEP / SMOOTH
- **SCAN**: OFF / LOW / HIGH（走査線）
- **CONTRAST**: SOFT / NORM / HARD（パネル・補助色のコントラスト）
- **LCD**: GB / PAPER / **DARK**（PAPER 反転ダークモード）

#### 3. 図鑑ソート（図鑑画面内）
- タイトル横に **順 / スコア**（EN: ORDER / SCORE）チップ
- `ScrapbookSort`: 解放順（CSV順） / ベストスコア降順
- 設定は SharedPreferences に永続化

### 触ったファイル
- `game/DisplayLanguage.kt`, `ChartLineStyle.kt`, `ScanlineStrength.kt`, `PanelContrast.kt`, `ScrapbookSort.kt`, `UiStrings.kt`（新規）
- 削除: `ButtonLabelStyle.kt`
- `data/SettingsRepository.kt`, `game/GameViewModel.kt`, `game/LcdThemeMode.kt`
- `ui/theme/LcdColors.kt`, `Theme.kt`, `LcdBackdrop.kt`, `LcdModifiers.kt`
- `ui/OptionPane.kt`, `GameScreen.kt`, `ScrapbookPane.kt`, `PriceChart.kt`, `MainActivity.kt`, `ShareTextBuilder.kt`

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — 利子バグ修正 / リザルト演出 / UI微調整

### 対応

#### 1. 利子計算バグ修正
- **原因**: 株保有中に `lastInterestSecond` が進まず、売却後に「現金でなかった秒」まで一括加算されていた
- **修正**: 株保有・注文中は利子なしのまま `lastInterestSecond` だけ進める

#### 2. リザルト誤押下対策（演出と一体）
- 終了直後 **1.4秒** はボタン `TIME UP`（網掛け・非活性）
- 到達率％カウントアップ（700ms）と同期してから `RETRY` が有効化
- パネルはフェードイン

#### 3. リザルト表示ブラッシュアップ
- チャートを **60秒フル表示** に切替（終盤20秒だけ見える問題を解消）
- 半透明スクラム + **中央配置** のRESULTパネル

#### 4. その他UI
- CONF画面: 「DEVICE」「見た目と…」見出し削除
- 日本語ボタン: **買う / 売る**
- 上部タイトル: PLAY=URIKAI / CONF=CONFIG / SCRAP=SCRAP（英語固定）
- DARKモード: 操作ボタン塗りを `#B8B8B8` に（白すぎ改善）

### 触ったファイル
- `game/GameViewModel.kt`, `UiStrings.kt`
- `ui/GameScreen.kt`, `PriceChart.kt`, `OptionPane.kt`
- `ui/theme/LcdColors.kt`

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — TONE（白の強さ）/ スタート画面 / 図鑑見出し削除

### 対応
- **TONE** 設定追加（CONFIG）: DIM / NORM / BRIGHT
  - PAPER背景・DARKの明るい文字/ボタンに反映。デフォルト NORM（従来よりやや抑えめ）
- 図鑑: 「HISTORY SCRAP」見出し削除。解放数 + ソートチップのみ
- スタート画面: アーケード風パネル
  - EN: `60 SEC.` / `CHART RUN` / `SYNC THE MARKET`
  - JP: `60秒` / `チャート勝負` / `相場に同調せよ`

### 触ったファイル
- `game/SurfaceTone.kt`（新規）, `UiStrings.kt`
- `data/SettingsRepository.kt`, `GameViewModel.kt`
- `ui/theme/LcdColors.kt`, `Theme.kt`
- `ui/OptionPane.kt`, `GameScreen.kt`, `ScrapbookPane.kt`, `MainActivity.kt`

### 検証
- `assembleDebug` 成功

---

## 2026-07-20 — CONFIGコンパクト化（LCDメニュー風）

### 問題
8枚の横長カードが縦に積まれ、下部（CONTRAST等）が見切れる。

### 対応
- 1枚の枠内に **ラベル左・チップ右** の7行メニュー
- チップ小型化、HUD説明文削除
- `LazyColumn` で小画面でもスクロール可能

### 触ったファイル
- `ui/OptionPane.kt`

### 検証
- `assembleDebug` 成功

---

## 2026-07-21 — CONFIG視認性強化・プレビュー・全項目説明

### 対応
- **TONE / SCAN / CONTRAST** の差を実機で分かるよう大幅に拡大
  - TONE: `#A8`〜`#F2`（GBも微妙に連動）
  - SCAN: OFF / LOW(20%) / HIGH(52%・太線・狭間隔)
  - CONTRAST: SOFT↔HARD のアルファ幅を約3倍に
- **HUD / CHART / TONE / SCAN / CONTRAST** カード右にライブプレビュー
- 全7項目に説明文（JP/EN）

### 触ったファイル
- `ui/theme/LcdColors.kt`, `ScanlineStyle.kt`, `LcdBackdrop.kt`
- `ui/ConfigPreviews.kt`（新規）, `OptionPane.kt`, `UiStrings.kt`

### 検証
- `assembleDebug` 成功

---

## 2026-07-21 — 終盤演出・RESULTグラフ確認・TONE簡素化・★閾値調整・WORKSHOP設計

### 対応

#### 1. CONFIG — TONEプレビュー削除
- TONEカードからサンプル画像（`TonePreview`）を外し、チップのみに

#### 2. 終盤演出（TIME 50〜60）
- **TIME 50〜**: 左上タイム表示が毎秒パルス（拡大＋枠線）
- **TIME 55〜59**: 画面中央へ 5→1 のカウントダウン（縮小アニメ）
- **TIME 60**: グラフ停止 → 中央に `FINISH!` → **1秒後** RESULT
- `GameState.FINISHING` を追加（`GameUiState` は未拡張）
- 終盤中は SCRAP / CONFIG 遷移・注文・RETRY をブロック

#### 3. RESULT — グラフ確認タップ
- リザルトカード外（半透明スクリーン部分）をタップ → カード非表示で60秒フルチャートを確認
- もう一度タップでリザルトに復帰

#### 4. ★閾値の見直し（暫定）
- 旧: 90 / 75 / 55 / 35 → 新: **92 / 78 / 62 / 45**
- 大損しないと★3以下になりにくい問題への暫定対応（実プレイ分布を見て再調整予定）

#### 5. WORKSHOP（案A）設計合意 — **未実装・Phase W1 以降**
- **場所**: 図鑑（SCRAP）内タブ「WORKSHOP」
- **モチーフ**: 基板＋ICソケット（3スロット）。CONFIG には載せない
- **初回解放**: 図鑑 **10/30 クリア**（★不問・1プレイ＝1クリア）
- **以降の節目**: 15/20/25/30 クリア、★4以上クリア数（10/15/20/25/30 等）で報酬チップ解放
- **コスト上限**: 省く（チップごとのデメリットでトレードオフ）
- **パラメータ**: 小学生でも分かる表現（例: ラグ短い/長い、利子アップ、チャート速い/遅い）
- **UI**: 最小構成例を超え、ソケット差し込み・LED点灯・チップ名ラベルで「改造感」を出す

### 触ったファイル
- `game/GameState.kt`, `GameViewModel.kt`, `ChartFrame.kt`
- `ui/EndgameOverlays.kt`（新規）, `GameScreen.kt`
- `ui/OptionPane.kt`, `ui/ConfigPreviews.kt`
- `game/ShareTextBuilder.kt`（`SyncRating`）

### 意図的にやらなかったこと
- WORKSHOP のコード実装（設計のみ）
- `GameUiState` フィールド追加

### 検証
- `assembleDebug` 成功

---

## 2026-07-21 — チャートだまし品質（案A/B）パイロット・scenario型整理

### 方針合意
- WORKSHOP は保留。手触り・チャート品質を最優先
- 案B（生成パイプライン）+ 案A（keyframes フェイク）を段階導入
- scenario 型はユーザー非表示の内部タグに降格

### 対応

#### 案B — `generate_charts_csv.py`
- keyframe 補間: **smoothstep → 線形**（折返し直前の減速サインを弱める）
- 折返し付近（±2秒）に **ノイズ強度アップ**（`inflection_window_seconds`）
- ノイズ強度引き上げ: DROP_REBOUND 0.90 / WHIPSAW 1.05 / RUNAWAY 0.92
- 旧型名 `V_SHAPE` 等は LEGACY マップで読み込み可

#### 案A — パイロット keyframes（3本）
- **v01** DROP_REBOUND: sec24 小反発→再下落→sec30 本底（`hint: まだ落ちるか？`）
- **z01** WHIPSAW: sec20 高値→sec22 急落の罠（`hint: 戻りは罠か？`）
- **b01** RUNAWAY: sec28 天井→sec30 押し目→sec38 再天井→崩落（`hint: 天井は本物か？`）
- 残り27本は従来形状のまま（次フェーズで展開予定）

#### scenario 型リネーム（内部のみ）
- `V_SHAPE` → `DROP_REBOUND`
- `ZIGZAG` → `WHIPSAW`
- `BUBBLE` → `RUNAWAY`
- 図鑑から `scenario_type` 表示を削除（上段は **時期 + BEST**）

### 触ったファイル
- `tools/generate_charts_csv.py`
- `content/stages_meta.csv`, `content/keyframes.csv`, `content/README.md`
- `app/src/main/assets/charts.csv`（再生成）
- `ui/ScrapbookPane.kt`

### 意図的にやらなかったこと
- 残り27本の keyframes 刷新（試遊フィードバック待ち）
- 案F（RESULT マーカー EXCELLENT/BAD）— 次タスク

### 検証
- `py -3 tools/generate_charts_csv.py` 成功（30ステージ / theo band 18499–21153）
- `assembleDebug` 成功

---

## 2026-07-21 — 試験用: パイロット3本のみ抽選フィルタ

### 目的
v01 / z01 / b01 の試遊時、他27本が混ざってパイロットプレイか判断できない問題への暫定対応。

### 対応
- `GameViewModel` に `PILOT_STAGE_FILTER_ENABLED = true`
- 有効時: 抽選・図鑑とも **v01, z01, b01 のみ**
- 全30本に戻すときはフラグを `false` にする

### 触ったファイル
- `game/GameViewModel.kt`

### 意図的にやらなかったこと
- 設定画面からの切替（試験終了後にフラグ削除 or false 化で十分）

### 検証
- `assembleDebug` 成功

---

## 2026-07-21 — ミクロ二層（設計＋セッションRNG）v01/z01 改稿

### 対応

#### 設計ミクロ — keyframes（v01 / z01）
- **v01**: 下降脚・上昇脚に 2〜3秒周期の小反発（sec7,13,24 底フェイク等）
- **z01**: マクロ WHIPSAW ＋ 各脚に細かい往復（sec22 戻り罠等）

#### セッション・ミクロ — `SessionMicroOverlay.kt`（新規）
- `startGame()` で seed を振り、マクロ列へ **脚ごと** にミクロを重ねる
- **周期 1〜4秒**（CHOP は 1〜2秒寄り）
- **脚スタイル RNG**（プレイごと `globalChaos` で比率が変わる）:
  - `QUIET` … ほぼ静か
  - `RIPPLE` … 通常往復
  - `CHOP` … 速い chop + スパイク
  - `COIL_SNAP` … マクロを部分的に打ち消して平坦化 → 1秒で急変
- 理論最大も同一 `sessionPriceRatios` で計算

#### 生成パイプライン
- ベースノイズを控えめに（0.40〜0.45）。ミクロの主役はランタイム側

### 触ったファイル
- `game/SessionMicroOverlay.kt`（新規）, `GameViewModel.kt`
- `content/keyframes.csv`（v01/z01）, `content/README.md`
- `tools/generate_charts_csv.py`, `assets/charts.csv`（再生成）

### 試験設定（継続）
- `PILOT_STAGE_FILTER_ENABLED = true`（v01 / z01 / b01 のみ）

### 検証
- `generate_charts_csv.py` / `assembleDebug` 成功

---

## 2026-07-21 — 全30本 keyframes 刷新・CLIFF 設計・本番抽選へ

### 対応

#### 全ステージ keyframes 再生成
- `tools/rewrite_keyframes.py` を全面改稿
  - マクロアンカー + `densify()` で **設計ミクロ**
  - v01 / z01 / b01 はパイロット確定版を維持
  - v02〜10 / z02〜10 / b02〜10 を型別テンプレートで生成

#### CLIFF（マクロ1秒急変）— 12本のみ
- `CLIFFS` 辞書で `(秒, 差分)` を定義（例: v08 sec18 → -20）
- 対象: v03,v05,v08,v10 / z03,z05,z07,z09 / b02,b03,b06,b08
- `content/README.md` に設計ガイド追記

#### 本番モード
- `PILOT_STAGE_FILTER_ENABLED = false` — **全30本** 抽選・図鑑

#### 3層構造（確定）
1. **マクロ + 設計ミクロ** … keyframes / CSV
2. **CLIFF** … keyframes（ごくたまに）
3. **セッション・ミクロ** … `SessionMicroOverlay`（プレイごと RNG）

### 触ったファイル
- `tools/rewrite_keyframes.py`, `content/keyframes.csv`, `content/README.md`
- `assets/charts.csv`（再生成）
- `game/GameViewModel.kt`（フィルタ解除）

### 意図的にやらなかったこと
- 案F（RESULT マーカー振り返り）— **実装済（次エントリ参照）**

### 検証
- `rewrite_keyframes.py` → 1977 keyframes / 12 cliffs
- `generate_charts_csv.py` → theo band 18499–18501
- `assembleDebug` 成功

---

## 2026-07-21 — チャート品質・現状承認 / SYNC見直しは延期

### 判断
- ミクロ強化により **SYNC 50%付近** になりやすいが、大きな問題なし → **現状のまま進行**
- **理論最大・SYNC 計算**の見直しは **RESULT ブラッシュアップ時**に合わせて検討（案F 等と一并）

### 意図的にやらなかったこと（当面）
- `SyncCalculator` / 星閾値の再調整
- RESULT マーカー評価（EXCELLENT/BAD）

---

## 2026-07-21 — RESULT マーカー振り返り（案F）

### 問題
- RESULT で約定の良し悪しが分かりにくい
- 理論ゴーストはジグザグで視認性が悪い

### 対応
- **ローカル極値ベース**の2段評価を追加
  - `好調!` / `NICE!` — 約定±3秒窓の底/天井付近
  - `ズレ` / `MISS` — 窓内の最悪側（高値買い・安値売り）
  - 中間は **ラベル非表示**
- `finishGame()` で filled マーカーのみ評価し `TradeMarker.timingGrade` に格納
- **RESULT カード**: `好調 N / ズレ M` + `背景タップ → チャート` ヒント（いずれかが1件以上あるときのみ）
- **グラフ確認（chartPeek）**: マーカー横に評価ラベル（買い=下、売り=上）

### 触ったファイル
- `game/TradeTimingGrade.kt`（新規）
- `game/TradeTimingEvaluator.kt`（新規）
- `game/TradeMarker.kt`（`timingGrade`）
- `game/ResultInfo.kt`（`niceTradeCount` / `missTradeCount` / `filledTradeCount`）
- `game/GameViewModel.kt`（`finishGame` 評価適用）
- `game/UiStrings.kt`（振り返り文言）
- `ui/PriceChart.kt`（`showTradeReview` + ラベル描画）
- `ui/GameScreen.kt`（chartPeek 連動・RESULT サマリー行）

### 意図的にやらなかったこと
- 理論ゴーストマーカー表示
- `SyncCalculator` / 星閾値の変更
- 共有テキストへの振り返り追記

### 検証
- `assembleDebug` 成功

---

## 2026-07-21 — RESULT chartPeek 視認性・凡例

### 問題
- グラフ上の評価ラベルが小さく、細かい上下動と重なって見づらい
- ▲▼ が買い/売りか分からない

### 対応
- **chartPeek 時にチャート下パネル**（`ResultChartPeekPanel`）を追加
  - 凡例: `▲ 買い` / `▼ 売り` / `◆ 好調` / `× ズレ`
  - 約定ごとのチップ（時刻 + 評価）を横スクロールで大きく表示
- グラフ上の評価は **文字→記号** に変更
  - 好調 = ◆ 菱形リング、ズレ = ○× バッジ（マーカーの背面）
- chartPeek レイアウトを Column 化（チャート + 下パネル）

### 触ったファイル
- `ui/ResultChartPeekPanel.kt`（新規）
- `ui/GameScreen.kt`（chartPeek レイアウト）
- `ui/PriceChart.kt`（記号バッジ）
- `game/UiStrings.kt`（凡例・戻る文言）

### 意図的にやらなかったこと
- 評価ロジック（±3秒窓）の変更
- SYNC / 星閾値

### 検証
- `assembleDebug` 成功

---

## 2026-07-21 — 評価表示を文字に戻し / 惜しかった一点ストリップ

### 問題
- ◆× 記号バッジでグラフがさらに見づらくなった
- 凡例・約定チップ一覧は情報過多で意味を感じにくい

### 対応
- グラフ上の評価を **好調!/ズレ の文字** に戻し、▲▼ の **横** に交互配置（マーカーと非重なり）
- 記号バッジ・凡例を削除
- chartPeek 下パネルを **ResultRegretStrip** に差し替え
  - 最も惜しかった約定1件だけ: `18秒の売り — もう少し待てた` 等
  - リトライ一言: `もう一度、底を狙える`
  - 惜しみがなければパネル非表示
- RESULT カードにも同じ惜しみ行 + 振り返り誘導を表示

### 触ったファイル
- `ui/PriceChart.kt`（横付き文字ラベル）
- `ui/ResultRegretStrip.kt`（新規、`ResultChartPeekPanel` 削除）
- `ui/GameScreen.kt`（カード連動）
- `game/TradeTimingEvaluator.kt`（`pickRegretMoment`）
- `game/ResultRegretMoment.kt`（新規）
- `game/UiStrings.kt`（惜しみ文言）

### 意図的にやらなかったこと
- 評価ロジック本体の変更
- 約定一覧 UI の復活

### 検証
- `assembleDebug` 成功

---

## 2026-07-21 — 下帯凡例・強制売りマーカー・peek 演出

### 問題
- 記号評価・凡例削除で ▲▼ の意味が分からなくなった
- 終了時強制売りが × 未約定と混同しやすい
- 下帯のリトライ文言・RESULT カードの惜しみ行・文字ヒントは過剰
- chartPeek 誘導は文字ではなくデザインで示したい

### 対応
- **強制売り** `isForcedClose` — 破線 ▼ + 上横棒（× ではない）
- 下帯 **ResultChartPeekFooter**: 凡例 `▲ 買い / ▼ 売り / ▼─ 終了売り / × 未約定` + ハイライト最大2行
  - ズレ最大 + 好調ベスト（なければあと一歩）
- RESULT カードから惜しみ行・タップ文言を削除。好調/ズレ集計は薄字で任意表示
- **ResultChartPeekScrim** — 中心濃・四隅薄のグラデ + コーナーブラケット脈動でタップ示唆

### 触ったファイル
- `game/TradeMarker.kt`（`isForcedClose`）
- `game/GameViewModel.kt`（終了決済フラグ）
- `ui/PriceChart.kt`（強制売り描画）
- `ui/ResultChartPeekFooter.kt`（新規、`ResultRegretStrip` 削除）
- `ui/ResultChartPeekScrim.kt`（新規）
- `ui/GameScreen.kt`
- `game/TradeTimingEvaluator.kt`（`pickRegretHighlights`）
- `game/UiStrings.kt`

### 検証
- `assembleDebug` 成功

---

## 2026-07-21 — 未約定×縮小・評価文言刷新・グラフ非重なり

### 問題
- 未約定 × が大きく丸囲みで他マーカーを隠す
- 好調!/ズレ は意味がズレ（相場状態 vs 底/高タイミング）、グラフとも重なる
- ハイライト「38秒の買い—底より高かった」が分かりにくい

### 対応
- **未約定 ×** — 円を廃止・小型（腕20px）・薄色。描画順は最背面
- **グラフ上の評価文字** — 廃止（激しい局面では重なり unavoidable）。説明は下帯のみ
- **用語** — NICE/MISS の本質に合わせる
  - 集計 JP: `狙い通り N / 惜しい M`（底買い・高売りどちらも「狙い通り」）
  - ハイライト: `38秒: 高い所で買った` / `12秒: 底付近で買えた` 等、売買と極値を明示
- EN は NICE/MISS 系のまま、side-specific 文案

### 触ったファイル
- `ui/PriceChart.kt`
- `game/UiStrings.kt`
- `ui/GameScreen.kt`

### 検証
- `assembleDebug` 成功

---

## 2026-07-22 — 吹き出し評価・厳格化・SMOOTH修正・CONFIG格子

### 問題
- グラフ評価の再表示要望、件数過多、文言と RESULT 集計の不整合
- SMOOTH チャートの断線・Y軸アニメによる過去線のうねり
- CONFIG が縦長すぎてスクロール必須

### 対応
- **評価記号統一**: ◎ = 好タイミング（底/高付近）、− = 惜しい（未約定 × と区別）
- **閾値厳格化** + **NICE 最低1件保証**（該当なし時は最良1件を ◎ に）
- **chartPeek 吹き出し**: プロット外側（上半分/下半分）に ◎底 / −高値 等、尾付き枠
- **下帯**: 条件付き凡例（終了売り・未約定は存在時のみ）、ハイライトは反転/薄字で良否表示
- **RESULT 集計**: `◎ N / − M` に統一
- **SMOOTH**: Yレンジ即時スナップ、0.25秒サンプル、現在秒まで線を延長
- **CONFIG**: 2×2 格子 + CONTRAST 行、ヒント文削除でスクロール不要

### 触ったファイル
- `game/TradeTimingEvaluator.kt`
- `game/UiStrings.kt`
- `ui/PriceChart.kt`
- `ui/ResultChartPeekFooter.kt`
- `ui/OptionPane.kt`
- `ui/GameScreen.kt`

### 検証
- `assembleDebug` 成功

---

## 2026-07-22 — ○×マーク・ハイライト連動・CONFIG再設計

### 問題
- 評価表示が細分化され見づらい / 吹き出し不要
- 下帯ハイライトとグラフの対応が弱い
- CONFIG 格子の死にスペース・凡例位置・説明欠落

### 対応
- 評価を **○（狙い通り）/ ×（惜しい）** の2種のみ。大きめ円/×をマーカーからずらして配置（重なり回避）
- 吹き出し廃止。下帯凡例に `○ 狙い通り` `× 惜しい` を追加
- **ハイライトタップ** → 該当 ▲▼ + ○× が脈打ち強調、他マーカーは薄表示。チャートタップで解除（解除後タップで peek 終了）
- RESULT 集計 `○ N / × M` を 14sp 太字に
- **CONFIG ConfigTile**: 上=タイトル / 中央=チップ+プレビュー / 下=説明文

### 触ったファイル
- `game/TimingFocus.kt`（新規）
- `game/UiStrings.kt`
- `ui/PriceChart.kt`
- `ui/ResultChartPeekFooter.kt`
- `ui/GameScreen.kt`
- `ui/OptionPane.kt`

### 検証
- `assembleDebug` 成功

---

## 2026-07-22 — RESULT ○×配置・ハイライト枠・売買損益

### 問題
- ○× が ▲▼ や他評価記号と重なりやすい。距離を開けても対応関係が分かりにくい
- ハイライト行タップで二重枠になり下帯が伸び、グラフ領域が縮んで見える
- 強調時のマーカー囲み（円）と NICE の ○ が同じに見える
- リザルト「売買」が利子除き総額（`finalAsset - interest`）になっており、株の増減分ではない

### 対応
- **○× 配置**: 縦横の候補を拡大（最大 ~124px）、重なり判定に余白 8px、買いは上優先で探索
- **引き線**: 評価記号から ▲▼ へ破線コネクタ（距離が空いても対応が追える）
- **強調リング**: 円 → 角括弧（L字4隅）に変更し NICE ○ と視覚分離
- **ハイライト枠**: 未選択時も透明の外枠 + 内枠で二重枠と同サイズを常時確保
- **売買表示**: `tradeGainLoss = finalAsset - INITIAL_ASSET - totalInterest` を `売買 +¥104` 形式で表示

### 触ったファイル
- `ui/PriceChart.kt`
- `ui/ResultChartPeekFooter.kt`
- `game/ResultInfo.kt`
- `game/GameViewModel.kt`
- `game/UiStrings.kt`
- `game/ShareTextBuilder.kt`
- `ui/GameScreen.kt`

### 検証
- `assembleDebug` 成功

---

## 2026-07-22 — CONFIG格子 / 凡例 / 利子レイアウト / カウントダウンSE / ハイライト文言

### 問題
- CONFIG の CONTRAST だけ2列幅になっていた
- 凡例の売りが ▼（塗り）でチャートの ▽（線）と不一致
- 惜しい（−）と未約定（×）が同じ記号
- 終盤カウントダウン・FINISH に SE がない
- 利子 +¥ 表示の出現で HUD が伸縮しグラフが揺れる
- ハイライト時 ○/× の拡大で配置が再計算され横に震える
- ハイライト文言が単調（「高い所で買った」等）

### 対応
- CONTRAST を4行目左列1枚に（右は Spacer）
- 凡例売りを `▽`、惜しいを `−`（チャート描画も横線に統一、未約定は × のまま）
- `se_countdown.wav`（5..1 ビープ・pitch 上昇）/ `se_finish.wav`（ホイッスル）追加
- 利子チップ用 `minWidth` ボックスで領域常時確保
- ○/× 配置は固定半径17px、拡大は描画半径のみ（位置不変）
- ハイライト文を秒数シードで複数候補から選択（高値をつかまされた、底値で拾えた 等）

### 触ったファイル
- `ui/OptionPane.kt`
- `game/UiStrings.kt`
- `ui/PriceChart.kt`
- `ui/GameScreen.kt`
- `game/GameViewModel.kt`
- `audio/GameSoundPlayer.kt`
- `tools/generate_se_wav.py`
- `res/raw/se_countdown.wav`, `se_finish.wav`

### 検証
- `assembleDebug` 成功

---

## 2026-07-22 — チャート形状タグ体系（30本付与）

### 方針合意
- 旧3分類（DROP_REBOUND / WHIPSAW / RUNAWAY）だけでは v10/z10/b10 が同型テンプレになりすぎる
- **2軸＋任意トリック**で keyframes 生成の多様化を進める
  - `macro_shape` … 60秒マクロ骨格（11種）
  - `pivot_timing` … 転換が EARLY / MID / LATE のいつか
  - `trick` … だまし（約40%のステージのみ、空可）
- `scenario_type` はノイズ強度用に残し、`macro_shape` との整合を生成スクリプトで検証

### 対応
- `content/stages_meta.csv` に3列追加し全30本タグ付け
- `tools/generate_charts_csv.py` にタグ検証・scenario 整合チェック追加
- `content/README.md` にタグ一覧と将来の史実アンカー設計メモ追記

### 触ったファイル
- `content/stages_meta.csv`
- `tools/generate_charts_csv.py`
- `content/README.md`

### 意図的にやらなかったこと
- `rewrite_keyframes.py` の shape 別ビルダー拡張（**次タスク**）
- 史実アンカー UI（将来検討）
- アプリ側へのタグ露出（UI非表示のまま）

### 将来検討: 史実タイムラインアンカー
- 底値→暴騰の転換点に「西暦＋出来事」、じり下げ期間の「何年〜何年」を照合可能に
- プレイ中は見せず、**クリア後の図鑑詳細 or RESULT peek** で細目盛り＋タップ1行
- データは `content/timeline_anchors.csv` 想定（編集は content/ のみ）

### 検証
- `py -3 tools/generate_charts_csv.py` 成功

---

## 2026-07-22 — macro_shape 別 keyframes 生成

### 対応
- `rewrite_keyframes.py` を全面刷新
  - `stages_meta.csv` を読み込み、`macro_shape` × `pivot_timing` × `trick` からアンカー生成
  - 11 shape 別ビルダー（V_RECOVER / W_DOUBLE / EARLY_SHOCK / LATE_SPIKE / RANGE / N_RALLY / INV_N / BUBBLE / INVERSE_V / GRIND_UP / GRIND_DOWN）
  - v01 / z01 / b01 は手調整パイロットを維持
  - `sanitize_anchors` で 0..69 秒に正規化
- 全30本 keyframes 再生成 → `generate_charts_csv.py` で charts.csv 更新

### 触ったファイル
- `tools/rewrite_keyframes.py`
- `content/keyframes.csv`（生成物）
- `app/src/main/assets/charts.csv`（生成物）

### 意図的にやらなかったこと
- 史実タイムラインアンカー UI
- 15本追加

### 検証
- `py -3 tools/rewrite_keyframes.py` 成功（1977 keyframes）
- `py -3 tools/generate_charts_csv.py` 成功（theo band 18499–18501）
- 例: b07 GRIND_DOWN max≈102（旧 runaway テンプレ ~140 から改善）

---

## 2026-07-22 — チャート形状ブラッシュアップ（だまし・不規則レンジ）

### 問題
- BUBBLE / V_RECOVER が「きれいな山・谷」で作業化しやすい
- RANGE（z02,z04,z06,z10）が正弦波的に規則的で音ゲー化
- 垂直加速トラップ・フラッシュクラッシュ型は好評 → 増やしたい

### 対応
- **V_RECOVER / V01**: 下落中の小反発、回復途中の押し目を追加
- **BUBBLE / B01**: 上昇中押し目 + ダブルトップ（二番天井の迷い）
- **build_bubble**: 全BUBBLEに押し目・二段天井・初動リジェクト
- **build_range**: 周期/振幅ランダム化、拡大型(z02等)・収束型(z08,z10)を混在
- **densify(irregular=True)**: RANGE のミクロ正弦波依存を低減
- **LATE_SPIKE / N_RALLY**: 垂直加速 → 直後暴落（v10 cliff@58, z09 cliff@54）
- **CLIFF追加**: b04/b05/b09 上昇中フラッシュクラッシュ

### 触ったファイル
- `tools/rewrite_keyframes.py`
- `content/keyframes.csv`, `charts.csv`, `chart_overview.png`（生成物）

### 検証
- rewrite + generate + preview 成功

---

## 2026-07-22 — だまし消失の根本修正（70秒/keyframe + punch）

### 問題
- 前回の macro_shape 改善がプレイ/一覧で単調に見える
- 原因: スパース keyframes の**線形補間**で cliff/punch が斜めに溶ける
- densify 正弦波が規則的テクスチャを上書き
- balance_series が全ステージの振幅を均一化

### 対応
- keyframes を **毎秒1点（70点）** で出力
- **PUNCHES** テーブルで秒単位の押し目/二番天井/垂直トラップを強制
- RANGE は sparse+補間をやめ **毎秒ランダムウォーク**
- generate: sharp 秒はノイズ付与を抑制（|Δ|≥7）
- 手調整パイロット廃止 → 全30本同一パイプライン

### 触ったファイル
- `tools/rewrite_keyframes.py`（全面刷新）
- `tools/generate_charts_csv.py`
- 生成物: keyframes.csv, charts.csv, chart_overview.png

### 検証
- v10 max1s≈24, z09≈21, b03≈21（垂直トラップ維持）
- theo band おおむね 18500 中心（GRIND_DOWN は floor 調整）

---

## 2026-07-22 — 設計ミクロ復元（マクロと分離）

### 問題
- 70秒/keyframe 化の際に **densify（設計ミクロ）を廃止**し、light_noise のみになっていた
- generate 側も sharp 秒でノイズ抑制しすぎ → 毎秒〜数秒の激しい ripple が消えた
- ユーザー意図は **マクロの単調さ** の話であり、ミクロ削除ではなかった

### 対応
- **マクロ**: PUNCH + CLIFF + 骨格アンカー（ロック秒として保護）
- **設計ミクロ**: `add_design_micro()` で旧 densify 相当を復元（ロック秒以外）
- **RANGE**: マクロ=不規則アンカー、ミクロ=design_micro（ランダムウォーク一本化をやめる）
- **generate**: `add_microstructure` を従来強度に戻す
- **セッション・ミクロ**: `SessionMicroOverlay` は変更なし（プレイ時追加）

### 触ったファイル
- `tools/rewrite_keyframes.py`
- `tools/generate_charts_csv.py`

---

## まだ未着手（本格化ロードマップ）

- **Phase W1**: WORKSHOP（案A）— 基板UI・3スロット・初回解放10/30

- Step D: ストア公開パック（着手済み → `store/STORE_CHECKLIST.md`）
- 商用OHLCへの完全差し替え（ライセンス整理後）
- 史実テキストの人手精査（content/ をユーザーが編集予定）

## 今後の運用ルール

1. 仕様・コードを変えたら **必ず本ファイルの末尾に日付付きエントリを追記**する
2. 各エントリには「問題 / 対応 / 触ったファイル / 意図的にやらなかったこと」を書く
3. `GameUiState` のフィールド追加は、ユーザー承認なしに行わない
4. **チャート名・コメント・形状の編集は `content/` のみ。`assets/charts.csv` は手編集しない**

---

## 2026-07-22 — Extreme ティア 15本 + まれな抽選

### 背景
- 標準30本は理論最大バンド（~18,500）に平準化され、現実の数倍級変動は意図的に除外されていた
- 50%級の終盤崩落・数倍級パabolic を **内部 Extreme（e01–e15）** として追加
- 出現率 8%（標準プールとは別抽選）。UI・シェアには tier 名を出さない

### 対応
- `content/stages_meta.csv`: `tier` 列追加、Extreme 15行（e01–e12 ≈50%級、e13–e15 ≈2.5–3.1x）
- `tools/rewrite_keyframes.py`: `EXTREME_ANCHORS` / CLIFF / 専用ビルド
- `tools/generate_charts_csv.py`: Extreme は `balance_series` スキップ（clamp のみ）
- `GameViewModel`: `pickRandomStage()` — 8% で e** のみ、それ以外は v/z/b 標準30
- `content/chart_overview.png` 再生成（45本）

### 触ったファイル
- `content/stages_meta.csv`, `content/keyframes.csv`（生成）
- `tools/rewrite_keyframes.py`, `tools/generate_charts_csv.py`, `tools/preview_charts.py`
- `app/.../charts.csv`, `app/.../GameViewModel.kt`

### 意図的にやらなかったこと
- 資産引き継ぎ（Crash 型メタ進行）— 設計議論のみ
- `GameUiState` 拡張、Extreme の UI 表示

---

## 2026-07-22 — Extreme 4本を「史実の期待裏切り・終盤上昇」に組替

### 背景
- e01–e15 が終盤下落ばかりで単調。上がって終わる型が不足していた

### 組替（e04/e06/e07/e10）
| ID | 史実ネタ | 裏切り |
|---|---|---|
| e04 | 2019年 パウエル「Patience」 | 利上げ警戒 → 垂直リスクオン |
| e06 | 1998年 LTCM後のFRB介入 | 危機続くはず → 終盤急回復 |
| e07 | 2016年 Brexit後の英国株 | 離脱=崩壊 → 買い戻しで高値終了 |
| e10 | 2010年 フラッシュクラッシュ | 市場終わり → 同日大復旧 |

### 触ったファイル
- `content/stages_meta.csv`, `tools/rewrite_keyframes.py`（`EXTREME_SURGES` 追加）
- `content/keyframes.csv`, `assets/charts.csv`, `chart_overview.png`（再生成）

---

## 2026-07-22 — e15 GME頂点化 + Extreme 全15 史実メタ校正

### 背景
- e15 が終値ほぼ開始並みでチャートと「崩れる」文言が矛盾
- e03「円急落」と終盤下落チャートの因果が曖昧（円安なら線は上向きになりやすい）
- e11/e12 など文言と終値乖離

### 対応
- **e15**: GME 2021年1月・空売り圧縮の**高値で終了**（終値 **+160%**）。e09（投げ崩落）と役割分離
- **e12**: 終盤清算を深く（終値 **-22%**）
- **全 e01–e15**: `brand_name` / `period` / `history_comment` をチャート形状と史実に合わせて校正
  - 例: e03 → 日銀利上げサプライズ後の**リスク資産急落**（2024年7月）
  - e09 → GME スクイーズ**後**の投げ / e15 → **頂点**の踏みとどまり

### 触ったファイル
- `content/stages_meta.csv`, `tools/rewrite_keyframes.py`
- 生成物: `keyframes.csv`, `charts.csv`, `chart_overview.png`

---

## 2026-07-22 — リザルト peek 視認性 + CHART SMOOTH 改善

### 問題
- ○/惜しいマークが ▲▼ と重なり判別困難、下帯も件数過多
- SMOOTH でも SessionMicroOverlay の秒以下 ripple がそのまま描画され「ぐにゃぐにゃ」

### 対応
- **pickReviewHighlights**: NICE / MISS 各最大3件（計6）。下帯とグラフで同じ集合
- **レーン配置**: レビュー時は上下11%を ◎（上）/ ×（下）専用レーンに分離、点線で接続
- **記号**: ◎ 狙い通り（二重円+中心点）、× 惜しい（▲▼・未約定×と区別）
- **SMOOTH**: 描画/Y軸レンジを **1秒刻み** サンプルに変更（ミクロは STEP のみ追随）

### 触ったファイル
- `TradeTimingEvaluator.kt`, `PriceChart.kt`, `ResultChartPeekFooter.kt`, `UiStrings.kt`

---

## 2026-07-22 — リザルトマーカー整理 + ハイライト3件 + SMOOTH Y軸修正

### 対応
- **未約定**: リザルト（全セッション表示）ではマーカー非表示
- **終了売り**: 通常 ▽ + 右側に **!**（破線▼廃止）
- **下帯ハイライト**: 強い順 **最大3件**（◎/× 合算）。直前売買比較軸 `RegretTradeContext` で文言多岐化
- **SMOOTH 過去線が動く問題**: Y軸を毎フレーム snap していたのが原因 → STEP と同じ **拡張のみ即反映・縮小は200ms補間** に統一

---

## 2026-07-22 — CHART SMOOTH/STEP の描画差分のみに統一

### 問題
- SMOOTH だけ path/Y サンプルを 1秒刻みにしていた → データ形状が STEP と不一致
- SMOOTH だけ末尾 tail 点を追加していた
- ユーザー意図は **同じ価格列を、直線結び vs 階段結びで描く差のみ**

### 対応
- サンプリング 0.05s・Y レンジ計算・Stroke を STEP/SMOOTH 共通
- 差分は `lineTo(x,y)` のみ（SMOOTH） vs 水平→垂直（STEP）

### 触ったファイル
- `ResultRegretMoment.kt`, `TradeTimingEvaluator.kt`, `PriceChart.kt`, `UiStrings.kt`, `ResultChartPeekFooter.kt`

---

## 2026-07-22 — SYNC再較正・NICE保証撤廃・約定後クールダウン

### 背景
- ◎/× ハイライトと ★（到達率）の乖離。Extreme の理論最大が Standard の2〜4倍で ★1 偏重
- NICE 0件時の「最良1件を ◎ 昇格」がハイライトの信頼性を下げていた
- 約定後の連打抑制と、押せる/押せないの視覚対比が必要

### 対応

**SYNC / ★**
- `SyncCalculator`: Extreme のみ理論最大を **INITIAL×2.2（¥22,000）** でキャップ
- `SyncRating` 閾値: ★5≥65% / ★4≥57% / ★3≥48% / ★2≥30% / ★1<30%
- 分布検証: `tools/simulate_sync_distribution.py`

**ハイライト**
- NICE 保証ロジック削除（`TradeTimingEvaluator.applyReviews`）
- `RegretTradeContext` → **`RegretTradeContextFlag` Set**（局所＋直前売買の合成文言）
- 終了売り: 通常 **▽** のみ（`!`・専用凡例廃止）

**約定後クールダウン（1秒・買い/売り共通）**
- 約定完了後 `ORDER_COOLDOWN_MS` まで再注文不可
- **チャージ中**: 背景=LcdBackground、ハッチ、下端4pxバー（滑らか）
- **完了**: 緑ボタン＋短いポップ（1.04→1.0）＋ティックSE＋触覚
- チャート減速・コップ充填・完了フラッシュ等は採用せず（引き算）

**リザルト**
- `ResultInfo` の ◎/× 件数を `resultReviewSummary` で併記（`GameScreen`）

### 触ったファイル
- `SyncCalculator.kt`, `ShareTextBuilder.kt`, `TradeTimingEvaluator.kt`, `ResultRegretMoment.kt`, `UiStrings.kt`
- `GameViewModel.kt`, `ChartFrame.kt`, `OrderCharge.kt`, `OrderChargeBar.kt`
- `GameScreen.kt`, `GameSoundPlayer.kt`, `HapticPlayer.kt`, `PriceChart.kt`, `ResultChartPeekFooter.kt`
- `tools/simulate_sync_distribution.py`, `tools/simulate_lag_sensitivity.py`

### 意図的にやらなかったこと
- 買い後/売り後でクールダウン秒数・演出を分ける（対称1秒のまま）
- `GameUiState` 拡張（クールダウン状態は `ChartFrame`）
- baseline 補正 SYNC（シミュレーション上 ★1 偏重のため不採用）

---

## 2026-07-25 — SYNC妥当化（ラグ付き理論最大・Extremeキャップ撤廃・★再較正）

### 問題
- 理論最大が **ラグ無し＋1秒先読み** の全知で、実プレイ（1秒ラグ＋1秒クール）と評価軸が不一致
- Extreme は分母を ¥22,000 にハードキャップして帳尻合わせ → 表示理論最大と到達率分母がズレうる
- 利子 4.5% 化後も旧★閾値（65/57/48/30）のまま

### 対応

**理論最大（`SyncCalculator`）**
- プレイヤーと同じ **1秒ラグ＋1秒クールダウン** 下の理想提出に変更
- 提出判断: 約定時刻 `fill` で、直後1秒が上げなら買い／下げなら売り（旧「次秒」判定を約定時刻へずらす）
- Extreme 分母キャップ（`INITIAL×2.2`）を **撤廃**
- 到達率 = `playerAsset / theoreticalMax * 100`（下限0）。リザルトの理論最大＝この分母

**★閾値（`SyncRating`）** — `tools/simulate_sync_distribution.py` で再計測
- ★5 ≥95% / ★4 ≥88% / ★3 ≥80% / ★2 ≥70% / ★1 <70%
- 目安: 現金放置は★1〜2中心、そこそこ往復は★2〜3、理想提出のみ★5が日常

**検証**
- `py -3 tools/simulate_sync_distribution.py` → `tools/sync_distribution_report.txt`
- STANDARD 理論最大 mean ≈14.1k（旧ラグ無し~18.5kより低下は制約反映）

### 触ったファイル
- `SyncCalculator.kt`, `GameViewModel.kt`, `ShareTextBuilder.kt`
- `tools/simulate_sync_distribution.py`, `tools/sync_distribution_report.txt`

### 意図的にやらなかったこと
- baseline 補正の再導入
- 到達率に判定／COMBOを混ぜる
- Extreme 抽選率・チャート形状の変更
- TIME UP SE のシンクロ帯しきい値変更

---

## 2026-07-26 — SYNC を HUMAN MAX（案C）に変更・★再較正

### 問題
- 旧理論（未来1秒／スキャル理想）はスパイク必中を含み、実力の参考軸として破綻しやすかった
- ★閾値（95/88/80/70）も旧分母向けのまま

### 対応

**理論最大 = HUMAN MAX（案C）**
- 価格を MA 3秒で平滑化し、相対振幅 ≥2% のマクロ山・谷だけを抽出
- 谷買い／山売り（ロングのみ）を 1秒ラグ＋1秒クール で実行
- `max(トレード結果, 現金放置)` — 下落見送りを 100% 基準に含める
- ノイズヒゲの一点は分母に含めない → 実プレイで超えれば OVER SYNC（100%超）

**★閾値（`SyncRating`）**
- ★5 ≥100% / ★4 ≥95% / ★3 ≥90% / ★2 ≥75% / ★1 <75%
- ★0 = RETIRE のみ（表示は☆☆☆☆☆）
- 100%超は `★★★★★+`（図鑑・シェア・リザルト共通）

**その他**
- TIME UP SE の帯を 100/90/75 に合わせて調整
- Tips 文言をマクロ波／OVER に更新
- 検証: `tools/simulate_human_max.py`, `tools/simulate_sync_distribution.py`

### 触ったファイル
- `SyncCalculator.kt`, `ShareTextBuilder.kt`, `GameScreen.kt`, `GameSoundPlayer.kt`, `StartScreenCopy.kt`
- `tools/simulate_human_max.py`, `tools/simulate_sync_distribution.py`, `tools/preview_charts.py`
- `tools/sync_distribution_report.txt`, `content/chart_overview.png`

### 意図的にやらなかったこと
- ★への COMBO／判定の混入
- OVER 専用の追加演出（エフェクト強化は後続可）
- レリック倍率を理論分母から除外する変更（現状どおり資産経由で SYNC に影響）

---

## 2026-07-26 — リザルト主役を資産へ（SYNC/★は参考）・利益キャップ撤廃

### 方針
- チャートごとのリスク・リターン差は残す
- ★を総合評価にも、円感覚への無理寄せ（利益キャップ）にもしない
- **リザルトの主役 = 最終資産**。SYNC/★は参考表示に一段落とす

### 対応
- `SKILL_GAIN_CAP`（現金+2000）を撤廃。HUMAN MAX（案C）はキャップなしに戻す
- リザルトUI: 反転パネルの大きな資産 → 小さめの％/★ → 内訳 → COMBO
- シェア文面も資産を先に
- 結果チュートリアルのステップ順を資産→SYNC参考に合わせる

### 触ったファイル
- `SyncCalculator.kt`, `GameScreen.kt`, `ShareTextBuilder.kt`, `UiStrings.kt`
- `tools/simulate_human_max.py`, `update.md`

---

## 2026-07-26 — ★閾値を資産床¥10,000＋理論までの等分に変更

### 方針
- ★1: 最終資産 &lt; ¥10,000（必ずマイナス → 納得しやすい）
- ★2床: ¥10,000
- ★3・★4: ¥10,000〜HUMAN（理論）を三等分した境界
- ★5: ≥ 理論 / ★5+: &gt; 理論
- 現金放置がチャートによって★2〜★4になりうるのは「そのチャートでは見送も正解」として許容

### 触ったファイル
- `ShareTextBuilder.kt`（`SyncRating`）, `GameScreen.kt`, `ScrapbookEntry.kt`, `ScrapbookRepository.kt`, `ScrapbookPane.kt`
- `tools/simulate_sync_distribution.py`, `tools/star_asset_thresholds.*`, `update.md`

---

## 2026-07-26 — リザルトを「★反転上段」に戻しつつ縦圧縮

### 方針
- 全部捨てて新レイアウトにするより、効いていた反転％★を上に戻す
- 資産は二重枠の一行に圧縮（反転パネルを重ねない＝ごちゃつき／縦長の主因を除去）

### 触ったファイル
- `GameScreen.kt`, `UiStrings.kt`, `update.md`

---

## 2026-07-27 — レリック獲得ポップ＋入力ゲート硬化

### 目的
- 出金〜リザルト導入後に、新規レリックがあれば短い獲得ポップで START 受取へ誘導
- 演出中／モーダル中に再挑戦や画面遷移できてしまう穴を塞ぐ

### 対応
- RESULT: 導入完了まで RETRY 不可（固定1.4秒ゲートを廃止）
- レリック受取／報酬箱／通算履歴／チュートガイド／免責／出金〜導入／獲得ポップ中はナビ・START をロック
- 今回出金で新規 pending になったレリック向け `RelicRewardToast`（スタンプSE）
- START 離脱時にローカルモーダル状態を破棄（戻り時の張り付き防止）

### 触ったファイル
- `GameViewModel.kt`, `GameScreen.kt`, `RelicUi.kt`, `UiStrings.kt`, `MainActivity.kt`, `update.md`

---

## 2026-07-27 — リザルト％★タップで評価内訳

### 目的
- SYNC／★の意味を、説教ではなく「この試合の数字」で見せる

### 対応
- 反転パネル（％★）タップで `SyncRatingBreakdownOverlay`
- 今回結果・目標額・★梯子（資産閾値とSYNC%）を表示。現在段を反転ハイライト
- 結果チュート step1 にタップ導線を一文追加

### 触ったファイル
- `RelicUi.kt`, `GameScreen.kt`, `UiStrings.kt`, `update.md`

---

## 2026-08-02 — MISS→LIFE因果 / OVER余韻 / 図鑑BEST資産 / 文言寄せ

### 対応
- MISS 判定の約 320ms 後に LIFE 球が割れ、-1 チップ＋触覚で減少を認知
- OVER SYNC 時、リザルト導入末に短い OVER スタンプ余韻
- 図鑑に自己ベスト最終資産を小さく併記（永続化追加）
- Tips / goal / ストア掲載文を「資産主役・％★は目安」に寄せる
- おまけ: CONFIG の DEV ラボを debug ビルドのみ／SHARE・資産TOPは intro 完了後のみ

### 触ったファイル
- `ChartFrame.kt`, `GameViewModel.kt`, `HealthRetireUi.kt`, `HapticPlayer.kt`
- `ScrapbookEntry.kt`, `ScrapbookRepository.kt`, `ScrapbookPane.kt`
- `GameScreen.kt`, `StartScreenCopy.kt`, `UiStrings.kt`, `OptionPane.kt`
- `goal.md`, `DESIGN.md`, `store/LISTING.md`, `update.md`

---

## 2026-08-02 — 疑似疾走（横軸窓圧縮）＋リザルト追い吹き出し

### 対応
- 可視窓: 20s →(30) 16s →(45) 12.5s →(53) 10s。目標まで 2s 線形補間（実時間不変）
- S4: TIME パルスを窓段階と同期（30〜弱 / 45〜中 / 53〜強）。資産隠しは従来どおり 50〜
- グリッド呼吸は見送り（チャートに時間グリッドが無く、詰める対象がない）
- リザルト資産行右上に吹き出し1つ: ★2–4は次★まであと¥、否则 BESTまであと¥。RETIRE／マイナス（★1）／OVERは無し。PREVなし

### 触ったファイル
- `VisibleWindow.kt`, `ResultChaseHints.kt`, `PriceChart.kt`, `GameScreen.kt`
- `ResultInfo.kt`, `GameViewModel.kt`, `ScrapbookRepository.kt`, `UiStrings.kt`, `update.md`

---

## 2026-08-02 — 可視窓を連続線形圧縮に強化＋LIFE鼓動

### 対応
- 窓キーポイント: (0,20)→(30,10)→(45,5)→(53,2)。区分線形で常時圧縮（傾きはほぼ均一のため単一直線は不要）
- LIFE 残2は周期約1.1s、残1は約0.7sの点滅。MISS減少演出中は一時停止

### 触ったファイル
- `VisibleWindow.kt`, `PriceChart.kt`, `HealthRetireUi.kt`, `GameViewModel.kt`, `update.md`

---

## 2026-08-02 — 可視窓の切り替え方式見直し／LIFE鼓動調整／利子HUD固定

### 対応
- 窓: 30で20→10を2秒補間、45→53のみ5→2常時線形（入場時2秒で合流）、53以降2固定
- LIFE鼓動は球のみ（文字は固定）。透明度を抑えつつスケール鼓動で視認性向上
- 利子ポップは常時同じ占有サイズ（alpha切替）でチャート領域のガクつき防止

### 触ったファイル
- `VisibleWindow.kt`, `PriceChart.kt`, `HealthRetireUi.kt`, `GameScreen.kt`, `update.md`

---

## 2026-08-02 — 可視窓キーポイント再設定／LIFE鼓動の視認性回復

### 対応
- 窓: (0,20)→(30,12)→(40,8)→(50,5)、すべて 2 秒補間（常時線形は廃止）
- LIFE鼓動が見えにくかった原因: 前回「薄くしない」調整で振幅を潰し、さらに alpha 下限 0.5 で点滅を殺していた
- 鼓動をスケール大＋谷での反転フラッシュに変更（文字は非鼓動のまま）

### 触ったファイル
- `VisibleWindow.kt`, `PriceChart.kt`, `HealthRetireUi.kt`, `update.md`

---

## 2026-08-02 — 序盤の横軸引き伸ばし修正／反転フラッシュ削除

### 原因
- 経過 &lt; 窓秒のとき `span = elapsed` で全幅に伸ばしていた → 序盤だけ速く見え、20秒に近づくと減速

### 対応
- プレイ中の横軸は常に `windowSec` 基準（左は未プレイの空白）。秒あたりのピクセルを一定に
- LIFE鼓動の谷反転フラッシュを削除（スケール鼓動のみ）

### 触ったファイル
- `PriceChart.kt`, `HealthRetireUi.kt`, `update.md`

---

## 2026-08-02 — 可視窓を終盤線形圧縮／序盤 span=elapsed に復帰

### 対応
- 横軸: 経過 &lt; 窓のとき再び `span = elapsed` で全幅伸縮（左空白方式を撤回）
- 窓: 〜40s（圧縮開始前）は 20s 固定、50→59s で 20s→6s 線形圧縮（段階 2 秒補間は廃止）

### 触ったファイル
- `VisibleWindow.kt`, `PriceChart.kt`, `update.md`

---

## 2026-08-02 — 終盤緊張 B案（窓圧縮廃止＋HUD／心拍）

### 対応
- 可視窓圧縮を廃止し常時 20s 固定（序盤の span=elapsed 伸縮は維持）
- TIME パルス: 45〜弱 / 50〜中 / 55〜強。資産隠しは 50〜、カウントは 55〜のまま
- 終盤心拍: se_tick＋短い触覚を 45〜加速（900→650→420ms）。55〜は SE を止めカウント優先

### 触ったファイル
- `VisibleWindow.kt`, `PriceChart.kt`, `GameViewModel.kt`, `GameSoundPlayer.kt`, `HapticPlayer.kt`, `update.md`

---

## 2026-08-02 — 初回 CONFIG 既定を HUD=FULL / LANG=JP

### 対応
- 未設定時の HUD を FOCUS→FULL、言語を EN→JP（既存保存値は維持）

### 触ったファイル
- `SettingsRepository.kt`, `update.md`

---

## 2026-08-02 — ストア公開ブラッシュアップ（詰め）

### 対応
1. プライバシー文面を実装データに同期（Contact は `REPLACE_WITH_DEVELOPER_EMAIL`）
2. 掲載スクショ等は人手（チェックリスト手順を明確化）
3. シェア文末尾に Play ストア URL／chooser タイトルを LANG 対応
4. レリック「★ GET! ★」を `UiStrings.relicGetBanner` 化
5. release `minify` + `shrinkResources` 有効化
6. アイコン PNG 仕上げは人手（チェックリスト残）
7. 未使用 CONFIG ヒント文言を削除
8. `allowBackup=false`
9. DATA_SAFETY メモをポリシーと同期
10. 実機 Delight は人手（`DEVICE_CHECKLIST`）

### 触ったファイル
- `store/PRIVACY_POLICY.md`, `store/DATA_SAFETY.md`, `store/STORE_CHECKLIST.md`
- `ShareTextBuilder.kt`, `MainActivity.kt`, `UiStrings.kt`, `RelicUi.kt`
- `app/build.gradle.kts`, `proguard-rules.pro`, `AndroidManifest.xml`, `update.md`

---

