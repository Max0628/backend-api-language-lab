package com.maxchauo.backendapilanguagelab.jobs

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet

/** PostgreSQL repository using JDBC parameterized SQL. */
class PostgresJobsRepository(private val dataSource: HikariDataSource) : JobsRepository {
    /** Executes the jobs query on the IO dispatcher. */
    override suspend fun list(query: ListJobsQuery): List<Job> = withContext(Dispatchers.IO) {
        val sql = JobsSql.build(query)
        dataSource.connection.use { connection ->
            connection.prepareStatement(sql.text).use { statement ->
                sql.parameters.forEachIndexed { index, value ->
                    statement.setObject(index + 1, value)
                }
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) add(resultSet.toJob())
                    }
                }
            }
        }
    }
}

private data class SqlQuery(val text: String, val parameters: List<Any>)

private object JobsSql {
    fun build(query: ListJobsQuery): SqlQuery {
        val conditions = mutableListOf("TRUE")
        val parameters = mutableListOf<Any>()

        if (!query.includeClosed) conditions += "status <> 'CLOSED'"
        addExactFilter(conditions, parameters, "source", query.source)
        addExactFilter(conditions, parameters, "city", query.city)
        addExactFilter(conditions, parameters, "district", query.district)
        addExactFilter(conditions, parameters, "employment_type", query.employmentType)
        addExactFilter(conditions, parameters, "job_type", query.jobType)
        if (!query.title.isNullOrEmpty()) {
            parameters += query.title
            conditions += "to_tsvector('english', title) @@ plainto_tsquery('english', ?)"
        }

        val direction = query.sortOrder.name
        val nulls = if (query.sortBy == "posted_at") " NULLS LAST" else ""
        parameters += query.pageSize
        parameters += (query.page - 1) * query.pageSize

        val text = """
            SELECT source, source_job_id, title, company, url,
                   posted_at, salary_min, salary_max, salary_currency,
                   city, district
            FROM jobs
            WHERE ${conditions.joinToString(" AND ")}
            ORDER BY ${sortColumn(query.sortBy)} $direction$nulls, id $direction
            LIMIT ? OFFSET ?
        """.trimIndent()
        return SqlQuery(text, parameters)
    }

    private fun addExactFilter(
        conditions: MutableList<String>,
        parameters: MutableList<Any>,
        column: String,
        value: String?,
    ) {
        if (!value.isNullOrEmpty()) {
            conditions += "$column = ?"
            parameters += value
        }
    }

    private fun sortColumn(value: String): String = when (value) {
        "posted_at" -> "posted_at"
        "first_seen_at" -> "first_seen_at"
        "last_seen_at" -> "last_seen_at"
        "salary_min" -> "salary_min"
        "salary_max" -> "salary_max"
        "title" -> "title"
        "company" -> "company"
        else -> error("sort column must be validated before repository access")
    }
}

private fun ResultSet.toJob(): Job = Job(
    source = getString("source"),
    sourceJobId = getString("source_job_id"),
    title = getString("title"),
    company = getString("company"),
    url = getString("url"),
    postedAt = getTimestamp("posted_at")?.toInstant()?.toString(),
    salary = Salary(
        minimum = getObject("salary_min")?.toString()?.toLong(),
        maximum = getObject("salary_max")?.toString()?.toLong(),
        currency = getString("salary_currency"),
    ),
    location = Location(
        city = getString("city"),
        district = getString("district"),
    ),
)
