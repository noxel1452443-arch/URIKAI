# ストア公開チェックリスト（Play Console）

アプリ本体は遊べる状態。ここからは **Console 用の資産・申告・署名**。

## A. あなたが手元でやること（秘密情報）

- [x] `keystore.properties.example` を参考に `keystore.properties` を作成
- [x] upload 用キーを生成（例は example ファイル内）し、**パスワードを安全に保管**（紛失＝更新不可に近い）
- [x] `.\gradlew.bat :app:bundleRelease` で AAB を出力  
      → `app/build/outputs/bundle/release/app-release.aab`
- [ ] Play Console でアプリ作成（パッケージ `com.urikai.app`）
- [ ] Privacy Policy を GitHub Pages で公開（手順: [`HOST_PRIVACY.md`](HOST_PRIVACY.md)）  
      ページ本体: `docs/index.html` / Contact: `noxel.1452443@gmail.com`  
      公開後URL例: `https://<GitHubユーザー名>.github.io/URIKAI/`
- [ ] 電話スクショ 2〜8枚（縦）を撮影（START / プレイ中 / リザルト / 図鑑 / CONFIG）
- [ ] フィーチャーグラフィック 1024×500 を用意（任意だが推奨）

## B. Play Console 入力（文面下書きあり）

- [ ] ストア掲載情報 JP/EN … `store/LISTING.md`
- [ ] データセーフティ … `store/DATA_SAFETY.md`
- [ ] コンテンツレーティング質問 … `store/CONTENT_RATING.md`
- [ ] 対象年齢・カテゴリ: **ゲーム > カジュアル**（金融シミュではなくタイミングゲーム）
- [ ] 連絡先メールアドレス（`noxel.1452443@gmail.com` — ポリシーと揃える）

## C. アプリ側（リポジトリで済んだ／済むもの）

- [x] `applicationId` / `versionName 1.6.0` / `versionCode 1` / targetSdk 35
- [x] release 署名を `keystore.properties` から読む設定
- [x] LCD 風 adaptive アイコン（要実機で最終確認）
- [x] ネット権限・広告・解析 SDK なし（Data Safety が単純）
- [x] release minify / shrinkResources 有効（`proguard-rules.pro`）
- [x] `allowBackup=false`（バックアップ復元で免責スキップ防止）
- [ ] 実機で `DEVICE_CHECKLIST.md` 一通り
- [ ] 必要ならアイコンを Image Asset で PNG 仕上げに差し替え
- [ ] release AAB を minify 有効後にもう一度ビルドして内部テスト

## D. 提出前の最終確認

- [ ] 内部テストトラックに AAB を上げて実機インストール
- [ ] 免責オーバーレイ（史実・ゲームである旨）が初回に出る
- [ ] About にフォント OFL / SE クレジットがある
- [ ] 本番チャートが「金融アドバイスではない」ことがストア説明に書かれている
