# Go Implementation

## 技術選擇

- Go
- Gin
- `database/sql`
- pgx PostgreSQL driver
- `log/slog`
- Standard Library `testing` 與 `net/http/httptest`

不使用 ORM、migration tool 或 Dependency Injection framework。

## 架構

```text
Handler → Service → Repository
```

- `Handler`：HTTP query parsing、status code、response DTO 與 error mapping。
- `Service`：查詢條件驗證與 application flow。
- `Repository`：SQL、`database/sql`、nullable mapping 與 PostgreSQL 存取。
- `Domain Model`：不依賴 Gin、JSON 或 `database/sql`。
- `Response DTO`：定義 API JSON response。

## Concurrency Model

Go 使用 goroutine 處理 concurrent request，並透過 `context.Context` 傳遞 request cancellation 與 database timeout。

`database/sql` 的 `*sql.DB` 負責管理 connection pool；Service 與 Repository 不依賴 Gin Context。

## 啟動

在 repository root 執行：

```bash
set -a
source .env
set +a

cd implementations/go
go run ./cmd/server
```

## 檢查與測試

```bash
cd implementations/go
gofmt -w $(find . -name '*.go' -type f)
go vet ./...
go test ./...
```
