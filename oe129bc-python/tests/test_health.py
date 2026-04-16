"""Tests for the health check endpoint."""

from fastapi.testclient import TestClient


def test_health_check_returns_200(client: TestClient) -> None:
    """GET /health should return 200 with status healthy."""
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "healthy"}


def test_health_check_content_type(client: TestClient) -> None:
    """GET /health should return JSON content type."""
    response = client.get("/health")
    assert response.headers["content-type"] == "application/json"
