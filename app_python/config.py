"""
Configuration constants migrated from COBOL copybooks.

Source copybooks:
  - COTTL01Y.cpy  -> Screen titles
  - CSMSG01Y.cpy  -> Common messages
  - COSGN00C.cbl  -> Sign-on specific messages
"""

import os

# --- From COTTL01Y.cpy ---
TITLE01 = "Mainframe Modernization"
TITLE02 = "CardDemo"
THANK_YOU = "Thank you for using CCDA application..."

# --- From CSMSG01Y.cpy ---
MSG_THANK_YOU = "Thank you for using CardDemo application..."
MSG_INVALID_KEY = "Invalid key pressed. Please see below..."

# --- Sign-on messages from COSGN00C.cbl ---
MSG_ENTER_USER_ID = "Please enter User ID ..."
MSG_ENTER_PASSWORD = "Please enter Password ..."
MSG_WRONG_PASSWORD = "Wrong Password. Try again ..."
MSG_USER_NOT_FOUND = "User not found. Try again ..."
MSG_UNABLE_VERIFY = "Unable to verify the User ..."

# --- Application metadata (from COSGN00C.cbl WORKING-STORAGE) ---
PROGRAM_NAME = "COSGN00C"
TRANSACTION_ID = "CC00"

# --- Database configuration ---
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
SQLALCHEMY_DATABASE_URI = os.environ.get(
    "DATABASE_URL",
    f"sqlite:///{os.path.join(BASE_DIR, 'carddemo.db')}",
)
SECRET_KEY = os.environ.get("SECRET_KEY", "carddemo-dev-secret-key")
