"""Run the experiment case by case: reference traces, generation cells, evaluation of every cell.

For every case of the config:
  1. reference traces of the driving scenario (skipped when present);
  2. generation cells, modes x rounds (+ once_modes in the first round), one after another; a cell
     that failed for an infrastructure reason (model gateway error, workspace preparation) is moved
     aside and rerun once, on the fallback API key if the failure was a gateway error;
  3. evaluation of every cell right after it is generated (verify, rq1, rq2, quality).

Everything is resumable: an existing cell is not regenerated and a filled evaluation step is not
recomputed. Several cases run at the same time (parallel_cases); the steps of a case run in order.

    python3 scripts/run.py --config config.yaml [--cases ID ...] [--dry-run]
    python3 scripts/run.py --config config.yaml --status
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import shutil
import subprocess
import sys
import threading
import time
import traceback
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from evaluation import evaluate as E  # noqa: E402
from evaluation import reference as R  # noqa: E402
from runner import agent as A  # noqa: E402
from runner import cell as G  # noqa: E402
from runner.cases import case_ids  # noqa: E402
from runner.config import Config, load_config  # noqa: E402

HASHED = ("runner", "evaluation", "pi", "prompts", "scripts")


def harness_revision() -> dict[str, Any]:
    def git(*args: str) -> str:
        r = subprocess.run(["git", *args], cwd=ROOT, capture_output=True, text=True)
        return r.stdout.strip() if r.returncode == 0 else ""
    diff = git("diff", "HEAD", "--", *HASHED)
    h = hashlib.sha256()
    for sub in HASHED:
        for f in sorted((ROOT / sub).rglob("*")):
            if f.is_file() and "__pycache__" not in f.parts:
                h.update(str(f.relative_to(ROOT)).encode())
                h.update(f.read_bytes())
    return {"commit": git("rev-parse", "HEAD") or None, "dirty": bool(diff), "sources_sha256": h.hexdigest()}


def cells_of(cfg: Config, case_id: str) -> list[tuple[str, str]]:
    """(mode, run id) of a case, in execution order."""
    out = []
    for rnd in cfg.rounds:
        out += [(mode, cfg.run_id(rnd)) for mode in cfg.modes]
        if rnd == cfg.rounds[0]:
            out += [(mode, cfg.run_id(rnd)) for mode in cfg.once_modes]
    return out


def _mem_available_gb() -> float | None:
    try:
        for line in Path("/proc/meminfo").read_text().splitlines():
            if line.startswith("MemAvailable:"):
                return round(int(line.split()[1]) / 1e6, 1)
    except OSError:
        pass
    return None


class Runner:
    def __init__(self, cfg: Config, dry_run: bool = False):
        self.cfg = cfg
        self.dry_run = dry_run
        self.lock = threading.Lock()
        for d in (cfg.cells_dir, cfg.references_dir, cfg.work_dir, cfg.results_dir / "status", cfg.results_dir / "logs"):
            d.mkdir(parents=True, exist_ok=True)
        self.meta = {"harness": harness_revision(), "pi_version": A.pi_version(cfg), "model": cfg.model,
                     "thinking": cfg.thinking, "provider": cfg.provider_name, "timeout": cfg.timeout}

    def log(self, case_id: str, msg: str) -> None:
        line = f"{G.now()} {msg}"
        with self.lock:
            with (self.cfg.results_dir / "logs" / f"{case_id}.log").open("a", encoding="utf-8") as h:
                h.write(line + "\n")
            print(f"[{case_id}] {msg}", flush=True)

    def status(self, case_id: str, **fields: Any) -> None:
        p = self.cfg.results_dir / "status" / f"{case_id}.json"
        with self.lock:
            cur = json.loads(p.read_text()) if p.is_file() else {"case_id": case_id}
            cur.update(fields, updated=G.now())
            p.write_text(json.dumps(cur, indent=1))

    def wait_resources(self, case_id: str) -> None:
        while True:
            disk = shutil.disk_usage(self.cfg.results_dir).free / 1e9
            if disk < self.cfg.min_free_disk_gb:
                raise SystemExit(f"free disk {disk:.1f} GB is below min_free_disk_gb {self.cfg.min_free_disk_gb}")
            mem = _mem_available_gb()
            if mem is None or mem >= self.cfg.min_free_mem_gb:
                return
            self.status(case_id, stage=f"waiting for memory ({mem} GB available)")
            time.sleep(30)

    def generate(self, case_id: str, mode: str, run_id: str) -> dict[str, Any] | None:
        cell = self.cfg.cell_dir(case_id, mode, run_id)
        key = self.cfg.api_key_env
        for attempt in range(2):
            summary_file = cell / "summary.json"
            if summary_file.is_file():
                s = json.loads(summary_file.read_text())
                if not G.is_infra_failure(s, cell) or attempt == 1:
                    return s
                aside = cell.with_name(f"{run_id}.infra-{int(time.time())}")
                shutil.move(str(cell), str(aside))
                self.log(case_id, f"{mode}/{run_id}: infrastructure failure, retry: {str(s.get('error'))[:160]}")
                fb = self.cfg.fallback_api_key_env
                if s.get("gateway_errors") and fb and os.environ.get(fb):
                    key = fb
                    self.log(case_id, f"{mode}/{run_id}: retry on the fallback key {fb}")
            elif cell.exists():
                shutil.move(str(cell), str(cell.with_name(f"{run_id}.incomplete-{int(time.time())}")))
            self.status(case_id, stage=f"generate {mode}/{run_id}")
            self.log(case_id, f"generate {mode}/{run_id}")
            if self.dry_run:
                return None
            s = G.generate(self.cfg, case_id, mode, run_id, key)
            s["meta"] = self.meta
            summary_file.write_text(json.dumps(s, indent=2, default=str))
            self.log(case_id, f"{mode}/{run_id}: {s['status']} {s.get('seconds')}s {s.get('error') or ''}")
        return json.loads(summary_file.read_text())

    def process(self, case_id: str) -> None:
        cells = cells_of(self.cfg, case_id)
        self.status(case_id, stage="start", total=len(cells), generated=0, evaluated=0, errors=0)
        errors = 0
        try:
            self.wait_resources(case_id)
            if not R.reference_exists(self.cfg, case_id):
                self.status(case_id, stage="reference")
                self.log(case_id, "reference")
                if not self.dry_run:
                    au = R.record(self.cfg, case_id)
                    self.log(case_id, f"reference: {au['runs'][0]['invocations']} invocations, "
                                      f"{au['runs'][0]['scenarios']} scenarios")
            generated = evaluated = 0
            outcomes: dict[str, str] = {}
            for mode, run_id in cells:
                self.wait_resources(case_id)
                s = self.generate(case_id, mode, run_id)
                if s is None:
                    continue
                cell = self.cfg.cell_dir(case_id, mode, run_id)
                if G.is_infra_failure(s, cell):
                    errors += 1
                    outcomes[f"{mode}/{run_id}"] = "infra_failed"
                    self.status(case_id, cells=outcomes, errors=errors)
                    continue
                generated += 1
                outcomes[f"{mode}/{run_id}"] = s["status"]
                self.status(case_id, stage=f"evaluate {mode}/{run_id}", generated=generated, cells=outcomes)
                try:
                    rec = E.evaluate_cell(self.cfg, cell)
                    evaluated += 1
                    if isinstance(rec.get("rq2"), dict) and "error" in rec["rq2"]:
                        errors += 1
                        self.log(case_id, f"{mode}/{run_id}: rq2 error: {rec['rq2']['error'][:160]}")
                    v = rec.get("verify") or {}
                    q = rec.get("rq1") or {}
                    self.log(case_id, f"{mode}/{run_id}: verify {v.get('compiles')}/{v.get('passes')} "
                                      f"S/A/B/C {q.get('recall_structure')}/{q.get('recall_io')}/"
                                      f"{q.get('recall_state')}/{q.get('recall_strict')}")
                except Exception as exc:
                    errors += 1
                    self.log(case_id, f"{mode}/{run_id}: evaluation error: {type(exc).__name__}: {exc}")
                self.status(case_id, evaluated=evaluated, errors=errors)
            self.status(case_id, stage="done", errors=errors)
        except Exception as exc:  # keep the other cases going
            self.log(case_id, f"error: {type(exc).__name__}: {exc}\n{traceback.format_exc()}")
            self.status(case_id, stage=f"error: {type(exc).__name__}: {str(exc)[:120]}", errors=errors + 1)

    def run(self, cases: list[str]) -> None:
        stamp = time.strftime("%Y%m%dT%H%M%S")
        shutil.copy2(self.cfg.path, self.cfg.results_dir / f"config-{stamp}.yaml")
        (self.cfg.results_dir / f"start-{stamp}.json").write_text(
            json.dumps({"cases": cases, **self.meta, "started": G.now()}, indent=1))
        with ThreadPoolExecutor(max_workers=self.cfg.parallel_cases) as pool:
            list(pool.map(self.process, cases))


def print_status(cfg: Config, cases: list[str]) -> None:
    total = {"cells": 0, "generated": 0, "evaluated": 0, "errors": 0}
    for cid in cases:
        p = cfg.results_dir / "status" / f"{cid}.json"
        n = len(cells_of(cfg, cid))
        total["cells"] += n
        if not p.is_file():
            print(f"{cid:62} pending")
            continue
        s = json.loads(p.read_text())
        for k in ("generated", "evaluated", "errors"):
            total[k] += int(s.get(k) or 0)
        print(f"{cid:62} {str(s.get('stage')):30.30} gen {s.get('generated', 0)}/{n} "
              f"eval {s.get('evaluated', 0)} err {s.get('errors', 0)}")
    disk = shutil.disk_usage(cfg.results_dir).free / 1e9 if cfg.results_dir.exists() else float("nan")
    print(f"TOTAL gen {total['generated']}/{total['cells']} eval {total['evaluated']} err {total['errors']} | "
          f"disk {disk:.0f} GB free" + (f", mem {_mem_available_gb()} GB" if _mem_available_gb() else ""))


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--config", default="config.yaml")
    ap.add_argument("--cases", nargs="*")
    ap.add_argument("--status", action="store_true", help="print the progress and exit")
    ap.add_argument("--dry-run", action="store_true", help="print the plan without running anything")
    a = ap.parse_args(argv)
    cfg = load_config(a.config)
    cases = case_ids(cfg, a.cases)
    if a.status:
        print_status(cfg, cases)
        return 0
    if a.dry_run:
        n = sum(len(cells_of(cfg, c)) for c in cases)
        print(f"{len(cases)} cases, {n} cells, rounds {cfg.rounds}, modes {cfg.modes} + {cfg.once_modes} once; "
              f"results in {cfg.results_dir}")
    Runner(cfg, a.dry_run).run(cases)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
