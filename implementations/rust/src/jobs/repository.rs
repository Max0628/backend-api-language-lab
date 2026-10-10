//! PostgreSQL repository for the jobs query.

use chrono::{DateTime, Utc};
use sqlx::{FromRow, PgPool, Postgres, QueryBuilder};

use super::model::{Job, ListJobsQuery, Location, Salary};
use super::service::{JobsRepository, RepositoryError};

/// PostgreSQL-backed jobs repository.
pub struct PostgresJobsRepository {
    pool: PgPool,
}

impl PostgresJobsRepository {
    /// Creates a repository using the provided SQLx pool.
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
}

#[async_trait::async_trait]
impl JobsRepository for PostgresJobsRepository {
    /// Executes the parameterized jobs query.
    async fn list(&self, query: &ListJobsQuery) -> Result<Vec<Job>, RepositoryError> {
        let mut builder = QueryBuilder::<Postgres>::new(
            "SELECT source, source_job_id, title, company, url, "
        );
        builder.push(
            "posted_at, salary_min, salary_max, salary_currency, city, district "
        );
        builder.push("FROM jobs WHERE TRUE");

        if !query.include_closed {
            builder.push(" AND status <> 'CLOSED'");
        }
        add_exact_filter(&mut builder, "source", query.source.as_deref());
        add_exact_filter(&mut builder, "city", query.city.as_deref());
        add_exact_filter(&mut builder, "district", query.district.as_deref());
        add_exact_filter(
            &mut builder,
            "employment_type",
            query.employment_type.as_deref(),
        );
        add_exact_filter(&mut builder, "job_type", query.job_type.as_deref());

        if let Some(title) = query.title.as_deref() {
            builder
                .push(" AND to_tsvector('english', title) ")
                .push("@@ plainto_tsquery('english', ")
                .push_bind(title)
                .push(")");
        }

        builder
            .push(" ORDER BY ")
            .push(sort_column(&query.sort_by))
            .push(" ")
            .push(query.sort_order.as_sql());
        if query.sort_by == "posted_at" {
            builder.push(" NULLS LAST");
        }
        builder
            .push(" , id ")
            .push(query.sort_order.as_sql())
            .push(" LIMIT ")
            .push_bind(query.page_size)
            .push(" OFFSET ")
            .push_bind((query.page - 1) * query.page_size);

        let rows = builder
            .build_query_as::<JobRow>()
            .fetch_all(&self.pool)
            .await
            .map_err(RepositoryError::Database)?;

        Ok(rows.into_iter().map(JobRow::into_domain).collect())
    }
}

fn add_exact_filter<'a>(
    builder: &mut QueryBuilder<'a, Postgres>,
    column: &str,
    value: Option<&'a str>,
) {
    if let Some(value) = value.filter(|value| !value.is_empty()) {
        builder
            .push(" AND ")
            .push(column)
            .push(" = ")
            .push_bind(value);
    }
}

fn sort_column(value: &str) -> &'static str {
    match value {
        "posted_at" => "posted_at",
        "first_seen_at" => "first_seen_at",
        "last_seen_at" => "last_seen_at",
        "salary_min" => "salary_min",
        "salary_max" => "salary_max",
        "title" => "title",
        "company" => "company",
        _ => unreachable!("sort column must be validated before repository access"),
    }
}

#[derive(Debug, FromRow)]
struct JobRow {
    source: String,
    source_job_id: String,
    title: String,
    company: Option<String>,
    url: String,
    posted_at: Option<DateTime<Utc>>,
    salary_min: Option<i64>,
    salary_max: Option<i64>,
    salary_currency: Option<String>,
    city: Option<String>,
    district: Option<String>,
}

impl JobRow {
    fn into_domain(self) -> Job {
        Job {
            source: self.source,
            source_job_id: self.source_job_id,
            title: self.title,
            company: self.company,
            url: self.url,
            posted_at: self.posted_at,
            salary: Salary {
                minimum: self.salary_min,
                maximum: self.salary_max,
                currency: self.salary_currency,
            },
            location: Location {
                city: self.city,
                district: self.district,
            },
        }
    }
}
