//! Application rules and validation for jobs queries.

use std::sync::Arc;

use async_trait::async_trait;
use thiserror::Error;

use super::model::{Job, ListJobsQuery, RawListJobsQuery, SortOrder};

/// Data access contract required by the jobs service.
#[async_trait]
pub trait JobsRepository: Send + Sync {
    /// Lists jobs using validated query conditions.
    async fn list(&self, query: &ListJobsQuery) -> Result<Vec<Job>, RepositoryError>;
}

/// Repository failure that should not be exposed to API clients.
#[derive(Debug, Error)]
pub enum RepositoryError {
    /// Database query or connection failure.
    #[error("database operation failed")]
    Database(#[source] sqlx::Error),
}

/// Application-level jobs service.
#[derive(Clone)]
pub struct JobsService {
    repository: Arc<dyn JobsRepository>,
}

impl JobsService {
    /// Creates a service with constructor-injected repository.
    pub fn new(repository: Arc<dyn JobsRepository>) -> Self {
        Self { repository }
    }

    /// Validates raw HTTP parameters and queries the repository.
    pub async fn list(&self, raw_query: RawListJobsQuery) -> Result<Vec<Job>, ServiceError> {
        let query = ListJobsQuery::try_from(raw_query)?;
        self.repository
            .list(&query)
            .await
            .map_err(ServiceError::Repository)
    }
}

/// Jobs service failures.
#[derive(Debug, Error)]
pub enum ServiceError {
    /// Query parameters are invalid.
    #[error("{0}")]
    InvalidParameter(String),
    /// Repository operation failed.
    #[error(transparent)]
    Repository(#[from] RepositoryError),
}

impl TryFrom<RawListJobsQuery> for ListJobsQuery {
    type Error = ServiceError;

    /// Converts and validates raw HTTP query parameters.
    fn try_from(raw: RawListJobsQuery) -> Result<Self, Self::Error> {
        let page = parse_integer(raw.page, "page", 1)?;
        let page_size = parse_integer(raw.page_size, "page_size", 30)?;
        if page < 1 {
            return Err(invalid("page must be at least 1"));
        }
        if !(30..=50).contains(&page_size) {
            return Err(invalid("page_size must be between 30 and 50"));
        }

        let sort_by = raw.sort_by.unwrap_or_else(|| "posted_at".to_owned());
        if !allowed_sort_column(&sort_by) {
            return Err(invalid("invalid sort_by"));
        }

        let sort_order = match raw.sort_order.as_deref().unwrap_or("desc") {
            "asc" => SortOrder::Asc,
            "desc" => SortOrder::Desc,
            _ => return Err(invalid("sort_order must be asc or desc")),
        };

        Ok(Self {
            page,
            page_size,
            source: raw.source,
            city: raw.city,
            district: raw.district,
            employment_type: raw.employment_type,
            job_type: raw.job_type,
            title: raw.title,
            include_closed: parse_boolean(raw.include_closed, "include_closed", false)?,
            sort_by,
            sort_order,
        })
    }
}

fn parse_integer(
    value: Option<String>,
    name: &'static str,
    default: i64,
) -> Result<i64, ServiceError> {
    match value.as_deref() {
        None | Some("") => Ok(default),
        Some(value) => value
            .parse()
            .map_err(|_| invalid(&format!("{name} must be an integer"))),
    }
}

fn parse_boolean(
    value: Option<String>,
    name: &'static str,
    default: bool,
) -> Result<bool, ServiceError> {
    match value.as_deref() {
        None | Some("") => Ok(default),
        Some("true") | Some("1") => Ok(true),
        Some("false") | Some("0") => Ok(false),
        Some(_) => Err(invalid(&format!("{name} must be a boolean"))),
    }
}

fn allowed_sort_column(value: &str) -> bool {
    matches!(
        value,
        "posted_at"
            | "first_seen_at"
            | "last_seen_at"
            | "salary_min"
            | "salary_max"
            | "title"
            | "company"
    )
}

fn invalid(message: &str) -> ServiceError {
    ServiceError::InvalidParameter(message.to_owned())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn applies_query_defaults() {
        let query = ListJobsQuery::try_from(RawListJobsQuery {
            page: None,
            page_size: None,
            source: None,
            city: None,
            district: None,
            employment_type: None,
            job_type: None,
            title: None,
            include_closed: None,
            sort_by: None,
            sort_order: None,
        })
        .expect("default query should be valid");

        assert_eq!(query.page, 1);
        assert_eq!(query.page_size, 30);
        assert_eq!(query.sort_by, "posted_at");
        assert_eq!(query.sort_order, SortOrder::Desc);
    }

    #[test]
    fn rejects_page_size_outside_allowed_range() {
        let result = ListJobsQuery::try_from(RawListJobsQuery {
            page: None,
            page_size: Some("20".to_owned()),
            source: None,
            city: None,
            district: None,
            employment_type: None,
            job_type: None,
            title: None,
            include_closed: None,
            sort_by: None,
            sort_order: None,
        });

        assert!(matches!(result, Err(ServiceError::InvalidParameter(_))));
    }
}
