/** 驗證 jobs service 與 Express handler 的主要 API 行為。 */
import assert from "node:assert/strict";
import { once } from "node:events";
import { test } from "node:test";
import type { Server } from "node:http";

import { createApp } from "../src/app";
import { JobsService } from "../src/jobs/service";
import type { Job, ListJobsQuery } from "../src/jobs/models";
import type { JobsRepository } from "../src/jobs/repository";

class FakeJobsRepository implements JobsRepository {
  public receivedQuery: ListJobsQuery | undefined;

  public constructor(
    private readonly jobs: Job[] = [],
    private readonly failure?: Error,
  ) {}

  public async list(query: ListJobsQuery): Promise<Job[]> {
    this.receivedQuery = query;
    if (this.failure) {
      throw this.failure;
    }
    return this.jobs;
  }
}

function sampleJob(): Job {
  return {
    source: "example",
    sourceJobId: "job-1",
    title: "Backend Engineer",
    company: "Example Company",
    url: "https://example.com/jobs/job-1",
    postedAt: new Date("2026-10-08T08:00:00Z"),
    salary: { minimum: 80_000, maximum: 120_000, currency: "TWD" },
    location: { city: "Taipei", district: "Da'an" },
  };
}

async function startServer(repository: JobsRepository): Promise<{ server: Server; url: string }> {
  const server = createApp(new JobsService(repository)).listen(0);
  await once(server, "listening");
  const address = server.address();
  assert(address && typeof address !== "string");
  return { server, url: `http://127.0.0.1:${address.port}` };
}

test("GET /jobs returns the response DTO list", async () => {
  const { server, url } = await startServer(new FakeJobsRepository([sampleJob()]));

  try {
    const response = await fetch(`${url}/jobs`);
    assert.equal(response.status, 200);
    assert.deepEqual(await response.json(), [
      {
        source: "example",
        source_job_id: "job-1",
        title: "Backend Engineer",
        company: "Example Company",
        url: "https://example.com/jobs/job-1",
        posted_at: "2026-10-08T08:00:00.000Z",
        salary: { min: 80_000, max: 120_000, currency: "TWD" },
        location: { city: "Taipei", district: "Da'an" },
      },
    ]);
  } finally {
    server.close();
  }
});

test("GET /jobs rejects an invalid page size", async () => {
  const { server, url } = await startServer(new FakeJobsRepository());

  try {
    const response = await fetch(`${url}/jobs?page_size=20`);
    assert.equal(response.status, 400);
    assert.deepEqual(await response.json(), {
      error: {
        code: "INVALID_PARAMETER",
        message: "page_size must be between 30 and 50",
      },
    });
  } finally {
    server.close();
  }
});

test("GET /jobs hides repository errors", async () => {
  const { server, url } = await startServer(
    new FakeJobsRepository([], new Error("database unavailable")),
  );

  try {
    const response = await fetch(`${url}/jobs`);
    assert.equal(response.status, 500);
    const body = await response.text();
    assert.match(body, /INTERNAL_SERVER_ERROR/);
    assert.doesNotMatch(body, /database unavailable/);
  } finally {
    server.close();
  }
});
