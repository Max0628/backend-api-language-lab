package com.maxchauo.backendapilanguagelab.jobs;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Tests jobs query defaults and validation without PostgreSQL. */
class JobsServiceTest {
    @Test
    void appliesDefaultQueryValues() {
        FakeRepository repository = new FakeRepository();
        JobsService service = new JobsService(repository);

        service.list(new JobQueryParameters());

        assertEquals(1, repository.query.getPage());
        assertEquals(30, repository.query.getPageSize());
        assertEquals("posted_at", repository.query.getSortBy());
        assertEquals(SortOrder.DESC, repository.query.getSortOrder());
    }

    @Test
    void rejectsInvalidPageSize() {
        JobQueryParameters parameters = new JobQueryParameters();
        parameters.setPage_size("20");

        InvalidParameterException exception = assertThrows(
                InvalidParameterException.class,
                () -> new JobsService(new FakeRepository()).list(parameters));
        assertEquals("page_size must be between 30 and 50", exception.getMessage());
    }

    private static final class FakeRepository implements JobsRepository {
        private ListJobsQuery query;

        @Override
        public List<Job> list(ListJobsQuery query) {
            this.query = query;
            return List.of();
        }
    }
}
