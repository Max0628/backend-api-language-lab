package com.maxchauo.backendapilanguagelab.jobs

import io.ktor.http.Parameters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Tests jobs query defaults and validation. */
class JobsServiceTest {
    @Test
    fun `applies default query values`() = kotlinx.coroutines.runBlocking {
        val repository = FakeJobsRepository()
        JobsService(repository).list(Parameters.Empty)

        assertEquals(1, repository.query?.page)
        assertEquals(30, repository.query?.pageSize)
        assertEquals("posted_at", repository.query?.sortBy)
        assertEquals(SortOrder.DESC, repository.query?.sortOrder)
    }

    @Test
    fun `rejects page size outside allowed range`() = kotlinx.coroutines.runBlocking {
        val exception = assertFailsWith<InvalidParameterException> {
            JobsService(FakeJobsRepository()).list(Parameters.build {
                append("page_size", "20")
            })
        }

        assertEquals("page_size must be between 30 and 50", exception.message)
    }

    private class FakeJobsRepository : JobsRepository {
        var query: ListJobsQuery? = null

        override suspend fun list(query: ListJobsQuery): List<Job> {
            this.query = query
            return emptyList()
        }
    }
}
