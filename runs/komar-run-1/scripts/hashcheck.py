"""komar-mode branch: the inputs of every finished cell are what the current code computes (except the code hash).
Old modes (v2-release): prompts re-rendered byte for byte, case/mode_input/project/config/prompts of r1 cells.
autonomous (autonomous-v2, autonomous-tools-projects) and tool cells (tools-v2, tools-tools-projects, tools-mtc-1c-*):
case/mode_input/project/config/prompts of every cell, rendered prompts of the autonomous cells.
Usage (from the repository): python3 hashcheck.py"""
import json, sys
from pathlib import Path
sys.path.insert(0, ".")
from runner.cases import load_case
from runner.config import load_config, AGENT_COMMANDS
from runner.prompts import render
from evaluation import inputs as I
R = Path.home() / "repos/results"
GROUPS = [("v2-release", "hashcheck-config.yaml", ("baseline", "dump", "rendered"), "r1-only"),
          ("autonomous-v2", "config.yaml", ("autonomous",), "all"),
          ("autonomous-tools-projects", "config-tools-projects.yaml", ("autonomous",), "all")]
TOOLS = [("tools-v2", "lane-a.yaml"), ("tools-tools-projects", "tp-lane-a.yaml"),
         ("tools-mtc-1c-v2", None), ("tools-mtc-1c-tools-projects", None)]
def check(results, cfg, modes, which):
    n = np = pdiff = 0; diffs = {}
    for cell in sorted((R / results / "cells").glob("*/*/r*")):
        case_id, mode, run = cell.parts[-3], cell.parts[-2], cell.parts[-1]
        if "." in run or mode not in modes or not (cell / "summary.json").is_file():
            continue
        case = load_case(case_id, cfg.cases_dir)
        if (cell / "prompts" / "task.txt").is_file():
            pr = render(cfg.prompts_dir, case, mode, {k: case.commands[k] for k in AGENT_COMMANDS}, cfg.debug_runs)
            np += 1
            for k in ("system", "task"):
                pdiff += pr[k] != (cell / "prompts" / f"{k}.txt").read_text(encoding="utf-8")
        if which == "r1-only" and run != "r1":
            continue
        stored = json.loads((cell / "summary.json").read_text()).get("inputs") or {}
        cur = I.generation_inputs(cfg, case, mode)
        n += 1
        for k in ("case", "mode_input", "project", "config", "prompts"):
            if stored.get(k) != cur.get(k):
                diffs.setdefault(k, []).append(f"{case_id}/{mode}/{run}")
    return n, np, pdiff, diffs
for results, conf, modes, which in GROUPS:
    cfg = load_config(str(Path.home() / "repos/full-run" / conf))
    n, np, pdiff, diffs = check(results, cfg, modes, which)
    print(f"{results}: cells {n}, prompts re-rendered {np}, prompt diffs {pdiff}, hash diffs { {k: len(v) for k, v in diffs.items()} }")
    for k, v in diffs.items(): print("  ", k, v[:3])
for results, conf in TOOLS:
    src = Path.home() / ("repos/tools-run" if conf else "repos/tools-run-1c")
    conf = conf or ("lane-a.yaml" if results.endswith("-v2") else "tp-lane-a.yaml")
    cfg = load_config(str(src / conf))
    n, _, _, diffs = check(results, cfg, ("pankti", "rick", "microtestcarver"), "all")
    print(f"{results}: tool cells {n}, hash diffs { {k: len(v) for k, v in diffs.items()} }")
    for k, v in diffs.items(): print("  ", k, v[:3])
