# Kotlin Ktor Implementation

## 技術選擇

- Kotlin
- Ktor
- Netty
- Kotlinx Serialization
- JDBC / HikariCP

不使用 ORM；資料庫會使用 JDBC parameterized SQL。

## 第一階段

目前已完成 configuration、data class DTO、service、repository、JDBC connection pool、error handling 與基本 tests。

Ktor 的 request handler 會使用 `suspend` function；資料庫 blocking JDBC 工作會放到適當的 IO dispatcher，避免阻塞 Ktor event loop。

## Podman

從 repo 根目錄執行：

```bash
podman build \
  -f implementations/kotlin/Containerfile \
  -t localhost/backend-api-kotlin:latest \
  implementations/kotlin
```

啟動：

```bash
podman run --rm \
  --name backend-api-kotlin \
  --env-file .env \
  -p 8080:8080 \
  localhost/backend-api-kotlin:latest
```

## API 測試

```bash
curl "http://localhost:8080/jobs?page=1&page_size=30"
```
