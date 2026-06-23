---
name: generate-modern-equivalent
description: Given a COBOL program or copybook name, generate a modern equivalent (REST API, microservice, or data class)
argument-hint: "<ProgramOrCopybook>"
allowed-tools:
  - read
  - grep
  - glob
---

You are a mainframe modernization expert working on the CardDemo COBOL application.

The user has asked you to generate a modern equivalent for: **$ARGUMENTS**

Follow these steps:

## Step 1 — Locate and read the source

- If the argument looks like a copybook (e.g. `CVACT01Y`, ends in `.cpy`), find it in `app/cpy/` or `app/cpy-bms/`.
- If it looks like a COBOL program (e.g. `COACTVWC`, ends in `.cbl`), find it in `app/cbl/`.
- Read the full source file. Also read any copybooks it includes (look for `COPY` statements) from `app/cpy/`.

## Step 2 — Understand the original

Briefly summarize:
- **What it does** — the business purpose in plain English
- **Data it reads/writes** — VSAM files, copybook record structures, fields with their PIC types
- **Key logic** — validation rules, calculations, branching, error handling
- **CICS/batch context** — is it a pseudo-conversational online screen or a batch job?

## Step 3 — Generate the modern equivalent

Produce clean, idiomatic modern code using these mappings:

| Mainframe concept | Modern equivalent |
|---|---|
| Copybook record (`CVACT01Y`) | TypeScript interface / Python dataclass / Java record |
| VSAM KSDS file | REST resource with CRUD endpoints |
| Online CICS program (screen) | REST API controller + request/response DTOs |
| Batch COBOL program | Service class or async job worker |
| CICS COMMAREA | Request/response payload |
| `EXEC CICS READ` | `GET /resource/{id}` |
| `EXEC CICS REWRITE` | `PUT /resource/{id}` |
| `EXEC CICS WRITE` | `POST /resource` |
| `EXEC CICS DELETE` | `DELETE /resource/{id}` |
| `PERFORM ... UNTIL` | `while` loop or stream pipeline |
| `MOVE` / `COMPUTE` | direct assignment / arithmetic |
| COBOL `PIC 9(11)` | `string` (ID) or `number` |
| COBOL `PIC S9(10)V99 COMP-3` | `number` (decimal, 2dp) |
| COBOL `PIC X(n)` | `string` |

**Default target language: TypeScript.** If the user specifies a different language (e.g. "in Python", "in Java"), use that instead.

For a **copybook**, produce:
- A typed data model (interface/class/dataclass)
- Field-by-field mapping comments showing the original PIC clause
- A short validation function for key business rules (e.g. active status, credit limit checks)

For an **online CICS program**, produce:
- A REST controller with appropriate HTTP verbs and routes
- Request/response DTO types derived from the BMS map fields and COMMAREA
- Service layer method stubs with the core business logic translated

For a **batch program**, produce:
- A service class or job function
- Input/output types
- The main processing loop translated to modern iteration
- Error handling equivalent to the COBOL error paragraphs

## Step 4 — Call out modernization considerations

After the code, add a short section called **"Modernization Notes"** covering:
- Any COBOL idioms that don't translate directly (e.g. REDEFINES, level-88 condition names, GO TO)
- Data integrity concerns (fixed-length VSAM → relational DB considerations)
- Security gaps in the original (e.g. plaintext passwords in `USRSEC`)
- Suggested next steps (e.g. add input validation, add auth middleware, add unit tests)
