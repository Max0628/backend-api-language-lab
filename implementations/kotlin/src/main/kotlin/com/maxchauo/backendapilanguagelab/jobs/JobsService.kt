package com.maxchauo.backendapilanguagelab.jobs

import io.ktor.http.Parameters

/** Repository contract required by the jobs service. */
interface JobsRepository {
    /** Lists jobs using validated query conditions. */
    suspend fun list(query: ListJobsQuery): List<Job>
}

/** Invalid query parameter error returned as HTTP 400. */
class InvalidParameterException(message: String) : RuntimeException(message)

/** Validates query parameters and coordinates the repository call. */
class JobsService(private val repository: JobsRepository) {
    /** Parses the HTTP query, validates it, and returns matching jobs. */
    suspend fun list(parameters: Parameters): List<Job> {
        return repository.list(parseQuery(parameters))
    }

    private fun parseQuery(parameters: Parameters): ListJobsQuery {
        val page = parseInteger(parameters["page"], "page", 1)
        val pageSize = parseInteger(parameters["page_size"], "page_size", 30)
        requireValid(page >= 1) { "page must be at least 1" }
        requireValid(pageSize in 30..50) { "page_size must be between 30 and 50" }

        val sortBy = parameters["sort_by"] ?: "posted_at"
        requireValid(sortBy in allowedSortColumns) { "invalid sort_by" }
        val sortOrder = when (parameters["sort_order"] ?: "desc") {
            "asc" -> SortOrder.ASC
            "desc" -> SortOrder.DESC
            else -> throw InvalidParameterException("sort_order must be asc or desc")
        }

        return ListJobsQuery(
            page = page,
            pageSize = pageSize,
            source = parameters.optional("source"),
            city = parameters.optional("city"),
            district = parameters.optional("district"),
            employmentType = parameters.optional("employment_type"),
            jobType = parameters.optional("job_type"),
            title = parameters.optional("title"),
            includeClosed = parseBoolean(parameters["include_closed"]),
            sortBy = sortBy,
            sortOrder = sortOrder,
        )
    }

    private fun parseInteger(value: String?, name: String, default: Int): Int {
        if (value.isNullOrEmpty()) return default
        return value.toIntOrNull()
            ?: throw InvalidParameterException("$name must be an integer")
    }

    private fun parseBoolean(value: String?): Boolean = when (value) {
        null, "" -> false
        "true", "1" -> true
        "false", "0" -> false
        else -> throw InvalidParameterException("include_closed must be a boolean")
    }

    private fun requireValid(condition: Boolean, message: () -> String) {
        if (!condition) throw InvalidParameterException(message())
    }

    private companion object {
        val allowedSortColumns = setOf(
            "posted_at",
            "first_seen_at",
            "last_seen_at",
            "salary_min",
            "salary_max",
            "title",
            "company",
        )
    }
}

private fun Parameters.optional(name: String): String? = get(name)?.takeUnless { it.isEmpty() }
