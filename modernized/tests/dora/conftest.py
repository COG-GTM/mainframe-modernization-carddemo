"""
conftest.py - Gestión de sesiones aisladas por test para suite DORA.

Cada test recibe su propia sesión de BD que se revierte al finalizar,
garantizando aislamiento total entre tests.

Patrón: nested transaction (SAVEPOINT) sobre una conexión con transacción abierta.
Ref: https://docs.sqlalchemy.org/en/20/orm/session_transaction.html#joining-a-session-into-an-external-transaction-such-as-for-test-suites
"""
import uuid
from datetime import datetime
from decimal import Decimal

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, event
from sqlalchemy.orm import Session, sessionmaker
from sqlalchemy.pool import StaticPool

from app.database import Base, get_db
from app.main import app
from app.models.account import Account
from app.models.card_xref import CardXref
from app.models.daily_transaction import DailyTransaction
from app.models.tran_cat_balance import TranCatBalance
from app.models.transaction import Transaction


# ---------------------------------------------------------------------------
# Scope=session: engine y tablas se crean UNA sola vez para todo el suite
# ---------------------------------------------------------------------------
@pytest.fixture(scope="session")
def engine():
    """Crea engine SQLite in-memory compartido por todos los tests.
    Usar StaticPool para que SQLite in-memory persista entre conexiones.

    Incluye un listener 'begin' que emite un BEGIN explícito al driver
    pysqlite.  Sin esto, SQLAlchemy 2.0 NO envía BEGIN real a SQLite
    (usa autobegin lógico), lo que hace que los SAVEPOINT actúen como
    transacciones independientes y RELEASE los confirme definitivamente,
    impidiendo el rollback de la conexión externa.
    """
    _engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )

    @event.listens_for(_engine, "begin")
    def _do_begin(dbapi_conn):
        dbapi_conn.exec_driver_sql("BEGIN")

    Base.metadata.create_all(bind=_engine)
    yield _engine
    Base.metadata.drop_all(bind=_engine)
    _engine.dispose()


# ---------------------------------------------------------------------------
# Scope=function: cada test obtiene su propia conexión + transacción + sesión
# ---------------------------------------------------------------------------
@pytest.fixture(scope="function")
def db_connection(engine):
    """Abre una conexión y comienza una transacción para UN test.
    Al finalizar el test, hace rollback y cierra la conexión."""
    connection = engine.connect()
    transaction = connection.begin()
    yield connection
    transaction.rollback()
    connection.close()


@pytest.fixture(scope="function")
def db_session(db_connection):
    """Crea una Session SQLAlchemy ligada a la conexión del test.

    Usa begin_nested() (SAVEPOINT) para que los commits dentro del
    código de la aplicación no persistan — al finalizar, el rollback
    de db_connection revierte todo.

    También maneja el caso donde la aplicación llama session.commit():
    al detectar el commit, reabre automáticamente un nuevo SAVEPOINT.
    """
    session = Session(bind=db_connection, join_transaction_mode="create_savepoint")

    @event.listens_for(session, "after_transaction_end")
    def restart_savepoint(session, transaction):
        if transaction.nested and not transaction._parent.nested:
            session.begin_nested()

    yield session
    session.close()


@pytest.fixture(scope="function")
def client(db_session):
    """TestClient de FastAPI que inyecta la sesión aislada del test.

    Sobreescribe la dependencia get_db para que todos los endpoints
    usen la sesión del test actual en lugar de crear una nueva.
    """

    def _override_get_db():
        try:
            yield db_session
        finally:
            pass  # No cerrar — lo gestiona el fixture db_session

    app.dependency_overrides[get_db] = _override_get_db
    with TestClient(app) as test_client:
        yield test_client
    app.dependency_overrides.clear()


