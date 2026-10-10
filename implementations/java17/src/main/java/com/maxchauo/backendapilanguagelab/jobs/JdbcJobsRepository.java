package com.maxchauo.backendapilanguagelab.jobs;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/** Blocking PostgreSQL repository using parameterized raw SQL. */
@Repository
public class JdbcJobsRepository implements JobsRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    /** Creates the repository with constructor-injected JDBC template. */
    public JdbcJobsRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Executes the jobs query and maps rows into domain models. */
    @Override
    public List<Job> list(ListJobsQuery query) {
        QueryParts queryParts = buildQuery(query);
        return jdbcTemplate.query(queryParts.getSql(), queryParts.getParameters(), this::mapJob);
    }

    private QueryParts buildQuery(ListJobsQuery query) {
        List<String> conditions = new ArrayList<>(List.of("TRUE"));
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        addExactFilter(conditions, parameters, "source", query.getSource());
        addExactFilter(conditions, parameters, "city", query.getCity());
        addExactFilter(conditions, parameters, "district", query.getDistrict());
        addExactFilter(conditions, parameters, "employment_type", query.getEmploymentType());
        addExactFilter(conditions, parameters, "job_type", query.getJobType());
        if (!query.isIncludeClosed()) conditions.add("status <> 'CLOSED'");
        if (query.getTitle() != null) {
            conditions.add("to_tsvector('english', title) @@ plainto_tsquery('english', :title)");
            parameters.addValue("title", query.getTitle());
        }

        String direction = query.getSortOrder() == SortOrder.ASC ? "ASC" : "DESC";
        String nulls = query.getSortBy().equals("posted_at") ? " NULLS LAST" : "";
        parameters.addValue("page_size", query.getPageSize());
        parameters.addValue("offset", (query.getPage() - 1) * query.getPageSize());
        String sql = """
                SELECT source, source_job_id, title, company, url,
                       posted_at, salary_min, salary_max, salary_currency,
                       city, district
                FROM jobs
                WHERE %s
                ORDER BY %s %s%s, id %s
                LIMIT :page_size OFFSET :offset
                """.formatted(
                String.join(" AND ", conditions), sortColumn(query.getSortBy()), direction, nulls, direction);
        return new QueryParts(sql, parameters);
    }

    private void addExactFilter(
            List<String> conditions,
            MapSqlParameterSource parameters,
            String column,
            String value) {
        if (value != null) {
            conditions.add(column + " = :" + column);
            parameters.addValue(column, value);
        }
    }

    private String sortColumn(String value) {
        return switch (value) {
            case "posted_at", "first_seen_at", "last_seen_at", "salary_min",
                    "salary_max", "title", "company" -> value;
            default -> throw new IllegalStateException("sort column must be validated");
        };
    }

    private Job mapJob(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Job(
                resultSet.getString("source"),
                resultSet.getString("source_job_id"),
                resultSet.getString("title"),
                resultSet.getString("company"),
                resultSet.getString("url"),
                toInstant(resultSet.getObject("posted_at")),
                new Salary(
                        nullableLong(resultSet, "salary_min"),
                        nullableLong(resultSet, "salary_max"),
                        resultSet.getString("salary_currency")),
                new Location(resultSet.getString("city"), resultSet.getString("district")));
    }

    private Long nullableLong(ResultSet resultSet, String column) throws SQLException {
        long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }

    private java.time.Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof OffsetDateTime offsetDateTime) return offsetDateTime.toInstant();
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant();
        throw new IllegalStateException("unsupported posted_at type");
    }

    @Getter
    @AllArgsConstructor
    private static class QueryParts {
        private final String sql;
        private final MapSqlParameterSource parameters;
    }
}
