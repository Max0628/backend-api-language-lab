#pragma once

#include "jobs/models.h"
#include "jobs/repository.h"

#include <memory>

namespace jobs {

class JobsService {
public:
    explicit JobsService(std::shared_ptr<JobsRepository> repository);

    void list(const ListJobsQuery& query, JobsSuccess success,
              JobsFailure failure) const;

private:
    std::shared_ptr<JobsRepository> repository_;
};

}  // namespace jobs
