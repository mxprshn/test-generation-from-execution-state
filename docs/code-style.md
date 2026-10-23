# Code style of the generated tests (post-hoc)

`evaluation/code_style.py` computes the code-style measures of the paper's Table 3 over finished results
directories: naturalness of the test code, test methods that use reflection, and test methods that load
objects from XML files. It is an analysis of the generated tests after the runs, **not an evaluation step**:
it runs no build, writes nothing into a results directory, and is not among the inputs hashed for any cell
(`evaluation/inputs.py`), so adding or changing it does not make any cell stale.

## Definitions

Unit of count: a test method (`@Test`, `@ParameterizedTest`, `@RepeatedTest`; JUnit 4 and 5). Every built
test is measured, also one that does not compile or fails; the tables report all methods and, separately,
the methods of passing tests (`passes` = verify of the cell). Cells without a test (`result.diff` empty or
missing) have no methods. The generated test is the file at `output_test_file` in `result.diff`.

- **Naturalness** (Hindle et al., On the Naturalness of Software, ICSE 2012). For each case a trigram model
  with interpolated Kneser–Ney smoothing (discount 0.75) is built over the Java test sources of the project
  at the pinned commit (`src/test/java*/`, `tst/`), without the case's hidden paths (the developers' tests of
  the target class) and without generated tests. Comments, `import` and `package` lines are dropped; string,
  character and number literals become `<STR>`, `<CHR>`, `<NUM>`; tokens seen once in the corpus become
  `<UNK>`. The naturalness of a test method is its mean −log₂ p(token | two previous tokens), bits per
  token: lower = closer to the project's own tests. The value of the whole test file
  (`class_naturalness`) is kept as a robustness check.
- **Reflection**: the test method, the `@Before`/`@BeforeEach`/`@BeforeClass`/`@BeforeAll` methods and the
  helper methods it calls (three levels) call `getDeclaredField`/`getField`/`getDeclaredMethod`/`getMethod`/
  `getDeclaredConstructor` (also plural forms), `setAccessible`, `invoke`, `Class.forName`,
  `ReflectionTestUtils`, `Whitebox` or `FieldUtils`.
- **Objects from XML**: the same code calls `deserializeObjectFrom*` or `fromXML` (XStream; the tests of
  Pankti and RICK load the recorded objects this way). The number of object files a tool cell adds
  (`object_files`) is counted from `result.diff` and checked against `quality.object_files` of `eval.json`.
- **Developers' tests**: the hidden paths of each case at the pinned commit (only `.java` sources; cases
  with no hidden Java test have no developers' methods).

The definitions are those of the paper's earlier tool comparison (18 cases); the only change is the test
roots: the earlier regular expression also took `.java` files under `src/test/resources` and `tst-rsrc`,
which in checkstyle (3302 input files of checks) and liquibase are test inputs, not tests. For jsoup, jgit
and mybatis both choices give the same corpus.

## Running

Project checkouts must exist (`scripts/setup.sh`, and for the 31 cases of `cases/tools-projects.csv` their
own projects directory); the pinned commit is read with `git show`, the working tree is not used.

```bash
python3 -m evaluation.code_style --out OUT --jobs 4 \
  --projects PROJECTS_70 PROJECTS_31 \
  --results v2=results/v2 auto70=autonomous-v2 auto31=autonomous-tools-projects \
            tools70=tools-v2 tools31=tools-tools-projects \
            mtc70=tools-mtc-1c-v2 mtc31=tools-mtc-1c-tools-projects \
  --modes-from tools70=pankti,rick tools31=pankti,rick
```

`--modes-from` restricts a results directory to some modes (the MicroTestCarver cells of tools-run-1/1b are
superseded by tools-run-1c). A cell (case, mode, run) present in two directories is an error. Set-aside cells
(`r2.infra-*`, `r1.infra-adapter-*`) are not read.

Output: `methods.jsonl` (one row per test method: group, project, case, mode, run, method, naturalness,
class naturalness, reflection, xml, passes, object files), `by_mode.csv`, `by_project_mode.csv`,
`by_case_mode.csv` (methods, mean/median naturalness, mean over cases, reflection and XML counts and %, the
same for passing tests) and `dominance.csv` (per group: in how many cases the mean naturalness of a mode is
above/below that of `autonomous`; for the tools also above every agent mode at once).

## Limits

Naturalness is statistical similarity to the project's tests, not readability; uniform code gets low
entropy; absolute values depend on the corpus and the settings and are comparable only between modes on the
same model, i.e. within a case. The agent modes could read the other tests of the project and take their
style; the tools could not.
