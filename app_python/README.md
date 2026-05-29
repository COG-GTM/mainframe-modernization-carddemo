# CardDemo Sign-on Module — Python/Flask Migration

This directory contains the Python/Flask migration of the **Sign-on module** from the CardDemo mainframe COBOL application.

## Migrated Module

**Original**: `COSGN00C.cbl` — CICS COBOL program that handles user authentication for the CardDemo application.

## Component Mapping

| COBOL Component | Python Equivalent | Description |
|---|---|---|
| `COSGN00C.cbl` | `routes/auth.py` + `services/auth_service.py` | Sign-on logic and user verification |
| `COSGN00.bms` | `templates/login.html` | 3270 screen layout → HTML form |
| `CSUSR01Y.cpy` | `models/user.py` | SEC-USER-DATA record → SQLAlchemy model |
| `COTTL01Y.cpy` | `config.py` | Screen titles and constants |
| `CSMSG01Y.cpy` | `config.py` | Common messages |
| `DUSRSECJ` (JCL) | `seed_users.py` | Batch job to load user security file |
| `COADM01C` (XCTL) | `templates/admin_menu.html` | Admin menu (placeholder) |
| `COMEN01C` (XCTL) | `templates/user_menu.html` | User menu (placeholder) |

## How to Run

```bash
# From the repository root directory:

# 1. Install dependencies
pip install -r app_python/requirements.txt

# 2. Seed the database with default users
python app_python/seed_users.py

# 3. Run the application
python -m app_python.app
```

The app will be available at `http://localhost:5000`.

## Default Users

| User ID | Password | Type | Description |
|---|---|---|---|
| `USER0001` | `PASSWORD` | User | Regular user |
| `USER0002` | `PASSWORD` | User | Regular user |
| `ADMIN001` | `PASSWORD` | Admin | Administrator |

## Security Improvements over Original

| Aspect | COBOL Original | Python Migration |
|---|---|---|
| Password storage | Plain text (`PIC X(08)`) | Hashed with Werkzeug (pbkdf2) |
| Password transmission | Clear text over 3270 | HTTPS-ready (password field type) |
| Session management | CICS COMMAREA | Flask-Login with secure cookies |
| Access control | Program-level XCTL | Decorators (`@login_required`, `@admin_required`) |

## Architecture

```
app_python/
├── app.py                 # Flask app factory + configuration
├── config.py              # Constants (migrated from copybooks)
├── models/
│   ├── __init__.py        # SQLAlchemy instance
│   └── user.py            # User model (from CSUSR01Y.cpy)
├── routes/
│   ├── __init__.py
│   └── auth.py            # Auth blueprint (from COSGN00C.cbl)
├── services/
│   ├── __init__.py
│   └── auth_service.py    # Auth logic (READ-USER-SEC-FILE)
├── templates/
│   ├── base.html          # Layout base
│   ├── login.html         # Login screen (from COSGN00.bms)
│   ├── admin_menu.html    # Admin menu placeholder
│   └── user_menu.html     # User menu placeholder
├── static/
│   └── style.css          # 3270 terminal-style CSS
├── seed_users.py          # Database seeder (from DUSRSECJ)
├── requirements.txt       # Python dependencies
└── README.md              # This file
```

## Database

By default, SQLite is used (`sqlite:///carddemo.db`). To use PostgreSQL or another database, set the `DATABASE_URL` environment variable:

```bash
export DATABASE_URL="postgresql://user:pass@localhost/carddemo"
```

## COBOL Logic Flow (for reference)

```
MAIN-PARA
  ├─ First time (EIBCALEN=0) → SEND-SIGNON-SCREEN
  └─ Subsequent
       ├─ ENTER key → PROCESS-ENTER-KEY
       │    ├─ Validate user_id not empty
       │    ├─ Validate password not empty
       │    ├─ Convert to UPPER-CASE
       │    └─ READ-USER-SEC-FILE
       │         ├─ RESP=0 + password match → XCTL to menu
       │         ├─ RESP=0 + password mismatch → "Wrong Password"
       │         ├─ RESP=13 (not found) → "User not found"
       │         └─ Other → "Unable to verify"
       ├─ PF3 key → "Thank you..." + RETURN
       └─ Other key → "Invalid key pressed"
```
