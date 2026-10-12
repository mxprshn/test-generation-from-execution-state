"""Hashes of everything a result depends on, so that stale results are never reused.

Every generation cell records the inputs of generation (summary.json: inputs), every evaluation step
the inputs of that step (eval.json: inputs.<step>). Reusing a stored result whose inputs differ from
the current ones is refused with a message; --force recomputes it.

Inputs:
  harness      source of the runner, the Pi extensions, the evaluation code, the tracer (not tests/)
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

HARNESS_DIRS = ("runner", "pi", "evaluation")
HARNESS_SKIP_SUFFIXES = (".pyc", ".json")  # evaluation data files are hashed separately
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


_HARNESS: str | None = None


def harness_hash() -> str:
    global _HARNESS
    if _HARNESS is None:
        files = [p for d in HARNESS_DIRS for p in (ROOT / d).rglob("*")
                 if p.is_file() and "__pycache__" not in p.parts and not p.name.endswith(HARNESS_SKIP_SUFFIXES)]
        _HARNESS = files_hash(files)
    return _HARNESS


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
        "harness": harness_hash(),
        "case": tree_hash(case.dir / "case.yaml"),
        "mode_input": mode_input_hash(case, mode),
        "prompts": tree_hash(cfg.prompts_dir) if mode != "rendered-raw" else None,
        "config": config_hash(cfg, GENERATION_CONFIG) if mode != "rendered-raw" else None,
        "project": project_hash(case.project),
    }


def step_inputs(cfg: Config, case, cell: Path, step: str) -> dict[str, Any]:
    out: dict[str, Any] = {
        "harness": harness_hash(),
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
