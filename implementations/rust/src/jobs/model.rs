//! Domain models and API DTOs for the jobs endpoint.

use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

/// Domain model for a normalized job.
#[derive(Debug, Clone, PartialEq)]
pub struct Job {
    /// Job source platform.
    pub source: String,
    /// Source platform job identifier.
    pub source_job_id: String,
    /// Normalized job title.
    pub title: String,
    /// Company name.
    pub company: Option<String>,
    /// Job detail URL.
    pub url: String,
    /// Posting or update timestamp.
    pub posted_at: Option<DateTime<Utc>>,
    /// Salary information.
    pub salary: Salary,
    /// Job location.
    pub location: Location,
}

/// Salary information in the domain model.
#[derive(Debug, Clone, PartialEq)]
pub struct Salary {
    /// Minimum salary.
    pub minimum: Option<i64>,
    /// Maximum salary.
    pub maximum: Option<i64>,
    /// Salary currency code.
    pub currency: Option<String>,
}

/// Location information in the domain model.
#[derive(Debug, Clone, PartialEq)]
pub struct Location {
    /// City or county name.
    pub city: Option<String>,
    /// District or township name.
    pub district: Option<String>,
}

/// Raw query parameters received from HTTP.
#[derive(Debug, Deserialize)]
pub struct RawListJobsQuery {
    /// Page number as a string for explicit validation messages.
    pub page: Option<String>,
    /// Page size as a string for explicit validation messages.
    pub page_size: Option<String>,
    /// Source filter.
    pub source: Option<String>,
    /// City filter.
    pub city: Option<String>,
    /// District filter.
    pub district: Option<String>,
    /// Employment type filter.
    pub employment_type: Option<String>,
    /// Job type filter.
    pub job_type: Option<String>,
    /// Title full-text search term.
    pub title: Option<String>,
    /// Whether closed jobs are included.
    pub include_closed: Option<String>,
    /// Sort column.
    pub sort_by: Option<String>,
    /// Sort direction.
    pub sort_order: Option<String>,
}

/// Validated jobs query used by service and repository layers.
#[derive(Debug, Clone, PartialEq)]
pub struct ListJobsQuery {
    /// Page number starting at one.
    pub page: i64,
    /// Number of jobs per page.
    pub page_size: i64,
    /// Source filter.
    pub source: Option<String>,
    /// City filter.
    pub city: Option<String>,
    /// District filter.
    pub district: Option<String>,
    /// Employment type filter.
    pub employment_type: Option<String>,
    /// Job type filter.
    pub job_type: Option<String>,
    /// Title full-text search term.
    pub title: Option<String>,
    /// Whether closed jobs are included.
    pub include_closed: bool,
    /// Whitelisted sort column.
    pub sort_by: String,
    /// Validated sort direction.
    pub sort_order: SortOrder,
}

/// Allowed SQL sort direction.
#[derive(Debug, Clone, Copy, PartialEq)]
pub enum SortOrder {
    /// Ascending order.
    Asc,
    /// Descending order.
    Desc,
}

impl SortOrder {
    /// Returns the SQL keyword for this direction.
    pub fn as_sql(self) -> &'static str {
        match self {
            Self::Asc => "ASC",
            Self::Desc => "DESC",
        }
    }
}

/// API response DTO for salary information.
#[derive(Debug, Serialize)]
pub struct SalaryResponse {
    /// Minimum salary.
    pub min: Option<i64>,
    /// Maximum salary.
    pub max: Option<i64>,
    /// Salary currency code.
    pub currency: Option<String>,
}

/// API response DTO for location information.
#[derive(Debug, Serialize)]
pub struct LocationResponse {
    /// City or county name.
    pub city: Option<String>,
    /// District or township name.
    pub district: Option<String>,
}

/// API response DTO for a job.
#[derive(Debug, Serialize)]
pub struct JobResponse {
    /// Job source platform.
    pub source: String,
    /// Source platform job identifier.
    pub source_job_id: String,
    /// Job title.
    pub title: String,
    /// Company name.
    pub company: Option<String>,
    /// Job detail URL.
    pub url: String,
    /// Posting or update timestamp.
    pub posted_at: Option<DateTime<Utc>>,
    /// Salary information.
    pub salary: SalaryResponse,
    /// Location information.
    pub location: LocationResponse,
}

impl From<Job> for JobResponse {
    /// Maps a domain job into the public API DTO.
    fn from(job: Job) -> Self {
        Self {
            source: job.source,
            source_job_id: job.source_job_id,
            title: job.title,
            company: job.company,
            url: job.url,
            posted_at: job.posted_at,
            salary: SalaryResponse {
                min: job.salary.minimum,
                max: job.salary.maximum,
                currency: job.salary.currency,
            },
            location: LocationResponse {
                city: job.location.city,
                district: job.location.district,
            },
        }
    }
}
