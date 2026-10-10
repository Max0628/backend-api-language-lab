/** 建立 Express application；HTTP server 啟動由 main.ts 負責。 */
import express, { type Express } from "express";

import { errorHandler } from "./errors";
import { createJobsRouter } from "./jobs/handler";
import type { JobsService } from "./jobs/service";

/** 建立尚未綁定 port 的 Express application。 */
export function createApp(service: JobsService): Express {
  const app = express();
  app.use(express.json());
  app.use(createJobsRouter(service));
  app.use(errorHandler);
  return app;
}
