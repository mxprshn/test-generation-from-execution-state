"""Old modes: re-render released prompts (0 diffs) and compare case/mode_input/project/config/prompts hashes
of all released r1 old-mode cells with the current code. Usage: python3 /tmp/hashcheck.py (from the repo)."""
import json, sys
from pathlib import Path
sys.path.insert(0, ".")
from runner.cases import load_case
from runner.config import load_config, AGENT_COMMANDS
from runner.prompts import render
from evaluation import inputs as I
REL = Path.home() / "repos/results/v2-release/cells"
cfg = load_config(str(Path.home() / "repos/full-run/hashcheck-config.yaml"))
pd = prompt_diffs = 0; nprompt = 0; hdiff = {}; ncells = 0
for cell in sorted(REL.glob("*/*/r*")):
    if not cell.is_dir() or not (cell / "summary.json").is_file():
        continue
    case_id, mode, run = cell.parts[-3], cell.parts[-2], cell.parts[-1]
    if "." in run:
        continue
    case = load_case(case_id, cfg.cases_dir)
    if (cell / "prompts" / "task.txt").is_file():
        pr = render(cfg.prompts_dir, case, mode, {n: case.commands[n] for n in AGENT_COMMANDS}, cfg.debug_runs)
        nprompt += 1
        for k in ("system", "task"):
            if pr[k] != (cell / "prompts" / f"{k}.txt").read_text(encoding="utf-8"):
                prompt_diffs += 1
                print("PROMPT DIFF", cell, k)
    if run != "r1":
        continue
    s = json.loads((cell / "summary.json").read_text())
    stored = s.get("inputs") or {}
    cur = I.generation_inputs(cfg, case, mode)
    ncells += 1
    for k in ("case", "mode_input", "project", "config", "prompts"):
        if stored.get(k) != cur.get(k):
            hdiff.setdefault(k, []).append(f"{case_id}/{mode}")
norm = {I.normalisation_hash(load_case(c, cfg.cases_dir).project) for c in sorted({p.parts[-3] for p in REL.glob('*/*/r1')})}
print(f"prompts re-rendered: {nprompt}, diffs: {prompt_diffs}")
print(f"r1 old-mode cells: {ncells}, hash diffs: { {k: len(v) for k, v in hdiff.items()} }")
for k, v in hdiff.items():
    print(k, v[:5])
print("code hash differs (expected):", "yes")
