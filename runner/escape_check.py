"""Check that a generation cell stayed inside its workspace.

There is no operating-system sandbox: the Pi guard confines the agent's tools to the workspace,
but the build and test commands run project code, and the generated test is project code too.
This check looks for signs that the agent or its test reached outside the workspace or read the
input of the mode at run time. A flagged cell is not excluded; the flag is reported next to the
metrics.

Rules (all modes):

  agent tool calls (session.jsonl)
    * a path argument that is absolute, starts with `~`, or leaves the workspace through `..`;
    * any argument mentioning a harness root (results, cases, repository, home directory).
  guard
    * every denied action (denied.jsonl).
  generated test (added lines of result.diff, comments and the file headers removed)
    * the mode input or the recording directory: `carving/`, `call-dumps.json`,
      `PreliminaryTest.java.txt` (a test must not read its input at run time);
    * an absolute file system path literal (`"/home/..."`, `"C:\\..."`, `"~/..."`);
    * a string literal leaving the working directory through `../`;
    * environment and user locations: `System.getenv`, `user.home`, `user.dir`;
    * process execution: `ProcessBuilder`, `Runtime.getRuntime().exec`;
    * walking up from the working directory: `Paths.get("")`, `Path.of("")`, `new File("")` or
      `.toAbsolutePath()` together with `getParent()`/`getParentFile()`;
    * listing the file system from the working directory or a root: `Files.walk/find/list` on
      `Paths.get("")`, `Path.of("")`, `"."`, `"/"`.
  Not flagged: relative project paths (`src/test/resources/...`), classpath resources
  (`getResource`), `getParent()` on non-file objects (AST nodes, DOM elements).
"""
from __future__ import annotations

import json
import re
from pathlib import Path
from typing import Any

INPUT_MARKERS = re.compile(r"carving/|call-dumps\.json|PreliminaryTest\.java\.txt")
ABS_LITERAL = re.compile(r'"(?:/(?:home|Users|tmp|private|var|root|etc|opt|mnt|srv|usr|proc)\b[^"]*|~/[^"]*|[A-Za-z]:\\\\[^"]*)"')
UP_LITERAL = re.compile(r'"(?:\.\./)+[^"]*"|"\.\."')
ENV = re.compile(r"System\s*\.\s*getenv\s*\(|\"user\.home\"|\"user\.dir\"")
PROCESS = re.compile(r"\bProcessBuilder\b|Runtime\s*\.\s*getRuntime\s*\(\s*\)\s*\.\s*exec\s*\(")
CWD = re.compile(r'(?:Paths\s*\.\s*get|Path\s*\.\s*of)\s*\(\s*""\s*\)|new\s+File\s*\(\s*""\s*\)|\.toAbsolutePath\s*\(\s*\)')
PARENT = re.compile(r"\.getParent(?:File)?\s*\(\s*\)")
LISTING = re.compile(r'Files\s*\.\s*(?:walk|find|list)\s*\(\s*(?:(?:Paths\s*\.\s*get|Path\s*\.\s*of)\s*\(\s*"(?:|\.|/)"\s*\))')
COMMENT = re.compile(r"//[^\n]*|/\*.*?\*/", re.S)
STRING = re.compile(r'"(?:\\.|[^"\\])*"')

PATH_KEYS = ("path", "file_path", "directory", "dir", "cwd", "glob")


def _strip_comments(code: str) -> str:
    """Remove comments, keeping string literals (a `//` inside a string is not a comment)."""
    out, i = [], 0
    for m in STRING.finditer(code):
        out.append(COMMENT.sub(" ", code[i:m.start()]))
        out.append(m.group(0))
        i = m.end()
    out.append(COMMENT.sub(" ", code[i:]))
    return "".join(out)


