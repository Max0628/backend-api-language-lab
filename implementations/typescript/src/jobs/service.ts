/** 實作 jobs query validation 與 application flow。 */
import { AppError } from "../errors";
import type {
  Job,
  ListJobsQuery,
  RawListJobsQuery,
  SortOrder,
} from "./models";
import type { JobsRepository } from "./repository";

const allowedSortColumns = new Set([
  "posted_at",
  "first_seen_at",
  "last_seen_at",
  "salary_min",
  "salary_max",
  "title",
  "company",
]);

/** 負責查詢條件驗證並呼叫 repository。 */
export class JobsService {
  public constructor(private readonly repository: JobsRepository) {}

  /** 驗證 HTTP query 並查詢職缺。 */
  public async list(rawQuery: RawListJobsQuery): Promise<Job[]> {
    const query = parseQuery(rawQuery);
    return this.repository.list(query);
  }
}

/** 將 raw query 轉換成 validated query。 */
export function parseQuery(rawQuery: RawListJobsQuery): ListJobsQuery {
  const page = parseInteger(rawQuery.page, "page", 1);
  const pageSize = parseInteger(rawQuery.pageSize, "page_size", 30);
  if (page < 1) {
    throw AppError.invalidParameter("page must be at least 1");
  }
  if (pageSize < 30 || pageSize > 50) {
    throw AppError.invalidParameter("page_size must be between 30 and 50");
  }

  const sortBy = rawQuery.sortBy ?? "posted_at";
  if (!allowedSortColumns.has(sortBy)) {
    throw AppError.invalidParameter("invalid sort_by");
  }

  const sortOrder = parseSortOrder(rawQuery.sortOrder ?? "desc");
  return {
    page,
    pageSize,
    source: rawQuery.source,
    city: rawQuery.city,
    district: rawQuery.district,
    employmentType: rawQuery.employmentType,
    jobType: rawQuery.jobType,
    title: rawQuery.title,
    includeClosed: parseBoolean(rawQuery.includeClosed, "include_closed", false),
    sortBy,
    sortOrder,
  };
}

function parseInteger(value: string | undefined, name: string, defaultValue: number): number {
  if (value === undefined || value === "") {
    return defaultValue;
  }
  const parsed = Number.parseInt(value, 10);
  if (!Number.isInteger(parsed)) {
    throw AppError.invalidParameter(`${name} must be an integer`);
  }
  return parsed;
}

function parseBoolean(value: string | undefined, name: string, defaultValue: boolean): boolean {
  if (value === undefined || value === "") {
    return defaultValue;
  }
  if (value === "true" || value === "1") {
    return true;
  }
  if (value === "false" || value === "0") {
    return false;
  }
  throw AppError.invalidParameter(`${name} must be a boolean`);
}

function parseSortOrder(value: string): SortOrder {
  if (value === "asc" || value === "desc") {
    return value;
  }
  throw AppError.invalidParameter("sort_order must be asc or desc");
}
