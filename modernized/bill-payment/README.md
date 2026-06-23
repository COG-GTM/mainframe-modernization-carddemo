# CardDemo Bill Payment — Modern TypeScript Equivalent

A modern, runnable TypeScript service that is the functional equivalent of the
CardDemo COBOL online **Bill Payment** program `COBIL00C` (CICS transaction
`CB00`, BMS map `COBIL0A`).

Generated with the `generate-modern-equivalent` skill (`.devin/skills/`).

## What COBIL00C does

The Bill Payment screen lets a user pay an account's outstanding balance **in
full**. On Enter, the program:

1. Validates the entered **Account ID** (required).
2. Reads the account (`ACCTDAT`) and displays the current balance.
3. If the balance is `<= 0`, reports *"You have nothing to pay..."*.
4. With confirm = `Y`, it:
   - looks up the card number via the `CXACAIX` account→card cross-reference,
   - finds the highest existing transaction id (`STARTBR`/`READPREV` on
     `TRANSACT`) and adds 1,
   - writes a new transaction for the full balance
     (type `02`, "BILL PAYMENT - ONLINE"),
   - subtracts the amount from the balance (→ `0.00`) and rewrites the account.
5. Reports *"Payment successful. Your Transaction ID is ..."*.

It is **pseudo-conversational**: each keystroke is a separate CICS task and
state flows through the COMMAREA (`COCOM01Y` / `CDEMO-CB00-INFO`).

## Architecture

| Layer | File | COBOL origin |
|-------|------|--------------|
| Data models | `src/models/*.ts` | copybooks `CVACT01Y`, `CVACT03Y`, `CVTRA05Y` |
| Fixed-point money | `src/money.ts` | `PIC S9(n)V99` packed decimals |
| Repositories | `src/repositories/*.ts` | CICS file control over `ACCTDAT` / `CXACAIX` / `TRANSACT` |
| Service | `src/services/billPaymentService.ts` | `PROCESS-ENTER-KEY` + helper paragraphs |
| DTOs | `src/dto/billPayment.ts` | BMS map `COBIL00.CPY` + COMMAREA |
| REST controller | `src/controllers/billPaymentController.ts` | the 3270 screen / transaction CB00 |
| Errors | `src/errors.ts` | WS-MESSAGE error literals / CICS RESP codes |

## Run it

```bash
cd modernized/bill-payment
npm install
npm test          # unit tests (vitest)
npm run typecheck # tsc --noEmit
npm run dev       # start API on http://localhost:3000
```

## API

```bash
# Display balance (READ-ACCTDAT-FILE)
curl http://localhost:3000/api/accounts/00000000011/balance

# Pay the balance in full (PROCESS-ENTER-KEY, confirm defaults to Y)
curl -X POST http://localhost:3000/api/accounts/00000000011/bill-payment \
     -H 'Content-Type: application/json' -d '{"confirm":"Y"}'
```

Seeded demo accounts: `00000000011` (balance 1234.56) and `00000000022`
(balance 0.00 — exercises "nothing to pay").

See [`MODERNIZATION_NOTES.md`](./MODERNIZATION_NOTES.md) for translation
decisions and recommended next steps.
