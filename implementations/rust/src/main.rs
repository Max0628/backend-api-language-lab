//! Rust + Axum implementation of the shared jobs API.

use std::{net::SocketAddr, sync::Arc};

use axum::{routing::get, Router};
use sqlx::postgres::PgPoolOptions;
use tokio::net::TcpListener;
use tracing::info;

mod config;
mod jobs;

use config::Settings;
use jobs::handler::{list_jobs, AppState};
use jobs::repository::PostgresJobsRepository;
use jobs::service::JobsService;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    tracing_subscriber::fmt::init();

    let settings = Settings::from_env()?;
    let pool = PgPoolOptions::new()
        .max_connections(10)
        .connect(&settings.database_url)
        .await?;
    let repository = PostgresJobsRepository::new(pool);
    let service = Arc::new(JobsService::new(Arc::new(repository)));
    let state = AppState { service };
    let application = Router::new()
        .route("/jobs", get(list_jobs))
        .with_state(state);
    let address = SocketAddr::from(([0, 0, 0, 0], settings.server_port));
    let listener = TcpListener::bind(address).await?;

    info!(%address, "server listening");
    axum::serve(listener, application)
        .with_graceful_shutdown(shutdown_signal())
        .await?;

    Ok(())
}

async fn shutdown_signal() {
    let _ = tokio::signal::ctrl_c().await;
    info!("shutdown signal received");
}
