# Nutrition Tracker

栄養管理アプリケーション - Spring Boot + Thymeleaf + Firebase認証

## 機能

- 食事記録の登録・編集・削除
- 栄養素（カロリー、タンパク質、脂質、炭水化物）の自動計算
- 日別・週別の栄養摂取量グラフ
- 食品マスタ管理
- ミールセット（よく使う食事の組み合わせ）
- Firebase認証によるユーザー管理

## 技術スタック

- Java 21
- Spring Boot 3.5.0
- Thymeleaf
- H2 Database（開発）/ PostgreSQL（本番）
- Firebase Authentication
- Chart.js

## セットアップ

### 1. リポジトリのクローン

```bash
git clone https://github.com/YOUR_USERNAME/nutrition-tracker.git
cd nutrition-tracker
```

### 2. Firebase設定（認証を使用する場合）

1. [Firebase Console](https://console.firebase.google.com/)でプロジェクトを作成
2. Authentication > Sign-in method で「メール/パスワード」を有効化
3. プロジェクト設定 > サービスアカウント > 新しい秘密鍵を生成
4. ダウンロードしたJSONファイルを `firebase-credentials.json` としてプロジェクトルートに配置
5. `src/main/resources/static/js/firebase-config.js.example` を `firebase-config.js` にコピーし、Firebase設定値を入力

### 3. 環境変数の設定

| 変数名 | 説明 | デフォルト |
|--------|------|------------|
| `PORT` | サーバーポート | 8080 |
| `FIREBASE_ENABLED` | Firebase認証の有効/無効 | false |
| `FIREBASE_CONFIG_PATH` | Firebase認証情報JSONのパス | firebase-credentials.json |

### 4. アプリケーションの起動

```bash
# 開発モード（Firebase認証なし）
./gradlew bootRun

# Firebase認証有効
FIREBASE_ENABLED=true FIREBASE_CONFIG_PATH=./firebase-credentials.json ./gradlew bootRun
```

### 5. アクセス

- アプリケーション: http://localhost:8080
- H2コンソール（開発用）: http://localhost:8080/h2-console

## 本番環境へのデプロイ

### Render.com

1. Renderでプロジェクトをインポート
2. 環境変数を設定:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `DATABASE_URL`（PostgreSQL接続URL）
   - `FIREBASE_ENABLED=true`
   - Firebase認証情報はSecretsまたはファイルとして設定

## ライセンス / License

このリポジトリに含まれるすべてのコード・資料は著作権により保護されています。
著作権者の明示的な許可なく、以下の行為を一切禁止します：

- 複製
- 改変
- 再配布
- 商用利用
- 他プロジェクトへの組み込み

本リポジトリの内容は「閲覧のみ」を目的として公開されています。
この条件に同意しない場合は、閲覧を中止してください。

---

All content in this repository is protected by copyright.
The following actions are strictly prohibited without explicit permission from the copyright holder:

- Copying
- Modification
- Redistribution
- Commercial use
- Integration into other projects

This repository is made available for viewing purposes only.
If you do not agree to these terms, please refrain from viewing the contents.
