"""CardDemo Account Lookup Service — FastAPI application entry point.

Python 3.12 / FastAPI port of the COBOL COACTVWC.cbl account-view program.
"""

from fastapi import FastAPI

from app.routers.accounts import router as accounts_router

app = FastAPI(
    title="CardDemo Account Lookup Service",
    description=(
        "Python/FastAPI port of the OE129BC Account Lookup Service "
        "(COACTVWC.cbl). Resolves card numbers to accounts and customers "
        "via a multi-file cross-reference chain."
    ),
    version="0.1.0",
)

app.include_router(accounts_router)


@app.get("/health", tags=["health"])
def health_check() -> dict[str, str]:
    """Liveness probe."""
    return {"status": "ok"}
