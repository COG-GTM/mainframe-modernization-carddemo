"""
Seed script for initial user data.

Equivalent to the batch job DUSRSECJ that loads the USRSEC VSAM file
from the flat file MFE.CARDDEMO.USRSEC.PS.

Usage:
    python seed_users.py
"""

import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app_python.app import create_app
from app_python.models import db
from app_python.models.user import User

SEED_USERS = [
    {
        "user_id": "USER0001",
        "first_name": "FIRST01",
        "last_name": "LAST01",
        "password": "PASSWORD",
        "user_type": "U",
    },
    {
        "user_id": "USER0002",
        "first_name": "FIRST02",
        "last_name": "LAST02",
        "password": "PASSWORD",
        "user_type": "U",
    },
    {
        "user_id": "ADMIN001",
        "first_name": "ADMIN",
        "last_name": "ADMIN",
        "password": "PASSWORD",
        "user_type": "A",
    },
]


def seed():
    app = create_app()
    with app.app_context():
        db.create_all()
        for data in SEED_USERS:
            existing = db.session.get(User, data["user_id"])
            if existing:
                print(f"  User {data['user_id']} already exists, skipping.")
                continue
            user = User(
                user_id=data["user_id"],
                first_name=data["first_name"],
                last_name=data["last_name"],
                user_type=data["user_type"],
            )
            user.set_password(data["password"])
            db.session.add(user)
            print(f"  Added user: {data['user_id']} (type={data['user_type']})")
        db.session.commit()
        print("Seed completed successfully.")


if __name__ == "__main__":
    seed()
