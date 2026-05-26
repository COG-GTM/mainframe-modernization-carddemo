"""
Test de verificación: confirma que las sesiones están correctamente aisladas.
Cada test debe ver la BD limpia (solo datos de seed_dora_data si se solicita).
"""
from decimal import Decimal

from app.models.account import Account
from app.models.transaction import Transaction


class TestSessionIsolation:
    """Verificar que cada test tiene su propia sesión aislada."""

    def test_insert_data_in_test_a(self, db_session, seed_dora_data):
        """Test A: insertar una transacción extra."""
        tran = Transaction(
            tran_id="ISOLATION_TEST_A",
            type_cd="SA",
            cat_cd=5001,
            source="TEST",
            description="Should not leak to test B",
            amount=Decimal("999.99"),
            merchant_id=1,
            merchant_name="X",
            merchant_city="X",
            merchant_zip="X",
            card_num="4111111111111111",
            orig_ts="2025-01-01-00.00.00.000000",
            proc_ts="2025-01-01-00.00.01.000000",
        )
        db_session.add(tran)
        db_session.commit()
        assert (
            db_session.query(Transaction)
            .filter_by(tran_id="ISOLATION_TEST_A")
            .first()
            is not None
        )

    def test_data_from_test_a_not_visible(self, db_session, seed_dora_data):
        """Test B: verificar que la transacción de Test A NO existe.
        Si este test pasa, las sesiones están correctamente aisladas."""
        leaked = (
            db_session.query(Transaction)
            .filter_by(tran_id="ISOLATION_TEST_A")
            .first()
        )
        assert (
            leaked is None
        ), "FALLO DE AISLAMIENTO: datos del test anterior se filtraron"

    def test_seed_data_fresh_each_time(self, db_session, seed_dora_data):
        """Verificar que seed_dora_data se inserta fresco en cada test."""
        acct = db_session.get(Account, 11111111111)
        assert acct is not None
        # Modificar el saldo
        acct.curr_bal = Decimal("999999.99")
        db_session.commit()

    def test_seed_data_unmodified(self, db_session, seed_dora_data):
        """Verificar que la modificación del test anterior no persiste."""
        acct = db_session.get(Account, 11111111111)
        assert acct.curr_bal == Decimal("1000.00"), (
            f"FALLO: curr_bal={acct.curr_bal}, esperado 1000.00"
        )
