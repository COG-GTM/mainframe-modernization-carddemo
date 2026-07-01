"""Shared fixtures for CBACT01C tests."""

from __future__ import annotations

import os
from pathlib import Path

import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import Session

from cbact01c.models import Base

FIXTURES_DIR = Path(__file__).parent / "fixtures"
SAMPLE_DATA = Path(__file__).resolve().parents[3] / "app" / "data" / "ASCII" / "acctdata.txt"

TEST_DSN = os.environ.get("TEST_DATABASE_URL", "postgresql:///carddemo_test")


@pytest.fixture(scope="session")
def engine():
    """Create a test database engine and set up the schema once per session."""
    from sqlalchemy_utils import create_database, database_exists  # noqa: F811

    eng = create_engine(TEST_DSN)
    if not database_exists(eng.url):
        create_database(eng.url)
    Base.metadata.drop_all(eng)
    Base.metadata.create_all(eng)
    yield eng
    Base.metadata.drop_all(eng)
    eng.dispose()


@pytest.fixture()
def db_session(engine):
    """Provide a transactional session that rolls back after each test."""
    connection = engine.connect()
    transaction = connection.begin()
    session = Session(bind=connection)

    yield session

    session.close()
    transaction.rollback()
    connection.close()
