package com.maxchauo.backendapilanguagelab.jobs

import kotlinx.serialization.Serializable

/** Domain model for a normalized job. */
data class Job(
    val source: String,
    val sourceJobId: String,
    val title: String,
    val company: String?,
    val url: String,
    val postedAt: String?,
    val salary: Salary,
    val location: Location,
)

/** Salary information in the domain model. */
data class Salary(
    val minimum: Long?,
    val maximum: Long?,
    val currency: String?,
)

/** Location information in the domain model. */
data class Location(
    val city: String?,
    val district: String?,
)

/** Validated query passed from service to repository. */
data class ListJobsQuery(
    val page: Int,
    val pageSize: Int,
    val source: String?,
    val city: String?,
    val district: String?,
    val employmentType: String?,
    val jobType: String?,
    val title: String?,
    val includeClosed: Boolean,
    val sortBy: String,
    val sortOrder: SortOrder,
)

/** Allowed SQL sort directions. */
enum class SortOrder {
    ASC,
    DESC,
}

/** Public API response DTO for a job. */
@Serializable
data class JobResponse(
    val source: String,
    val source_job_id: String,
    val title: String,
    val company: String?,
    val url: String,
    val posted_at: String?,
    val salary: SalaryResponse,
    val location: LocationResponse,
)

/** Public API response DTO for salary information. */
@Serializable
data class SalaryResponse(
    val min: Long?,
    val max: Long?,
    val currency: String?,
)

/** Public API response DTO for location information. */
@Serializable
data class LocationResponse(
    val city: String?,
    val district: String?,
)

/** Maps a domain job into the public API DTO. */
fun Job.toResponse(): JobResponse = JobResponse(
    source = source,
    source_job_id = sourceJobId,
    title = title,
    company = company,
    url = url,
    posted_at = postedAt,
    salary = SalaryResponse(salary.minimum, salary.maximum, salary.currency),
    location = LocationResponse(location.city, location.district),
)
