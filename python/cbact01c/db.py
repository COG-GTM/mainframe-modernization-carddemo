"""Database engine / session helpers.

SQLite is used for local development and testing, but because everything goes
through SQLAlchemy the same code runs against Postgres or MySQL simply by
passing a different URL (e.g. ``postgresql+psycopg://user:pw@host/db``).
"""

from __future__ import annotations

import os

from sqlalchemy import Engine, create_engine
from sqlalchemy.orm import Session, sessionmaker

from cbact01c.account import Base

# Default local-dev database. Override with the CBACT01C_DB_URL env var.
DEFAULT_DB_URL = "sqlite:///cbact01c.db"


def get_db_url(db_url: str | None = None) -> str:
    """Resolve the database URL from an explicit arg, env var, or default."""
    return db_url or os.environ.get("CBACT01C_DB_URL") or DEFAULT_DB_URL


def create_db_engine(db_url: str | None = None, *, echo: bool = False) -> Engine:
    """Create a SQLAlchemy engine for the resolved database URL."""
    return create_engine(get_db_url(db_url), echo=echo)


def create_session_factory(engine: Engine) -> sessionmaker[Session]:
    """Create a bound session factory for the given engine."""
    return sessionmaker(bind=engine)


def init_schema(engine: Engine) -> None:
    """Create all tables (accounts) if they do not already exist."""
    Base.metadata.create_all(engine)
