# Java 17 Spring MVC Implementation

## 技術選擇

- Java 17
- Spring Boot Spring MVC
- PostgreSQL JDBC Driver
- `NamedParameterJdbcTemplate`

不使用 JPA、Hibernate、Spring Data JPA 或其他 ORM；Repository 直接執行 parameterized SQL。

## Blocking I/O 設計

- Controller 使用 Spring MVC annotation，直接回傳同步結果。
- Repository 使用 `NamedParameterJdbcTemplate` 執行 SQL。
- Java 17 尚未提供 virtual thread；這一版刻意使用傳統 blocking JDBC，方便先理解標準 Spring MVC 流程。
- Connection pool 使用 Spring Boot 預設提供的 HikariCP。

## Podman

```bash
podman build --no-cache \
  -f implementations/java/Containerfile \
  -t localhost/backend-api-java:latest \
  implementations/java
```

```bash
podman run --rm \
  --name backend-api-java \
  --env-file .env \
  -p 8080:8080 \
  localhost/backend-api-java:latest
```
