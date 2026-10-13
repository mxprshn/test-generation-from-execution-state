"""Hashes of everything a result depends on, so that stale results are never reused.

Every generation cell records the inputs of generation (summary.json: inputs), every evaluation step
the inputs of that step (eval.json: inputs.<step>). Reusing a stored result whose inputs differ from
the current ones is refused with a message; --force recomputes it.

Inputs:
  code         source files the result is computed by: generation hashes runner/ and pi/; every
               evaluation step hashes evaluate.py, the shared runner and trace modules and the modules
               of that step (STEP_CODE), so a change in one step does not invalidate the others
  case         case.yaml of the case
  mode_input   the execution input of the mode (call dump, preliminary test, workspace-files/)
  prompts      the prompt templates
  config       the fields of config.yaml that change results (model, limits, depth, ...)
  project      the project entry of projects.yaml (commit, evaluation arguments)
  normalisation nondeterminism.json, components.json, test-classes.json (RQ1 only)
  reference    the reference trace of the case (RQ1 only)
  result       result.diff of the cell (evaluation steps)
  verify       the verification result the step relies on (RQ1, RQ2)
"""
from __future__ import annotations

import hashlib
import json
from pathlib import Path
from typing import Any

from runner.config import ROOT, Config

GENERATION_CODE = ("runner", "pi")
# evaluation code shared by every step: the record, the workspaces, the case commands
EVAL_COMMON = ("evaluation/evaluate.py", "evaluation/trace.py", "runner/cases.py", "runner/config.py",
               "runner/process.py")
STEP_CODE = {
    "verify": ("evaluation/surefire.py",),
    "rq1": ("evaluation/compare.py", "evaluation/reference.py", "evaluation/tracer"),
    "rq2": ("evaluation/mutation.py",),
    "quality": ("evaluation/quality.py",),
}
SKIP_SUFFIXES = (".pyc", ".json")  # evaluation data files are hashed separately (normalisation)
GENERATION_CONFIG = ("provider_name", "base_url", "model", "thinking", "timeout", "command_timeout")
EVAL_CONFIG = ("command_timeout", "trace_depth", "reference_runs")


def _sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()[:16]


def files_hash(paths: list[Path], base: Path = ROOT) -> str:
    h = hashlib.sha256()
    for p in sorted(paths):
        if p.is_file():
            h.update(str(p.relative_to(base) if p.is_relative_to(base) else p.name).encode())
            h.update(b"\0")
            h.update(p.read_bytes())
            h.update(b"\0")
    return h.hexdigest()[:16]


def tree_hash(root: Path | None) -> str | None:
    if root is None or not root.exists():
        return None
    if root.is_file():
        return _sha(root.read_bytes())
    return files_hash([p for p in root.rglob("*") if p.is_file()], root)


_CODE: dict[tuple[str, ...], str] = {}


def code_hash(paths: tuple[str, ...]) -> str:
    """Hash of the source files under the given repository paths (files or directories)."""
    if paths not in _CODE:
        files = []
        for rel in paths:
            p = ROOT / rel
            found = [p] if p.is_file() else [f for f in p.rglob("*") if f.is_file()]
            files += [f for f in found if "__pycache__" not in f.parts and not f.name.endswith(SKIP_SUFFIXES)]
        _CODE[paths] = files_hash(files)
    return _CODE[paths]


def generation_code_hash() -> str:
    return code_hash(GENERATION_CODE)


def step_code_hash(step: str) -> str:
    return code_hash(EVAL_COMMON + STEP_CODE[step])


def config_hash(cfg: Config, fields: tuple[str, ...]) -> str:
    return _sha(json.dumps({f: str(getattr(cfg, f)) for f in fields}, sort_keys=True).encode())


def project_hash(project: str) -> str:
    from runner.cases import load_projects
    p = load_projects()[project]
    return _sha(json.dumps([p.url, p.commit, p.jdk, list(p.eval_maven_args)]).encode())


def normalisation_hash() -> str:
    d = ROOT / "evaluation"
    return files_hash([d / "nondeterminism.json", d / "components.json", d / "test-classes.json"])


def mode_input_hash(case, mode: str) -> str | None:
    if mode == "baseline":
        return None
    src = case.dump if mode == "dump" else case.preliminary_test
    parts = [tree_hash(src), tree_hash(case.workspace_files)]
    return _sha(json.dumps(parts).encode())


def generation_inputs(cfg: Config, case, mode: str) -> dict[str, Any]:
    return {
        "code": generation_code_hash(),
        "case": tree_hash(case.dir / "case.yaml"),
        "mode_input": mode_input_hash(case, mode),
        "prompts": tree_hash(cfg.prompts_dir) if mode != "rendered-raw" else None,
        "config": config_hash(cfg, GENERATION_CONFIG) if mode != "rendered-raw" else None,
        "project": project_hash(case.project),
    }


def step_inputs(cfg: Config, case, cell: Path, step: str) -> dict[str, Any]:
    out: dict[str, Any] = {
        "code": step_code_hash(step),
        "case": tree_hash(case.dir / "case.yaml"),
        "project": project_hash(case.project),
        "config": config_hash(cfg, EVAL_CONFIG),
        "result": tree_hash(cell / "result.diff"),
    }
    if step == "rq1":
        out["normalisation"] = normalisation_hash()
        out["reference"] = tree_hash(cfg.references_dir / case.id / "run1.json")
    if step in ("rq1", "rq2"):
        rec = cell / "eval.json"
        verify = (json.loads(rec.read_text()).get("verify") if rec.is_file() else None) or {}
        out["verify"] = _sha(json.dumps(verify.get("passes")).encode())
    if step == "quality" and case.preliminary_test is not None:
        out["mode_input"] = tree_hash(case.preliminary_test)
    return out
