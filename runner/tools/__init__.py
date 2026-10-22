"""External test generators as modes without an agent: pankti, rick, microtestcarver (docs/tools.md).

A tool cell is prepared like any other cell (exported commit, hidden tests removed, scenario check); then
the tool records the driving scenario with its own instrumentation and generates a test class, which is
installed as the case's output test file together with the files it loads (object files) and the test
dependencies it needs (module pom). result.diff holds all of them and the evaluation is the same as for
the other modes. A tool that produces no test is a result of the tool (status failed, reason in the
summary), scored like any cell without a passing test.
"""
from __future__ import annotations

import json
import shutil
import time
from pathlib import Path
from typing import Any

from runner.cases import Case, eval_command
from runner.config import Config
from runner.tools.common import ToolContext, ToolError

TOOL_MODES = ("pankti", "rick", "microtestcarver")


class ToolSetupError(Exception):
    """The external tools are not installed (an infrastructure failure, not a result of the tool)."""


def versions(cfg: Config) -> dict[str, Any] | None:
    f = cfg.external_tools_dir / "versions.json"
    return json.loads(f.read_text()) if f.is_file() else None


KEEP = ("tool.log", "record.log", "record-*.log", "target-method.csv", "extract/*.csv",
        "*-generate/output/generated/**/*PanktiGen.java", "generate/test-output/*.java",
        "object-data/*-count.txt", "object-data/invoked-methods.csv", "trace.btrace")


def _keep_artifacts(work: Path, dest: Path) -> None:
    for pattern in KEEP:
        for f in sorted(work.glob(pattern)):
            if f.is_file():
                target = dest / f.relative_to(work)
                target.parent.mkdir(parents=True, exist_ok=True)
                data = f.read_bytes()
                target.write_bytes(data[-200000:] if f.suffix in (".log", ".btrace") else data)


def run_tool(cfg: Config, case: Case, mode: str, ws: Path, work: Path, env: dict[str, str],
             artifacts: Path) -> dict[str, Any]:
    """Run the tool of the mode on the prepared workspace. Returns {generated, support_files, meta}."""
    if versions(cfg) is None:
        raise ToolSetupError(f"external tools are not installed in {cfg.external_tools_dir} "
                             "(scripts/setup_external_tools.sh)")
    work.mkdir(parents=True, exist_ok=True)
    ctx = ToolContext(cfg=cfg, case=case, workspace=ws, work=work, env=env,
                      scenario=eval_command(case, "scenario"), meta={"tool": mode})
    started = time.time()
    placed: dict[str, Any] = {"generated": False, "support_files": []}
    try:
        if mode == "microtestcarver":
            from runner.tools import mtc
            placed = mtc.run(ctx, cfg.tool_timeout)
        else:
            from runner.tools import pankti
            placed = pankti.run(ctx, mode, cfg.tool_timeout)
    except ToolError as exc:
        ctx.meta["outcome"] = f"tool error: {exc}"
    finally:
        ctx.meta["seconds"] = round(time.time() - started, 1)
        _keep_artifacts(work, artifacts / "tool")
        shutil.rmtree(ws / "pool", ignore_errors=True)
    ctx.meta.update({k: v for k, v in placed.items() if k not in ("generated", "support_files")})
    return {"generated": placed["generated"], "support_files": placed["support_files"], "meta": ctx.meta}