# ---------------------------------------------------------------------------
# Datos semilla DORA — se insertan frescos en cada test que los solicite
# porque la sesión se revierte al finalizar cada test
# ---------------------------------------------------------------------------
@pytest.fixture(scope="function")
def seed_dora_data(db_session):
    """Inserta datos base para tests DORA en la sesión aislada del test.

    Al finalizar el test, db_session hace rollback y estos datos
    desaparecen automáticamente — no contaminan otros tests.

    Datos:
    - 3 cuentas: normal, al límite, expirada
    - 3 card_xrefs vinculando tarjetas a cada cuenta
    """
    # Cuenta normal
    account_normal = Account(
        acct_id=11111111111,
        active_status="Y",
        curr_bal=Decimal("1000.00"),
        credit_limit=Decimal("10000.00"),
        cash_credit_limit=Decimal("5000.00"),
        open_date="2020-01-01",
        expiration_date="2030-12-31",
        reissue_date="2025-01-01",
        curr_cyc_credit=Decimal("500.00"),
        curr_cyc_debit=Decimal("200.00"),
        addr_zip="10001",
        group_id="GRP001",
    )
    # Cuenta al límite
    account_limit = Account(
        acct_id=22222222222,
        active_status="Y",
        curr_bal=Decimal("95.00"),
        credit_limit=Decimal("100.00"),
        cash_credit_limit=Decimal("50.00"),
        open_date="2020-01-01",
        expiration_date="2030-12-31",
        reissue_date="2025-01-01",
        curr_cyc_credit=Decimal("95.00"),
        curr_cyc_debit=Decimal("0.00"),
        addr_zip="10002",
        group_id="GRP001",
    )
    # Cuenta expirada
    account_expired = Account(
        acct_id=33333333333,
        active_status="Y",
        curr_bal=Decimal("500.00"),
        credit_limit=Decimal("10000.00"),
        cash_credit_limit=Decimal("5000.00"),
        open_date="2018-01-01",
        expiration_date="2020-01-01",
        reissue_date="2019-01-01",
        curr_cyc_credit=Decimal("500.00"),
        curr_cyc_debit=Decimal("0.00"),
        addr_zip="10003",
        group_id="GRP002",
    )
    db_session.add_all([account_normal, account_limit, account_expired])
    db_session.flush()

    # Cross-references tarjeta → cuenta
    xref_normal = CardXref(
        card_num="4111111111111111", cust_id=100000001, acct_id=11111111111
    )
    xref_limit = CardXref(
        card_num="4222222222222222", cust_id=100000002, acct_id=22222222222
    )
    xref_expired = CardXref(
        card_num="4333333333333333", cust_id=100000003, acct_id=33333333333
    )
    db_session.add_all([xref_normal, xref_limit, xref_expired])
    db_session.flush()

    return {
        "accounts": {
            "normal": account_normal,
            "limit": account_limit,
            "expired": account_expired,
        },
        "xrefs": {
            "normal": xref_normal,
            "limit": xref_limit,
            "expired": xref_expired,
        },
    }


# ---------------------------------------------------------------------------
# Helper reutilizable para crear transacciones de test
# ---------------------------------------------------------------------------
def make_daily_transaction(
    card_num="4111111111111111", amount=100.00, **overrides
):
    """Genera un dict de transacción diaria para enviar al endpoint POST.

    Mapea la estructura DALYTRAN-RECORD del copybook CVTRA06Y.cpy:
    - tran_id: PIC X(16)
    - type_cd: PIC X(02)
    - cat_cd: PIC 9(04)
    - amount: PIC S9(09)V99
    - card_num: PIC X(16)
    - orig_ts: PIC X(26)
    """
    base = {
        "tran_id": uuid.uuid4().hex[:16],
        "type_cd": "SA",
        "cat_cd": 5001,
        "source": "ONLINE",
        "description": "DORA TEST TRANSACTION",
        "amount": float(amount),
        "merchant_id": 123456789,
        "merchant_name": "DORA TEST MERCHANT",
        "merchant_city": "FRANKFURT",
        "merchant_zip": "60311",
        "card_num": card_num,
        "orig_ts": datetime.now().strftime("%Y-%m-%d-%H.%M.%S.000000"),
    }
    base.update(overrides)
    return base


# ---------------------------------------------------------------------------
# Metadata DORA para informes de cumplimiento
# ---------------------------------------------------------------------------
@pytest.fixture(scope="function")
def dora_metadata():
    """Metadata DORA para trazabilidad en informes de cumplimiento."""
    return {
        "regulation": "Reglamento (UE) 2022/2554 (DORA)",
        "entity_type": "Entidad financiera - procesamiento de tarjetas de crédito",
        "system": "CardDemo Transaction Posting API (migrado de CBTRN02C.cbl)",
        "test_date": datetime.now().isoformat(),
    }
