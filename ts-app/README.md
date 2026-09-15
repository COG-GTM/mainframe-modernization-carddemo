# CardDemo — TypeScript reimplementation

A TypeScript port of the CardDemo COBOL/CICS application that lives alongside the original: the
COBOL under `app/` is untouched and remains the parity reference for every behaviour implemented
here.

The first slice covers **signon → main menu → account view** online and the **daily transaction
posting batch job**, and establishes the patterns (copybook → layout, VSAM → repository,
COMMAREA → session, XCTL → route) used to migrate the remaining programs.

## Getting started

```bash
cd ts-app
npm install
npm run typecheck && npm run lint && npm test
npm run serve         # online services on http://localhost:3000
npm run batch:post    # posting job; exits 4 when any transaction is rejected
```

Node 20+ is required. The VM snapshot for this repo does not ship Node; install it with
`source ~/.nvm/nvm.sh && nvm install 22`.

### Online API

| Route                        | COBOL program    | Notes                                        |
| ---------------------------- | ---------------- | -------------------------------------------- |
| `POST /signon`               | `COSGN00C`       | returns `sessionId` + target program/route   |
| `GET /menu`, `POST /menu`    | `COMEN01C`       | option validation and admin-only enforcement |
| `GET /accounts/view?acctId=` | `COACTVWC`       | account + customer + xref                    |
| `POST /signoff`              | `COSGN00C` (PF3) | discards the session                         |

Every request after signon carries the session id in the `x-carddemo-session` header; a `401`
response is the equivalent of CICS finding `EIBCALEN = 0` and returning to the signon screen.
Monetary values are serialized as strings so no precision is lost in JSON.

## Migration patterns

| COBOL concept                                               | TypeScript counterpart                                                                                                                         |
| ----------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| Copybook record layout (`PIC X/9/S9V99`)                    | `RecordLayout<T>` + `FieldSpec` in `src/codec/fixedWidth.ts`, one per copybook in `src/domain/`                                                |
| Signed display/packed decimal (overpunch `{`, `}`, `A`–`R`) | `src/codec/zonedDecimal.ts`, decoded to `Decimal` (`decimal.js`)                                                                               |
| VSAM KSDS file + key                                        | `KeyedRepository<T>` (`src/data/repositories.ts`); alternate indexes are a second repository over the same records (e.g. `cardXrefsByAccount`) |
| Sequential QSAM output                                      | `SequentialWriter<T>` + `encodeRecord`                                                                                                         |
| `COMMAREA` (`COCOM01Y`) passed on `RETURN TRANSID`          | `CardDemoCommarea` held in `SessionStore` (`src/online/session.ts`), keyed by session id                                                       |
| `EXEC CICS XCTL PROGRAM(...)`                               | return the target program name; `routeForProgram()` maps it to an HTTP route (`src/online/routes.ts`)                                          |
| BMS map field edits and `WS-MESSAGE` text                   | validation inside the service, with the COBOL message strings reproduced verbatim                                                              |
| Menu option table (`COMEN02Y`, `COADM02Y`)                  | `MAIN_MENU_OPTIONS` / `ADMIN_MENU_OPTIONS`                                                                                                     |
| `RETURN-CODE 4`                                             | `process.exit(4)` from the batch CLI                                                                                                           |

## Program and copybook mapping

### Migrated

| COBOL                                               | TypeScript                                          |
| --------------------------------------------------- | --------------------------------------------------- |
| `app/cbl/COSGN00C.cbl`                              | `src/online/signonService.ts`                       |
| `app/cbl/COMEN01C.cbl`                              | `src/online/menuService.ts`, `src/online/routes.ts` |
| `app/cbl/COACTVWC.cbl`                              | `src/online/accountViewService.ts`                  |
| `app/cbl/CBTRN02C.cbl` (`POSTTRAN.jcl`)             | `src/batch/postTransactions.ts`                     |
| `app/cpy/CVACT01Y.cpy` (account)                    | `src/domain/account.ts`                             |
| `app/cpy/CVACT02Y.cpy` (card)                       | `src/domain/card.ts`                                |
| `app/cpy/CVACT03Y.cpy` (card xref)                  | `src/domain/cardXref.ts`                            |
| `app/cpy/CVCUS01Y.cpy` (customer)                   | `src/domain/customer.ts`                            |
| `app/cpy/CSUSR01Y.cpy` (user security)              | `src/domain/user.ts`                                |
| `app/cpy/CVTRA05Y.cpy` (transaction)                | `src/domain/transaction.ts`                         |
| `app/cpy/CVTRA06Y.cpy` (daily transaction)          | `src/domain/dailyTransaction.ts`                    |
| `app/cpy/CVTRA01Y.cpy` (category balance)           | `src/domain/tranCatBalance.ts`                      |
| `app/cpy/COCOM01Y.cpy` (COMMAREA)                   | `src/domain/commarea.ts`                            |
| `app/cpy/COMEN02Y.cpy`, `COADM02Y.cpy`              | `src/online/routes.ts`                              |
| `app/bms/COSGN00.bms`, `COMEN01.bms`, `COACTVW.bms` | `web/` (React screens)                              |

### Not yet migrated

Online: `COADM01C` (admin menu), `COACTUPC`, `COCRDLIC`, `COCRDSLC`, `COCRDUPC`, `COTRN00C`,
`COTRN01C`, `COTRN02C`, `CORPT00C`, `COBIL00C`, `COUSR00C`, `COUSR01C`, `COUSR02C`, `COUSR03C`,
and the utility `CSUTLDTC`.

Batch: `CBACT01C`–`CBACT04C`, `CBCUS01C`, `CBTRN01C`, `CBTRN03C`, `CBSTM03A`/`CBSTM03B`.

Copybooks: `CVTRA02Y`–`CVTRA04Y`, `CVTRA07Y`, `CVCRD01Y`, `CUSTREC`, `COSTM01`, `COTTL01Y`,
`CSDAT01Y`, `CSLKPCDY`, `CSMSG01Y`/`CSMSG02Y`, `CSSETATY`, `CSSTRPFY`, `CSUTLDPY`/`CSUTLDWY`.

Selecting a menu option whose program is not migrated yet returns the COBOL "coming soon" message
rather than routing; `IMPLEMENTED_PROGRAMS` in `src/online/routes.ts` is the single place to
update as programs land.

## Known deviations from the COBOL

- Pseudo-conversational screen flow (send map / receive map / PF keys) is replaced by stateless
  HTTP requests plus a server-side session; screen re-display is the client's responsibility.
- The user-security file ships only as EBCDIC in `app/data/EBCDIC`, so the ASCII fixture in
  `data/usrsec.txt` was regenerated from `app/jcl/DUSRSECJ.jcl` (see `data/README.md`).
- `COMEN01C` guards unimplemented options by checking a `DUMMY` program name in its table; here
  the check is against the set of programs that have a TypeScript handler.
- CICS `RESP`/`RESP2` values are not real: the not-found messages reproduce the COBOL text with
  the `NOTFND` response code so the wording matches.
