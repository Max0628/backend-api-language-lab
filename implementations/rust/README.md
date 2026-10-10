# Rust Axum Implementation

## 技術選擇

- Rust
- Axum
- Tokio
- SQLx PostgreSQL driver 與 connection pool
- `tracing`

不使用 ORM；資料庫查詢會使用 SQLx 的 raw SQL 與 parameter binding。

## 第一階段

目前已完成 configuration、domain model、repository、service、handler 與基本 service tests。

Repository 使用 SQLx raw SQL 與 parameter binding，不使用 ORM；sort column 由 whitelist 控制，避免將 query parameter 直接拼入 SQL。

## 本地檢查

Mac 尚未安裝 Rust toolchain 時，可使用 Podman：

```bash
cd implementations/rust
podman build -f Containerfile -t backend-api-rust .
```

啟動 API：

```bash
cd ../..
podman run --rm --name backend-api-rust -p 8080:8080 backend-api-rust
```

Rust toolchain 可用後，常用檢查為：

```bash
cargo fmt --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test
```
