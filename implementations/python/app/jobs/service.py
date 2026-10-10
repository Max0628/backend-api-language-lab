"""實作職缺查詢的參數驗證與 application flow。"""

from __future__ import annotations

from typing import Protocol

from .models import Job, ListJobsQuery


class InvalidParameterError(ValueError):
    """表示查詢參數不符合 API 規格。"""


class JobsRepository(Protocol):
    """定義職缺資料存取所需的最小介面。"""

    def list(self, query: ListJobsQuery) -> list[Job]:
        """依查詢條件回傳職缺列表。"""


class JobsService:
    """負責驗證查詢條件並呼叫 repository。"""

    def __init__(self, repository: JobsRepository) -> None:
        """建立職缺 service。"""
        self._repository = repository

    def list(self, query: ListJobsQuery) -> list[Job]:
        """驗證查詢條件並回傳職缺列表。"""
        validate_query(query)
        return self._repository.list(query)


_ALLOWED_SORT_BY = {
    "posted_at",
    "first_seen_at",
    "last_seen_at",
    "salary_min",
    "salary_max",
    "title",
    "company",
}


def validate_query(query: ListJobsQuery) -> None:
    """驗證分頁、排序與查詢參數。"""
    if query.page < 1:
        raise InvalidParameterError("page must be at least 1")
    if not 30 <= query.page_size <= 50:
        raise InvalidParameterError("page_size must be between 30 and 50")
    if query.sort_by not in _ALLOWED_SORT_BY:
        raise InvalidParameterError("invalid sort_by")
    if query.sort_order not in {"asc", "desc"}:
        raise InvalidParameterError("sort_order must be asc or desc")
