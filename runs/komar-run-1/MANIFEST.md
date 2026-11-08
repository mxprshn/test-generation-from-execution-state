# komar-run-1

Mode `komar` (`docs/komar-mode.md`): the autonomous mode of the study with the debugger replaced by Komar
(github.com/mxprshn/komar `1f4d6e1`). The agent writes a Komar spec, runs the driving scenario under
`komar run` and reads the JSONL output with jq. 101 cases (the 70 of the study and the 31 of
`cases/tools-projects.csv`) x 2 rounds = 202 cells. Compared for quality with autonomous-run-1; time and cost
are given, not compared (the runs are weeks apart). RQ2 (PIT) is not computed.

**Every cell of this run was generated on tag `komar-run-1b`. No cell generated on `komar-run-1` is a result.**

## Code and settings

- Tag `komar-run-1` = `bca1e3e`: the mode (`a448e85`) and a docs note. The run started on it and was stopped
  after 10 minutes: the agent gave `komar run` a bash timeout of 120 s; under load Maven needed longer, Pi
  killed the shell, and the wrapper, Komar and the scenario's JVMs kept running detached (8 such runs, load
  40 on 8 cores). The 8 cells then running (mybatis, round 1, none finished) were stopped unevaluated and kept
  aside; they are not in the archives.
- Tag `komar-run-1b` = `5633764`: the guard drops the bash timeout of `komar run`; the wrapper bounds a run
  by 300 s itself (the first `resume` of the debugger has the same bound) under a supervisor in a session of
  its own, which kills every process group of the run (Komar, Maven, test JVMs) on expiry or when the
  wrapper dies; an expired run counts as a run and the agent reads "Komar run stopped after 300 s; the
  scenario did not finish". The prompt is unchanged. 187 tests pass. Unchanged by the commit (checked): the
  prompts of the old modes (366 released prompts re-rendered, 0 diffs), the generation inputs of the 202
  autonomous cells and of the 404 tool cells.
- `0e70a44` (after the run): `komar` in the mode order of `evaluation/code_style.py` (post-hoc analysis only).
- Settings of autonomous-run-1: `OpenAI/gpt-6.1-sol`, thinking medium, agent timeout 600 s, command timeout
  900 s, `debug_runs` 3 (here: at most 3 Komar runs per cell), evaluation `verify`, `rq1`, `quality`.
  References: copies of the released ones (70) and of those of tools-run-1 (31), byte-identical.
- Concurrency: 4 cells at once, as in autonomous-run-1, and never two cells of the same project at once
  (`scripts/worker.sh`: a worker takes the first queued case whose project no other worker runs; the set of
  concurrent projects is on every start line of `logs/dispatch.log`). Each worker runs both rounds of its
  case (`run.py --cases`); `scripts/run-lane.sh` refused to start unless the worktree was a clean checkout
  of the tag, and every `run.py` call checked the model first.

## Time span (UTC)

2026-11-07 22:15 – 2026-11-08 06:09. No incident on `komar-run-1b`; checks every 10 minutes in `RUN-LOG.md`.
No cell ended on a gateway error; no infra rerun.

## Cells

| Group | Cells | Completed | Timed out (600 s) | Compile | Pass |
|---|---|---|---|---|---|
| 70 | 140 | 124 | 16 | 138 | 133 |
| 31 | 62 | 62 | 0 | 62 | 62 |

The 16 timeouts (autonomous-run-1: 5) are cells of mybatis (9), checkstyle (3) and liquibase (4); each made
2–3 Komar runs of 85–158 s (a full Maven run of the scenario). No Komar run reached the 300 s bound. One
escape finding (`jgit-merger-merge/r1`: `//` inside a jq filter read as a path; a false positive of the check).

## RQ1 (`evaluation.aggregate`; mean over cases, each case the mean of its rounds; a cell without a passing test scores 0)

| Group | Mode | S | A | B | C |
|---|---|---|---|---|---|
| 70 | baseline | 0.980 | 0.722 | 0.431 | 0.264 |
| 70 | autonomous | 0.986 | 0.893 | 0.629 | 0.286 |
| 70 | komar | 0.950 | 0.872 | 0.619 | 0.318 |
| 31 | autonomous | 1.000 | 0.919 | 0.532 | 0.306 |
| 31 | komar | 1.000 | 0.935 | 0.554 | 0.328 |

