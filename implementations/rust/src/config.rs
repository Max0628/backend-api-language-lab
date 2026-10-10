//! Application configuration loaded from environment variables.

use std::env;

use thiserror::Error;

/// Runtime settings required by the API server.
#[derive(Debug, Clone)]
pub struct Settings {
    /// PostgreSQL connection URL.
    pub database_url: String,
    /// HTTP server port.
    pub server_port: u16,
}

/// Configuration loading errors.
#[derive(Debug, Error)]
pub enum ConfigError {
    /// A required environment variable was not configured.
    #[error("{0} is required")]
    Missing(&'static str),
    /// An environment variable could not be parsed.
    #[error("{name} must be a valid {kind}")]
    Invalid {
        /// Environment variable name.
        name: &'static str,
        /// Expected value type.
        kind: &'static str,
    },
}

impl Settings {
    /// Loads settings from the process environment.
    pub fn from_env() -> Result<Self, ConfigError> {
        let database_url = env::var("DATABASE_URL")
            .map_err(|_| ConfigError::Missing("DATABASE_URL"))?;
        let server_port = env::var("SERVER_PORT")
            .unwrap_or_else(|_| "8080".to_owned())
            .parse()
            .map_err(|_| ConfigError::Invalid {
                name: "SERVER_PORT",
                kind: "port",
            })?;
        if server_port == 0 {
            return Err(ConfigError::Invalid {
                name: "SERVER_PORT",
                kind: "port",
            });
        }

        Ok(Self {
            database_url,
            server_port,
        })
    }
}
