"""定義職缺 domain model、查詢條件與 API response DTO。"""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import Optional

from pydantic import BaseModel


@dataclass(frozen=True)
class Salary:
    """表示職缺薪資資訊。"""

    minimum: Optional[int]
    maximum: Optional[int]
    currency: Optional[str]


@dataclass(frozen=True)
class Location:
    """表示職缺地點資訊。"""

    city: Optional[str]
    district: Optional[str]


@dataclass(frozen=True)
class Job:
    """表示不含 HTTP 與 database 細節的職缺 domain model。"""

    source: str
    source_job_id: str
    title: str
    company: Optional[str]
    url: str
    posted_at: Optional[datetime]
    salary: Salary
    location: Location


@dataclass
class ListJobsQuery:
    """表示職缺列表查詢條件。"""

    page: int = 1
    page_size: int = 30
    source: Optional[str] = None
    city: Optional[str] = None
    district: Optional[str] = None
    employment_type: Optional[str] = None
    job_type: Optional[str] = None
    title: Optional[str] = None
    include_closed: bool = False
    sort_by: str = "posted_at"
    sort_order: str = "desc"


class SalaryResponse(BaseModel):
    """表示 API response 的薪資 DTO。"""

    min: Optional[int]
    max: Optional[int]
    currency: Optional[str]


class LocationResponse(BaseModel):
    """表示 API response 的地點 DTO。"""

    city: Optional[str]
    district: Optional[str]


class JobResponse(BaseModel):
    """表示 GET /jobs 的 API response DTO。"""

    source: str
    source_job_id: str
    title: str
    company: Optional[str]
    url: str
    posted_at: Optional[datetime]
    salary: SalaryResponse
    location: LocationResponse


def to_response(job: Job) -> JobResponse:
    """將 domain model 轉成 API response DTO。"""
    return JobResponse(
        source=job.source,
        source_job_id=job.source_job_id,
        title=job.title,
        company=job.company,
        url=job.url,
        posted_at=job.posted_at,
        salary=SalaryResponse(
            min=job.salary.minimum,
            max=job.salary.maximum,
            currency=job.salary.currency,
        ),
        location=LocationResponse(
            city=job.location.city,
            district=job.location.district,
        ),
    )
