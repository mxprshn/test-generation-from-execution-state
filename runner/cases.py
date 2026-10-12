"""Case manifests (cases/<id>/case.yaml) and the subject projects (projects.yaml)."""
from __future__ import annotations

import csv
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import yaml

from runner.config import ROOT, Config


class CaseError(Exception):
    pass


@dataclass
class Project:
    name: str
    url: str
    commit: str
    jdk: int
    license: str

    @property
    def checkout_name(self) -> str:
        """Directory name of the checkout: the repository name from the URL (e.g. mybatis-3)."""
        return self.url.rstrip("/").rsplit("/", 1)[-1].removesuffix(".git")


@dataclass
class Case:
    id: str
    project: str
    group: str
    target: dict[str, str]
    scenario_test: str
    scenario_source: str
    output_test_file: str
    hidden_paths: list[str]
    commands: dict[str, list[str]]
    dir: Path

    @property
    def dump(self) -> Path | None:
        p = self.dir / "call-dumps.json"
        return p if p.is_file() else None

    @property
    def preliminary_test(self) -> Path | None:
        p = self.dir / "preliminary-test.java"
        return p if p.is_file() else None

    @property
    def workspace_files(self) -> Path | None:
        p = self.dir / "workspace-files"
        return p if p.is_dir() else None


def _relative(value: Any, name: str, source: Path) -> str:
    p = Path(str(value or ""))
    if not str(value or "") or p.is_absolute() or ".." in p.parts:
        raise CaseError(f"{source}: {name} must be a relative path inside the project, got {value!r}")
    return p.as_posix()


def load_case(case_id: str, cases_dir: Path = ROOT / "cases") -> Case:
    source = cases_dir / case_id / "case.yaml"
    if not source.is_file():
        raise CaseError(f"case not found: {source}")
    data = yaml.safe_load(source.read_text(encoding="utf-8"))
    commands = data.get("commands") or {}
    for name in ("build", "scenario", "generated_test"):
        argv = commands.get(name)
        if not isinstance(argv, list) or not argv or not all(isinstance(a, str) for a in argv):
            raise CaseError(f"{source}: command {name} must be a non-empty argv list")
    return Case(
        id=data["id"],
        project=data["project"],
        group=data["group"],
        target={k: str(data["target"][k]) for k in ("class", "method", "signature")},
        scenario_test=str(data["scenario"]["test"]),
        scenario_source=_relative(data["scenario"]["source"], "scenario.source", source),
        output_test_file=_relative(data["output_test_file"], "output_test_file", source),
        hidden_paths=[_relative(p, "hidden_paths", source) for p in data.get("hidden_paths") or []],
        commands={name: list(argv) for name, argv in commands.items()},
        dir=source.parent,
    )


def case_rows(cases_dir: Path = ROOT / "cases") -> list[dict[str, str]]:
    with (cases_dir / "cases.csv").open(encoding="utf-8") as handle:
        return list(csv.DictReader(handle))


def case_ids(cfg: Config, only: list[str] | None = None) -> list[str]:
    ids = [row["id"] for row in case_rows(cfg.cases_dir)]
    wanted = only or cfg.cases
    if wanted:
        unknown = sorted(set(wanted) - set(ids))
        if unknown:
            raise CaseError(f"unknown cases: {', '.join(unknown)}")
        ids = [c for c in ids if c in wanted]
    return ids


def load_projects(path: Path = ROOT / "projects.yaml") -> dict[str, Project]:
    data = yaml.safe_load(path.read_text(encoding="utf-8"))
    return {name: Project(name=name, url=v["url"], commit=v["commit"], jdk=int(v["jdk"]), license=v["license"])
            for name, v in data.items()}


def project_checkout(cfg: Config, project: str) -> Path:
    return cfg.projects_dir / load_projects()[project].checkout_name
