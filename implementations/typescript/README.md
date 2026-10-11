# TypeScript Express Implementation

## 技術選擇

- TypeScript
- Node.js
- Express
- `pg` PostgreSQL driver

不使用 ORM；資料庫會使用 `pg` 的 parameterized SQL。

## 第一階段

目前已完成 strict TypeScript、configuration、domain model、repository、service、handler、tests 與 multi-stage Containerfile。

Repository 使用 `pg` raw SQL 與 parameter binding；排序欄位使用 whitelist，response DTO 與 domain model 分離。

## 本地檢查

```bash
cd implementations/typescript
npm install
npm run typecheck
npm run build
```

## Podman

第一次 build 前，先用 Node container 產生 lockfile，避免在 Mac 安裝 dependencies：

```bash
podman run --rm \
  -v "$PWD/implementations/typescript:/app" \
  -w /app \
  node:20-bookworm-slim \
  npm install --package-lock-only --ignore-scripts
```

之後從 repo 根目錄執行：

```bash
podman build -f implementations/typescript/Containerfile -t backend-api-typescript implementations/typescript
podman run --rm --name backend-api-typescript --env-file .env -p 8080:8080 backend-api-typescript
```

## API 測試

```bash
curl "http://localhost:8080/jobs?page=1&page_size=30"
```
