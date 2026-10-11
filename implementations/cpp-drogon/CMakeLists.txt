cmake_minimum_required(VERSION 3.20)
project(backend_api_language_lab_cpp LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

find_package(Drogon CONFIG REQUIRED)

add_executable(backend-api-cpp
    src/main.cc
    src/jobs/handler.cc
    src/jobs/postgres_repository.cc
    src/jobs/service.cc
)

target_include_directories(backend-api-cpp PRIVATE include)
target_link_libraries(backend-api-cpp PRIVATE Drogon::Drogon)

target_compile_options(backend-api-cpp PRIVATE -Wall -Wextra -Wpedantic)
