from decimal import Decimal
from datetime import datetime
from typing import Dict, Any
from .models import TransactionRecord
from .data_store import data_store


class BillPaymentService:
    
    def validate_account(self, acct_id: str) -> Dict[str, Any]:
        if not acct_id or acct_id.strip() == "":
            return {
                "success": False,
                "error": "Acct ID can NOT be empty..."
            }
        
        account = data_store.get_account(acct_id)
        if account is None:
            return {
                "success": False,
                "error": "Account ID NOT found..."
            }
        
        if account.acct_curr_bal <= Decimal("0.00"):
            return {
                "success": False,
                "error": "You have nothing to pay..."
            }
        
        return {
            "success": True,
            "account": account,
            "current_balance": float(account.acct_curr_bal)
        }
    
    def process_payment(self, acct_id: str, confirm: str) -> Dict[str, Any]:
        if confirm.upper() not in ['Y', 'YES']:
            return {
                "success": False,
                "error": "Confirm to make a bill payment..."
            }
        
        validation = self.validate_account(acct_id)
        if not validation["success"]:
            return validation
        
        account = validation["account"]
        
        card_xref = data_store.get_card_xref(acct_id)
        if card_xref is None:
            return {
                "success": False,
                "error": "Account ID NOT found in cross-reference..."
            }
        
        highest_tran_id = data_store.get_highest_transaction_id()
        new_tran_id = str(highest_tran_id + 1).zfill(16)
        
        current_ts = datetime.now().strftime("%Y-%m-%d-%H:%M:%S.000000")
        
        transaction = TransactionRecord(
            tran_id=new_tran_id,
            tran_type_cd="02",
            tran_cat_cd=2,
            tran_source="POS TERM",
            tran_desc="BILL PAYMENT - ONLINE",
            tran_amt=account.acct_curr_bal,
            tran_merchant_id=999999999,
            tran_merchant_name="BILL PAYMENT",
            tran_merchant_city="N/A",
            tran_merchant_zip="N/A",
            tran_card_num=card_xref.xref_card_num,
            tran_orig_ts=current_ts,
            tran_proc_ts=current_ts
        )
        
        data_store.write_transaction(transaction)
        
        payment_amount = account.acct_curr_bal
        account.acct_curr_bal = Decimal("0.00")
        data_store.update_account(account)
        
        return {
            "success": True,
            "message": f"Payment successful. Your Transaction ID is {new_tran_id}.",
            "transaction_id": new_tran_id,
            "amount_paid": float(payment_amount)
        }


bill_payment_service = BillPaymentService()
