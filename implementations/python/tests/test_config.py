"""Tests for application configuration."""

import pytest

from app.config import load_settings


def test_load_settings_requires_database_url(monkeypatch: pytest.MonkeyPatch) -> None:
    """Verify that DATABASE_URL is required."""
    monkeypatch.delenv("DATABASE_URL", raising=False)

    with pytest.raises(ValueError, match="DATABASE_URL is required"):
        load_settings()


def test_load_settings_uses_default_port(monkeypatch: pytest.MonkeyPatch) -> None:
    """Verify that SERVER_PORT defaults to 8080."""
    monkeypatch.setenv("DATABASE_URL", "postgresql://localhost/job_radar")
    monkeypatch.delenv("SERVER_PORT", raising=False)

    settings = load_settings()

    assert settings.server_port == 8080
