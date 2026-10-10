package com.maxchauo.backendapilanguagelab.jobs;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Validated query used by the repository layer. */
@Getter
@AllArgsConstructor
public class ListJobsQuery {
    private final int page;
    private final int pageSize;
    private final String source;
    private final String city;
    private final String district;
    private final String employmentType;
    private final String jobType;
    private final String title;
    private final boolean includeClosed;
    private final String sortBy;
    private final SortOrder sortOrder;
}