def added_code(diff: str) -> dict[str, str]:
    """Added lines of a unified diff by file (new files of the cell are fully added)."""
    files: dict[str, list[str]] = {}
    current = None
    for line in diff.splitlines():
        if line.startswith("+++ "):
            name = line[4:].strip()
            current = name[2:] if name.startswith("b/") else name
            files.setdefault(current, [])
        elif current and line.startswith("+"):
            files[current].append(line[1:])
    return {f: "\n".join(lines) for f, lines in files.items()}


def _leaves(path: str) -> bool:
    depth = 0
    for part in path.replace("\\", "/").split("/"):
        if part == "..":
            depth -= 1
            if depth < 0:
                return True
        elif part not in ("", "."):
            depth += 1
    return False


def check_code(code: str, where: str) -> list[dict[str, str]]:
    findings = []
    text = _strip_comments(code)

    def add(kind: str, m: re.Match) -> None:
        line = text.count("\n", 0, m.start()) + 1
        findings.append({"kind": kind, "where": f"{where}:{line}", "evidence": m.group(0)[:160]})

    for kind, rx in (("reads mode input", INPUT_MARKERS), ("absolute path", ABS_LITERAL),
                     ("leaves working directory", UP_LITERAL), ("environment or user location", ENV),
                     ("runs a process", PROCESS), ("lists the file system", LISTING)):
        for m in rx.finditer(text):
            add(kind, m)
    cwd = list(CWD.finditer(text))
    if cwd and PARENT.search(text):
        add("walks up from the working directory", cwd[0])
    return findings


def check_tool_calls(calls: list[dict], harness_roots: list[str]) -> list[dict[str, str]]:
    findings = []
    roots = [r.rstrip("/") for r in harness_roots if r and len(r.rstrip("/")) > 1]
    for i, call in enumerate(calls, start=1):
        args = call.get("arguments") or {}
        name = call.get("name") or "?"
        where = f"tool call {i} ({name})"
        for key in PATH_KEYS:
            v = args.get(key)
            if isinstance(v, str) and (v.startswith("/") or v.startswith("~") or _leaves(v)):
                findings.append({"kind": "tool path outside the workspace", "where": where, "evidence": f"{key}={v[:160]}"})
        blob = json.dumps(args, ensure_ascii=False)
        for r in roots:
            if r in blob:
                findings.append({"kind": "tool argument names a harness location", "where": where, "evidence": r})
                break
    return findings


def _jsonl(path: Path) -> list[dict]:
    if not path.is_file():
        return []
    out = []
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        try:
            row = json.loads(line)
        except json.JSONDecodeError:
            continue
        if isinstance(row, dict):
            out.append(row)
    return out


def tool_calls(events: list[dict]) -> list[dict]:
    calls, seen = [], set()
    for event in events:
        message = event.get("message")
        if not isinstance(message, dict) or not isinstance(message.get("content"), list):
            continue
        for block in message["content"]:
            if isinstance(block, dict) and block.get("type") == "toolCall":
                ident = block.get("id")
                if ident and ident in seen:
                    continue
                seen.add(ident)
                calls.append(block)
    return calls


def check_cell(cell: Path, harness_roots: list[str]) -> dict[str, Any]:
    """Escape check of a generation cell: result.diff, artifacts/session.jsonl, artifacts/denied.jsonl."""
    findings: list[dict[str, str]] = []
    for row in _jsonl(cell / "artifacts" / "denied.jsonl"):
        findings.append({"kind": "denied by the guard", "where": str(row.get("kind") or "?"),
                         "evidence": str(row.get("path") or row.get("reason") or row)[:160]})
    findings += check_tool_calls(tool_calls(_jsonl(cell / "artifacts" / "session.jsonl")), harness_roots)
    diff = cell / "result.diff"
    if diff.is_file():
        for name, code in added_code(diff.read_text(encoding="utf-8", errors="replace")).items():
            if name.endswith((".java", ".kt", ".groovy")):
                findings += check_code(code, name)
    return {"status": "flagged" if findings else "clean", "findings": findings}
