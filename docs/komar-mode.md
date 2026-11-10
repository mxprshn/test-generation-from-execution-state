# Komar mode

An agent mode that compares observation tools: the `autonomous` mode with the debugger replaced by
[Komar](https://github.com/mxprshn/komar), a tool that records what a JVM program does from a YAML
spec into a JSONL file. The agent gets no recorded execution data. It writes a spec, runs the case's
driving scenario under Komar, queries the output with jq and uses the observed values in the test.
Everything else is the same as in the autonomous mode: `system.txt` and `task-common.txt`, model,
limits, the guard, the limit of scenario runs (`debug_runs`) and the evaluation.

The mode runs only when a config names it (`modes: [komar]`). A config without `modes` runs the four
modes of the study, as before.

## Prompt

`prompts/task-komar.txt` replaces `task-autonomous.txt`. Its first paragraph is the debugger
paragraph with the tool names replaced. The remainder tells the agent where to write specs, gives the
two command forms, and carries a short extract of the Komar documentation at the pinned commit:
the README example, the probe kinds and matching rules, the CEL variables, how values are read, the
limits with their defaults and markers, the record kinds, and two jq queries (from `README.md`,
`docs/design/spec-language.md` and `docs/design/output-format.md`). The extract describes only
features that Komar has at the pinned commit; it gives no procedure.

## Commands

`pi/guard.ts` accepts two command forms besides the named case commands. It reads them word by
word, as bash would, and rejects them when bash would not read them literally (operators,
expansions, globs, comments, line breaks):

    komar run komar/NAME.yaml -o komar/NAME.jsonl -- <the scenario command of the case>
    jq [-c -r -s -n -S -e -j, --arg N V, --argjson N V] 'FILTER' komar/NAME.jsonl ...

The scenario command is the one the harness checks and the debugger runs: `eval_command(case,
"scenario")`, with the project's `eval_maven_args`. jq reads only files in `komar/`; `-f`,
`--rawfile`, `--slurpfile`, `-L` and filters with `import` or `include` are refused. `write` and
`edit` may also write `komar/NAME.yaml`. `komar/` is a scratch directory of the workspace. It is not
part of the result, and the escape check flags a generated test that names it.

`runner/komar.py` has the same rules in Python; `tests/test_komar.py` checks that both agree on a
table of commands.

Pi cuts the output of a bash command at 50 KB, in every mode; the agent sees the tail and the path
of the full output, which lies outside the workspace and cannot be read. A jq query over a large
output is therefore cut too. In the smoke this happened in 2 of 6 cells; both times the agent
narrowed the query. The debugger tools have their own limit (8 KB per answer).

The agent may give a bash command a timeout. In komar-run-1 it gave `komar run` 120 s; under load
Maven needed longer, Pi killed the shell, and the wrapper, Komar and the scenario's JVMs kept running,
detached. komar-run-1b fixed this with the run bound and the supervisor above; no komar-run-1 cell is a
result.

## The wrapper and the run limit

`komar` on the PATH of the agent's commands is a wrapper (`runner/komar.py`, `wrapper`). For each
call it:

- refuses a run beyond `debug_runs` (default 3) with `komar: limit of N scenario runs reached;
  write the test from the data you have`;
- refuses a spec with `calls: true`. As in the debugger, no code of the program runs for the
  observation. Komar reads values from fields unless a spec enables calls;
- runs the pinned binary with the installed agent jar (`KOMAR_AGENT_JAR`, nothing is extracted to
  the user cache) under a supervisor in a session of its own (`--supervise`), which notes every
  process group of the run (Komar starts the scenario in a group of its own) in `komar-pids.txt`;
- bounds a run by 300 s (`RUN_TIMEOUT`, as the first `resume` of the debugger). The guard drops the
  `timeout` the agent gives the bash command of a `komar run`, so Pi does not kill the shell earlier.
  When the run outlives 300 s, the supervisor kills every group of the run (Komar, Maven, the test
  JVMs), the run counts as a run (`timed_out` in the journal), and the agent reads `komar: Komar run
  stopped after 300 s; the scenario did not finish`. When the wrapper itself dies, the supervisor
  kills the run as well;
- journals the call to `artifacts/komar-log.jsonl`: spec text, exit code, seconds, output size,
  hits per probe, diagnostics by level, the `end` record.

A spec that Komar rejects (exit code 2, nothing started) does not count as a run. Neither does a
refused `calls: true`. The process groups of runs still alive when the agent stops are killed (`kill_leftovers` over
`komar-pids.txt`).

`summary.json` gets `komar`, with the counts of the cell: runs, spec errors, refused runs, runs
without an `end` record, runs whose scenario failed, hits, diagnostics, output bytes, jq queries
and failed jq queries. It also gets `komar_versions`. The specs and outputs of the cell are kept
under `artifacts/komar/`; each file is cut at 2 MB.

## Installation

    scripts/setup_komar.sh <config.yaml> [<dir>]

The script builds the pinned commit
`1f4d6e1284bd1647a785caa17ea254a8fb86b131` from source into `paths.komar`, by default
`<paths.tools>/komar`. It uses Go 1.26.10, Gradle 9.8.2 and the config's JDK 21. The Go and Gradle
downloads are sha256-checked; Go modules are checked by Komar's `go.sum`. The build is reproducible:
two builds gave the same binary and agent jar.

`versions.json` records the commit, the toolchain, jq and the sha256 of the binary and the jar. Its
hash is the `mode_input` of the komar cells (`evaluation/inputs.py`). The agent queries the output
with jq; jq must be on PATH (jq 1.7 here).

## Inputs of the other modes

Adding the mode changes no input of the cells of the other modes:

- their prompt hash leaves out `task-komar.txt`;
- `AGENT_MODES` gains `komar`, while the default mode list stays the four modes of the study;
- the guard behaves as before unless its config has a `komar` entry.

The komar cells hash all prompt templates, the installed Komar and `debug_runs`, as in the
autonomous config hash.

## The komar-compact mode

The `komar` mode with another Komar and another way of reading its output. Komar is built from the
branch `compact-view`, commit `03f4534a0fecd75a1e7c097c88fb01922200a92f`
(`KOMAR_COMMIT=03f4534a0fecd75a1e7c097c88fb01922200a92f scripts/setup_komar.sh <config> <dir>`, into a
directory of its own). `komar run` then also writes `NAME.komar` next to the dump `NAME.jsonl`: a
compact view of the run with the hits per probe, warnings, the end record and each distinct execution
of a probed method once, as a tree with its collaborator calls and its outcome. At this commit
call-site hits also record the called method (`callee`), so the dump differs from that of the
komar mode as well.

The agent reads only the view:

- `prompts/task-komar-compact.txt` is `task-komar.txt` with the jq command replaced by reading
  `komar/NAME.komar` with the read tool, the JSONL description and the jq examples replaced by a
  description of the view (checked against `internal/compact` at the commit) and its example from
  `e2e/testdata/compact-view.txtar`, and the marker names of the dump replaced by a pointer to the
  view. The first paragraph says the agent reads a compact view instead of querying with jq. The
  spec documentation is unchanged;
- the guard refuses jq, `read` and `grep` of `komar/*.jsonl`, and a `grep` glob that could match the
  dumps (anything but a negated glob or a glob ending in another extension) over the workspace or
  `komar/`. `runner/komar.py` adds `/komar/*.jsonl` to `.git/info/exclude`, so a plain `grep` of the
  workspace, which keeps the ignore rules, skips the dumps. `find` and `ls` list file names only;
- the wrapper and the run limit are those of the komar mode; the journal also records the size of the
  view (`view_bytes`).

The komar cells keep their prompt hash: it leaves out `task-komar-compact.txt`, and the komar-compact
cells leave out `task-komar.txt`.

