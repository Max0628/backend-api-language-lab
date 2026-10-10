/** 載入並驗證 API server 的環境設定。 */

export interface Settings {
  /** PostgreSQL connection string。 */
  databaseUrl: string;
  /** HTTP server port。 */
  serverPort: number;
}

/** 從 process environment 建立 runtime settings。 */
export function loadSettings(
  environment: NodeJS.ProcessEnv = process.env,
): Settings {
  const databaseUrl = environment.DATABASE_URL;
  if (!databaseUrl) {
    throw new Error("DATABASE_URL is required");
  }

  const serverPort = Number.parseInt(environment.SERVER_PORT ?? "8080", 10);
  if (!Number.isInteger(serverPort) || serverPort < 1 || serverPort > 65_535) {
    throw new Error("SERVER_PORT must be an integer between 1 and 65535");
  }

  return { databaseUrl, serverPort };
}
