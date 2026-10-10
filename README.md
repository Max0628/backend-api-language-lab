# Backend API Language Lab

同一份 PostgreSQL `jobs` API，使用不同語言與 Web framework 實作，藉此比較語法、架構、錯誤處理與 concurrency model。

## 共通規格

- API Specification：[`docs/api-spec.md`](docs/api-spec.md)
- Database Schema：既有 PostgreSQL `jobs` table schema
- API：`GET /jobs`
- Database：既有 PostgreSQL `jobs` table
- 所有 implementation 都不使用 ORM，直接使用 PostgreSQL driver 與 parameterized SQL。

## Implementations

- [`implementations/go`](implementations/go)：Go + Gin + `database/sql` + pgx

後續會加入 C#、C++、Rust、TypeScript、Python 與 Kotlin implementation。

## 環境設定

複製 `.env.example` 成 `.env`，再填入本機使用的設定。`.env` 不會提交到 Git。

每次只啟動一個 implementation，預設使用 `SERVER_PORT=8080`。
