"""Prompt templates (prompts/) rendered for one case and mode."""
from __future__ import annotations

import json
import re
from pathlib import Path
from typing import Any

from runner.cases import Case, load_projects

PLACEHOLDER = re.compile(r"\{\{\s*([a-zA-Z_][a-zA-Z0-9_]*)\s*\}\}")
# Where the execution input of the dump/rendered modes is placed in the agent workspace.
# The dump is split into one file per recorded execution (carving/executions/0001.json, ...);
# the preliminary test gets a .txt suffix so that the build does not compile it.
EXECUTION_INPUT = {"dump": "carving/executions/", "rendered": "carving/PreliminaryTest.java.txt"}


class PromptError(Exception):
    pass


def render_template(text: str, context: dict[str, Any], name: str) -> str:
    missing: list[str] = []

    def replace(match: re.Match[str]) -> str:
        key = match.group(1)
        if key not in context:
            missing.append(key)
            return match.group(0)
        return str(context[key])

    out = PLACEHOLDER.sub(replace, text)
    if missing:
        raise PromptError(f"{name}: unknown placeholder(s): {', '.join(sorted(set(missing)))}")
    return out


def context(case: Case, commands: dict[str, list[str]], mode: str) -> dict[str, Any]:
    """Placeholders of the templates. The scenario is given the same way for every case and mode:
    the id of the scenario test and the path of its source file."""
    project = load_projects()[case.project]
    return {
        "case_id": case.id,
        "repository": project.checkout_name,
        "commit": project.commit,
        "language": "java",
        "jdk": project.jdk,
        "working_directory": ".",
        "target_class": case.target["class"],
        "target_method": case.target["method"],
        "target_signature": case.target["signature"],
        "scenario_description": f"test {case.scenario_test}",
        "scenario_source_paths": case.scenario_source,
        "output_test_file": case.output_test_file,
        "commands": json.dumps(commands, indent=2),
        "command_names": ", ".join(sorted(commands)),
        "execution_input": EXECUTION_INPUT.get(mode, ""),
    }


def render(prompts_dir: Path, case: Case, mode: str, commands: dict[str, list[str]]) -> dict[str, str]:
    ctx = context(case, commands, mode)
    files = ("system.txt", "task-common.txt", f"task-{mode}.txt")
    text = {}
    for name in files:
        path = prompts_dir / name
        if not path.is_file():
            raise PromptError(f"prompt file not found: {path}")
        text[name] = render_template(path.read_text(encoding="utf-8"), ctx, name)
    return {"system": text["system.txt"], "task": f"{text['task-common.txt']}\n{text[f'task-{mode}.txt']}"}
