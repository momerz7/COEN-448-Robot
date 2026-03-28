# Walkthrough QA - Laski-Korel-Weyuker Data-Flow Audit

## Scope and Setup

- Scope: `parse(String line)` in `src/main/java/com/coen448/robot/CommandParser.java`.
- Test artifact created: `src/test/java/com/coen448/robot/LKWQA.java`.
- Supporting QA artifacts created under `QA/` at project root.
- Production code was not changed.

---

## Step 1 - Source Read

- Read only the target source file `CommandParser.java` and identified definitions/uses in `parse(String line)`.
- Key line references used in this audit:
  - Parameter definition: line 11 (`line`)
  - Local definitions: line 13 (`s`), line 18 (`c`), line 32 (`m`), line 34 (`c`), line 35 (`value`)
  - Terminal throws: lines 12, 15, 28, 38, 41, 46

---

## Step 2 - All-Definitions Audit (Dead Data)

### 2a What was created

- AD test set in `LKW_QA.java`: `AD1` through `AD5`.
- These tests force each assignment site and validate at least one use for the defined value.

### 2b What was checked

- Each definition has at least one reachable use before method exit.
- Checked defs: `line`, `s`, single-char `c`, init-branch `m`, init-branch `c`, `value`.

### 2c Why this was done

- All-Defs criterion requires every definition to be exercised to at least one use.
- This is used to detect dead data (values defined but never read).

### Definition terminations identified

- No pure dead definitions were found in `parse`.
- A logic conflict was identified later (line 41): branch checks `value < 0` but regex at line 9 only accepts digits (`\\d+`), making that throw unreachable.

---

## Step 3 - All-Uses Audit (Logical Leak)

### 3a What was created

- AU test set in `LKW_QA.java`: `AU1` through `AU4`.
- `AU1` parameterized over all single-character command uses (`U,D,R,L,P,C,Q,H`).

### 3b What was checked

- Every observable use site of assigned values is exercised, including:
  - predicate uses (`if`/`switch`) and computation uses (message formatting and command argument construction).
- Invalid-format terminal path (line 46) covered via `AU4`.

### 3c Why this was done

- All-Uses criterion ensures each definition is checked across all reachable uses.
- This helps identify logical leaks where data is not validated/consumed consistently.

### Logical leaks identified

- Line 41 condition (`value < 0`) is incompatible with parser regex constraint (`\\d+`), creating unreachable validation logic.
- Error text at line 38 says "non-negative" while condition rejects zero (`<= 0`), creating semantic mismatch.

---

## Step 4 - All-DU Audit

### 4a What was created

- DU test set in `LKW_QA.java`: `DU1` through `DU7`.
- Each test is mapped to one or more specific DU chains documented in `QA/DU_Table.md`.

### 4b What was checked

- Definition-to-usage reachability by variable and line number.
- Representative chains for all definitions in the method, including exceptional terminal path at line 46.

### 4c Why this was done

- All-DU criterion is stricter than All-Defs/All-Uses and validates definition-use pairing paths.

### DU pairs identified

- See complete enumerated table in `QA/DU_Table.md`.

---

## Step 5 - Antidecomposition (Weyuker Axiom 7)

### 5a What was created

- `AX7-P1`: defines a type-level integrated test set `T_P` using wrapper plan `P` (method `integratedPlanP`).
- `AX7-Q1`: defines component-focused set `T_Q` for `Q=parse`, asserting both command type and argument value.

### 5b What was checked

- `T_P` is adequate when program-level behavior only checks routing by `CommandType`.
- `T_P` is not adequate for component-level guarantees of argument normalization/propagation.
- `T_Q` adds assertions that close this gap.

### 5c Why this was done

- Demonstrates antidecomposition: adequacy for integrated `P` does not imply adequacy for component `Q`.

### Q vs P identification and remedy

- `Q`: `parse(String line)` as standalone component.
- `P`: integrated behavior where only `cmd.type()` is consumed.
- Why `T_P` is inadequate for `Q`: it can miss defects in `arg` handling while still passing type-only checks.
- How `T_Q` remedies: explicit argument assertions and normalization checks were added in `AX7-Q1`.
