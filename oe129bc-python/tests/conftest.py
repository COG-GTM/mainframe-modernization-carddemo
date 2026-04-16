"""Shared test fixtures for OE129BC tests."""

import pytest
from fastapi.testclient import TestClient

from oe129bc.app import create_app


@pytest.fixture()
def client() -> TestClient:
    """Create a test client for the FastAPI application."""
    app = create_app()
    return TestClient(app)
