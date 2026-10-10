"""FastAPI application entrypoint 與 dependency wiring。"""

from __future__ import annotations

from contextlib import asynccontextmanager
from typing import Optional

from fastapi import FastAPI
from psycopg_pool import ConnectionPool

from .config import Settings, load_settings
from .jobs.repository import PostgresJobsRepository
from .jobs.router import create_router
from .jobs.service import JobsRepository, JobsService


def create_app(
    settings: Optional[Settings] = None,
    repository: Optional[JobsRepository] = None,
) -> FastAPI:
    """建立 FastAPI application，支援測試時注入 fake repository。"""
    pool: Optional[ConnectionPool] = None

    @asynccontextmanager
    async def lifespan(app: FastAPI):
        """管理 PostgreSQL connection pool 的生命週期。"""
        nonlocal pool
        if repository is None:
            app_settings = settings or load_settings()
            pool = ConnectionPool(
                conninfo=app_settings.database_url,
                min_size=1,
                max_size=10,
                open=True,
            )
            pool.wait()
            app.state.jobs_repository = PostgresJobsRepository(pool)
        try:
            yield
        finally:
            if pool is not None:
                pool.close()

    app = FastAPI(
        title="Backend API Language Lab - Python",
        version="0.1.0",
        lifespan=lifespan,
    )
    app.include_router(
        create_router(JobsService(repository or _state_repository(app)))
    )
    return app


def _state_repository(app: FastAPI) -> JobsRepository:
    """建立延遲從 application state 取得 repository 的 proxy。"""

    class StateRepository:
        """將查詢轉送至 lifespan 建立的 PostgreSQL repository。"""

        def list(self, query):
            """呼叫 application state 中的 repository。"""
            return app.state.jobs_repository.list(query)

    return StateRepository()


app = create_app()
