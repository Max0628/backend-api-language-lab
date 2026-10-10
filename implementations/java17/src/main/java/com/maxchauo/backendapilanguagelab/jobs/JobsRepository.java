package com.maxchauo.backendapilanguagelab.jobs;

import java.util.List;

/** Minimal repository contract required by the jobs service. */
public interface JobsRepository {
    /** Lists jobs using validated query conditions. */
    List<Job> list(ListJobsQuery query);
}
