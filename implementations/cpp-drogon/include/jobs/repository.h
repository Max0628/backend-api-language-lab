#pragma once

#include "jobs/models.h"

namespace jobs {

class JobsRepository {
public:
    virtual ~JobsRepository() = default;
    virtual void list(const ListJobsQuery& query, JobsSuccess success,
                      JobsFailure failure) = 0;
};

}  // namespace jobs
