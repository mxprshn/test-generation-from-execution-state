# Selection of the tools-projects cases

The scripts and data that selected the 31 cases of `cases/tools-projects.csv` (docs/tools-projects.md).
They ran once, on the checkouts of the four projects built online, with Temurin 11 for the projects and
JDK 21 for the JDI clients; paths in the shell scripts are those of that machine (`~/newcases`).

Steps:

1. **Targets from the authors' lists.** PDFBox: the method rows of Pankti, Table 3
   (`data/pdfbox-targets.txt`, 25 methods); GraphHopper: the classes of the tests RICK generated in its
   replication package (castor-software/rick-experiments, `graphhopper/tests/rick-tests`; class names in
   `data/rick-graphhopper-test-classes.txt`) and the methods those tests call; PetClinic: the classes of
   MicroTestCarver, Table IV, and their public methods; Lab-Insurance: the pricing classes of Table IV.
2. **Scenario search.** `scan.sh` runs a project's whole test suite once under JDWP; `Scan.java` stops at
   the entry of every watched target and counts its invocations per test method (the outermost frame of a
   `*Test`, `*Tests` or `*IT` class), marking static and non-public targets: `data/scan/*.tsv`
   (`N<TAB>target <- test`). Lab-Insurance was searched by hand (one module, few tests).
3. **Filter.** `pick.py` keeps, per target, the test methods that reach it 1–13 times and do not name the
   method in their body; static and non-public targets are dropped (Pankti and RICK select public
   instance methods). `selection_report.py` reproduces this from the committed scans and lists every
   scanned target with its outcome.
4. **Choice.** One test per chosen target went into `data/candidates.tsv` (33 rows, with the evidence of
   the target). The choice among possible targets and tests was made by hand: targets of the authors'
   lists first, a test with few invocations and a short run; the reasons for leaving a possible target
   out were not recorded (`selection_report.py` marks them "not taken").
5. **Debugger check.** `run-all.sh` → `probe.sh` → `Probe.java`: the scenario test runs offline under
   JDWP on JDK 11; a JDI client stops at the entry (breakpoint at the first location) and the exit
   (method exit, thread-filtered) of every invocation and reads arguments, fields of `this` and the
   return value: `data/results/<id>.json`, `<id>.run.json`. A candidate that failed was dropped
   (`data/dropped/`).
6. **Cases.** `meta.py` (expected tool applicability by the tools' selection rules, signature, hidden
   paths) and `mkcases.py` wrote the drafts that became `cases/<id>/case.yaml` and `meta.yaml`.

`Probe.java` and `Scan.java` are JDI clients that listen (`server=n` on the debuggee side), unlike the
JDI server of the autonomous mode, which attaches.
