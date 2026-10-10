/** 定義 jobs route 與 HTTP query parsing。 */
import { Router, type Request } from "express";

import { AppError, asyncHandler } from "../errors";
import { toJobResponse } from "./models";
import type { JobsService } from "./service";

/** 建立注入 jobs service 的 Express router。 */
export function createJobsRouter(service: JobsService): Router {
  const router = Router();

  router.get(
    "/jobs",
    asyncHandler(async (request, response) => {
      const jobs = await service.list(parseQuery(request));
      response.status(200).json(jobs.map(toJobResponse));
    }),
  );

  return router;
}

function parseQuery(request: Request) {
  return {
    page: queryString(request.query.page, "page"),
    pageSize: queryString(request.query.page_size, "page_size"),
    source: queryString(request.query.source, "source"),
    city: queryString(request.query.city, "city"),
    district: queryString(request.query.district, "district"),
    employmentType: queryString(request.query.employment_type, "employment_type"),
    jobType: queryString(request.query.job_type, "job_type"),
    title: queryString(request.query.title, "title"),
    includeClosed: queryString(request.query.include_closed, "include_closed"),
    sortBy: queryString(request.query.sort_by, "sort_by"),
    sortOrder: queryString(request.query.sort_order, "sort_order"),
  };
}

function queryString(value: unknown, name: string): string | undefined {
  if (value === undefined) {
    return undefined;
  }
  if (typeof value !== "string") {
    throw AppError.invalidParameter(`${name} must be a string`);
  }
  return value;
}
