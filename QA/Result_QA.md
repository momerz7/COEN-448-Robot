# Result QA - Audit Findings

## All-Definitions Audit

- Finding: All in-method definitions in `parse(String line)` have at least one reachable use.
- Evidence:
  - `line` def/use: line 11 -> line 12/13
  - `s` def/use: line 13 -> lines 15/17/32/46 (and line 28 on single-char default)
  - `c` single-char def/use: line 18 -> line 19
  - `m` def/use: line 32 -> lines 33/34/35
  - `c` init def/use: line 34 -> line 37
  - `value` def/use: line 35 -> lines 38/39/41/42
- Dead data status: none confirmed for current implementation.

## All-Uses Audit

- Finding: Reachable uses for command routing and init/move parsing are covered by AU tests.
- Logical leak #1 (unreachable check): line 41 (`value < 0`) cannot be true because regex line 9 accepts only digits (`\\d+`).
- Logical leak #2 (semantic inconsistency): line 38 condition is `value <= 0` but error message says "non-negative".

## All-DU Audit

- Finding: DU-chain coverage achieved for all key variables (`line`, `s`, `c`, `m`, `value`) using DU1-DU7 tests.
- Full DU-chain list and test mapping provided in `QA/DU_Table.md`.

## Antidecomposition Audit

- `P` (integrated plan): behavior checks only returned `CommandType` (via helper `integratedPlanP`).
- `Q` (component): `parse(String line)` must guarantee correct `type` and `arg` semantics.
- Finding: `T_P` is adequate for type-level integration behavior but inadequate for full component obligations.
- Remedy: `T_Q` extends adequacy with argument and normalization assertions (`AX7-Q1`).

## Technical Debt Summary

- Unreachable branch risk: line 41.
- Message-spec mismatch risk: line 38 text vs condition.
- No definition termination causing dead data in current method.
