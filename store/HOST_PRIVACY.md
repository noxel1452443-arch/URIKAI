# プライバシーポリシーを GitHub Pages で公開する

Play Console には **HTTPS の公開URL** が必要です。このリポジトリでは `docs/index.html` を Pages で公開します。

Contact: `noxel.1452443@gmail.com`

---

## まだ GitHub リポジトリがない場合（初回）

ターミナル（PowerShell）でプロジェクトフォルダに移動してから:

```powershell
cd C:\Users\reo\Documents\URIKAI

# 初回コミット（keystore.properties は .gitignore 済み）
git add -A
git status
git commit -m "Initial commit: URIKAI app and privacy policy page"

# GitHub にリポジトリ作成して push（要: gh ログイン済み）
gh auth status
gh repo create URIKAI --public --source=. --remote=origin --push
```

`gh` が未ログインなら:

```powershell
gh auth login
```

ブラウザの指示に従って GitHub アカウントで認証してください。

---

## GitHub Pages を有効にする

1. ブラウザでリポジトリを開く（例: `https://github.com/<あなたのID>/URIKAI`）
2. **Settings** → 左メニュー **Pages**
3. **Build and deployment** → Source: **Deploy from a branch**
4. Branch: **main** 、 folder: **/docs** → **Save**
5. 1〜2分待つ
6. 同じ Pages 画面の上部に公開URLが出る:

   `https://<あなたのID>.github.io/URIKAI/`

7. シークレットウィンドウでそのURLを開き、ポリシーが表示されることを確認
8. [Play Console](https://play.google.com/console) → アプリ → **プライバシーポリシー** にそのURLを貼って保存

---

## あとから文面を直すとき

1. `docs/index.html`（と必要なら `store/PRIVACY_POLICY.md`）を編集
2. `git add` → `commit` → `git push`
3. 数十秒〜数分で Pages が更新される

---

## 別案: Notion

GitHub が難しい場合は Notion 公開でも可（以前の手順）。ただしこのプロジェクトは GitHub Pages を推奨。
