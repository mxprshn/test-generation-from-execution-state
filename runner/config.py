"""Experiment configuration (config.yaml) and the environment of build and test commands."""
from __future__ import annotations

import atexit
import hashlib
import os
import shutil
import tempfile
import threading
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

import yaml

ROOT = Path(__file__).resolve().parents[1]
AGENT_MODES = ("baseline", "dump", "rendered", "autonomous")
NO_AGENT_MODES = ("rendered-raw",)
MODES = AGENT_MODES + NO_AGENT_MODES
AGENT_COMMANDS = ("build", "scenario", "generated_test")
EVAL_STEPS = ("verify", "rq1", "rq2", "quality")

# Environment of build and test commands: these variables of the runner's environment are passed
# through, nothing else (no API keys, no credentials). config.yaml `env` adds or overrides variables.
ENV_ALLOWLIST = ("PATH", "HOME", "USER", "LOGNAME", "SHELL", "TERM", "TZ", "LANG", "LANGUAGE", "TMPDIR",
                 "MAVEN_OPTS", "MAVEN_ARGS", "MAVEN_HOME", "M2_HOME", "MAVEN_USER_HOME", "GRADLE_USER_HOME",
                 "GRADLE_OPTS")
ENV_ALLOWLIST_PREFIXES = ("LC_",)


class ConfigError(Exception):
    pass


def neutral_name(*parts: str) -> str:
    """Directory name that does not reveal the case, mode or run (w + 12 hex digits)."""
    return "w" + hashlib.sha256("\0".join(parts).encode()).hexdigest()[:12]


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
    work_root: Path | None = None
    # cases/<case_list>: the case set of the run (cases.csv, the 70 cases of the study, by default)
    case_list: str = "cases.csv"
    # major version of the JDK at paths.java_home; every project of the case set must use it
    jdk: int = 21
    # autonomous mode: how many times the agent may start the scenario under the debugger in a cell
    debug_runs: int = 3
    extra_env: dict[str, str] = field(default_factory=dict)
    _work_dir: Path | None = field(default=None, repr=False)

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
        """Scratch space for workspaces and traces, outside the results tree and the repository:
        paths.work of the config, or a fresh directory in the system temp directory (removed at exit).
        A test run there cannot reach the results through a fixed relative path."""
        with _WORK_LOCK:
            return self._make_work_dir()

    def _make_work_dir(self) -> Path:
        if self._work_dir is None:
            if self.work_root is not None:
                self.work_root.mkdir(parents=True, exist_ok=True)
                self._work_dir = Path(tempfile.mkdtemp(prefix="tg-", dir=self.work_root))
            else:
                self._work_dir = Path(tempfile.mkdtemp(prefix="tg-"))
            atexit.register(shutil.rmtree, self._work_dir, True)
        return self._work_dir

    def run_id(self, round_number: int) -> str:
        return self.run_id_format.format(round=round_number)

    def cell_dir(self, case_id: str, mode: str, run_id: str) -> Path:
        return self.cells_dir / case_id / mode / run_id

    def command_env(self) -> dict[str, str]:
        """Environment of build/test commands: the allowlisted variables of the runner's environment,
        `env` from the config, JDK first on PATH."""
        env = {k: v for k, v in os.environ.items()
               if k in ENV_ALLOWLIST or k.startswith(ENV_ALLOWLIST_PREFIXES)}
        env.update(self.extra_env)
        env["JAVA_HOME"] = str(self.java_home)
        env["PATH"] = f"{self.java_home / 'bin'}{os.pathsep}{env.get('PATH', '')}"
        return env


_WORK_LOCK = threading.Lock()


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
        raise ConfigError("set paths.java_home in the config or JAVA_HOME in the environment (the JDK of `jdk`, 21 by default)")
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
        work_root=_path(paths.get("work")),
        extra_env={str(k): os.path.expandvars(str(v)) for k, v in (raw.get("env") or {}).items()},
        case_list=str(raw.get("case_list") or "cases.csv"),
        jdk=int(raw.get("jdk", 21)),
        debug_runs=int(raw.get("debug_runs", 3)),
    )
    if cfg.debug_runs < 1:
        raise ConfigError(f"debug_runs must be at least 1, got {cfg.debug_runs}")
    if Path(cfg.case_list).name != cfg.case_list or not cfg.case_list.endswith(".csv"):
        raise ConfigError(f"case_list must be a .csv file name in cases/, got {cfg.case_list!r}")
    if cfg.work_root is not None:
        for inside in (cfg.results_dir, ROOT):
            if cfg.work_root == inside or inside in cfg.work_root.parents:
                raise ConfigError(f"paths.work must be outside {inside}: {cfg.work_root}")
    configure_tools(cfg)
    return cfg
