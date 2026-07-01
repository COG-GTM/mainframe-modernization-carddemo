"""Database engine and session factory."""

import os

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker

DEFAULT_DSN = "postgresql://localhost/carddemo"


def get_engine(dsn: str | None = None):
    """Create a SQLAlchemy engine from *dsn* or the ``DATABASE_URL`` env var."""
    dsn = dsn or os.environ.get("DATABASE_URL", DEFAULT_DSN)
    return create_engine(dsn, echo=False)


def get_session(dsn: str | None = None) -> Session:
    """Return a new Session bound to the engine."""
    engine = get_engine(dsn)
    factory = sessionmaker(bind=engine)
    return factory()
