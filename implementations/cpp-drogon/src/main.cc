#include "jobs/handler.h"
#include "jobs/repository.h"

#include <drogon/drogon.h>

#include <cstdlib>
#include <memory>

namespace jobs {
std::shared_ptr<JobsRepository> makePostgresJobsRepository(
    drogon::orm::DbClientPtr client);
}

int main() {
    const char* databaseUrl = std::getenv("DATABASE_URL");
    if (databaseUrl == nullptr || *databaseUrl == '\0') {
        LOG_ERROR << "DATABASE_URL is required";
        return EXIT_FAILURE;
    }

    auto client = drogon::orm::DbClient::newPgClient(databaseUrl, 4);
    auto repository = jobs::makePostgresJobsRepository(client);
    auto service = std::make_shared<jobs::JobsService>(repository);
    drogon::app().registerController(
        std::make_shared<jobs::JobsHandler>(service));

    const char* portValue = std::getenv("SERVER_PORT");
    const int port = portValue == nullptr ? 8080 : std::atoi(portValue);
    drogon::app().addListener("0.0.0.0", port).run();
}
