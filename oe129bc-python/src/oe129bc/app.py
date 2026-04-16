"""FastAPI application factory for OE129BC."""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from oe129bc.api.routes import router


def create_app() -> FastAPI:
    """Create and configure the FastAPI application."""
    app = FastAPI(
        title="OE129BC Validation Service",
        description="Python/FastAPI port of the OE129BC COBOL validation program",
        version="0.1.0",
        docs_url="/docs",
        redoc_url="/redoc",
    )

    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.include_router(router)

    return app


app = create_app()
