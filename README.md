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
| `cases/<id>/case.yaml` | target method, scenario test, output test class, hidden tests, build and test commands |
| `cases/<id>/call-dumps.json` | recorded executions of the target method: receiver state, arguments, calls to collaborators with their arguments and results, outcome (65 cases) |
| `cases/<id>/preliminary-test.java` | the test rendered deterministically from the dump (51 cases) |
| `cases/<id>/workspace-files/` | resources the recorded data refer to (large values stored as files), installed into the project for the `dump`, `rendered` and `rendered-raw` modes |
| `projects.yaml` | the subject projects: repository, commit, JDK, license |
| `prompts/` | system prompt and task prompts (common part + one per mode) |
| `pi/provider.ts`, `pi/guard.ts` | Pi extensions: the model provider from the config; the workspace guard |
| `runner/` | one generation cell: workspace, scenario check, agent, result diff |
| `evaluation/` | reference traces (BTrace), RQ1 levels, PIT, quality statistics, tables |
| `scripts/setup.sh` | tools (Node.js, Pi, BTrace, PIT) and the subject projects |
| `scripts/run.py` | the experiment, case by case: references, generation, evaluation; status |
| `config.example.yaml` | provider, model, limits, modes, rounds, paths |

Data provenance: the raw execution data (`call-dumps.json`, `preliminary-test.java`, `workspace-files/`) were collected by a closed-source tool; the repository starts from these data.

## Requirements

- Linux or macOS, 8+ cores and 16 GB RAM recommended, ~40 GB disk for the projects and the Maven repository;
- JDK 21, Maven 3.9, git, Python 3.10+ with PyYAML, curl, network access for the setup;
- an API key of an OpenAI-compatible provider.

`scripts/setup.sh` installs Node.js 22.17.0 and Pi 0.84.1 into `.tools/`, BTrace 2.2.6 (checksum verified) and PIT 1.15.8 into the local Maven repository, clones the projects at the pinned commits and builds them once online, so that all case commands run offline afterwards.

## Quick start

```bash
cp config.example.yaml config.yaml      # set provider.base_url, provider.api_key_env, model, paths
export TESTGEN_API_KEY=...               # the variable named in provider.api_key_env

scripts/setup.sh config.yaml             # tools + projects (about an hour, mostly Maven downloads)

# smoke test: one case, all modes and rounds of the config, with evaluation (~10 min per round)
python3 scripts/run.py --config config.yaml --cases jsoup-normalise-whitespace

# full run (in tmux or similar), progress, tables
python3 scripts/run.py --config config.yaml
python3 scripts/run.py --config config.yaml --status
python3 -m evaluation.aggregate --config config.yaml   # -> <results>/report/report.md and CSV tables
```

Everything is resumable: an existing cell is not regenerated and a computed evaluation step is not recomputed. `python3 -m evaluation.evaluate --config config.yaml --steps rq1 --force` recomputes a step from the saved traces and diffs.

## How a case runs

For every case, `scripts/run.py`:

1. records **reference traces**: the scenario test runs twice under BTrace on a copy of the project with the hidden tests removed; every invocation of the target method is recorded (receiver and argument field graphs up to depth 5, outgoing call signatures, outcome, returned value). The two runs are compared to audit nondeterminism.
2. generates the **cells**: every mode in every round, plus `rendered-raw` once. A cell is a fresh export of the project commit with the existing tests of the target class removed (`hidden_paths`) and no git history. The scenario test must pass in it. The agent gets the prompts and may read the workspace, write only the output test class and run only three commands: `build`, `scenario` (the scenario test) and `generated_test`. It has no debugger and no access outside the workspace through its tools. The result is `result.diff` against the original commit.
3. **evaluates** every cell right after it is generated, on a fresh copy of the compiled project with the diff applied (no execution input, nothing else from the agent run):
   - `verify`: `build` and `generated_test`; this is the compile/pass of the paper, independent of the runner status (a test left by an agent that timed out is evaluated the same way);
   - `rq1`: the generated test runs under the tracer; its invocations of the target method are compared with the reference;
   - `rq2`: PIT on the target class with the generated test only;
   - `quality`: mocks, assertions, readability indicators; for `rendered`, how much of the preliminary test survived.

### Modes

| Mode | Input of the agent besides the code |
|---|---|
| `baseline` | none: the scenario test and the production code only |
| `dump` | the recorded call dump (`carving/call-dumps.json`) |
| `rendered` | the preliminary test (`carving/PreliminaryTest.java.txt`), to be refactored; checks may be added only on recorded values |
| `rendered-raw` | no agent: the preliminary test is installed as the output class as is |

The scenario is given the same way in every mode: the id of the scenario test and the path of its source file. A mode without input for a case (no dump or no preliminary test) gives `method_failed`.

### Metrics (RQ1)

A scenario is a distinct invocation of the target method in the reference. A level is the share of reference scenarios that the generated test reproduces:

| Level | Compared |
|---|---|
| S, structure | outcome (return or exception) and the ordered signatures of the calls the target method makes |
| A, input and output | S + arguments + returned value (main data level) |
| B, with state | A + receiver; receiver fields holding collaborators (mocks, proxies, Spring components, stubs from test sources, `null` against an object) are compared by presence only |
| C, strict | A + full receiver |

The same normalisation applies to every level and mode: time values, UUIDs, identity hash codes, workspace and JUnit temporary paths, JDK collections by interface, and the values listed in `evaluation/nondeterminism.json`. RQ2 reports, per mode, the oracle strength (killed / covered mutants of the target class) and the mutants killed in addition to the baseline test of the same case.

## Known limitations

- There is no operating-system sandbox. The Pi guard confines the agent's tools to the workspace, but the build and test commands the agent runs execute project code, which could read other files of the machine.
- BTrace does not record the arguments and results of the outgoing calls of the target method, nor the type of a thrown exception: the structure level compares call signatures and the outcome only.
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
