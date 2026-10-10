package com.maxchauo.backendapilanguagelab.jobs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/** Public API response DTO for a job. */
@Getter
@AllArgsConstructor
public class JobResponse {
    private final String source;
    @JsonProperty("source_job_id")
    private final String sourceJobId;
    private final String title;
    private final String company;
    private final String url;
    @JsonProperty("posted_at")
    private final Instant postedAt;
    private final SalaryResponse salary;
    private final LocationResponse location;
}

/** Public API response DTO for salary information. */
@Getter
@AllArgsConstructor
class SalaryResponse {
    private final Long min;
    private final Long max;
    private final String currency;
}

/** Public API response DTO for location information. */
@Getter
@AllArgsConstructor
class LocationResponse {
    private final String city;
    private final String district;
}

/** Maps domain models into public API response DTOs. */
final class JobResponseMapper {
    private JobResponseMapper() {
    }

    /** Maps one domain job into its response DTO. */
    static JobResponse toResponse(Job job) {
        return new JobResponse(
                job.getSource(),
                job.getSourceJobId(),
                job.getTitle(),
                job.getCompany(),
                job.getUrl(),
                job.getPostedAt(),
                new SalaryResponse(
                        job.getSalary().getMinimum(),
                        job.getSalary().getMaximum(),
                        job.getSalary().getCurrency()),
                new LocationResponse(job.getLocation().getCity(), job.getLocation().getDistrict()));
    }
}
