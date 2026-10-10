/** TypeScript + Express API server entrypoint。 */
import { createApp } from "./app";
import { loadSettings } from "./config";
import { JobsService } from "./jobs/service";
import { PostgresJobsRepository } from "./jobs/repository";
import { Pool } from "pg";

async function main(): Promise<void> {
  const settings = loadSettings();
  const pool = new Pool({ connectionString: settings.databaseUrl, max: 10 });
  await pool.query("SELECT 1");

  const repository = new PostgresJobsRepository(pool);
  const service = new JobsService(repository);
  const app = createApp(service);
  const server = app.listen(settings.serverPort, "0.0.0.0", () => {
    console.log(`server listening on :${settings.serverPort}`);
  });

  const shutdown = (): void => {
    server.close(() => {
      void pool.end();
    });
  };
  process.once("SIGINT", shutdown);
  process.once("SIGTERM", shutdown);
}

main().catch((error: unknown) => {
  console.error("server startup failed", error);
  process.exitCode = 1;
});
