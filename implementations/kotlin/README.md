# Kotlin Ktor Implementation

## 技術選擇

- Kotlin
- Ktor
- Netty
- Kotlinx Serialization
- JDBC / HikariCP（後續加入）

不使用 ORM；資料庫會使用 JDBC parameterized SQL。

## 第一階段

目前已建立 Kotlin JVM、Ktor Netty server、Gradle build 與 container flow。後續會加入 configuration、data class DTO、service、repository、JDBC connection pool、error handling 與 tests。

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
