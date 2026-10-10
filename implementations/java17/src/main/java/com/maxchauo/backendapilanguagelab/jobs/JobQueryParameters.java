package com.maxchauo.backendapilanguagelab.jobs;

import lombok.Getter;
import lombok.Setter;

/** Raw HTTP query parameters before service validation. */
@Getter
@Setter
public class JobQueryParameters {
    private String page;
    private String page_size;
    private String source;
    private String city;
    private String district;
    private String employment_type;
    private String job_type;
    private String title;
    private String include_closed;
    private String sort_by;
    private String sort_order;
}
