# External test generators: modes `pankti`, `rick`, `microtestcarver`

Three published tools that build unit tests from execution data are run on the same cases, prepared the
same way and evaluated by the same code as the agent modes (verify, RQ1, RQ2, quality). They have no agent:
the tool records the driving scenario of the case with its own instrumentation and generates the test class
with its own generator. Nothing is chosen by hand: the target method comes from `case.yaml`, the workload is
the case's `scenario` command (as the evaluation runs it, `runner.cases.eval_command`), and the generated class
becomes the case's output test file.

| Mode | Tool | Upstream |
|---|---|---|
| `pankti` | Pankti (Tiwari, Zhang, Monperrus, Baudry, IEEE TR 2022): differential tests from recorded receiver, arguments and result | [ASSERT-KTH/pankti](https://github.com/ASSERT-KTH/pankti) at `c755a13` |
| `rick` | RICK (Tiwari, Monperrus, Baudry, TSE 2024): Pankti's recording plus the mockable calls of the target; output, parameter and call oracles with mocks | same repository (`rick/`) |
| `microtestcarver` | MicroTestCarver (Deljouyi, Zaidman, SCAM 2023): modified BTrace trace of the target class, Velocity templates | replication package [amirdeljouyi/SCAM-2023-microtestcarver-replication](https://github.com/amirdeljouyi/SCAM-2023-microtestcarver-replication) at `88af3ad` |

The adapter (`runner/tools/`) is a port of the harness of the earlier study (aithd-ppr, `baselines/tool.py`,
`baselines/mtc.py`); the patches are the same files.

## Install

`scripts/setup_external_tools.sh <config> [<dir>]` (not part of `scripts/setup.sh`; the default setup is
unchanged) installs into `paths.external_tools` (default `<paths.tools>/external`), checksum-verified:
Temurin 17.0.20.1 (Pankti/RICK analysis and generation, recording of JDK 21 projects), Temurin 11.0.32.1 (MicroTestCarver recording),
Corretto 8.462 (BTrace build), Gradle 7.6, Glowroot 0.14.0, a Python venv with numpy 1.26.4 and pandas 2.2.2
(Pankti's instrumentation scripts), the two upstream commits with the patches below, and the test
dependencies of the generated tests (XStream 1.4.12, JUnit Jupiter 5.10.3, Mockito 5.11.0, Hamcrest 2.2) in the
config's Maven repository so that the evaluation runs offline. It writes `versions.json`, which every tool
cell records (`summary.json: tool_versions`) and which is part of the cell's input hash.

## A cell of a tool mode

1. Preparation as for every mode: exported commit, hidden tests removed, a fresh git repository, the
   project's prebuild, the scenario check.
2. The tool: Pankti/RICK — `pankti-extract` on a copy of the module of the target class (Spoon, language
   level 17), the row of the target method (class, name, erased parameter types), a Glowroot plugin built for
   it, the scenario command with Glowroot in the forked test JVM on JDK 17, generation. MicroTestCarver — the
   authors' BTrace probe for the target method and the constructors of its class, the scenario with the
   modified BTrace in the forked test JVM on JDK 11, generation from the trace and the object pool. The tool
   run is limited by `tool_timeout` (2400 s).
3. The generated class of the target is renamed to the output class and written to the output test file.
   Pankti and RICK load the recorded objects from XML files: those the test refers to are copied to the test
   resources of the module. Test dependencies the generated code needs and the module lacks are added to the
   module `pom.xml` (the project's own versions win). `result.diff` holds the class, the XML files and the pom.
4. Status `completed` when a test class was produced, otherwise `failed` with the tool's reason in `error`
   and `summary.json: tool.outcome` (e.g. not a candidate of pankti-extract, no mockable invocations, not
   recordable on JDK 11, the generator failed). Such a cell has no test and scores 0 like any cell without
   a passing test; it is never dropped. An uninstalled tool is an infrastructure failure (`ToolSetupError`).
5. Evaluation: unchanged. `verify` applies `result.diff` to a clean copy and runs `build` and
   `generated_test` on the config's JDK; RQ1 traces the generated test; quality measures the test class only
   and counts the object files (`quality.object_files`).

The generated tests of Pankti and RICK deserialize objects with XStream 1.4.12, which needs `--add-opens` of
JDK packages on JDK 17+: a config of these modes sets `env: JDK_JAVA_OPTIONS` to the list of
`runner.tools.common.ADD_OPENS` (it reaches the JVMs of the evaluation as well; the agent modes never run
with it).

## Deviations from the upstream tools

All are needed to run the tools on these projects at all; none adds a capability.

`external/pankti.patch` (Pankti and RICK):

| Change | Reason |
|---|---|
| The limit on recorded invocations is lifted (upstream records the first invocation of a target method; nested invocations up to 10) | the cases reproduce every invocation of the scenario; with the limit only one could be reproduced |
| Object file names use simple parameter type names | the upstream names exceed the 255-byte file name limit for long signatures |
| The storage directory is a system property (`pankti.storage.dir`) | one directory per cell instead of the shared `/tmp/pankti-object-data` |
| Spoon 8.0.0 → 10.4.2, no-classpath mode, language level from `pankti.compliance` (17 used) | Spoon 8 cannot parse the Java 17 sources of JGit; the dependency classpath is not resolved offline |
| Spoon's identifier validation is not enforced (`setIgnoreSyntaxErrors`) | Spoon 10 validates identifiers, Spoon 8 (which the generators were written for) did not |
| Returned strings are escaped as Java string literals | upstream inserts the raw value, which does not compile for quotes or line breaks |

Source fixes of the generated class (`runner/tools/pankti.py: fix_source`), recorded in the summary:

| Change | Reason |
|---|---|
| Imports of `Field`, `Assertions`, `InOrder`, mocked types and Mockito argument matchers added | RICK uses them without importing them (does not compile) |
| Unused imports and the unused `expectedObject` local (with the string it was read from) removed; unused private helpers made package-private | JGit compiles tests with ecj settings that make these errors |

`external/mtc/btrace-build.patch`, `external/mtc/carver-build.patch` (MicroTestCarver, build only; the tool
code is not changed):

| Change | Reason |
|---|---|
| BTrace: the `versioning` Gradle plugin removed, the build revision a constant | its grgit dependency is no longer resolvable |
| Carver: the jars of the applications of the original study removed from the tool's dependencies, hibernate-core added explicitly, zip64 shadow jar | the jars were on its classpath only to deserialize their own objects; the project under test is put on the classpath instead |

Source fixes of the generated class (`runner/tools/mtc.py: fix_source`): unused imports and the import of the
class under test from its own package removed (JGit's ecj settings).

## Conditions of the tools that are kept

- Pankti and RICK: only public, non-static, non-abstract methods are candidates; RICK generates tests only
  for methods with mockable invocations. Glowroot 0.14 cannot weave JDK 21 class files: the scenario of a
  JDK 21 project is recorded on JDK 17 (`-Djvm` of surefire) with `--add-opens` for XStream, the generated test
  runs on JDK 21. A JDK 11 project is recorded on JDK 11, the JDK its tests run on: XStream serializes the
  fields of JDK classes, which differ between JDK versions (e.g. `RandomAccessFile.closeLock` exists on 17,
  not on 11). The generators need JUnit 5:
  JUnit 4 modules (JGit, PDFBox) get the Jupiter API and engine as test dependencies.
- MicroTestCarver: the modified BTrace records only on JDK 11 (on 17+ the JVM aborts when BTrace
  retransforms a class), so projects whose tests need a newer JDK cannot be recorded; its bytecode fails
  verification, so the scenario JVM runs with `-XX:-BytecodeVerificationRemote`; the abort is intermittent on
  JDK 11 as well, the recording is retried up to three times; if BTrace breaks the bytecode of the class
  (`VerifyError`), the trace is narrowed to the target method; JaCoCo is skipped while recording.
- Tool cells run once (round 1): the tools are deterministic (checked on reruns, `runs/tools-run-1`).
