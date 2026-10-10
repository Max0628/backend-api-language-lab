# Jobs API Specification

## 1. API 概述

提供已 normalized 職缺資料的唯讀查詢 API。

- Base Path：`/jobs`
- HTTP Method：`GET`
- 資料來源：PostgreSQL `jobs` table
- API 不提供新增、修改或刪除功能

## 2. Endpoint

```http
GET /jobs
```

## 3. Query Parameters

| Parameter | Type | Required | Default | 說明 |
|---|---|---:|---|---|
| `page` | integer | No | `1` | 頁碼，從 `1` 開始。 |
| `page_size` | integer | No | `30` | 每頁筆數，允許範圍為 `30`–`50`。 |
| `source` | string | No | - | 依職缺來源篩選。 |
| `city` | string | No | - | 依城市或縣市篩選。 |
| `district` | string | No | - | 依區或鄉鎮市篩選。 |
| `employment_type` | string | No | - | 依 employment type 篩選。 |
| `job_type` | string | No | - | 依 job type 篩選。 |
| `title` | string | No | - | 依職缺標題進行 full-text search。 |
| `include_closed` | boolean | No | `false` | 是否包含 `CLOSED` 狀態的職缺。 |
| `sort_by` | string | No | `posted_at` | 排序欄位。 |
| `sort_order` | string | No | `desc` | 排序方向，可使用 `asc` 或 `desc`。 |

### 3.1 分頁規則

- `page` 必須是大於或等於 `1` 的整數。
- `page_size` 必須介於 `30` 與 `50`，包含邊界值。
- 未提供 `page` 時使用 `1`。
- 未提供 `page_size` 時使用 `30`。
- 查詢使用 offset pagination：

```text
offset = (page - 1) * page_size
```

### 3.2 狀態規則

- `include_closed=false` 時，排除 `status = 'CLOSED'` 的資料。
- `include_closed=true` 時，不限制 `status`，回傳所有狀態的資料。

### 3.3 排序規則

允許的 `sort_by` 欄位：

- `posted_at`
- `first_seen_at`
- `last_seen_at`
- `salary_min`
- `salary_max`
- `title`
- `company`

預設排序為：

```sql
ORDER BY posted_at DESC NULLS LAST, id DESC
```

`id` 是固定的次要排序欄位，用來確保相同排序值時仍有穩定結果。

### 3.4 篩選規則

- `source`、`city`、`district`、`employment_type`、`job_type` 使用 exact match。
- `title` 使用 PostgreSQL full-text search。
- 多個篩選條件同時存在時，使用 `AND` 串接。
- 未提供的篩選條件不加入查詢條件。

## 4. Response

### 4.1 成功回應

HTTP Status：`200 OK`

Response body 直接回傳 JSON list，不另外包裝 `data` 或 `pagination` 欄位。

Response 僅回傳以下 8 個 top-level 欄位：

- `source`
- `source_job_id`
- `title`
- `company`
- `url`
- `posted_at`
- `salary`
- `location`

`salary` 與 `location` 為 response object，用來整理相關欄位。

```json
[
  {
    "source": "example",
    "source_job_id": "job-123",
    "title": "Backend Engineer",
    "company": "Example Company",
    "url": "https://example.com/jobs/job-123",
    "posted_at": "2026-10-08T08:00:00Z",
    "salary": {
      "min": 80000,
      "max": 120000,
      "currency": "TWD"
    },
    "location": {
      "city": "Taipei",
      "district": "Da’an"
    }
  }
]
```

Database `NULL` 欄位以 JSON `null` 回傳。`attrs`、`id`、`content_hash`、`status`、`first_seen_at`、`last_seen_at` 及其他未列出的 database 欄位不回傳。

查無資料時回傳空 list：

```json
[]
```

## 5. Error Response

錯誤回應格式：

```json
{
  "error": {
    "code": "INVALID_PARAMETER",
    "message": "page_size must be between 30 and 50"
  }
}
```

### 5.1 HTTP Status

| Status | Code | 使用情境 |
|---:|---|---|
| `400` | `INVALID_PARAMETER` | Query parameter 格式錯誤或超出允許範圍。 |
| `500` | `INTERNAL_SERVER_ERROR` | Database 或其他未預期的系統錯誤。 |

內部 SQL、Database connection 或 stack trace 不得回傳給 API client。

## 6. Request Examples

### 查詢預設資料

```http
GET /jobs
```

### 查詢第 2 頁，每頁 50 筆

```http
GET /jobs?page=2&page_size=50
```

### 篩選職缺

```http
GET /jobs?source=linkedin&city=Taipei&employment_type=FULL_TIME
```

### 搜尋標題並包含 CLOSED

```http
GET /jobs?title=backend&include_closed=true
```

### 依最低薪資由高到低排序

```http
GET /jobs?sort_by=salary_min&sort_order=desc
```
