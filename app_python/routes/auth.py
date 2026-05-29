"""
Authentication routes migrated from COSGN00C.cbl.

Mapping:
  GET  /login  -> SEND-SIGNON-SCREEN (lines 145-157)
  POST /login  -> PROCESS-ENTER-KEY  (lines 108-140)
  GET  /logout -> DFHPF3 / SEND-PLAIN-TEXT (lines 88-90, 162-172)
"""

from functools import wraps

from flask import Blueprint, flash, redirect, render_template, request, url_for
from flask_login import current_user, login_required, login_user, logout_user

from ..config import (
    MSG_ENTER_PASSWORD,
    MSG_ENTER_USER_ID,
    MSG_THANK_YOU,
)
from ..services.auth_service import authenticate_user

auth_bp = Blueprint("auth", __name__)


def admin_required(f):
    """Decorator that ensures the user is an admin (user_type == 'A')."""

    @wraps(f)
    @login_required
    def decorated_function(*args, **kwargs):
        if not current_user.is_admin:
            flash("Access denied. Admin privileges required.")
            return redirect(url_for("auth.user_menu"))
        return f(*args, **kwargs)

    return decorated_function


@auth_bp.route("/login", methods=["GET"])
def login():
    if current_user.is_authenticated:
        if current_user.is_admin:
            return redirect(url_for("auth.admin_menu"))
        return redirect(url_for("auth.user_menu"))
    return render_template("login.html")


@auth_bp.route("/login", methods=["POST"])
def login_post():
    user_id = request.form.get("user_id", "").strip()
    password = request.form.get("password", "").strip()

    # Validate user_id not empty (equivalent to lines 118-122)
    if not user_id:
        flash(MSG_ENTER_USER_ID, "error")
        return render_template("login.html")

    # Validate password not empty (equivalent to lines 123-127)
    if not password:
        flash(MSG_ENTER_PASSWORD, "error")
        return render_template("login.html")

    # Convert to uppercase (equivalent to line 132: MOVE FUNCTION UPPER-CASE)
    user_id = user_id.upper()

    user, error = authenticate_user(user_id, password)

    if user is None:
        flash(error, "error")
        return render_template("login.html")

    login_user(user)

    # Redirect based on user type (equivalent to XCTL calls, lines 231-239)
    if user.is_admin:
        return redirect(url_for("auth.admin_menu"))
    return redirect(url_for("auth.user_menu"))


@auth_bp.route("/logout")
@login_required
def logout():
    logout_user()
    flash(MSG_THANK_YOU, "info")
    return redirect(url_for("auth.login"))


@auth_bp.route("/admin/menu")
@admin_required
def admin_menu():
    return render_template("admin_menu.html")


@auth_bp.route("/menu")
@login_required
def user_menu():
    return render_template("user_menu.html")
