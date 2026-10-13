"""Pack a results directory into one archive for publication.

    python3 scripts/export_results.py --results <results dir> --archive results.tar.gz \
        [--redact OLD=NEW ...] [--redact-file FILE] [--extra FILE ...] [--extra-dir NAME=DIR ...]

The archive unpacks into a results directory (paths are relative to it):
  cells/<case>/<mode>/<run-id>/   summary.json, eval.json, result.diff, prompts/, artifacts/ (the agent
                                  session cleaned, see below), eval-artifacts/ (verify logs, candidate
                                  traces, mutations.xml, PIT log)
  references/<case>/              reference traces and their audit
  status/, logs/, report/
  provenance/                     one config-<hash>.yaml per distinct config, runs.jsonl (every start with
                                  its config), --extra files, --extra-dir directories
Left out: *.pre-migration copies, <run-id>.incomplete-* and <run-id>.infra-* directories (interrupted or
infrastructure-failed attempts that were regenerated).

The agent session (artifacts/session.jsonl, Pi's JSON event stream) is kept without its redundant parts:
the streaming events (message_update, tool_execution_update) and message_start of messages that end
(message_end repeats them), the content repeated by turn_end, agent_end, tool_execution_start and
tool_execution_end (kept once, in message_end), and the opaque signatures of thinking blocks. Every message,
tool call with its arguments, tool result, error, usage and timestamp stays; a message cut off by a
timeout (no message_end) keeps its streaming events.

--redact OLD=NEW replaces a string in every file (applied in the given order), e.g. the absolute root of
the machine and the provider's address; --redact-file reads such pairs from a JSON list of [OLD, NEW]
(applied before --redact). The archive is deterministic: sorted entries, fixed time, owner
and mode, gzip header without time.
"""
from __future__ import annotations

import argparse
import gzip
import hashlib
import io
import json
import re
import tarfile
from pathlib import Path

SKIP_DIR = re.compile(r"\.(incomplete|infra)-")
EXPORTED = ("cells", "references", "status", "logs", "report")
SESSION = "session.jsonl"
STREAM_EVENTS = {"message_update", "tool_execution_update"}
SIGNATURE_KEYS = ("thinkingSignature", "signature", "redacted", "encrypted_content", "encryptedContent")
MTIME = 1767225600  # 2026-01-01T00:00:00Z


def skipped(rel: Path) -> bool:
    return rel.name.endswith(".pre-migration") or any(SKIP_DIR.search(p) for p in rel.parts)


def _meta(message: dict) -> dict:
    """A message without its content (role, ids, usage, stopReason, timestamps stay)."""
    return {k: v for k, v in message.items() if k not in ("content", "details")}


def _no_signature(message: dict) -> dict:
    content = message.get("content")
    if not isinstance(content, list):
        return message
    blocks = []
    for b in content:
        if isinstance(b, dict) and b.get("type") in ("thinking", "redacted_thinking"):
            b = {k: v for k, v in b.items() if k not in SIGNATURE_KEYS}
        blocks.append(b)
    return {**message, "content": blocks}


def _dump(e) -> str:
    return json.dumps(e, ensure_ascii=False, separators=(",", ":"))


def clean_session(lines: list[str]) -> list[str]:
    """Pi event stream without the redundant parts (see the module docstring)."""
    events = []
    for line in lines:
        line = line.strip()
        if not line:
            continue
        try:
            events.append(json.loads(line))
        except json.JSONDecodeError:
            events.append(line)
    typ = [e.get("type") if isinstance(e, dict) else None for e in events]
    # message_start whose message never ends (cut off): keep it and its streaming events
    open_from = None
    for i, t in enumerate(typ):
        if t == "message_start":
            open_from = i
        elif t == "message_end":
            open_from = None
    out = []
    for i, e in enumerate(events):
        if not isinstance(e, dict):
            out.append(e)
            continue
        t = typ[i]
        if open_from is not None and i >= open_from:
            out.append(_dump(e))
            continue
        if t in STREAM_EVENTS or t == "message_start":
            continue
        if t == "message_end" and isinstance(e.get("message"), dict):
            e = {**e, "message": _no_signature(e["message"])}
        elif t == "turn_end":
            e = {**e}
            if isinstance(e.get("message"), dict):
                e["message"] = _meta(e["message"])
            if isinstance(e.get("toolResults"), list):
                e["toolResults"] = [_meta(r) if isinstance(r, dict) else r for r in e["toolResults"]]
        elif t == "agent_end" and isinstance(e.get("messages"), list):
            e = {**e, "messages": [_meta(m) if isinstance(m, dict) else m for m in e["messages"]]}
        elif t == "tool_execution_start":
            e = {k: v for k, v in e.items() if k != "args"}
        elif t == "tool_execution_end" and isinstance(e.get("result"), dict):
            e = {**e, "result": _meta(e["result"])}
        out.append(_dump(e))
    return out


