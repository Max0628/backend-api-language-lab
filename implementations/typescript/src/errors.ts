/** 定義 API error 與 Express error middleware。 */
import type { ErrorRequestHandler, RequestHandler } from "express";

export type ErrorCode = "INVALID_PARAMETER" | "INTERNAL_SERVER_ERROR";

/** 可安全回傳給 API client 的 application error。 */
export class AppError extends Error {
  public constructor(
    public readonly statusCode: number,
    public readonly code: ErrorCode,
    message: string,
  ) {
    super(message);
    this.name = "AppError";
  }

  /** 建立 invalid parameter error。 */
  public static invalidParameter(message: string): AppError {
    return new AppError(400, "INVALID_PARAMETER", message);
  }
}

/** 將 synchronous 或 asynchronous handler 的錯誤交給 middleware。 */
export const asyncHandler = (handler: RequestHandler): RequestHandler => {
  return (request, response, next) => {
    Promise.resolve(handler(request, response, next)).catch(next);
  };
};

/** 統一處理 API error，避免暴露 SQL 或 stack trace。 */
export const errorHandler: ErrorRequestHandler = (error, _request, response, _next) => {
  if (error instanceof AppError) {
    response.status(error.statusCode).json({
      error: { code: error.code, message: error.message },
    });
    return;
  }

  console.error("unhandled request error", error);
  response.status(500).json({
    error: {
      code: "INTERNAL_SERVER_ERROR",
      message: "failed to query jobs",
    },
  });
};
