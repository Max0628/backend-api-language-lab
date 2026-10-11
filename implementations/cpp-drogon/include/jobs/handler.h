#pragma once

#include "jobs/service.h"

#include <drogon/HttpController.h>

namespace jobs {

class JobsHandler : public drogon::HttpController<JobsHandler> {
public:
    // The handler uses constructor injection, so Drogon must not auto-create it.
    static constexpr bool isAutoCreation = false;

    METHOD_LIST_BEGIN
    ADD_METHOD_TO(JobsHandler::list, "/jobs", drogon::Get);
    METHOD_LIST_END

    explicit JobsHandler(std::shared_ptr<JobsService> service);

    void list(const drogon::HttpRequestPtr& request,
              std::function<void(const drogon::HttpResponsePtr&)>&& callback) const;

private:
    std::shared_ptr<JobsService> service_;
};

}  // namespace jobs
