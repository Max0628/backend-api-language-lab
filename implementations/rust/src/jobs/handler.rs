//! Axum HTTP handler and error response mapping for jobs.

use std::sync::Arc;

use axum::{extract::Query, extract::State, http::StatusCode, response::IntoResponse, Json};
use serde::Serialize;
use tracing::error;

use super::model::{JobResponse, RawListJobsQuery};
use super::service::{JobsService, ServiceError};

/// Shared application state for jobs routes.
#[derive(Clone)]
pub struct AppState {
    /// Jobs application service.
    pub service: Arc<JobsService>,
}

/// Handles `GET /jobs`.
pub async fn list_jobs(
    State(state): State<AppState>,
    Query(query): Query<RawListJobsQuery>,
) -> Result<Json<Vec<JobResponse>>, ApiError> {
    let jobs = state.service.list(query).await.map_err(|error| {
        if matches!(error, ServiceError::InvalidParameter(_)) {
            return ApiError::invalid_parameter(error.to_string());
        }
        error!(?error, "list jobs failed");
        ApiError::internal_server_error()
    })?;

    Ok(Json(jobs.into_iter().map(JobResponse::from).collect()))
}

/// Standard API error response.
#[derive(Debug)]
pub struct ApiError {
    status: StatusCode,
    body: ErrorBody,
}

#[derive(Debug, Serialize)]
struct ErrorBody {
    error: ErrorDetails,
}

#[derive(Debug, Serialize)]
struct ErrorDetails {
    code: &'static str,
    message: String,
}

impl ApiError {
    fn invalid_parameter(message: String) -> Self {
        Self {
            status: StatusCode::BAD_REQUEST,
            body: ErrorBody {
                error: ErrorDetails {
                    code: "INVALID_PARAMETER",
                    message,
                },
            },
        }
    }

    fn internal_server_error() -> Self {
        Self {
            status: StatusCode::INTERNAL_SERVER_ERROR,
            body: ErrorBody {
                error: ErrorDetails {
                    code: "INTERNAL_SERVER_ERROR",
                    message: "failed to query jobs".to_owned(),
                },
            },
        }
    }
}

impl IntoResponse for ApiError {
    fn into_response(self) -> axum::response::Response {
        (self.status, Json(self.body)).into_response()
    }
}
