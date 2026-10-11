#include "jobs/handler.h"

#include <drogon/drogon.h>

#include <charconv>
#include <memory>
#include <string_view>

namespace jobs {

namespace {

std::string parameter(const drogon::HttpRequestPtr& request,
                      const std::string& name) {
    return request->getParameter(name);
}

bool parseInteger(const std::string& value, const char* name, int defaultValue,
                 int& output, std::string& error) {
    if (value.empty()) {
        output = defaultValue;
        return true;
    }
    const auto* begin = value.data();
    const auto* end = begin + value.size();
    const auto [position, code] = std::from_chars(begin, end, output);
    if (code != std::errc{} || position != end) {
        error = std::string(name) + " must be an integer";
        return false;
    }
    return true;
}

bool parseBoolean(const std::string& value, bool& output, std::string& error) {
    if (value.empty() || value == "false" || value == "0") {
        output = false;
        return true;
    }
    if (value == "true" || value == "1") {
        output = true;
        return true;
    }
    error = "include_closed must be a boolean";
    return false;
}

bool validSortBy(const std::string& value) {
    return value == "posted_at" || value == "first_seen_at" ||
           value == "last_seen_at" || value == "salary_min" ||
           value == "salary_max" || value == "title" || value == "company";
}

void sendError(std::function<void(const drogon::HttpResponsePtr&)>& callback,
               drogon::HttpStatusCode status, const std::string& code,
               const std::string& message) {
    Json::Value body;
    body["error"]["code"] = code;
    body["error"]["message"] = message;
    auto response = drogon::HttpResponse::newHttpJsonResponse(body);
    response->setStatusCode(status);
    callback(response);
}

Json::Value nullable(const std::optional<std::string>& value) {
    return value ? Json::Value(*value) : Json::Value(Json::nullValue);
}

Json::Value toJson(const Job& job) {
    Json::Value body;
    body["source"] = job.source;
    body["source_job_id"] = job.sourceJobId;
    body["title"] = job.title;
    body["company"] = nullable(job.company);
    body["url"] = nullable(job.url);
    body["posted_at"] = nullable(job.postedAt);
    body["salary"]["min"] = job.salary.minimum
        ? Json::Value(static_cast<Json::Int64>(*job.salary.minimum))
        : Json::Value(Json::nullValue);
    body["salary"]["max"] = job.salary.maximum
        ? Json::Value(static_cast<Json::Int64>(*job.salary.maximum))
        : Json::Value(Json::nullValue);
    body["salary"]["currency"] = nullable(job.salary.currency);
    body["location"]["city"] = nullable(job.location.city);
    body["location"]["district"] = nullable(job.location.district);
    return body;
}

}  // namespace

JobsHandler::JobsHandler(std::shared_ptr<JobsService> service)
    : service_(std::move(service)) {}

void JobsHandler::list(
    const drogon::HttpRequestPtr& request,
    std::function<void(const drogon::HttpResponsePtr&)>&& callback) const {
    ListJobsQuery query;
    std::string error;
    if (!parseInteger(parameter(request, "page"), "page", 1, query.page,
                      error) ||
        !parseInteger(parameter(request, "page_size"), "page_size", 30,
                       query.pageSize, error)) {
        sendError(callback, drogon::k400BadRequest, "INVALID_PARAMETER",
                  error);
        return;
    }
    if (query.page < 1) {
        sendError(callback, drogon::k400BadRequest, "INVALID_PARAMETER",
                  "page must be at least 1");
        return;
    }
    if (query.pageSize < 30 || query.pageSize > 50) {
        sendError(callback, drogon::k400BadRequest, "INVALID_PARAMETER",
                  "page_size must be between 30 and 50");
        return;
    }

    query.source = parameter(request, "source");
    query.city = parameter(request, "city");
    query.district = parameter(request, "district");
    query.employmentType = parameter(request, "employment_type");
    query.jobType = parameter(request, "job_type");
    query.title = parameter(request, "title");
    query.sortBy = parameter(request, "sort_by");
    if (query.sortBy.empty()) query.sortBy = "posted_at";
    if (!validSortBy(query.sortBy)) {
        sendError(callback, drogon::k400BadRequest, "INVALID_PARAMETER",
                  "invalid sort_by");
        return;
    }

    const auto sortOrder = parameter(request, "sort_order");
    if (sortOrder == "asc") {
        query.sortOrder = SortOrder::Asc;
    } else if (sortOrder.empty() || sortOrder == "desc") {
        query.sortOrder = SortOrder::Desc;
    } else {
        sendError(callback, drogon::k400BadRequest, "INVALID_PARAMETER",
                  "sort_order must be asc or desc");
        return;
    }

    if (!parseBoolean(parameter(request, "include_closed"), query.includeClosed,
                      error)) {
        sendError(callback, drogon::k400BadRequest, "INVALID_PARAMETER",
                  error);
        return;
    }

    auto responseCallback = std::make_shared<
        std::function<void(const drogon::HttpResponsePtr&)>>(std::move(callback));
    service_->list(
        query,
        [responseCallback](Jobs jobs) {
            Json::Value body(Json::arrayValue);
            for (const auto& job : jobs) body.append(toJson(job));
            (*responseCallback)(drogon::HttpResponse::newHttpJsonResponse(body));
        },
        [responseCallback](const std::string&) {
            sendError(*responseCallback, drogon::k500InternalServerError,
                      "INTERNAL_SERVER_ERROR", "failed to query jobs");
        });
}

}  // namespace jobs
