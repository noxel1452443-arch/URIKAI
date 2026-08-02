# URIKAI

60秒・1ボタンのタイミングゲーム。歴史チャートにラグを読んで BUY/SELL する、ローファイ液晶風アクション。

**投資シミュレーターではありません。** リザルトの主役は**最終資産**（SYNC% / ★ は参考）。

設計の正本: [`DESIGN.md`](DESIGN.md)　／　ビジョン: [`goal.md`](goal.md)　／　変更履歴: [`update.md`](update.md)

---

## 遊び方（要約）

1. 巨大ボタン1つ — 現金なら BUY、保有中なら SELL、注文中はタップ不可  
2. 注文には約1秒のラグ（レリックで短縮可）。一歩手前で押すスキルゲーム  
3. 売りには判定（GREAT〜MISS）。MISS は LIFE 減少、0で RETIRE  
4. 60秒終了 → 出金演出 → 結果。クリア銘柄は図鑑へ  

---

## リポジトリ構成

```
app/                 Android アプリ（Kotlin / Compose）
  src/main/assets/   実行時 charts.csv
  src/main/java/…/   MainActivity, game/, ui/, data/, audio/, feedback/
content/             チャート編集用 CSV・メタ（アプリは直接読まない）
tools/               charts.csv 生成・シミュレーション用 Python
docs/                GitHub Pages（プライバシーポリシー）
store/               Play 掲載文・提出チェック・ポリシー下書き
```

---

## ビルド

要件: Android SDK、JDK 17。

```powershell
cd C:\Users\reo\Documents\URIKAI
.\gradlew.bat :app:assembleDebug
```

Release / AAB は `keystore.properties`（example 参照）を置いてから:

```powershell
.\gradlew.bat :app:bundleRelease
```

---

## コンテンツ編集

1. `content/` の CSV / メタを編集  
2. `tools/generate_charts_csv.py` 等で `app/src/main/assets/charts.csv` を再生成  
3. 手順の詳細は [`content/README.md`](content/README.md)

---

## ストア提出

手順: [`store/STORE_CHECKLIST.md`](store/STORE_CHECKLIST.md)  
ポリシー公開: [`store/HOST_PRIVACY.md`](store/HOST_PRIVACY.md)（Pages: `docs/index.html`）  
実機確認: [`DEVICE_CHECKLIST.md`](DEVICE_CHECKLIST.md)

---

## 開発メモ

- パッケージ ID: `com.urikai.app`
- ネット権限・広告・解析 SDK なし
- AI／実装者向け: 仕様の意図と禁止事項は **`DESIGN.md` を優先**。古い議論の全文は持たない（履歴は `update.md`）
