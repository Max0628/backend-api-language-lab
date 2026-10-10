"""驗證 Python jobs service 與 HTTP handler 的主要行為。"""

from datetime import datetime, timezone

from fastapi.testclient import TestClient

from app.jobs.models import Job, ListJobsQuery, Location, Salary
from app.jobs.service import JobsService
from app.main import create_app


class FakeRepository:
    """提供測試用的 in-memory repository。"""

    def __init__(self, jobs=None, error=None):
        """建立 fake repository。"""
        self.jobs = jobs or []
        self.error = error
        self.query = None

    def list(self, query):
        """記錄 query 並回傳預設資料或錯誤。"""
        self.query = query
        if self.error:
            raise self.error
        return self.jobs


def sample_job() -> Job:
    """建立測試用職缺。"""
    return Job(
        source="example",
        source_job_id="job-1",
        title="Backend Engineer",
        company="Example Company",
        url="https://example.com/jobs/job-1",
        posted_at=datetime(2026, 10, 8, 8, tzinfo=timezone.utc),
        salary=Salary(80000, 120000, "TWD"),
        location=Location("Taipei", "Da'an"),
    )


def test_service_applies_defaults():
    """Service 應將空查詢套用預設分頁與排序。"""
    repository = FakeRepository()

    JobsService(repository).list(ListJobsQuery())

    assert repository.query.page == 1
    assert repository.query.page_size == 30
    assert repository.query.sort_by == "posted_at"
    assert repository.query.sort_order == "desc"


def test_handler_returns_json_list():
    """成功查詢應回傳直接 JSON list。"""
    repository = FakeRepository([sample_job()])
    app = create_app(repository=repository)

    with TestClient(app) as client:
        response = client.get("/jobs")

    assert response.status_code == 200
    assert response.json()[0]["source"] == "example"
    assert response.json()[0]["salary"]["min"] == 80000


def test_handler_rejects_invalid_page_size():
    """不合法 page_size 應回傳 400 與標準 error shape。"""
    app = create_app(repository=FakeRepository())

    with TestClient(app) as client:
        response = client.get("/jobs?page_size=20")

    assert response.status_code == 400
    assert response.json()["error"]["code"] == "INVALID_PARAMETER"


def test_handler_hides_internal_error():
    """Repository 錯誤不應暴露內部細節。"""
    app = create_app(repository=FakeRepository(error=RuntimeError("database unavailable")))

    with TestClient(app) as client:
        response = client.get("/jobs")

    assert response.status_code == 500
    assert response.json()["error"]["code"] == "INTERNAL_SERVER_ERROR"
    assert "database unavailable" not in response.text
