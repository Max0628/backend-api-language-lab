#pragma once

#include <chrono>
#include <cstdint>
#include <functional>
#include <optional>
#include <string>
#include <vector>

namespace jobs {

enum class SortOrder { Asc, Desc };

struct ListJobsQuery {
    int page{1};
    int pageSize{30};
    std::string source;
    std::string city;
    std::string district;
    std::string employmentType;
    std::string jobType;
    std::string title;
    bool includeClosed{false};
    std::string sortBy{"posted_at"};
    SortOrder sortOrder{SortOrder::Desc};
};

struct Salary {
    std::optional<std::int64_t> minimum;
    std::optional<std::int64_t> maximum;
    std::optional<std::string> currency;
};

struct Location {
    std::optional<std::string> city;
    std::optional<std::string> district;
};

struct Job {
    std::string source;
    std::string sourceJobId;
    std::string title;
    std::optional<std::string> company;
    std::optional<std::string> url;
    std::optional<std::string> postedAt;
    Salary salary;
    Location location;
};

using Jobs = std::vector<Job>;
using JobsSuccess = std::function<void(Jobs)>;
using JobsFailure = std::function<void(const std::string&)>;

}  // namespace jobs
