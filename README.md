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
- [`implementations/python`](implementations/python)：Python + FastAPI + psycopg
- [`implementations/rust`](implementations/rust)：Rust + Axum + SQLx
- [`implementations/typescript`](implementations/typescript)：TypeScript + Express + pg
- [`implementations/csharp`](implementations/csharp)：C# + ASP.NET Core + Npgsql
- [`implementations/kotlin`](implementations/kotlin)：Kotlin + Ktor + JDBC/HikariCP
- [`implementations/java17`](implementations/java17)：Java 17 + Spring MVC + JDBC
- [`implementations/cpp-drogon`](implementations/cpp-drogon)：C++17 + Drogon + PostgreSQL `DbClient`

## 環境設定

複製 `.env.example` 成 `.env`，再填入本機使用的設定。`.env` 不會提交到 Git。

每次只啟動一個 implementation，預設使用 `SERVER_PORT=8080`。
