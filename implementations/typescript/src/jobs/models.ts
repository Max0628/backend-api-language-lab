/** 定義 jobs domain model、query 與 API response DTO。 */

export interface Salary {
  /** 最低薪資。 */
  minimum: number | null;
  /** 最高薪資。 */
  maximum: number | null;
  /** 薪資幣別。 */
  currency: string | null;
}

export interface Location {
  /** 城市或縣市。 */
  city: string | null;
  /** 區或鄉鎮市。 */
  district: string | null;
}

/** 不包含 database 與 HTTP 細節的職缺 domain model。 */
export interface Job {
  source: string;
  sourceJobId: string;
  title: string;
  company: string | null;
  url: string;
  postedAt: Date | null;
  salary: Salary;
  location: Location;
}

export type SortOrder = "asc" | "desc";

/** HTTP query parsing 後、尚未完成數值驗證的查詢條件。 */
export interface RawListJobsQuery {
  page?: string;
  pageSize?: string;
  source?: string;
  city?: string;
  district?: string;
  employmentType?: string;
  jobType?: string;
  title?: string;
  includeClosed?: string;
  sortBy?: string;
  sortOrder?: string;
}

/** 經過 service 驗證後的職缺查詢條件。 */
export interface ListJobsQuery {
  page: number;
  pageSize: number;
  source?: string;
  city?: string;
  district?: string;
  employmentType?: string;
  jobType?: string;
  title?: string;
  includeClosed: boolean;
  sortBy: string;
  sortOrder: SortOrder;
}

/** API response 的薪資 DTO。 */
export interface SalaryResponse {
  min: number | null;
  max: number | null;
  currency: string | null;
}

/** API response 的地點 DTO。 */
export interface LocationResponse {
  city: string | null;
  district: string | null;
}

/** GET /jobs 的 API response DTO。 */
export interface JobResponse {
  source: string;
  source_job_id: string;
  title: string;
  company: string | null;
  url: string;
  posted_at: Date | null;
  salary: SalaryResponse;
  location: LocationResponse;
}

/** 將 domain model mapping 成 API response DTO。 */
export function toJobResponse(job: Job): JobResponse {
  return {
    source: job.source,
    source_job_id: job.sourceJobId,
    title: job.title,
    company: job.company,
    url: job.url,
    posted_at: job.postedAt,
    salary: {
      min: job.salary.minimum,
      max: job.salary.maximum,
      currency: job.salary.currency,
    },
    location: job.location,
  };
}
