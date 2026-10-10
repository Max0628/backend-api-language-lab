"""使用 psycopg 存取 PostgreSQL jobs table。"""

from __future__ import annotations

from datetime import datetime
from typing import Any

from psycopg import sql
from psycopg.rows import dict_row
from psycopg_pool import ConnectionPool

from .models import Job, ListJobsQuery, Location, Salary


class PostgresJobsRepository:
    """使用 PostgreSQL connection pool 查詢職缺。"""

    def __init__(self, pool: ConnectionPool) -> None:
        """建立 PostgreSQL repository。"""
        self._pool = pool

    def list(self, query: ListJobsQuery) -> list[Job]:
        """使用 parameterized SQL 查詢職缺。"""
        statement, parameters = build_query(query)
        with self._pool.connection() as connection:
            with connection.cursor(row_factory=dict_row) as cursor:
                cursor.execute(statement, parameters)
                rows = cursor.fetchall()
        return [row_to_job(row) for row in rows]


def build_query(query: ListJobsQuery) -> tuple[sql.Composed, list[Any]]:
    """建立含 whitelist sort column 的 parameterized SQL。"""
    statement = sql.SQL(
        "SELECT source, source_job_id, title, company, url, "
        "posted_at, salary_min, salary_max, salary_currency, city, district "
        "FROM jobs WHERE TRUE"
    )
    parameters: list[Any] = []

    if not query.include_closed:
        statement += sql.SQL(" AND status <> 'CLOSED'")
    for column, value in (
        ("source", query.source),
        ("city", query.city),
        ("district", query.district),
        ("employment_type", query.employment_type),
        ("job_type", query.job_type),
    ):
        if value:
            statement += sql.SQL(" AND {} = %s").format(sql.Identifier(column))
            parameters.append(value)
    if query.title:
        statement += sql.SQL(
            " AND to_tsvector('english', title) "
            "@@ plainto_tsquery('english', %s)"
        )
        parameters.append(query.title)

    direction = sql.SQL({"asc": "ASC", "desc": "DESC"}[query.sort_order])
    statement += sql.SQL(" ORDER BY {} {} ").format(
        sql.Identifier(query.sort_by), direction
    )
    if query.sort_by == "posted_at":
        statement += sql.SQL("NULLS LAST ")
    statement += sql.SQL(", id {} LIMIT %s OFFSET %s").format(direction)
    parameters.extend([query.page_size, (query.page - 1) * query.page_size])
    return statement, parameters


def row_to_job(row: dict[str, Any]) -> Job:
    """將 database row 轉換成 domain model。"""
    return Job(
        source=row["source"],
        source_job_id=row["source_job_id"],
        title=row["title"],
        company=row["company"],
        url=row["url"],
        posted_at=row["posted_at"],
        salary=Salary(
            minimum=row["salary_min"],
            maximum=row["salary_max"],
            currency=row["salary_currency"],
        ),
        location=Location(city=row["city"], district=row["district"]),
    )
