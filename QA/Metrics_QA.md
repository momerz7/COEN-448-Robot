# Metrics QA - Program and Audit Metrics

## Program Code Metrics

1. Cyclomatic Complexity per function (audit target)
   - `CommandParser.parse(String line)`: 10
     - Basis: 1 + decisions at lines 12, 15, 17, 33, 37, 38, 41, plus 2 additional branch outcomes in switch grouping.
2. Total lines of production code: 434
3. Total functions/methods (production): 36
4. Total classes/records/enums (production): 9
5. Total variable declarations detected (production): 34

## Minimum Test Cases (for parse data-flow criteria)

1. All-Defs minimum practical set used: 5 tests (`AD1`-`AD5`)
2. All-Uses minimum practical set used: 4 tests (`AU1`-`AU4`, where `AU1` is parameterized across 8 commands)
3. All-DU minimum practical set used: 7 tests (`DU1`-`DU7`)
4. Antidecomposition set used:
   - `T_P`: 1 integrated adequacy test (`AX7-P1`) with 5 representative inputs
   - `T_Q`: 1 component adequacy test (`AX7-Q1`) with argument-focused assertions

## Dead Data

1. Confirmed dead data in `parse`: none
2. Notes: every definition has at least one reachable use in current control-flow.

## Logical Leaks

1. Unreachable negative-distance guard at line 41 due to regex constraint at line 9.
2. Message mismatch at line 38: text says non-negative while check rejects zero.

## T_P vs T_Q Adequacy

1. `T_P` adequacy target: integrated type-routing behavior (`P`) only.
2. `T_Q` adequacy target: full component obligations (`Q=parse`), including `arg` correctness and normalization.
3. Adequacy result: `T_P` does not subsume `T_Q`; extra component assertions are required.

## Test Execution Outcome

1. Created tests in `LKWQA.java`: 17 declared test methods with one parameterized family expanded to 25 executed test cases.
2. Status after execution:
   - Passed: 25
   - Failed: none
   - Exceptions: expected `IllegalArgumentException` paths only (asserted)
