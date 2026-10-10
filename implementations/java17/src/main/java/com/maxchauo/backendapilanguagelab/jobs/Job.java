package com.maxchauo.backendapilanguagelab.jobs;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/** Domain model for a normalized job. */
@Getter
@AllArgsConstructor
public class Job {
    private final String source;
    private final String sourceJobId;
    private final String title;
    private final String company;
    private final String url;
    private final Instant postedAt;
    private final Salary salary;
    private final Location location;
}

/** Domain model for salary information. */
@Getter
@AllArgsConstructor
class Salary {
    private final Long minimum;
    private final Long maximum;
    private final String currency;
}

/** Domain model for location information. */
@Getter
@AllArgsConstructor
class Location {
    private final String city;
    private final String district;
}
