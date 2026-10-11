# C++17 Drogon Implementation

## 技術選擇

- C++17
- Drogon HTTP framework
- Drogon PostgreSQL `DbClient`
- CMake
- raw parameterized SQL
- 不使用 ORM

Repository 直接使用 SQL 查詢 PostgreSQL；Drogon `DbClient` 負責非同步 database I/O，避免在 HTTP handler 中直接阻塞等待資料庫結果。

## Podman

```bash
podman build --no-cache \
  -f implementations/cpp-drogon/Containerfile \
  -t localhost/backend-api-cpp:latest \
  implementations/cpp-drogon
```

```bash
podman run --rm \
  --name backend-api-cpp \
  --env-file .env \
  -p 8080:8080 \
  localhost/backend-api-cpp:latest
```

## API 測試

```bash
curl "http://localhost:8080/jobs?page=1&page_size=30"
```
