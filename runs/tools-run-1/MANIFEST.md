# tools-run-1

The external test generators Pankti, RICK and MicroTestCarver as modes without an agent (`pankti`, `rick`,
`microtestcarver`; `docs/tools.md`) on the 70 cases of the study and on the 31 cases of
`cases/tools-projects.csv`, one round (`r1`; the tools are deterministic, see below). Evaluated by the same
code as the agent modes (verify, RQ1, quality); **RQ2 (PIT) is not computed yet**. The cells of the agent modes
are the published ones (`v2-results`, `autonomous-run-1`) and are combined with this run for the tables.

## Code and settings

- Tag `tools-run-1` = `ce918715d0b65f0b201a29adcb0aac47bd4a17df`: the 70 cases (lanes `lane-a`, `lane-b`), except
  one cell. Tag `tools-run-1b` = `a3487b0` (adapter fix, see Incidents): the 31 cases (`tp-lane-a`, `tp-lane-b`)
  and the cell `jsoup-consume-css-identifier/microtestcarver/r1` (`configs/rerun-1b.yaml`). The commit changes
  only `runner/tools/` and `runner/cell.py` for cells whose three MicroTestCarver recordings all abort or write
  no object pool; no other cell of the 70 is in that situation (all their `record_attempts` have a recording
  without abort). `scripts/run-lane.sh` refused to start unless the repository was a clean checkout of the tag.
- External tools (`tool-versions.json`, installed by `scripts/setup_external_tools.sh`): Pankti/RICK `c755a13` +
  `external/pankti.patch`, MicroTestCarver replication package `88af3ad` + `external/mtc/*.patch`, Glowroot
  0.14.0, Temurin 17.0.20.1 and 11.0.32.1, Corretto 8.462, Gradle 7.6; generated tests' dependencies XStream
  1.4.12, JUnit Jupiter 5.10.3, Mockito 5.11.0, Hamcrest 2.2.
- Recording: Pankti/RICK on JDK 17 for the 70 cases (Glowroot cannot weave JDK 21), on JDK 11 for the 31;
  MicroTestCarver on JDK 11 with `-XX:-BytecodeVerificationRemote`, up to 3 attempts. `tool_timeout` 2400 s.
  Tests run and are evaluated on the group's JDK (21 / 11) with `JDK_JAVA_OPTIONS` = the `--add-opens` of
  `runner.tools.common.ADD_OPENS` (XStream).
- `configs/base-70.yaml`, `base-31.yaml`: the settings of `autonomous-run-1` with `modes: []`,
  `once_modes: [pankti, rick, microtestcarver]`, `rounds: [1]`, steps verify/rq1/quality; the lane configs
  (`scripts/make_lanes.py`) differ by the case list (disjoint projects), the work directory, `parallel_cases: 2`
  and `JDK_JAVA_OPTIONS`. References: byte-identical copies of the released ones (70) and of those of
  `autonomous-run-1` (31).

## Time span (UTC)

- 70 cases: 2026-10-22 12:57 – 15:53 (both lanes exit 0); rerun of one cell 15:56 – 15:57.
- 31 cases: 2026-10-22 15:56 – 16:59 (both lanes exit 0).
- Checks every ~10 min, incidents and actions: `RUN-LOG.md`; lane log summaries: `logs/`.

## Cells

Recall: mean over the cells of the group, a cell without a passing test counts 0 (`evaluation.aggregate`,
`rq1_by_mode.csv`, gives the same case means).

| Group | Tool | Cells | test built | compiles | passes | S / A / B / C | median time |
|---|---|---|---|---|---|---|---|
| 70 | pankti | 70 | 21 | 19 | 9 | 0.129 / 0.114 / 0.107 / 0.107 | 157 s |
| 70 | rick | 70 | 6 | 6 | 2 | 0.013 / 0.005 / 0.005 / 0 | 146 s |
| 70 | microtestcarver | 70 | 7 | 0 | 0 | 0 | 135 s |
| 31 | pankti | 31 | 22 | 20 | 12 | 0.387 / 0.387 / 0.387 / 0.387 | 199 s |
| 31 | rick | 31 | 8 | 6 | 1 | 0.008 / 0.008 / 0.008 / 0 | 97 s |
| 31 | microtestcarver | 31 | 6 | 0 | 0 | 0 | 81 s |

For comparison (same cases, published cells, 2 rounds): 70 cases — autonomous 0.986 / 0.893 / 0.629 / 0.286
(138/140 pass), baseline 0.980 / 0.722 / 0.431 / 0.264, dump 0.844 / 0.774 / 0.570 / 0.211; 31 cases — autonomous
1.0 / 0.919 / 0.532 / 0.306 (62/62).

Why a tool produced no passing test (cells):

