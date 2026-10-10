package com.maxchauo.backendapilanguagelab.jobs;

import java.util.Set;

/** Validates jobs query parameters and coordinates repository access. */
@org.springframework.stereotype.Service
public class JobsService {
    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of(
            "posted_at", "first_seen_at", "last_seen_at", "salary_min",
            "salary_max", "title", "company");

    private final JobsRepository repository;

    /** Creates the service with constructor-injected repository. */
    public JobsService(JobsRepository repository) {
        this.repository = repository;
    }

    /** Validates parameters and returns matching jobs synchronously. */
    public java.util.List<Job> list(JobQueryParameters parameters) {
        return repository.list(parse(parameters));
    }

    private ListJobsQuery parse(JobQueryParameters parameters) {
        int page = parseInteger(parameters.getPage(), "page", 1);
        int pageSize = parseInteger(parameters.getPage_size(), "page_size", 30);
        if (page < 1) {
            throw new InvalidParameterException("page must be at least 1");
        }
        if (pageSize < 30 || pageSize > 50) {
            throw new InvalidParameterException("page_size must be between 30 and 50");
        }

        String sortBy = valueOrDefault(parameters.getSort_by(), "posted_at");
        if (!ALLOWED_SORT_COLUMNS.contains(sortBy)) {
            throw new InvalidParameterException("invalid sort_by");
        }

        SortOrder sortOrder = switch (valueOrDefault(parameters.getSort_order(), "desc")) {
            case "asc" -> SortOrder.ASC;
            case "desc" -> SortOrder.DESC;
            default -> throw new InvalidParameterException("sort_order must be asc or desc");
        };

        return new ListJobsQuery(
                page,
                pageSize,
                optional(parameters.getSource()),
                optional(parameters.getCity()),
                optional(parameters.getDistrict()),
                optional(parameters.getEmployment_type()),
                optional(parameters.getJob_type()),
                optional(parameters.getTitle()),
                parseBoolean(parameters.getInclude_closed()),
                sortBy,
                sortOrder);
    }

    private int parseInteger(String value, String name, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new InvalidParameterException(name + " must be an integer");
        }
    }

    private boolean parseBoolean(String value) {
        return switch (valueOrDefault(value, "false")) {
            case "true", "1" -> true;
            case "false", "0" -> false;
            default -> throw new InvalidParameterException("include_closed must be a boolean");
        };
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String optional(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
