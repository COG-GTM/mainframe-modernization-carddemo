"""
User model migrated from CSUSR01Y.cpy (SEC-USER-DATA record layout).

Original COBOL layout:
  01 SEC-USER-DATA.
    05 SEC-USR-ID      PIC X(08).
    05 SEC-USR-FNAME   PIC X(20).
    05 SEC-USR-LNAME   PIC X(20).
    05 SEC-USR-PWD     PIC X(08).
    05 SEC-USR-TYPE    PIC X(01).
    05 SEC-USR-FILLER  PIC X(23).
"""

from flask_login import UserMixin
from werkzeug.security import generate_password_hash, check_password_hash

from . import db


class User(db.Model, UserMixin):
    __tablename__ = "users"

    user_id = db.Column(db.String(8), primary_key=True)
    first_name = db.Column(db.String(20), nullable=False)
    last_name = db.Column(db.String(20), nullable=False)
    password_hash = db.Column(db.String(256), nullable=False)
    user_type = db.Column(db.String(1), nullable=False)

    def get_id(self):
        return self.user_id

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)

    @property
    def is_admin(self):
        return self.user_type == "A"
