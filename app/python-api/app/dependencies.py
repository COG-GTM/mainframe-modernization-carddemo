"""FastAPI dependency injection wiring.

Provides factory functions that the router uses via ``Depends()``.
The repository instance is module-level so it can be replaced in tests.
"""

from app.repositories.account_repository import AccountRepository, InMemoryAccountRepository
from app.services.account_lookup_service import AccountLookupService

# Default repository — swap via ``set_repository()`` for testing or production.
_repository: AccountRepository = InMemoryAccountRepository()


def set_repository(repo: AccountRepository) -> None:
    """Replace the active repository (used by tests and app startup)."""
    global _repository  # noqa: PLW0603
    _repository = repo


def get_repository() -> AccountRepository:
    """Return the current repository instance."""
    return _repository


def get_account_lookup_service() -> AccountLookupService:
    """Build the service with the current repository."""
    return AccountLookupService(_repository)
