"""Transaction validation rules ported from COTRN02C.cbl.

Validation rules extracted from the VALIDATE-INPUT-DATA-FIELDS and
VALIDATE-INPUT-KEY-FIELDS paragraphs of COTRN02C.cbl:

1. Transaction ID: 16-char, must not be empty
2. Type code: 2-char, must not be empty, must be numeric
3. Category code: 4-digit, must not be empty, must be numeric
4. Source: must not be empty
5. Description: must not be empty
6. Amount: must not be empty, format sign(+/-) + 8 digits + '.' + 2 digits
7. Origination date: must not be empty, YYYY-MM-DD format with position validation
8. Processing date: must not be empty, YYYY-MM-DD format with position validation
9. Merchant ID: must not be empty, must be numeric
10. Merchant Name: must not be empty
11. Merchant City: must not be empty
12. Merchant Zip: must not be empty
13. Card Number: must not be empty
"""

from dataclasses import dataclass, field
from datetime import datetime
from decimal import Decimal

from oe129bc.models.transaction import TransactionRecord

# Maximum amount that can be stored in PIC S9(09)V99
MAX_AMOUNT = Decimal("999999999.99")
MIN_AMOUNT = Decimal("-999999999.99")


@dataclass
class FieldValidationError:
    """A single field-level validation error."""

    field_name: str
    message: str
    rule: str


@dataclass
class ValidationResult:
    """Result of validating a TransactionRecord."""

    is_valid: bool
    errors: list[FieldValidationError] = field(default_factory=list)

    def add_error(self, field_name: str, message: str, rule: str) -> None:
        self.errors.append(FieldValidationError(field_name=field_name, message=message, rule=rule))
        self.is_valid = False


def _is_empty(value: str | None) -> bool:
    """Check if a string value is empty or whitespace (COBOL SPACES/LOW-VALUES)."""
    return value is None or str(value).strip() == ""


def _validate_tran_id(record: TransactionRecord, result: ValidationResult) -> None:
    """Transaction ID: 16-char, must not be empty."""
    if _is_empty(record.tran_id):
        result.add_error("tran_id", "Transaction ID can NOT be empty", "required")
        return
    if len(record.tran_id) > 16:
        result.add_error("tran_id", "Transaction ID must not exceed 16 characters", "max_length")


def _validate_type_cd(record: TransactionRecord, result: ValidationResult) -> None:
    """Type CD: 2-char, must not be empty, must be numeric (COTRN02C lines 252-258, 323-328)."""
    if _is_empty(record.tran_type_cd):
        result.add_error("tran_type_cd", "Type CD can NOT be empty", "required")
        return
    if not record.tran_type_cd.isdigit():
        result.add_error("tran_type_cd", "Type CD must be Numeric", "numeric")
    if len(record.tran_type_cd) > 2:
        result.add_error("tran_type_cd", "Type CD must not exceed 2 characters", "max_length")


def _validate_cat_cd(record: TransactionRecord, result: ValidationResult) -> None:
    """Category CD: 4-digit, must not be empty, must be numeric (COTRN02C lines 258-263, 329-334)."""
    if _is_empty(record.tran_cat_cd):
        result.add_error("tran_cat_cd", "Category CD can NOT be empty", "required")
        return
    if not record.tran_cat_cd.isdigit():
        result.add_error("tran_cat_cd", "Category CD must be Numeric", "numeric")
    if len(record.tran_cat_cd) > 4:
        result.add_error("tran_cat_cd", "Category CD must not exceed 4 characters", "max_length")


def _validate_source(record: TransactionRecord, result: ValidationResult) -> None:
    """Source: must not be empty (COTRN02C lines 264-269)."""
    if _is_empty(record.tran_source):
        result.add_error("tran_source", "Source can NOT be empty", "required")


def _validate_desc(record: TransactionRecord, result: ValidationResult) -> None:
    """Description: must not be empty (COTRN02C lines 270-275)."""
    if _is_empty(record.tran_desc):
        result.add_error("tran_desc", "Description can NOT be empty", "required")


def _validate_amount(record: TransactionRecord, result: ValidationResult) -> None:
    """Amount: must be a valid decimal within PIC S9(09)V99 range.

    COBOL validation (COTRN02C lines 339-351):
    Position 1: sign (+/-)
    Positions 2-9: 8 numeric digits
    Position 10: decimal point '.'
    Positions 11-12: 2 numeric digits
    """
    if record.tran_amt is None:
        result.add_error("tran_amt", "Amount can NOT be empty", "required")
        return

    if record.tran_amt > MAX_AMOUNT:
        result.add_error("tran_amt", "Amount exceeds maximum value (999999999.99)", "range")
    elif record.tran_amt < MIN_AMOUNT:
        result.add_error("tran_amt", "Amount below minimum value (-999999999.99)", "range")

    # Check decimal precision (at most 2 decimal places)
    _, _, exponent = record.tran_amt.as_tuple()
    if isinstance(exponent, int) and exponent < -2:
        result.add_error("tran_amt", "Amount must have at most 2 decimal places", "precision")