Paired by case (`scripts/paired.py`, the functions of `evaluation.aggregate`; bootstrap 95% CI, Wilcoxon):

| Group | Comparison | Level | Diff | 95% CI | Better / equal / worse | p |
|---|---|---|---|---|---|---|
| 70 | komar − autonomous | S | −0.036 | [−0.086, +0.007] | 1 / 65 / 4 | 0.17 |
| 70 | komar − autonomous | A | −0.021 | [−0.078, +0.029] | 4 / 61 / 5 | 0.62 |
| 70 | komar − autonomous | B | −0.011 | [−0.074, +0.048] | 8 / 54 / 8 | 0.98 |
| 70 | komar − autonomous | C | +0.031 | [+0.000, +0.068] | 6 / 63 / 1 | 0.10 |
| 70 | komar − baseline | A | +0.151 | [+0.055, +0.248] | 20 / 46 / 4 | 0.006 |
| 70 | komar − baseline | B | +0.187 | [+0.081, +0.295] | 24 / 42 / 4 | 0.003 |
| 70 | komar − baseline | C | +0.054 | [−0.008, +0.121] | 9 / 59 / 2 | 0.10 |
| 31 | komar − autonomous | A | +0.016 | [+0.000, +0.048] | 1 / 30 / 0 | 1.0 |
| 31 | komar − autonomous | B | +0.022 | [−0.059, +0.113] | 3 / 26 / 2 | 0.78 |
| 31 | komar − autonomous | C | +0.022 | [−0.059, +0.113] | 3 / 26 / 2 | 0.78 |

## Code style (`evaluation/code_style.py`, definitions in `docs/code-style.md`)

| Group | Mode | Test methods | Naturalness, bits/token (per method / per case) | Reflection | XML |
|---|---|---|---|---|---|
| 70 | developers | 2202 | 2.56 / 2.89 | 9.9% | 0 |
| 70 | autonomous | 970 | 4.03 / 3.60 | 33.7% | 0 |
| 70 | komar | 970 | 3.74 / 3.64 | 24.2% | 0 |
| 31 | developers | 65 | 3.23 / 3.24 | 0 | 0 |
| 31 | autonomous | 127 | 3.61 / 3.70 | 30.7% | 0 |
| 31 | komar | 130 | 3.48 / 3.62 | 30.8% | 0 |

Within a case, komar is less natural than autonomous in 36 of 70 cases (more natural in 33) and in 13 of 30
(16). The other modes' numbers equal those of `results/code-style` (autonomous-mode branch).

## Use of Komar (202 cells)

- Runs per cell: 0 in 4 cells, 1 in 114, 2 in 76, 3 in 8; 290 runs, median 68 s, max 158 s; none hit the
  300 s bound; no run refused by the limit. 4 runs recorded no hit.
- Spec errors (reported before anything started, not counted as runs): 20, in 18 cells.
- Specs: `depth` raised above the default 3 in 187 of 290 runs; call-site probes in 166, line probes in 15,
  `when` in 4, `stop` in 4.
- jq queries per cell: median 2; none failed.
- Agent time per cell (median): 323 s (70), 174 s (31). Tool calls per cell: median 12. Cost: $19.25 in
  total, median $0.081 per cell.

## Archives

| Archive | Cells | Size, bytes | sha256 |
|---|---|---|---|
| `komar-v2.tar.gz` | 140 | 8 012 239 | `82ddc6b787eae435e8c24fce7398d0d03168e56bc423e9896fa3b818973efb98` |
| `komar-tools-projects.tar.gz` | 62 | 10 748 482 | `4a996e4352cfc80e20109b7fe6c1a5160722f90553f5b0fb57df606f986a4c34` |

Produced by `scripts/export_results.py` (`scripts/export.sh`), exported twice with identical sha256, with the
run configs, `RUN-LOG.md`, `dispatch.log` and Komar's `versions.json`; paths, host and provider redacted, no
key. Combine with the autonomous-run-1 and v2 archives by `scripts/combine_results.py` (`scripts/aggregate.sh`).

## Files

`configs/` (worker configs w1–w4 per group), `scripts/` (make_configs.py, worker.sh, run-lane.sh, start.sh,
stop.sh, check.py, checker.sh, hashcheck.py, infra_scan.py, paired.py, aggregate.sh, export.sh),
`RUN-LOG.md`, `logs/dispatch.log`.
