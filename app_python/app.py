"""
Flask application factory for the CardDemo Sign-on module.

Migrated from: COSGN00C.cbl (CICS COBOL Program)
Original function: Signon Screen for the CardDemo Application
"""

from datetime import datetime

from flask import Flask
from flask_login import LoginManager

from .config import (
    PROGRAM_NAME,
    SECRET_KEY,
    SQLALCHEMY_DATABASE_URI,
    TITLE01,
    TITLE02,
    TRANSACTION_ID,
)
from .models import db
from .models.user import User
from .routes.auth import auth_bp


def create_app():
    app = Flask(__name__)

    app.config["SECRET_KEY"] = SECRET_KEY
    app.config["SQLALCHEMY_DATABASE_URI"] = SQLALCHEMY_DATABASE_URI
    app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False

    db.init_app(app)

    login_manager = LoginManager()
    login_manager.login_view = "auth.login"
    login_manager.init_app(app)

    @login_manager.user_loader
    def load_user(user_id):
        return db.session.get(User, user_id)

    app.register_blueprint(auth_bp)

    @app.context_processor
    def inject_header_info():
        """Equivalent to POPULATE-HEADER-INFO in COSGN00C.cbl."""
        now = datetime.now()
        return {
            "config_tran": TRANSACTION_ID,
            "config_pgm": PROGRAM_NAME,
            "config_title01": TITLE01,
            "config_title02": TITLE02,
            "current_date": now.strftime("%m/%d/%y"),
            "current_time": now.strftime("%H:%M:%S"),
        }

    with app.app_context():
        db.create_all()

    return app


if __name__ == "__main__":
    app = create_app()
    app.run(debug=True, host="0.0.0.0", port=5000)