def conversation(lines: list[str]) -> dict:
    """What a session records: messages (with signatures removed), tool calls, usage; for tests."""
    msgs, cut_off = [], False
    for line in lines:
        if not line.strip():
            continue
        e = json.loads(line)
        if e.get("type") == "message_start":
            cut_off = True
        if e.get("type") == "message_end":
            msgs.append(_no_signature(e["message"]))
            cut_off = False
    calls = [b for m in msgs if isinstance(m.get("content"), list) for b in m["content"]
             if isinstance(b, dict) and b.get("type") == "toolCall"]
    return {"messages": msgs, "tool_calls": calls, "cut_off": cut_off,
            "usage": [m.get("usage") for m in msgs if m.get("usage")]}


def redact(data: bytes, pairs: list[tuple[str, str]]) -> bytes:
    for old, new in pairs:
        data = data.replace(old.encode(), new.encode())
    return data


def content_of(src: Path, rel: Path, pairs: list[tuple[str, str]]) -> bytes:
    data = src.read_bytes()
    if rel.name == SESSION and rel.parent.name == "artifacts":
        text = data.decode("utf-8", errors="replace")
        data = ("\n".join(clean_session(text.splitlines())) + "\n").encode()
    return redact(data, pairs)


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def collect(results: Path, extras: list[Path], extra_dirs: dict[str, Path],
            pairs: list[tuple[str, str]], stats: dict) -> dict[str, bytes | Path]:
    """Archive path -> content (bytes) or source file (read when packed)."""
    entries: dict[str, bytes | Path] = {}
    for top in EXPORTED:
        base = results / top
        if not base.is_dir():
            continue
        for src in base.rglob("*"):
            if not src.is_file():
                continue
            rel = src.relative_to(results)
            if skipped(rel):
                stats["skipped"] += 1
                continue
            entries[str(rel)] = src
    configs: dict[str, str] = {}
    runs = []
    for start in sorted(results.glob("start-*.json")):
        stamp = start.stem.removeprefix("start-")
        cfg = results / f"config-{stamp}.yaml"
        name = None
        if cfg.is_file():
            text = redact(cfg.read_bytes(), pairs)
            digest = hashlib.sha256(text).hexdigest()[:12]
            name = configs.setdefault(digest, f"config-{digest}.yaml")
            entries[f"provenance/{name}"] = text
        rec = json.loads(redact(start.read_bytes(), pairs))
        runs.append(json.dumps({"started": stamp, "config": name, **rec}))
    entries["provenance/runs.jsonl"] = ("\n".join(runs) + "\n").encode() if runs else b""
    for f in extras:
        entries[f"provenance/{f.name}"] = redact(f.read_bytes(), pairs)
    for name, d in extra_dirs.items():
        for src in d.rglob("*"):
            if src.is_file():
                entries[f"provenance/{name}/{src.relative_to(d)}"] = redact(src.read_bytes(), pairs)
    stats["configs"] = len(configs)
    stats["runs"] = len(runs)
    return entries


def export(results: Path, archive: Path, extras: list[Path] | None = None,
           extra_dirs: dict[str, Path] | None = None, pairs: list[tuple[str, str]] | None = None) -> dict:
    pairs = pairs or []
    stats = {"files": 0, "bytes": 0, "skipped": 0, "session_bytes_before": 0, "session_bytes_after": 0}
    entries = collect(results, extras or [], extra_dirs or {}, pairs, stats)
    archive.parent.mkdir(parents=True, exist_ok=True)
    with open(archive, "wb") as raw, gzip.GzipFile(filename="", fileobj=raw, mode="wb", mtime=0) as gz, \
            tarfile.open(fileobj=gz, mode="w", format=tarfile.PAX_FORMAT) as tar:
        for name in sorted(entries):
            item = entries[name]
            if isinstance(item, Path):
                data = content_of(item, Path(name), pairs)
                if item.name == SESSION:
                    stats["session_bytes_before"] += item.stat().st_size
                    stats["session_bytes_after"] += len(data)
            else:
                data = item
            info = tarfile.TarInfo(name)
            info.size, info.mtime, info.mode = len(data), MTIME, 0o644
            info.uid = info.gid = 0
            info.uname = info.gname = ""
            tar.addfile(info, io.BytesIO(data))
            stats["files"] += 1
            stats["bytes"] += len(data)
    stats["archive"] = str(archive)
    stats["archive_bytes"] = archive.stat().st_size
    stats["archive_sha256"] = sha256(archive)
    return stats


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--results", required=True, type=Path)
    ap.add_argument("--archive", required=True, type=Path)
    ap.add_argument("--redact", nargs="*", default=[], help="OLD=NEW, applied in order")
    ap.add_argument("--redact-file", type=Path, help="JSON list of [OLD, NEW] pairs")
    ap.add_argument("--extra", nargs="*", default=[], type=Path)
    ap.add_argument("--extra-dir", nargs="*", default=[])
    a = ap.parse_args(argv)
    dirs = {k: Path(v) for k, v in (x.split("=", 1) for x in a.extra_dir)}
    pairs = [tuple(p) for p in json.loads(a.redact_file.read_text())] if a.redact_file else []
    pairs += [tuple(x.split("=", 1)) for x in a.redact]
    print(json.dumps(export(a.results, a.archive, a.extra, dirs, pairs), indent=1))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
