# DU Table - parse(String line)

| Variable | Definition (Line #) | Usage (Line #) | Usage Type | Test Case ID |
| :--- | :--- | :--- | :--- | :--- |
| `line` | 11 | 12 | p-use (null guard) | AD1 |
| `line` | 11 | 13 | c-use (`trim`) | AD2 |
| `s` | 13 | 15 | p-use (`isEmpty`) | AD2 |
| `s` | 13 | 17 | c-use (`SINGLE_CHAR_COMMAND.matcher`) | DU1 |
| `s` | 13 | 18 | c-use (`charAt(0)`) | AD3 |
| `s` | 13 | 28 | c-use (error message concat) | AU1/DU3 (switch default path reachable by mutation only) |
| `s` | 13 | 32 | c-use (`INIT_COMMAND.matcher`) | DU2 |
| `s` | 13 | 46 | c-use (invalid-format error message) | DU7/AU4 |
| `c` (single-char) | 18 | 19 | p-use (`switch`) | AD3, AU1, DU3 |
| `m` | 32 | 33 | p-use (`m.matches`) | DU4 |
| `m` | 32 | 34 | c-use (`m.group(1)`) | DU4 |
| `m` | 32 | 35 | c-use (`m.group(3)`) | DU4 |
| `c` (init/move) | 34 | 37 | p-use (`c == 'I'`) | DU4 |
| `value` | 35 | 38 | p-use (`value <= 0`) | AD4 |
| `value` | 35 | 39 | c-use (Init arg) | DU5 |
| `value` | 35 | 41 | p-use (`value < 0`) | DU6 (path analyzed; condition unreachable with current regex) |
| `value` | 35 | 42 | c-use (Move arg) | DU6 |

## Notes

- Line references map to `src/main/java/com/coen448/robot/CommandParser.java`.
- `value` usage at line 41 is a logical leak candidate because current regex grammar (`\\d+`) prevents negatives.
