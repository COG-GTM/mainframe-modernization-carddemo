"""Python migration of the CardDemo batch program CBACT01C.

CBACT01C is a standalone mainframe batch COBOL program that reads the account
master VSAM KSDS file sequentially in ACCT-ID order and prints every record.

This package reimplements that behaviour on top of a relational database using
SQLAlchemy (SQLite for local development, easily repointed at Postgres/MySQL).
"""

from cbact01c.account import Account, Base

__all__ = ["Account", "Base"]
