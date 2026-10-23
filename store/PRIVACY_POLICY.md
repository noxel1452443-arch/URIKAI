# Privacy Policy — URIKAI

**Last updated:** 2026-10-23  
**Contact:** noxel.1452443@gmail.com  

公開 URL（GitHub Pages）: `https://noxel1452443-arch.github.io/URIKAI/`  
（ソース: `docs/index.html`）

---

## Japanese

### はじめに

URIKAI（以下「本アプリ」）は、60秒のタイミングゲームです。  
本ポリシーは、本アプリおよび Google Play 上の関連サービスにおけるデータの取り扱いを説明します。

### 端末内にだけ保存するデータ

本アプリは、次の情報を**端末内**（SharedPreferences 等）に保存します。これらはゲーム進行・設定のためのものであり、当社の独自サーバへ送信することを目的としません。

- 図鑑の解放状況・自己ベスト
- 口座残高・通算損益・出金回数・運用方針・カイの寝床などメタ進行
- レリック（報酬）の受取／適用状態
- 表示言語・HUD・液晶テーマ・音量・触覚などの設定
- チュートリアル・免責・広告除去フラグなどの既読／購入状態

アプリのアンインストール、または端末のアプリデータ削除で消去できます。

### 広告・課金（Google サービス）

本アプリは Google Play 経由で、次を提供する場合があります。

- **広告**（Google AdMob）— 例: プレイ終了後のインタースティシャル、任意のリワード広告
- **買い切り型のアプリ内課金**（Google Play の課金システム）— 例: 広告の非表示

これらの機能が有効なビルドでは、広告・課金の配信・不正防止・測定のために **Google の SDK がネットワーク通信を行い、デバイス情報や広告識別子などを Google のポリシーに従って処理**することがあります。詳細は次を参照してください。

- [Google プライバシーポリシー](https://policies.google.com/privacy)
- [AdMob ヘルプ（データの収集）](https://support.google.com/admob/answer/6128543)

広告除去を購入した場合、その購入状態は Google Play の購入情報および端末内フラグで管理されます。

開発・テスト段階では広告 SDK を接続していないビルドもあります。ストアに公開する本番ビルドの挙動は、接続後の本ポリシーに従います。

### 解析

クラッシュ報告や利用状況の把握のため、Firebase 等の解析ツールを段階的に導入する場合があります。導入時は本ポリシーを更新します。

### 権限

- **振動（VIBRATE）**: ゲーム内の触覚フィードバック用。設定でオフにできます。
- 広告・課金・解析を有効にしたビルドでは、それら SDK が必要とするネットワーク通信が発生します。

### 子どものプライバシー

本アプリは子どもからデータを収集することを目的としていません。また、子ども向けアプリとしての設計ではありません。

### 変更

本ポリシーを変更する場合は、このページの「Last updated」を更新します。

---

## English

### Introduction

URIKAI (“the App”) is a 60-second timing game. This policy describes how data is handled in the App and related Google Play services.

### Data stored only on device

The App stores the following **on the device** (e.g. SharedPreferences) for progress and settings. We do not operate a first-party server whose purpose is to collect this data:

- Scrapbook unlocks and personal bests
- Account balance, lifetime P/L, cash-out count, invest plan, Kai’s bed, and similar meta progress
- Relic claim / apply state
- Settings (language, HUD, LCD theme, audio, haptics, etc.)
- Tutorial / disclaimer / ads-removed flags

Uninstalling the App or clearing app data removes this information.

### Ads and in-app purchases (Google services)

The App may offer, via Google Play:

- **Ads** (Google AdMob) — e.g. interstitial after a run, optional rewarded ads
- **One-time in-app purchases** (Google Play Billing) — e.g. remove ads

When these features are enabled, Google’s SDKs may use network communication and process device information and advertising identifiers under Google’s policies. See:

- [Google Privacy Policy](https://policies.google.com/privacy)
- [AdMob help (data collection)](https://support.google.com/admob/answer/6128543)

If you purchase ad removal, that state is managed via Google Play purchase records and an on-device flag.

Some development builds may ship without ad SDKs. Production builds on Play follow this policy once connected.

### Analytics

We may gradually add crash / usage analytics (e.g. Firebase). This page will be updated when that happens.

### Permissions

- **VIBRATE**: optional in-game haptic feedback (can be disabled in settings).
- Builds with ads, billing, or analytics perform network communication required by those SDKs.

### Children’s privacy

We do not knowingly collect data from children. The App is not designed as a children’s product.

### Changes

We will update the “Last updated” date on this page when the policy changes.
