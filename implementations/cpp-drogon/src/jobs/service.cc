#include "jobs/service.h"

#include <utility>

namespace jobs {

JobsService::JobsService(std::shared_ptr<JobsRepository> repository)
    : repository_(std::move(repository)) {}

void JobsService::list(const ListJobsQuery& query, JobsSuccess success,
                       JobsFailure failure) const {
    repository_->list(query, std::move(success), std::move(failure));
}

}  // namespace jobs
