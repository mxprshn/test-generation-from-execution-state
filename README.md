# Test generation from execution state

Companion repository for the paper **Automatic Test Generation from Execution State Using Large Language Models** (Trudy ISP RAN / Proc. ISP RAS, in Russian).

Authors:
- M. A. Parshin, Saint Petersburg State University, ORCID [0009-0006-8486-3816](https://orcid.org/0009-0006-8486-3816)
- D. S. Stepanov, ITMO University, ORCID [0000-0003-1719-0325](https://orcid.org/0000-0003-1719-0325)
- D. V. Koznov, Saint Petersburg State University, ORCID [0000-0003-2632-3193](https://orcid.org/0000-0003-2632-3193)

The repository contains the data set of the experiment (70 target methods in 7 open-source Java projects, with the execution data recorded while an existing high-level test ran), the prompts, a runner that generates unit tests with the [Pi](https://www.npmjs.com/package/@earendil-works/pi-coding-agent) coding agent and any OpenAI-compatible model, and the evaluation (reference traces, scenario reproduction, mutation testing, test quality).

## Contents

| Path | What |
|---|---|
| `cases/cases.csv` | the 70 cases: project, group (`direct`: the scenario test calls the target method itself, `indirect`: through a call chain), target method, scenario test, which execution data exist |
| `cases/tools-projects.csv` | 31 further cases on the projects Pankti, RICK and MicroTestCarver were evaluated on (PDFBox, GraphHopper, Spring PetClinic, Lab-Insurance; JDK 11), for the autonomous mode and the tool modes only; `cases/<id>/meta.yaml` per case; see `docs/tools-projects.md` |
| `cases/<id>/case.yaml` | target method, scenario test, output test class, hidden tests, build and test commands |
| `cases/<id>/call-dumps.json` | recorded executions of the target method: receiver state, arguments, calls to collaborators with their arguments and results, outcome (65 cases) |
| `cases/<id>/preliminary-test.java` | the test rendered deterministically from the dump (51 cases) |
| `cases/<id>/workspace-files/` | resources the recorded data refer to (large values stored as files), installed into the project for the `dump`, `rendered` and `rendered-raw` modes |
| `runner/tools/`, `external/`, `scripts/setup_external_tools.sh` | the external test generators Pankti, RICK and MicroTestCarver as modes without an agent (`pankti`, `rick`, `microtestcarver`): adapter, the patches applied to the pinned upstream sources, their installation; see `docs/tools.md` |
| `projects.yaml` | the subject projects: repository, commit, JDK, license (and `prebuild` commands where a case needs sibling modules installed) |
| `prompts/` | system prompt and task prompts (common part + one per mode) |
| `pi/provider.ts`, `pi/guard.ts` | Pi extensions: the model provider from the config; the workspace guard |
| `runner/` | one generation cell: workspace, scenario check, agent, result diff; escape check (`runner/escape_check.py`) |
| `evaluation/` | reference traces (BTrace), RQ1 levels, PIT, quality statistics, tables |
| `scripts/setup.sh` | tools (Node.js, Pi, BTrace, PIT) and the subject projects |
| `scripts/run.py` | the experiment, case by case: references, generation, evaluation; status |
| `config.example.yaml` | provider, model, limits, modes, rounds, paths |

Data provenance: the raw execution data (`call-dumps.json`, `preliminary-test.java`, `workspace-files/`) were collected by a closed-source tool; the repository starts from these data.

## Requirements

- Linux or macOS, 8+ cores and 16 GB RAM recommended, ~5 GB disk for the tools, the projects and the Maven repository, plus space for results;
- JDK 21, Maven 3.9, git, Python 3.10+ with PyYAML, curl, network access for the setup;
- an API key of an OpenAI-compatible provider.

`scripts/setup.sh` installs Node.js 22.17.0 and Pi 0.84.1 into `.tools/`, BTrace 2.2.6 (checksum verified) and PIT 1.15.8 with the JUnit vintage engine 5.9.3 (JUnit 4 tests of JGit run on the JUnit Platform under PIT) into the local Maven repository, clones the projects at the pinned commits and builds them once online, so that all case commands run offline afterwards.

## Quick start

```bash
cp config.example.yaml config.yaml      # set provider.base_url, provider.api_key_env, model, paths
export TESTGEN_API_KEY=...               # the variable named in provider.api_key_env

scripts/setup.sh config.yaml             # tools + projects (about 30 min on 8 cores, mostly Maven;
                                         # rerun it after an interruption, --rebuild to rebuild projects)

# smoke test: one case, all modes and rounds of the config, with evaluation (~10 min per round)
python3 scripts/run.py --config config.yaml --cases jsoup-normalise-whitespace

# full run (in tmux or similar), progress, tables
python3 scripts/run.py --config config.yaml
python3 scripts/run.py --config config.yaml --status
python3 -m evaluation.aggregate --config config.yaml   # -> <results>/report/report.md and CSV tables
```

Everything is resumable: an existing cell is not regenerated and a computed evaluation step is not recomputed. Every cell and every evaluation step records hashes of what it depends on (the code it is computed by — `runner/` and `pi/` for generation, the modules of each evaluation step for that step —, case, mode input, prompts, config, project entry, normalisation lists, reference trace; `evaluation/inputs.py`). A stored result whose inputs differ from the current ones is never reused silently: the run stops with a message, and `python3 -m evaluation.evaluate --config config.yaml --steps rq1 --force` recomputes a step (a generation cell is regenerated by removing its directory). After a change of the comparison or the normalisation only, `--steps rq1 --force --reuse-traces` compares the kept candidate traces again without running the tests. A step that failed for a transient reason (a timeout, a tool error) is not stored and runs again next time.

## How a case runs

For every case, `scripts/run.py`:

1. records **reference traces**: the scenario test runs twice under BTrace on a copy of the project with the hidden tests removed; every invocation of the target method is recorded (receiver and argument field graphs up to depth 5, outgoing call signatures, outcome, returned value). The two runs are compared to audit nondeterminism.
Before the first cell, one minimal request checks that the configured model answers on one of the configured keys (`--skip-probe` skips it); the run does not start otherwise.

2. generates the **cells**: every mode in every round, plus `rendered-raw` once. A cell is a fresh export of the project commit with the existing tests of the target class removed (`hidden_paths`) and no git history. The scenario test must pass in it. The agent gets the prompts and may read the workspace, write only the output test class and run only three commands: `build`, `scenario` (the scenario test) and `generated_test`. It has no debugger and no access outside the workspace through its tools. The result is `result.diff` against the original commit.
   A cell that failed for an infrastructure reason is moved aside (`*.infra-*`) and generated once more: a gateway error (429, 5xx, connection failures), a 4xx answer of the gateway before the agent made any tool call (e.g. a model the gateway lists but does not serve), a provider that could not be reached, a failed workspace preparation. A 401/403 stops the whole run: the key is refused.
   After the agent, the **escape check** (`runner/escape_check.py`) looks for signs that the agent or its test reached outside the workspace (see Known limitations). It records `escape_check` in the cell summary with the status `clean`, `denied_only` (the guard refused an action inside the workspace, nothing left it) or `escape` (the agent or its test reached outside the workspace or read the mode input at run time); every cell is evaluated as usual.
3. **evaluates** every cell right after it is generated, on a fresh copy of the compiled project with the diff applied (no execution input, nothing else from the agent run). Build and test commands of the evaluation are the case commands plus the project's `eval_maven_args` from `projects.yaml` (for Klaw: code-format checks off), the same for every mode; the generated test is never reformatted:
   - `verify`: `build` and `generated_test`; this is the compile/pass of the paper, independent of the runner status (a test left by an agent that timed out is evaluated the same way). A test passes when the command succeeds and the surefire reports of the generated class show at least one test run and no failure, error or skipped test (`tests_run`, `failures`, `errors`, `skipped` are recorded), so a class whose tests are all disabled, stopped by assumptions or not seen by the JUnit engine does not pass;
   - `rq1`: the generated test runs under the tracer; its invocations of the target method are compared with the reference. All invocations count; the record also shows the share of scenarios called from the generated class itself (`called_from_test_class`) and flags tests that run the scenario or call the target mostly through other classes (`runs_scenario`);
   - `rq2`: PIT on the target class with the generated test only;
   - `quality`: mocks, assertions, readability indicators; for `rendered`, how much of the preliminary test survived.

### Modes

| Mode | Input of the agent besides the code |
|---|---|
| `baseline` | none: the scenario test and the production code only |
| `dump` | the recorded call dump (`carving/call-dumps.json`) |
| `rendered` | the preliminary test (`carving/PreliminaryTest.java.txt`), to be refactored; checks may be added only on recorded values |
| `rendered-raw` | no agent: the preliminary test is installed as the output class as is |

The scenario is given the same way in every mode: the id of the scenario test and the path of its source file. A mode without input for a case (no dump or no preliminary test) gives `method_failed`. The prompts differ only in the mode block (`prompts/task-<mode>.txt`); the baseline block also says how to find the values without execution data (read the scenario and the production code along the call chain).

Workspaces, traces and PIT runs live in a scratch directory outside the results and the repository (`paths.work`, by default a fresh directory in the system temp directory), under names that reveal neither the case nor the mode.

### Metrics (RQ1)

A scenario is a distinct invocation of the target method in the reference. A level is the share of reference scenarios that the generated test reproduces:

| Level | Compared |
|---|---|
| S, structure | outcome (return, or the class of the thrown exception) and the ordered signatures of the calls the target method makes |
| A, input and output | S + arguments + returned value (main data level); a value inside the arguments or the result that is a collaborator on either side (mock or proxy, an object of an infrastructure type such as a JDBC driver, a lambda, a component, a stub from test sources) is compared by presence only, keeping whether it was null |
| B, with state | A + receiver; receiver fields holding collaborators (mocks, proxies, Spring components, stubs from test sources, `null` against an object) are compared by presence only, keeping whether they were null; decided for each pair of reference and candidate trace |
| C, strict | arguments, returned value and the full receiver, without any collaborator markers |

The same normalisation applies to every level and mode: time values, UUIDs, identity hash codes, workspace and JUnit temporary paths (a path inside the workspace is compared relative to it), JDK collections by interface, JDK proxy class names, field names without the declaring-class prefix of inherited fields (unless two fields would clash), and the types, type prefixes and fields listed in `evaluation/nondeterminism.json`; a value of a nondeterministic type (time, UUID) also equals `null` at the same position on the other side, at every level. Inline Mockito mocks of concrete classes are recorded as mocks. The tracer writes at most 200 elements of a collection in iteration order; a truncated Set or Map is compared by its interface and size only. A reference is used only if both recordings exit with 0, record the target and agree on every level; a rejected reference keeps `audit-failed.json` and its traces (`failed-run<i>.json`) for inspection.

Tables (`evaluation/aggregate.py`): applicability (cases with input per mode) separately from quality; recall per case (mean over rounds), averaged over cases and over projects; every mode paired with baseline on the cases where both have input (mean difference, bootstrap 95% interval, Wilcoxon signed-rank test); scenario-pooled shares for reference only. RQ2 reports, per mode, the oracle strength (killed / covered mutants of the target class) and the mutants killed in addition to the baseline test of the same case and round (a baseline test that does not pass has killed nothing).

## Results

The results of the run reported in the paper are not in the repository: they are the asset `results.tar.gz` of the release `v2-results` (≈ 20 MB, ≈ 310 MB unpacked, sha256 `47a3855c25ed4defbff5634da35e52e474825e48bc237cab658d899cc4331264`). The run: model `OpenAI/gpt-6.1-sol`, thinking `medium`, 600 s per agent cell; 70 cases, rounds 1 and 2 of `baseline`, `dump`, `rendered` and one `rendered-raw` cell per case, 490 cells; 2026-10-12/13.

```bash
gh release download v2-results -p results.tar.gz     # or: curl -LO https://github.com/mxprshn/test-generation-from-execution-state/releases/download/v2-results/results.tar.gz
shasum -a 256 results.tar.gz                         # must match the sha256 above
mkdir -p results && tar -xzf results.tar.gz -C results
```

| Path in `results/` | What |
|---|---|
| `cells/<case>/<mode>/<run-id>/` | `summary.json` (generation: status, time, tokens, cost), `eval.json` (verify, RQ1, RQ2, quality, escape check, input hashes), `result.diff` and `artifacts/generated/` (the generated test), `prompts/` (as sent, incl. `system.effective.txt`), `artifacts/session.jsonl` (the agent session, cleaned, see below), `agent-commands.jsonl`, `denied.jsonl`, `pi-stderr.txt`, `scenario-check.log`, `eval-artifacts/` (verify logs, candidate traces, `mutations.xml`, PIT log) |
| `cells/<case>/<mode>/<run-id>.redo-*` | earlier attempts of regenerated cells |
| `references/<case>/` | reference traces (`run1.json`, `run2.json`) and their audit; `old-b978737/` for the 9 references rejected in the main run |
| `status/`, `logs/` | per-case progress and log of the runner |
| `report/` | the output of `python3 -m evaluation.aggregate` |
| `provenance/` | distinct configs of the runs and every start (`runs.jsonl`), lane configs (`lane-configs/all.yaml` covers all 70 cases), regenerated cells with reasons (`moved.json`), load of the machine (`load/`) |

To recompute the tables from the records, or RQ1 from the kept traces, use a config with the settings of `results/provenance/lane-configs/all.yaml` and `paths.results: results`:

```bash
python3 scripts/run.py --config config.yaml --status                    # 490 cells evaluated
python3 -m evaluation.aggregate --config config.yaml                    # -> results/report, as published
python3 -m evaluation.evaluate --config config.yaml --steps rq1 --force --reuse-traces
```

Left out: interrupted and infrastructure-failed attempts that were regenerated (`*.incomplete-*`, `*.infra-*`) and pre-migration copies of records. Agent sessions are kept without the redundant parts of Pi's event stream: streaming events, repeated message content and the opaque signatures of reasoning blocks (`scripts/export_results.py`); every message, tool call, tool result, usage and timestamp is kept. Three timeouts happened while the machine was overloaded (`mybatis-foreach-parameter-handler` dump/r2, `mybatis-keygen-jdbc3` dump/r1, `mybatis-prepared-statement-update` rendered/r2) and are kept as results.

Redactions (every file): the root directory of the machine → `/<root>`, the home directory → `/<home>`, the user name in surefire reports → `<user>`, the host name → `<host>`, the provider's address → `https://<openai-compatible-gateway>/v1`, the names of the key variables → `TESTGEN_API_KEY`, `TESTGEN_API_KEY_FALLBACK`. Passwords, e-mail addresses and tokens that occur in the code and test data of the subject projects are left as they are.

**Autonomous run.** The `autonomous` mode (`docs/autonomous-mode.md`) was run on the 70 cases and on the 31 cases of `cases/tools-projects.csv` (2026-10-21/22, tag `autonomous-run-1`, the settings above, `debug_runs` 3; RQ2 not yet computed). Its results are two further archives, `autonomous-v2.tar.gz` and `autonomous-tools-projects.tar.gz`, made the same way; `runs/autonomous-run-1/MANIFEST.md` lists their sha256, the configs, the cell counts, the timeouts and the four infrastructure reruns, and shows how to combine them with `results.tar.gz` for `evaluation.aggregate`.

The generated tests, the traces, the sessions and the PIT reports contain code and data of the subject projects; they are distributed under the licenses of the respective projects (see Licenses). The archive is produced by `scripts/export_results.py`.

## Known limitations

- There is no operating-system sandbox. The Pi guard confines the agent's tools (read, write, search, bash) to the workspace and to the three case commands. The build and test commands execute project code, including the generated test, which could read other files of the machine. Every cell is therefore checked automatically (`runner/escape_check.py`): the agent's tool calls (absolute paths, `..` outside the workspace, harness locations), every action denied by the guard, and the generated test (reading the mode input such as `carving/call-dumps.json` at run time, absolute paths, `../` paths, environment and user locations, process execution, walking up or listing the file system from the working directory). Findings fall into two classes: `escape` (paths or commands outside the workspace, harness locations, anything flagged in the generated test) and `denied` (an action the guard refused that stays inside the workspace, such as a shell command line instead of a named case command). `report.md` shows both per mode in `escape_check_by_mode`: cells with an escape are listed one by one, cells with denied actions only are counted with the kinds of refused actions. No cell is excluded automatically.
- The arguments and results of the outgoing calls of the target method are not recorded: the structure level compares call signatures and the outcome. The class of a thrown exception is recorded (BTrace `Kind.ERROR`); an exceptional exit detected only by the fallback (stack depth) has no class.
- Commands run with an allowlisted environment (`runner/config.py`: `ENV_ALLOWLIST`); the API key reaches only the Pi provider extension, which removes it from its environment before any command runs.
- `hidden_paths` remove the existing tests of the target class and their compiled classes; other tests of the project may still call the target and are visible to the agent in every mode (e.g. `TagSet.valueOf` in jsoup's `XmlTreeBuilderTest`, `HtmlTreeBuilderTest`, `TokeniserStateTest`; `Element.hasClass` in `CssTest`). The data of the cases is used as published.
- Model latency counts against the wall-clock limit of a cell.

## Licenses

The code and the data of this repository are licensed under the Apache License 2.0 (`LICENSE`).

The subject projects are not included; `scripts/setup.sh` clones them. The data contain fragments of their code and test data (class and method names, string values, test resources) under the projects' licenses: Apache-2.0 (Apollo, Klaw, MyBatis 3), MIT (jsoup), LGPL-2.1 (Checkstyle), EDL-1.0 (JGit), FSL-1.1-ALv2 (Liquibase). BTrace (GPL-2.0) and PIT (Apache-2.0) are downloaded by the setup and not redistributed.

## Citation

```bibtex
@article{parshin2026execstate,
  author  = {Parshin, M. A. and Stepanov, D. S. and Koznov, D. V.},
  title   = {Automatic Test Generation from Execution State Using Large Language Models},
  journal = {Trudy ISP RAN / Proc. ISP RAS},
  year    = {TODO},
  volume  = {TODO},
  number  = {TODO},
  pages   = {TODO},
  doi     = {TODO}
}
```
