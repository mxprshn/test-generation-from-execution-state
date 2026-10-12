"""Framework components and test-source classes per project -> components.json, test-classes.json.

Both lists feed the dependency rule of RQ1 level B (compare.py): a receiver field that holds a
framework component or an instance of a test-source class (a hand-written stub) is a collaborator,
not test data, and is compared by presence only.

  * components.json  : classes with a Spring stereotype annotation (@Service, @Component,
                       @Repository, @Controller, @RestController, @Configuration) in main sources;
  * test-classes.json: top-level classes declared in test sources (src/test, src/it, jgit tst/ and
                       the jgit test-support modules org.eclipse.jgit.junit*).

The lists are derived from the project sources mechanically, the same for every mode. The files in the
repository were generated from the checkouts at the commits of projects.yaml:

    python3 -m evaluation.components --config config.yaml
"""
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path

from runner.cases import load_projects
from runner.config import load_config

HERE = Path(__file__).resolve().parent
OUT = HERE / "components.json"
OUT_TESTS = HERE / "test-classes.json"

STEREOTYPE = re.compile(
    r"^\s*@(?:org\.springframework\.\w+(?:\.\w+)*\.)?"
    r"(Service|Component|Repository|Controller|RestController|Configuration)\b",
    re.M,
)
PACKAGE = re.compile(r"^\s*package\s+([\w.]+)\s*;?", re.M)
TYPE_DECL = re.compile(
    r"^\s*(?:public\s+|protected\s+|private\s+)?(?:abstract\s+|final\s+|static\s+)*"
    r"(?:class|interface|enum|record)\s+(\w+)",
    re.M,
)
TEST_SOURCE = re.compile(r"/(src/test|src/it|src/test-\w+|tst)/|/org\.eclipse\.jgit\.junit[\w.]*/src/")


def _sources(repo: Path):
    for ext in ("*.java", "*.groovy"):
        for src in repo.rglob(ext):
            p = src.as_posix()
            if "/target/" in p or "/build/" in p:
                continue
            yield src, p


def components(repo: Path) -> list[str]:
    found: set[str] = set()
    for src, p in _sources(repo):
        if "/src/main/" not in p or not p.endswith(".java"):
            continue
        text = src.read_text(errors="ignore")
        if not STEREOTYPE.search(text):
            continue
        pkg = PACKAGE.search(text)
        decl = TYPE_DECL.search(text)
        if decl:
            found.add((pkg.group(1) + "." if pkg else "") + decl.group(1))
    return sorted(found)


def test_classes(repo: Path) -> list[str]:
    found: set[str] = set()
    for src, p in _sources(repo):
        if not TEST_SOURCE.search(p) or "/resources/" in p:
            continue
        text = src.read_text(errors="ignore")
        pkg = PACKAGE.search(text)
        prefix = pkg.group(1) + "." if pkg else ""
        found.add(prefix + src.stem)
        for decl in TYPE_DECL.finditer(text):
            if decl.group(1) != src.stem and not decl.group(0).startswith((" ", "\t")):
                found.add(prefix + decl.group(1))
    return sorted(found)


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--config", default="config.yaml")
    cfg = load_config(ap.parse_args(argv).config)
    repos = sorted((name, cfg.projects_dir / p.checkout_name) for name, p in load_projects().items())
    comps = {project: components(repo) for project, repo in repos}
    tests = {project: test_classes(repo) for project, repo in repos}
    OUT.write_text(json.dumps(comps, indent=1) + "\n")
    OUT_TESTS.write_text(json.dumps(tests, indent=1) + "\n")
    for project, _ in repos:
        print(f"{project:12} components={len(comps[project]):4} test_classes={len(tests[project])}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
