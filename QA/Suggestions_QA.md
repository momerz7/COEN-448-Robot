# Suggestions QA - Remediation Proposals

## 1. Unreachable Validation Branch (Line 41)

- Issue: `if (value < 0)` cannot trigger because parser regex (`\\d+`) disallows negative input.
- Impact: dead validation logic and misleading confidence in negative-distance handling.
- Suggested fixes (choose one):
  1. Keep non-negative-only grammar and remove line 41 check.
  2. Accept signed values in grammar (e.g., `[-+]?\\d+`) and keep line 41 as a true guard.

## 2. Validation Message Mismatch (Line 38)

- Issue: Condition is `value <= 0` while message says "non-negative".
- Impact: specification ambiguity and developer confusion.
- Suggested fixes (choose one):
  1. If zero is invalid, message should state "must be positive".
  2. If zero should be valid, change check to `value < 0`.

## 3. Strengthen QA Regression Safety

- Keep `LKW_QA.java` in CI with:
  1. `mvn -Dtest=LKW_QA test`
  2. JaCoCo threshold checks
  3. SonarQube hotspot review for parser boundary conditions

## 4. Improve Parse Contract Clarity

- Add explicit method-level contract in docs/Javadoc:
  1. accepted command grammar
  2. zero/negative policy for Init/Move
  3. whitespace and case normalization behavior
