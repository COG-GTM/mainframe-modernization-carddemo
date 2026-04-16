"""Tests for FastAPI application configuration."""

from fastapi.testclient import TestClient

from oe129bc.app import create_app


def test_openapi_docs_available(client: TestClient) -> None:
    """OpenAPI docs should be served at /docs."""
    response = client.get("/docs")
    assert response.status_code == 200


def test_redoc_available(client: TestClient) -> None:
    """ReDoc should be served at /redoc."""
    response = client.get("/redoc")
    assert response.status_code == 200


def test_openapi_json_available(client: TestClient) -> None:
    """OpenAPI JSON schema should be available."""
    response = client.get("/openapi.json")
    assert response.status_code == 200
    data = response.json()
    assert data["info"]["title"] == "OE129BC Validation Service"
    assert data["info"]["version"] == "0.1.0"


def test_cors_headers(client: TestClient) -> None:
    """CORS headers should be present for cross-origin requests."""
    response = client.options(
        "/health",
        headers={
            "Origin": "http://localhost:3000",
            "Access-Control-Request-Method": "GET",
        },
    )
    assert response.headers.get("access-control-allow-origin") == "http://localhost:3000"


def test_app_factory_creates_new_instance() -> None:
    """create_app should return a new FastAPI instance each time."""
    app1 = create_app()
    app2 = create_app()
    assert app1 is not app2
