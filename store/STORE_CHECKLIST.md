# ストア公開チェックリスト（Play Console）

アプリ本体は遊べる状態。ここからは **Console 用の資産・申告・署名**。

## いまの優先順（人手）

1. Play Console でアプリ作成（`com.urikai.app`）
2. 電話スクショ撮影 → [`SCREENSHOTS.md`](SCREENSHOTS.md)
3. 掲載文・データセーフティ・レーティングを Console に転記（下書きは本フォルダ）
4. フィーチャーグラフィック投入 → [`feature_graphic_1024x500.png`](feature_graphic_1024x500.png)
5. release AAB を内部テストへ（`app/build/outputs/bundle/release/app-release.aab`・minify 再ビルド済み）→ 実機で [`../DEVICE_CHECKLIST.md`](../DEVICE_CHECKLIST.md)

## A. あなたが手元でやること（秘密情報）

- [x] `keystore.properties.example` を参考に `keystore.properties` を作成
- [x] upload 用キーを生成（例は example ファイル内）し、**パスワードを安全に保管**（紛失＝更新不可に近い）
- [x] `.\gradlew.bat :app:bundleRelease` で AAB を出力  
      → `app/build/outputs/bundle/release/app-release.aab`
- [ ] Play Console でアプリ作成（パッケージ `com.urikai.app`）
- [x] Privacy Policy を GitHub Pages で公開  
      URL: `https://noxel1452443-arch.github.io/URIKAI/`（Contact: `noxel.1452443@gmail.com`）  
      ※Play Console のプライバシー欄への貼り付けは人手
- [ ] 電話スクショ 2〜8枚（縦）を撮影 … 手順 [`SCREENSHOTS.md`](SCREENSHOTS.md)
- [x] フィーチャーグラフィック 1024×500 … [`feature_graphic_1024x500.png`](feature_graphic_1024x500.png)（必要なら差し替え可）

## B. Play Console 入力（文面下書きあり）

- [x] ストア掲載情報 JP/EN 下書き … `LISTING.md`（投資助言ではない旨あり）
- [x] データセーフティ下書き … `DATA_SAFETY.md`
- [x] コンテンツレーティング下書き … `CONTENT_RATING.md`
- [ ] 上記を Play Console に転記
- [ ] 対象年齢・カテゴリ: **ゲーム > カジュアル**（金融シミュではなくタイミングゲーム）
- [ ] 連絡先メールアドレス（`noxel.1452443@gmail.com` — ポリシーと揃える）
- [ ] プライバシーポリシー URL を Console に貼る

## C. アプリ側（リポジトリで済んだ／済むもの）

- [x] `applicationId` / `versionName 1.7.0` / `versionCode 3` / targetSdk 36
- [x] release 署名を `keystore.properties` から読む設定
- [x] LCD 風アイコン … ストア用 `store/ic_launcher_512.png`（512）／端末は adaptive＋mipmap
- [x] ネット権限・広告・解析 SDK なし（Data Safety が単純）
- [x] release minify / shrinkResources 有効（`proguard-rules.pro`）
- [x] `allowBackup=false`（バックアップ復元で免責スキップ防止）
- [ ] 実機で `DEVICE_CHECKLIST.md` 一通り
- [ ] 必要ならアイコンを Image Asset で PNG 仕上げに差し替え
- [x] release AAB を minify 有効後にもう一度ビルド（`app/build/outputs/bundle/release/app-release.aab`）
- [ ] 内部テストへアップロードして実機インストール

## D. 提出前の最終確認

- [ ] 内部テストトラックに AAB を上げて実機インストール
- [ ] 免責オーバーレイ（架空・ゲームである旨）が初回に出る
- [ ] About にフォント OFL / SE クレジットがある
- [x] 本番チャートが「金融アドバイスではない」ことがストア説明に書かれている（`LISTING.md`）
