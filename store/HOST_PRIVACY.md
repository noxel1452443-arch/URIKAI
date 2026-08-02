# プライバシーポリシーを GitHub Pages で公開する

Play Console には **HTTPS の公開URL** が必要です。  
このリポジトリの `docs/index.html` を GitHub Pages で公開します。

Contact: `noxel.1452443@gmail.com`

ローカルの初回コミットは済みです（`keystore` 秘密情報は含めていません）。

---

## 手順（ブラウザ中心・推奨）

### 1. GitHub で空のリポジトリを作る

1. ブラウザで [https://github.com/new](https://github.com/new) を開く（要ログイン）
2. Repository name: `URIKAI`
3. **Public** を選ぶ
4. 「Add a README」などのチェックは **すべて外す**（空のまま Create）
5. Create repository

### 2. パソコンから push する

PowerShell で:

```powershell
cd C:\Users\reo\Documents\URIKAI

# ★ 下の YOUR_GITHUB_USERNAME を自分の GitHub ユーザー名に書き換える
git remote add origin https://github.com/YOUR_GITHUB_USERNAME/URIKAI.git
git push -u origin main
```

ログインを求められたら GitHub のユーザー名／パスワード（または Personal Access Token）を入力します。  
（近年はパスワードの代わりに Token が必要なことがあります。[Token作成](https://github.com/settings/tokens)）

### 3. Pages を有効にする

1. リポジトリページ `https://github.com/YOUR_GITHUB_USERNAME/URIKAI` を開く
2. **Settings** → 左の **Pages**
3. Source: **Deploy from a branch**
4. Branch: **main** 、 folder: **/docs** → **Save**
5. 1〜2分待つ
6. 上部に出る URL をコピー:

   `https://YOUR_GITHUB_USERNAME.github.io/URIKAI/`

7. シークレットウィンドウで開き、ポリシーが表示されることを確認
8. [Play Console](https://play.google.com/console) → プライバシーポリシー欄にその URL を貼る

---

## （任意）GitHub CLI がある場合

```powershell
winget install --id GitHub.cli
gh auth login
gh repo create URIKAI --public --source=. --remote=origin --push
```

その後、上記「3. Pages を有効にする」と同じ。

---

## 文面を直すとき

1. `docs/index.html` を編集（必要なら `store/PRIVACY_POLICY.md` も）
2. `git add` → `git commit` → `git push`
3. 数十秒〜数分で Pages が更新されます
