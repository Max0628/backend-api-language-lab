"""定義並載入 Python API 的環境變數設定。"""

from dataclasses import dataclass
import os


@dataclass(frozen=True)
class Settings:
    """Runtime settings required by the API server."""

    database_url: str
    server_port: int


def load_settings() -> Settings:
    """Load and validate server settings from environment variables."""
    database_url = os.getenv("DATABASE_URL")
    if not database_url:
        raise ValueError("DATABASE_URL is required")

    raw_port = os.getenv("SERVER_PORT", "8080")
    try:
        server_port = int(raw_port)
    except ValueError as exc:
        raise ValueError("SERVER_PORT must be an integer") from exc

    if not 1 <= server_port <= 65535:
        raise ValueError("SERVER_PORT must be between 1 and 65535")

    return Settings(database_url=database_url, server_port=server_port)
