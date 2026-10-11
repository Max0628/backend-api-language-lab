# C# ASP.NET Core Implementation

## 技術選擇

- C# / .NET 8
- ASP.NET Core Web API Controllers
- Npgsql PostgreSQL driver

不使用 Entity Framework Core 或其他 ORM；資料庫存取會使用 Npgsql 的 parameterized SQL。

## 第一階段

目前已完成 nullable reference types、configuration、DTO、Controller、service、repository、error handling 與 Service unit tests。

Repository 使用 Npgsql raw SQL 與 parameterized query；沒有使用 Entity Framework Core 或其他 ORM。

## Podman

從 repo 根目錄執行：

```bash
podman build \
  -f implementations/csharp/Containerfile \
  -t backend-api-csharp \
  implementations/csharp
```

啟動：

```bash
podman run --rm \
  --name backend-api-csharp \
  --env-file .env \
  -p 8080:8080 \
  backend-api-csharp
```

## API 測試

```bash
curl "http://localhost:8080/jobs?page=1&page_size=30"
```
