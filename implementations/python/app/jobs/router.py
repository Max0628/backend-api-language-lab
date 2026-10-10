"""定義職缺 API 的 FastAPI route 與 HTTP error mapping。"""

from __future__ import annotations

import logging
from typing import Optional

from fastapi import APIRouter, Query, Request
from fastapi.responses import JSONResponse

from .models import JobResponse, ListJobsQuery, to_response
from .service import InvalidParameterError, JobsService

logger = logging.getLogger(__name__)


def create_router(service: JobsService) -> APIRouter:
    """建立注入指定 service 的職缺 router。"""
    router = APIRouter()

    @router.get("/jobs", response_model=list[JobResponse])
    def list_jobs(
        request: Request,
        page: Optional[str] = Query(default=None),
        page_size: Optional[str] = Query(default=None),
        source: Optional[str] = Query(default=None),
        city: Optional[str] = Query(default=None),
        district: Optional[str] = Query(default=None),
        employment_type: Optional[str] = Query(default=None),
        job_type: Optional[str] = Query(default=None),
        title: Optional[str] = Query(default=None),
        include_closed: Optional[str] = Query(default=None),
        sort_by: Optional[str] = Query(default=None),
        sort_order: Optional[str] = Query(default=None),
    ) -> list[JobResponse]:
        """處理 GET /jobs 並回傳職缺 JSON list。"""
        try:
            query = parse_query(
                page,
                page_size,
                source,
                city,
                district,
                employment_type,
                job_type,
                title,
                include_closed,
                sort_by,
                sort_order,
            )
            jobs = service.list(query)
            return [to_response(job) for job in jobs]
        except InvalidParameterError as error:
            return JSONResponse(
                status_code=400,
                content={"error": {"code": "INVALID_PARAMETER", "message": str(error)}},
            )
        except Exception:
            logger.exception("list jobs failed", extra={"path": str(request.url.path)})
            return JSONResponse(
                status_code=500,
                content={
                    "error": {
                        "code": "INTERNAL_SERVER_ERROR",
                        "message": "failed to query jobs",
                    }
                },
            )

    return router


def parse_query(
    page: Optional[str],
    page_size: Optional[str],
    source: Optional[str],
    city: Optional[str],
    district: Optional[str],
    employment_type: Optional[str],
    job_type: Optional[str],
    title: Optional[str],
    include_closed: Optional[str],
    sort_by: Optional[str],
    sort_order: Optional[str],
) -> ListJobsQuery:
    """將 HTTP query string 解析成 domain query。"""
    return ListJobsQuery(
        page=parse_int(page, "page", 1),
        page_size=parse_int(page_size, "page_size", 30),
        source=source,
        city=city,
        district=district,
        employment_type=employment_type,
        job_type=job_type,
        title=title,
        include_closed=parse_bool(include_closed, "include_closed", False),
        sort_by=sort_by or "posted_at",
        sort_order=sort_order or "desc",
    )


def parse_int(value: Optional[str], name: str, default: int) -> int:
    """解析 optional integer query parameter。"""
    if value is None or value == "":
        return default
    try:
        return int(value)
    except ValueError as error:
        raise InvalidParameterError(f"{name} must be an integer") from error


def parse_bool(value: Optional[str], name: str, default: bool) -> bool:
    """解析 optional boolean query parameter。"""
    if value is None or value == "":
        return default
    if value.lower() in {"true", "1"}:
        return True
    if value.lower() in {"false", "0"}:
        return False
    raise InvalidParameterError(f"{name} must be a boolean")
