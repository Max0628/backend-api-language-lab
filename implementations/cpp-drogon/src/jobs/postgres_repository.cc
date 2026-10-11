#include "jobs/repository.h"

#include <drogon/orm/DbClient.h>

#include <sstream>
#include <stdexcept>
#include <utility>

namespace jobs {

namespace {

std::optional<std::string> optionalString(const drogon::orm::Row& row,
                                          const char* column) {
    if (row[column].isNull()) {
        return std::nullopt;
    }
    return row[column].as<std::string>();
}

std::optional<std::int64_t> optionalInteger(const drogon::orm::Row& row,
                                            const char* column) {
    if (row[column].isNull()) {
        return std::nullopt;
    }
    return row[column].as<std::int64_t>();
}

std::string sortDirection(SortOrder order) {
    return order == SortOrder::Asc ? "ASC" : "DESC";
}

std::string sortColumn(const std::string& value) {
    if (value == "posted_at" || value == "first_seen_at" ||
        value == "last_seen_at" || value == "salary_min" ||
        value == "salary_max" || value == "title" || value == "company") {
        return value;
    }
    throw std::invalid_argument("invalid sort_by");
}

}  // namespace

class PostgresJobsRepository final : public JobsRepository {
public:
    explicit PostgresJobsRepository(drogon::orm::DbClientPtr client)
        : client_(std::move(client)) {}

    void list(const ListJobsQuery& query, JobsSuccess success,
              JobsFailure failure) override {
        const auto direction = sortDirection(query.sortOrder);
        const auto column = sortColumn(query.sortBy);
        const auto nulls = query.sortBy == "posted_at" ? " NULLS LAST" : "";

        const std::string sql = R"SQL(
            SELECT source, source_job_id, title, company, url,
                   posted_at, salary_min, salary_max, salary_currency,
                   city, district
            FROM jobs
            WHERE ($1::text = '' OR source = $1)
              AND ($2::text = '' OR city = $2)
              AND ($3::text = '' OR district = $3)
              AND ($4::text = '' OR employment_type = $4)
              AND ($5::text = '' OR job_type = $5)
              AND ($6::text = '' OR
                   to_tsvector('english', title) @@ plainto_tsquery('english', $6))
              AND ($7::boolean OR status <> 'CLOSED')
            ORDER BY )SQL" +
                                  column + " " + direction + nulls + ", id " +
                                  direction + R"SQL(
            LIMIT $8 OFFSET $9
        )SQL";

        client_->execSqlAsync(
            sql,
            [success = std::move(success)](const drogon::orm::Result& result) {
                Jobs jobs;
                jobs.reserve(result.size());
                for (const auto& row : result) {
                    jobs.push_back(Job{
                        row["source"].as<std::string>(),
                        row["source_job_id"].as<std::string>(),
                        row["title"].as<std::string>(),
                        optionalString(row, "company"),
                        optionalString(row, "url"),
                        optionalString(row, "posted_at"),
                        Salary{optionalInteger(row, "salary_min"),
                               optionalInteger(row, "salary_max"),
                               optionalString(row, "salary_currency")},
                        Location{optionalString(row, "city"),
                                 optionalString(row, "district")}});
                }
                success(std::move(jobs));
            },
            [failure = std::move(failure)](const drogon::orm::DrogonDbException& error) {
                failure(error.base().what());
            },
            query.source, query.city, query.district, query.employmentType,
            query.jobType, query.title, query.includeClosed,
            static_cast<std::int64_t>(query.pageSize),
            static_cast<std::int64_t>((query.page - 1) * query.pageSize));
    }

private:
    drogon::orm::DbClientPtr client_;
};

std::shared_ptr<JobsRepository> makePostgresJobsRepository(
    drogon::orm::DbClientPtr client) {
    return std::make_shared<PostgresJobsRepository>(std::move(client));
}

}  // namespace jobs
