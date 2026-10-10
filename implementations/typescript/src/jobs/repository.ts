/** 使用 pg 執行 jobs table 的 parameterized SQL。 */
import type { Pool, QueryResultRow } from "pg";

import type { Job, ListJobsQuery } from "./models";

export interface JobsRepository {
  /** 依 validated query 查詢職缺。 */
  list(query: ListJobsQuery): Promise<Job[]>;
}

interface JobRow extends QueryResultRow {
  source: string;
  source_job_id: string;
  title: string;
  company: string | null;
  url: string;
  posted_at: Date | null;
  salary_min: number | string | null;
  salary_max: number | string | null;
  salary_currency: string | null;
  city: string | null;
  district: string | null;
}

/** PostgreSQL jobs repository。 */
export class PostgresJobsRepository implements JobsRepository {
  public constructor(private readonly pool: Pool) {}

  /** 執行 raw SQL 並將 row mapping 成 domain model。 */
  public async list(query: ListJobsQuery): Promise<Job[]> {
    const { text, values } = buildJobsQuery(query);
    const result = await this.pool.query<JobRow>(text, values);
    return result.rows.map(toJob);
  }
}

interface SqlQuery {
  text: string;
  values: unknown[];
}

/** 建立含 parameter binding 與安全排序 whitelist 的 SQL。 */
export function buildJobsQuery(query: ListJobsQuery): SqlQuery {
  const conditions = ["TRUE"];
  const values: unknown[] = [];

  if (!query.includeClosed) {
    conditions.push("status <> 'CLOSED'");
  }
  addExactFilter(conditions, values, "source", query.source);
  addExactFilter(conditions, values, "city", query.city);
  addExactFilter(conditions, values, "district", query.district);
  addExactFilter(conditions, values, "employment_type", query.employmentType);
  addExactFilter(conditions, values, "job_type", query.jobType);

  if (query.title) {
    values.push(query.title);
    conditions.push(
      `to_tsvector('english', title) @@ plainto_tsquery('english', $${values.length})`,
    );
  }

  const direction = query.sortOrder === "asc" ? "ASC" : "DESC";
  const nulls = query.sortBy === "posted_at" ? " NULLS LAST" : "";
  const offset = (query.page - 1) * query.pageSize;

  values.push(query.pageSize, offset);
  const limitParameter = values.length - 1;
  const offsetParameter = values.length;

  return {
    text: [
      "SELECT source, source_job_id, title, company, url,",
      "posted_at, salary_min, salary_max, salary_currency, city, district",
      "FROM jobs",
      `WHERE ${conditions.join(" AND ")}`,
      `ORDER BY ${sortColumn(query.sortBy)} ${direction}${nulls}, id ${direction}`,
      `LIMIT $${limitParameter} OFFSET $${offsetParameter}`,
    ].join(" "),
    values,
  };
}

function addExactFilter(
  conditions: string[],
  values: unknown[],
  column: string,
  value: string | undefined,
): void {
  if (!value) {
    return;
  }
  values.push(value);
  conditions.push(`${column} = $${values.length}`);
}

function sortColumn(value: string): string {
  const columns: Record<string, string> = {
    posted_at: "posted_at",
    first_seen_at: "first_seen_at",
    last_seen_at: "last_seen_at",
    salary_min: "salary_min",
    salary_max: "salary_max",
    title: "title",
    company: "company",
  };
  return columns[value] ?? "posted_at";
}

function toJob(row: JobRow): Job {
  return {
    source: row.source,
    sourceJobId: row.source_job_id,
    title: row.title,
    company: row.company,
    url: row.url,
    postedAt: row.posted_at,
    salary: {
      minimum: toNumber(row.salary_min),
      maximum: toNumber(row.salary_max),
      currency: row.salary_currency,
    },
    location: { city: row.city, district: row.district },
  };
}

function toNumber(value: number | string | null): number | null {
  if (value === null) {
    return null;
  }
  const parsed = typeof value === "number" ? value : Number(value);
  if (!Number.isSafeInteger(parsed)) {
    throw new Error("salary value is outside the safe integer range");
  }
  return parsed;
}