def validate_amount_format(amount_str: str) -> list[str]:
    """Validate amount string matches COBOL input format: +99999999.99.

    This implements the exact position-level checks from COTRN02C.cbl lines 340-343:
        WHEN TRNAMTI(1:1) NOT EQUAL '-' AND '+'
        WHEN TRNAMTI(2:8) NOT NUMERIC
        WHEN TRNAMTI(10:1) NOT = '.'
        WHEN TRNAMTI(11:2) IS NOT NUMERIC
    """
    errors = []
    if len(amount_str) < 12:
        errors.append("Amount should be in format +99999999.99")
        return errors

    if amount_str[0] not in "+-":
        errors.append("Amount position 1 must be sign (+/-)")
    if not amount_str[1:9].isdigit():
        errors.append("Amount positions 2-9 must be numeric digits")
    if amount_str[9] != ".":
        errors.append("Amount position 10 must be decimal point '.'")
    if not amount_str[10:12].isdigit():
        errors.append("Amount positions 11-12 must be numeric digits")

    return errors


def _validate_date_format(value: str, field_name: str, label: str, result: ValidationResult) -> None:
    """Validate date format YYYY-MM-DD with position-level checks.

    COBOL validation (COTRN02C lines 353-366, 368-381):
        Position 1-4: numeric (year)
        Position 5: '-'
        Position 6-7: numeric (month)
        Position 8: '-'
        Position 9-10: numeric (day)
    Then calls CSUTLDTC for actual date validity.
    """
    if _is_empty(value):
        result.add_error(field_name, f"{label} can NOT be empty", "required")
        return

    # Position-level format check (matches COBOL EVALUATE TRUE pattern)
    if len(value) < 10:
        result.add_error(field_name, f"{label} should be in format YYYY-MM-DD", "format")
        return

    date_part = value[:10]

    if not date_part[0:4].isdigit():
        result.add_error(field_name, f"{label} should be in format YYYY-MM-DD", "format")
        return
    if date_part[4] != "-":
        result.add_error(field_name, f"{label} should be in format YYYY-MM-DD", "format")
        return
    if not date_part[5:7].isdigit():
        result.add_error(field_name, f"{label} should be in format YYYY-MM-DD", "format")
        return
    if date_part[7] != "-":
        result.add_error(field_name, f"{label} should be in format YYYY-MM-DD", "format")
        return
    if not date_part[8:10].isdigit():
        result.add_error(field_name, f"{label} should be in format YYYY-MM-DD", "format")
        return

    # Actual date validity check (equivalent to CSUTLDTC call)
    try:
        datetime.strptime(date_part, "%Y-%m-%d")
    except ValueError:
        result.add_error(field_name, f"{label} - Not a valid date", "invalid_date")


def _validate_orig_ts(record: TransactionRecord, result: ValidationResult) -> None:
    """Origination timestamp: YYYY-MM-DD format (COTRN02C lines 282-287, 353-366)."""
    _validate_date_format(record.tran_orig_ts, "tran_orig_ts", "Orig Date", result)


def _validate_proc_ts(record: TransactionRecord, result: ValidationResult) -> None:
    """Processing timestamp: YYYY-MM-DD format (COTRN02C lines 288-293, 368-381)."""
    _validate_date_format(record.tran_proc_ts, "tran_proc_ts", "Proc Date", result)


def _validate_merchant_id(record: TransactionRecord, result: ValidationResult) -> None:
    """Merchant ID: must not be empty, must be numeric (COTRN02C lines 294-299, 430-436)."""
    if _is_empty(record.tran_merchant_id):
        result.add_error("tran_merchant_id", "Merchant ID can NOT be empty", "required")
        return
    if not record.tran_merchant_id.isdigit():
        result.add_error("tran_merchant_id", "Merchant ID must be Numeric", "numeric")


def _validate_merchant_name(record: TransactionRecord, result: ValidationResult) -> None:
    """Merchant Name: must not be empty (COTRN02C lines 300-305)."""
    if _is_empty(record.tran_merchant_name):
        result.add_error("tran_merchant_name", "Merchant Name can NOT be empty", "required")


def _validate_merchant_city(record: TransactionRecord, result: ValidationResult) -> None:
    """Merchant City: must not be empty (COTRN02C lines 306-311)."""
    if _is_empty(record.tran_merchant_city):
        result.add_error("tran_merchant_city", "Merchant City can NOT be empty", "required")


def _validate_merchant_zip(record: TransactionRecord, result: ValidationResult) -> None:
    """Merchant Zip: must not be empty (COTRN02C lines 312-317)."""
    if _is_empty(record.tran_merchant_zip):
        result.add_error("tran_merchant_zip", "Merchant Zip can NOT be empty", "required")


def _validate_card_num(record: TransactionRecord, result: ValidationResult) -> None:
    """Card Number: must not be empty (COTRN02C lines 210-222)."""
    if _is_empty(record.tran_card_num):
        result.add_error("tran_card_num", "Card Number can NOT be empty", "required")


def validate_transaction(record: TransactionRecord) -> ValidationResult:
    """Validate a TransactionRecord against all COBOL validation rules.

    Runs all field-level validations from COTRN02C.cbl's
    VALIDATE-INPUT-KEY-FIELDS and VALIDATE-INPUT-DATA-FIELDS paragraphs.

    Args:
        record: The TransactionRecord to validate.

    Returns:
        ValidationResult with is_valid flag and list of errors.
    """
    result = ValidationResult(is_valid=True)

    _validate_tran_id(record, result)
    _validate_type_cd(record, result)
    _validate_cat_cd(record, result)
    _validate_source(record, result)
    _validate_desc(record, result)
    _validate_amount(record, result)
    _validate_orig_ts(record, result)
    _validate_proc_ts(record, result)
    _validate_merchant_id(record, result)
    _validate_merchant_name(record, result)
    _validate_merchant_city(record, result)
    _validate_merchant_zip(record, result)
    _validate_card_num(record, result)

    return result
