from typing import Dict, Optional
from .models import AccountRecord, CardXrefRecord, TransactionRecord
from decimal import Decimal


class DataStore:
    
    def __init__(self):
        self.accounts: Dict[str, AccountRecord] = {}
        self.card_xrefs: Dict[str, CardXrefRecord] = {}
        self.transactions: Dict[str, TransactionRecord] = {}
    
    def get_account(self, acct_id: str) -> Optional[AccountRecord]:
        return self.accounts.get(acct_id)
    
    def update_account(self, account: AccountRecord) -> None:
        self.accounts[account.acct_id] = account
    
    def get_card_xref(self, acct_id: str) -> Optional[CardXrefRecord]:
        return self.card_xrefs.get(acct_id)
    
    def get_highest_transaction_id(self) -> int:
        if not self.transactions:
            return 0
        return max(int(tran_id) for tran_id in self.transactions.keys())
    
    def write_transaction(self, transaction: TransactionRecord) -> None:
        self.transactions[transaction.tran_id] = transaction
    
    def add_sample_data(self) -> None:
        self.accounts["12345678901"] = AccountRecord(
            acct_id="12345678901",
            acct_active_status="Y",
            acct_curr_bal=Decimal("1500.50"),
            acct_credit_limit=Decimal("5000.00"),
            acct_cash_credit_limit=Decimal("1000.00"),
            acct_open_date="2020-01-15",
            acct_expiration_date="2025-01-15",
            acct_reissue_date="2020-01-15",
            acct_curr_cyc_credit=Decimal("0.00"),
            acct_curr_cyc_debit=Decimal("1500.50"),
            acct_addr_zip="12345",
            acct_group_id="GROUP001"
        )
        
        self.card_xrefs["12345678901"] = CardXrefRecord(
            xref_card_num="4111111111111111",
            xref_cust_id="000000001",
            xref_acct_id="12345678901"
        )
        
        self.accounts["98765432109"] = AccountRecord(
            acct_id="98765432109",
            acct_active_status="Y",
            acct_curr_bal=Decimal("2500.75"),
            acct_credit_limit=Decimal("10000.00"),
            acct_cash_credit_limit=Decimal("2000.00"),
            acct_open_date="2019-06-20",
            acct_expiration_date="2024-06-20",
            acct_reissue_date="2019-06-20",
            acct_curr_cyc_credit=Decimal("0.00"),
            acct_curr_cyc_debit=Decimal("2500.75"),
            acct_addr_zip="54321",
            acct_group_id="GROUP002"
        )
        
        self.card_xrefs["98765432109"] = CardXrefRecord(
            xref_card_num="4222222222222222",
            xref_cust_id="000000002",
            xref_acct_id="98765432109"
        )
        
        self.accounts["00000000000"] = AccountRecord(
            acct_id="00000000000",
            acct_active_status="Y",
            acct_curr_bal=Decimal("0.00"),
            acct_credit_limit=Decimal("3000.00"),
            acct_cash_credit_limit=Decimal("500.00"),
            acct_open_date="2023-03-10",
            acct_expiration_date="2028-03-10",
            acct_reissue_date="2023-03-10",
            acct_curr_cyc_credit=Decimal("0.00"),
            acct_curr_cyc_debit=Decimal("0.00"),
            acct_addr_zip="99999",
            acct_group_id="GROUP003"
        )
        
        self.card_xrefs["00000000000"] = CardXrefRecord(
            xref_card_num="4333333333333333",
            xref_cust_id="000000003",
            xref_acct_id="00000000000"
        )


data_store = DataStore()
