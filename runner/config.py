"""Experiment configuration (config.yaml) and the environment of build and test commands."""
from __future__ import annotations

import os
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

import yaml

ROOT = Path(__file__).resolve().parents[1]
AGENT_MODES = ("baseline", "dump", "rendered")
NO_AGENT_MODES = ("rendered-raw",)
MODES = AGENT_MODES + NO_AGENT_MODES
AGENT_COMMANDS = ("build", "scenario", "generated_test")
EVAL_STEPS = ("verify", "rq1", "rq2", "quality")


class ConfigError(Exception):
    pass


def _path(value: Any, default: str | None = None) -> Path | None:
    value = value if value not in (None, "") else default
    if value in (None, ""):
        return None
    p = Path(os.path.expandvars(str(value))).expanduser()
    return p if p.is_absolute() else (ROOT / p).resolve()


@dataclass
class Config:
    path: Path
    raw: dict[str, Any]
    provider_name: str
    base_url: str
    api_key_env: str
    fallback_api_key_env: str | None
    provider_models: list[Any]
    model: str
    thinking: str | None
    timeout: int
    command_timeout: int
    modes: list[str]
    once_modes: list[str]
    rounds: list[int]
    run_id_format: str
    cases: list[str]
    parallel_cases: int
    min_free_disk_gb: float
    min_free_mem_gb: float
    projects_dir: Path
    results_dir: Path
    tools_dir: Path
    java_home: Path
    maven_repository: Path
    eval_steps: list[str]
    reference_runs: int
    trace_depth: int
    extra_env: dict[str, str] = field(default_factory=dict)

    @property
    def cases_dir(self) -> Path:
        return ROOT / "cases"

    @property
    def prompts_dir(self) -> Path:
        return ROOT / "prompts"

    @property
    def references_dir(self) -> Path:
        return self.results_dir / "references"

    @property
    def cells_dir(self) -> Path:
        return self.results_dir / "cells"

    @property
    def work_dir(self) -> Path:
        return self.results_dir / "work"

    def run_id(self, round_number: int) -> str:
        return self.run_id_format.format(round=round_number)

    def cell_dir(self, case_id: str, mode: str, run_id: str) -> Path:
        return self.cells_dir / case_id / mode / run_id

    def command_env(self) -> dict[str, str]:
        """Environment of build/test commands: JDK first on PATH, plus `env` from the config."""
        env = dict(os.environ)
        env.update(self.extra_env)
        env["JAVA_HOME"] = str(self.java_home)
        env["PATH"] = f"{self.java_home / 'bin'}{os.pathsep}{env.get('PATH', '')}"
        return env


def configure_tools(cfg: "Config") -> None:
    """Point the evaluation modules at the tools directory and the Maven repository of the config."""
    from evaluation import mutation, trace
    trace.configure(cfg.tools_dir)
    mutation.M2 = cfg.maven_repository


def load_config(path: str | Path) -> Config:
    path = Path(path).expanduser().resolve()
    if not path.is_file():
        raise ConfigError(f"config not found: {path}")
    raw = yaml.safe_load(path.read_text(encoding="utf-8")) or {}
    provider = raw.get("provider") or {}
    paths = raw.get("paths") or {}
    evaluation = raw.get("evaluation") or {}
    java_home = _path(paths.get("java_home")) or (Path(os.environ["JAVA_HOME"]) if os.environ.get("JAVA_HOME") else None)
    if java_home is None:
        raise ConfigError("set paths.java_home in the config or JAVA_HOME in the environment (JDK 21)")
    modes = list(raw.get("modes") or AGENT_MODES)
    once_modes = list(raw.get("once_modes") or [])
    unknown = sorted(set(modes + once_modes) - set(MODES))
    if unknown:
        raise ConfigError(f"unknown modes: {', '.join(unknown)}")
    steps = list(evaluation.get("steps") or EVAL_STEPS)
    if set(steps) - set(EVAL_STEPS):
        raise ConfigError(f"unknown evaluation steps: {', '.join(sorted(set(steps) - set(EVAL_STEPS)))}")
    for key in ("base_url", "api_key_env"):
        if not provider.get(key):
            raise ConfigError(f"provider.{key} is required")
    if not raw.get("model"):
        raise ConfigError("model is required")
    cfg = Config(
        path=path,
        raw=raw,
        provider_name=str(provider.get("name") or "custom"),
        base_url=str(provider["base_url"]).rstrip("/"),
        api_key_env=str(provider["api_key_env"]),
        fallback_api_key_env=provider.get("fallback_api_key_env") or None,
        provider_models=list(provider.get("models") or []),
        model=str(raw["model"]),
        thinking=raw.get("thinking"),
        timeout=int(raw.get("timeout", 600)),
        command_timeout=int(raw.get("command_timeout", 900)),
        modes=modes,
        once_modes=once_modes,
        rounds=[int(r) for r in (raw.get("rounds") or [1])],
        run_id_format=str(raw.get("run_id_format") or "r{round}"),
        cases=list(raw.get("cases") or []),
        parallel_cases=max(1, int(raw.get("parallel_cases", 1))),
        min_free_disk_gb=float(raw.get("min_free_disk_gb", 15)),
        min_free_mem_gb=float(raw.get("min_free_mem_gb", 0)),
        projects_dir=_path(paths.get("projects"), "../projects"),
        results_dir=_path(paths.get("results"), "../results/v2"),
        tools_dir=_path(paths.get("tools"), ".tools"),
        java_home=java_home,
        maven_repository=_path(paths.get("maven_repository"), "~/.m2/repository"),
        eval_steps=steps,
        reference_runs=int(evaluation.get("reference_runs", 2)),
        trace_depth=int(evaluation.get("trace_depth", 5)),
        extra_env={str(k): os.path.expandvars(str(v)) for k, v in (raw.get("env") or {}).items()},
    )
    configure_tools(cfg)
    return cfg
