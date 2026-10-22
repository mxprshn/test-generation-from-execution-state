# tools-run-1c

MicroTestCarver (mode `microtestcarver`, no agent; `docs/tools.md`) rerun on the 70 cases of the study and on the
31 cases of `cases/tools-projects.csv`, one round (`r1`), with the **full classpath of the target module** given
to its generator. **The MicroTestCarver cells of this run supersede those of `tools-run-1` and `tools-run-1b`**
for the tables of the paper; the `pankti` and `rick` cells of `tools-run-1`/`1b` stay as they are. RQ2 (PIT)
is not computed yet.

## Why

The generator of MicroTestCarver deserializes the recorded objects (XStream) and therefore needs every class
they reference. The authors' build bundled each application of their study with all its dependencies into the
generator (the jars `external/mtc/carver-build.patch` removes). `tools-run-1`/`1b` passed only the compiled
classes of the workspace (`target/classes`), as the earlier harness of the study did, so the generator failed
on objects of dependency classes (XStream `CannotResolveClassException`/`ConversionException`,
`NoClassDefFoundError`). A check on 15 cells (`mtc-check`, same recordings, both classpaths) showed that the
full classpath removes most of these crashes and changes no generated source that the narrow classpath already
produced.

## Code and settings

- Tag `tools-run-1c` = `5d9a09a595fb1c93d7c3f4d3e72f589e0a8dee80` (one commit on top of `d23effe`): the generator's
  classpath is the compiled main and test classes of the workspace and the test classpath of the target module
  (`maven-dependency-plugin:3.6.1:build-classpath`, offline, with the group's Maven repository; the scenario's
  Maven invocation without `-q`, `-Dtest` and the lifecycle phase). Only `runner/tools/mtc.py` changes
  (+ a unit test, `docs/tools.md`).
- Unchanged by the commit (checked): the prompts of the old modes (366 released prompts re-rendered, 0 diffs);
  the generation inputs (case, mode input, project, config, prompts) of the 202 autonomous cells and of the
  303 tool cells of `tools-run-1`/`1b`; the evaluation step code. 161 tests pass.
- Everything else as in `tools-run-1` (`runs/tools-run-1/MANIFEST.md`): the same external tools and versions
  (`tool-versions.json` in the archives), recording on JDK 11 with `-XX:-BytecodeVerificationRemote`, up to 3
  attempts, `tool_timeout` 2400 s; tests run and are evaluated on the group's JDK (21 / 11). Lanes: the same
  split over disjoint projects (`configs/`, from `scripts/make_lanes.py`), mode `microtestcarver` only,
  evaluation `verify`, `rq1`, `quality`, 2 cells per lane. References: copies of the released ones (70) and
  of those of `tools-run-1` (31), byte-identical. `scripts/run-lane.sh` refused to start unless the
  repository was a clean checkout of the tag.

## Time span (UTC)

- 70 cases: 2026-10-22 19:44 – 20:36 (`lane-a`, `lane-b`, exit 0).
- 31 cases: 2026-10-22 20:39 – 20:57 (`tp-lane-a`, `tp-lane-b`, exit 0).
- No incident; every check and the end of the lanes: `RUN-LOG.md`, `logs/`.

## Cells, compared with tools-run-1/1b

`scripts/compare_1c.py` (`transitions.md`); categories: not recorded / generator crash / built, not compiling /
compiles, 0 tests / compiles, failing / passes.

| Group | tools-run-1/1b → tools-run-1c | Cells |
|---|---|---|
| 70 cases | not recorded → not recorded (the project's tests need a newer JDK than 11) | 59 |
| | generator crash → generator crash | 4 |
| | built, not compiling → built, not compiling | 7 |
| 31 cases | not recorded → not recorded | 6 |
| | not recorded → built, not compiling | 1 |
| | generator crash → generator crash | 6 |
| | generator crash → built, not compiling | 11 |
| | generator crash → compiles, 0 tests | 1 |
| | built, not compiling → built, not compiling | 6 |

No MicroTestCarver test passes in either run; RQ1 is 0 in all 101 cells of both runs.

- 70 cases: the classpath changes nothing for the categories. Two of the seven non-compiling classes of jsoup
  differ in their source (`element-css-selector`: the recorded sibling indexes; `element-has-class`: an extra
  `setParentNode(...)` with the HTML text of the parent inserted as code); the others are identical.
- 31 cases: 12 generator crashes become classes (11 do not compile — the object rendered as a string, unknown
  symbols, a wrong constructor; `petclinic-pet-validator-validate` compiles with no test method). 6 crashes
  remain (Mockito classes of `@MockBean` in the recording of `petclinic-pet-type-formatter-parse`, XStream
  conversions in PDFBox and GraphHopper). `gh-douglas-peucker-set-max-distance` was "not recorded" in
  `tools-run-1b` and records the target in this run (11 invocations in the trace): the recording is the
  tool's own, repeated per run; in both runs the scenario JVM under BTrace terminates abnormally (surefire:
  "The forked VM terminated without properly saying goodbye", not the BTrace abort the adapter retries), in
  `tools-run-1b` before the target is reached (2-line trace), in this run after it (796 lines). Its generated
  class does not compile.

## Archives

| Archive | Cells | Size, bytes | sha256 |
|---|---|---|---|
| `tools-mtc-1c-v2.tar.gz` | 70 | 1 233 068 | `4f8b336f33cc7d56f8a27c374a40554887b24dd23c1f6454bbf3fa2d198b0fb1` |
| `tools-mtc-1c-tools-projects.tar.gz` | 31 | 4 376 750 | `e38b9f8a9b4d56f25a7d1a1e01b59199d121fd400b69d1aa3e3b6de846dbcb3e` |

Produced by `scripts/export_results.py` (`scripts/export.sh`), exported twice with identical sha256; the same
redactions as `v2-results` and `tools-run-1` (`/<root>`, `/<home>`, `<host>`, the provider's address and key
variables); no key value, provider address, `/home/vps`, host name or address of the machine in the archives.
`provenance/` holds `RUN-LOG.md`, `tool-versions.json` and `lane-configs/`.

## Combining

Unpack `v2-results`, `autonomous-run-1`, `tools-run-1` and `tools-run-1c`; **remove the `microtestcarver`
cells of the `tools-run-1` archives**; then `scripts/combine_results.py` and `evaluation.aggregate` as in
`scripts/agg-check.sh` (checked: 858 cells for the 70 cases, 155 for the 31; exit 0).
