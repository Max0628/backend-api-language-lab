# Python FastAPI Implementation

## 技術選擇

- Python
- FastAPI
- Pydantic
- Uvicorn
- psycopg 3
- `pytest`

不使用 ORM、SQLAlchemy 或 migration tool，資料庫存取會使用 psycopg 直接執行 parameterized SQL。

## Concurrency Model

HTTP 使用 FastAPI/ASGI，SQL 查詢使用同步 `psycopg` connection pool；同步 route 會由 FastAPI 放到 thread pool 執行，不會阻塞 ASGI event loop。

這個選擇讓目前的同步 SQL 程式碼容易閱讀，也方便和 Go 的 `database/sql` 版本比較。CPU-bound 工作不應直接放在 request handler 中。

## 開發環境

```bash
cd implementations/python
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
python -m pip install -e '.[test]'
```

## 檢查與測試

```bash
cd implementations/python
pytest
```

目前已完成 `/jobs` route、Repository、Service、Response DTO 與 PostgreSQL connection pool。

## 啟動 API

在專案根目錄準備好 `.env` 後：

```bash
cd implementations/python
source .venv/bin/activate
uvicorn app.main:app --host 0.0.0.0 --port 8080
```

實作分層如下：

- `app/jobs/router.py`：解析 HTTP query、呼叫 service、處理 API error。
- `app/jobs/service.py`：驗證分頁與排序條件。
- `app/jobs/repository.py`：使用 psycopg 執行 parameterized SQL。
- `app/jobs/models.py`：domain model 與 Pydantic response DTO。
- `app/main.py`：建立 FastAPI app 與 PostgreSQL connection pool。

## Podman

在 repo 根目錄執行：

```bash
podman build -f implementations/python/Containerfile -t backend-api-python implementations/python
podman run --rm --name backend-api-python --env-file .env -p 8080:8080 backend-api-python
```

## API 測試

```bash
curl "http://localhost:8080/jobs?page=1&page_size=30"
```