| Group | Tool | Reasons |
|---|---|---|
| 70 | pankti | pankti-extract crashes 38 (NullPointerException in RICK's MockableSelector: apollo 15, klaw 11, liquibase 3; Spoon UnsupportedOperationException `PartialSourcePosition`: checkstyle 9); not a candidate (static/non-public) 5; not recorded 2; no class generated 4; does not compile 2; fails 10 |
| 70 | rick | pankti-extract crashes 38 (as pankti); no mockable invocations 19; not a candidate 5; no class generated 2; fails 4 |
| 70 | microtestcarver | not recordable on JDK 11 (tests compiled for 17+) 59 (all but jsoup); generator fails 4; does not compile 7 |
| 31 | pankti | pankti-extract crashes 9 (Spoon cannot read the PetClinic pom: 8; NullPointerException as above: Lab-Insurance 1); does not compile 2; fails 8 (graphhopper 5, pdfbox 3) |
| 31 | rick | pankti-extract crashes 9; no mockable invocations 14; does not compile 2; fails 5 |
| 31 | microtestcarver | generator fails 18 (XStream ConversionException, missing classes, index errors); not recorded 7 (graphhopper: the test JVM dies at start under BTrace; pdfbox: no trace); does not compile 6 |

Per-project counts: `summary.json: tool.outcome` of every cell; `scripts/summarize.py` reproduces the tables.

## Smoke and determinism (before the tag, dev worktree)

- 7 cases (jsoup-element-has-class, jsoup-token-queue-chomp-balanced, mybatis-dynamic-sql-source,
  jgit-config-to-text, pdfbox-catalog-get-viewer-preferences, petclinic-owner-get-pet,
  lab-calculate-price-handler-handle) × 3 tools, compared with the earlier harness (aithd-ppr, run `final-1`):
  same outcomes (e.g. Pankti passes 13/13 on Element.hasClass, fails on DynamicSqlSource with BoundSql compared by
  reference; RICK 12 methods / 4 failing on chompBalanced; MicroTestCarver `char` without quotes, Java 17
  projects not recordable). Fixed during the smoke: surefire's JUnit Platform provider for JUnit 4 modules was not
  in the offline Maven repository (setup now fetches it); JDK 11 projects are recorded on JDK 11 (XStream object
  files carry JDK-version-specific fields).
- On the 18 cases of the earlier study the full run agrees with `final-1` except: RICK now passes
  `jsoup-element-has-class` (final-1 ran before the matcher-import fix of that harness, commit d3661fd). Pankti
  passes the same 7 of the 18 (jgit: config-to-text, path-matcher, refspec; jsoup: consume-css-identifier,
  element-has-class, safelist, chomp-balanced); MicroTestCarver passes none, as in final-1. The aborted-recording
  case (`jsoup-consume-css-identifier`) had no aborted attempt on rerun; no MicroTestCarver cell of the 70 has an
  aborted attempt.
- Determinism: pankti and rick on jsoup-element-has-class and jsoup-token-queue-chomp-balanced, pankti on
  pdfbox-catalog-get-viewer-preferences, run twice: identical results; RICK's test differs only in the UUID
  suffixes of its method names; same S/A/B/C.

## Incidents

- `jsoup-consume-css-identifier/microtestcarver/r1` on `tools-run-1`: all three recordings aborted under BTrace,
  the adapter then read the removed object pool (FileNotFoundError). Adapter defect, fixed in `a3487b0`
  (`tools-run-1b`); the old cell is kept on the machine as `r1.infra-adapter-1792684535` (not in the archive,
  like the `.infra-*` cells of the other runs); rerun on `tools-run-1b`: recorded on the first attempt, the
  generator fails (NullPointerException, as in final-1). `provenance/moved.json` of the archive.
- No infrastructure failure, no timeout, no lane error otherwise.

## Archives (draft release `tools-run-1`)

| Archive | Size | sha256 |
|---|---|---|
| `tools-v2.tar.gz` (70 cases, 210 cells) | 4 874 439 B | `bf671da90fc771e5880e538cf893e42e3a6febd1ed856364ba9acf0034171a51` |
| `tools-tools-projects.tar.gz` (31 cases, 93 cells) | 24 979 178 B | `ec8b35f8611c20b4c97ee0c1431105aff0b4f27daef38d8b3ef75fbff6f0aecc` |

Made with `scripts/export_results.py` (`scripts/export.sh`): deterministic (two exports, same sha256), paths and
provider redacted as for `v2-results`; no key value, provider address, home path or host name in them.
`provenance/` holds the configs, `runs.jsonl`, `RUN-LOG.md`, `tool-versions.json`, `lane-configs/` and (70)
`moved.json`.

Unpack and aggregate with the other runs (`scripts/agg-check.sh` does this; `evaluation.aggregate` exit 0, 858 and
155 cells):

```bash
mkdir -p u/v2 u/a70 u/t70 u/a31 u/t31
tar -xzf results.tar.gz -C u/v2                       # v2-results
tar -xzf autonomous-v2.tar.gz -C u/a70                # autonomous-run-1
tar -xzf tools-v2.tar.gz -C u/t70
python3 scripts/combine_results.py combined u/v2 u/a70 u/t70
# config: paths.results = combined, modes [baseline, dump, rendered, autonomous],
#         once_modes [rendered-raw, pankti, rick, microtestcarver]
python3 -m evaluation.aggregate --config combined.yaml
```

RQ2 later: `python3 -m evaluation.evaluate --config <lane config> --modes pankti rick microtestcarver --steps rq2`
on the tag the cells were generated on (generation and the other steps are not recomputed).
