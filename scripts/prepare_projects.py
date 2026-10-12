"""Clone the subject projects at their commits and build them, so that case commands run offline.

For every project of projects.yaml: clone into <projects>/<repository name> (or reuse an existing
checkout of the same commit), then run, online, the distinct `build` and `scenario` commands of the
project's cases (Maven fills the local repository with everything the offline commands need).

    python3 scripts/prepare_projects.py --config config.yaml [--projects jsoup ...] [--skip-build]
"""
from __future__ import annotations

import argparse
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from runner.cases import case_rows, load_case, load_projects  # noqa: E402
from runner.config import load_config  # noqa: E402
from runner.process import resolve_argv, run  # noqa: E402

ONLINE_DROP = ("-o", "--offline", "-q")


def checkout(url: str, commit: str, dest: Path) -> None:
    if (dest / ".git").exists():
        head = subprocess.run(["git", "-C", str(dest), "rev-parse", "HEAD"], capture_output=True, text=True).stdout.strip()
        if head == commit:
            return
        subprocess.run(["git", "-C", str(dest), "fetch", "--quiet", "origin", commit], check=False)
    else:
        dest.parent.mkdir(parents=True, exist_ok=True)
        subprocess.run(["git", "clone", "--quiet", url, str(dest)], check=True)
    subprocess.run(["git", "-C", str(dest), "checkout", "--quiet", "--detach", commit], check=True)


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--config", default="config.yaml")
    ap.add_argument("--projects", nargs="*")
    ap.add_argument("--skip-build", action="store_true")
    a = ap.parse_args(argv)
    cfg = load_config(a.config)
    env = cfg.command_env()
    failed = 0
    for name, project in load_projects().items():
        if a.projects and name not in a.projects:
            continue
        dest = cfg.projects_dir / project.checkout_name
        checkout(project.url, project.commit, dest)
        print(f"{name}: {dest} at {project.commit[:12]}", flush=True)
        if a.skip_build:
            continue
        seen: set[tuple[str, ...]] = set()
        for row in case_rows(cfg.cases_dir):
            if row["project"] != name:
                continue
            case = load_case(row["id"], cfg.cases_dir)
            for kind in ("build", "scenario"):
                argv_ = [x for x in case.commands[kind] if x not in ONLINE_DROP]
                key = tuple(x for x in argv_ if not (kind == "scenario" and x.startswith("-Dtest=")))
                if key in seen:
                    continue
                seen.add(key)
                started = time.time()
                r = run(resolve_argv(argv_, dest), dest, env, 7200, kind)
                print(f"  {row['id']} {kind}: exit {r.exit_code} in {time.time() - started:.0f}s", flush=True)
                if not r.ok:
                    failed += 1
                    print("\n".join((r.stdout + r.stderr).splitlines()[-25:]), flush=True)
    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
