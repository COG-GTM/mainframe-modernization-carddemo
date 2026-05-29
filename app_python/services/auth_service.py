"""
Authentication service migrated from COSGN00C.cbl READ-USER-SEC-FILE paragraph
(lines 209-257).

This replicates the VSAM KSDS READ logic that looks up a user by their ID
and verifies the password.
"""

from ..config import MSG_USER_NOT_FOUND, MSG_WRONG_PASSWORD, MSG_UNABLE_VERIFY
from ..models import db
from ..models.user import User


def authenticate_user(user_id: str, password: str) -> tuple:
    """
    Replica of READ-USER-SEC-FILE from COSGN00C.cbl.

    Returns (user, error_message).
    If user is None, error_message contains the reason.
    """
    try:
        user = db.session.get(User, user_id.upper())
    except Exception:
        return None, MSG_UNABLE_VERIFY

    if user is None:
        # Equivalent to WHEN 13 (record not found) in COBOL
        return None, MSG_USER_NOT_FOUND

    if not user.check_password(password):
        # Equivalent to ELSE branch (wrong password) in COBOL
        return None, MSG_WRONG_PASSWORD

    # Equivalent to WHEN 0 + password match
    return user, ""
