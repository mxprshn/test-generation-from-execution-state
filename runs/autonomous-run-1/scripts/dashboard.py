#!/usr/bin/env python3
"""Live status page of the full autonomous run (read-only; Python standard library only).

Serves on 127.0.0.1 only; open it through an SSH tunnel:
    ssh -L 8780:127.0.0.1:8780 <user>@<host>   ->   http://localhost:8780

Sources (never written): the lane configs, their results directories (status/, logs/, cells/), the
scratch work directories of running cells (live Pi session and debugger journal), the lane logs
~/repos/full-run/logs/<lane>.log|.done, the released v2 cells for comparison, /proc and the disk.

    dashboard.py [--port 8780] [--lanes a.yaml b.yaml ...] [--results DIR ...] [--release DIR]
--results monitors results directories without lane configs (cases are taken from their cells/).
"""
from __future__ import annotations

import argparse
import glob
import hashlib
import json
import os
import re
import shutil
import statistics
import threading
import time
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

FR = Path.home() / "repos" / "full-run"
DEFAULT_LANES = [FR / n for n in ("lane-a.yaml", "lane-b.yaml", "tp-lane-a.yaml", "tp-lane-b.yaml")]
DEFAULT_RELEASE = Path.home() / "repos" / "results" / "v2-release"
MODE = "autonomous"
ERR_RE = re.compile(r"ERROR|Traceback|Exception|error:|infrastructure failure|evaluation error|transient|refused",
                    re.I)


# ---------------------------------------------------------------- small helpers (robust to partial files)

def read_json(p: Path):
    try:
        return json.loads(p.read_text(encoding="utf-8"))
    except Exception:
        return None


def read_text(p: Path, limit: int | None = None) -> str:
    try:
        with p.open("rb") as h:
            if limit:
                size = p.stat().st_size
                h.seek(max(0, size - limit))
            return h.read().decode("utf-8", "replace")
    except Exception:
        return ""


def parse_ts(s: str | None) -> float | None:
    if not s:
        return None
    try:
        return datetime.fromisoformat(s.replace("Z", "+00:00")).timestamp()
    except Exception:
        return None


def neutral_name(*parts: str) -> str:  # as runner/config.py
    return "w" + hashlib.sha256("\0".join(parts).encode()).hexdigest()[:12]


def load_yaml(p: Path) -> dict:
    """The few fields of a lane config this page needs (PyYAML if present, else a minimal reader)."""
    text = read_text(p)
    try:
        import yaml  # type: ignore
        return yaml.safe_load(text) or {}
    except ImportError:
        pass
    out: dict = {"paths": {}}
    section = None
    for line in text.splitlines():
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        if line.startswith("- ") and section == "cases":
            out.setdefault("cases", []).append(line[2:].strip())
            continue
        m = re.match(r"^(\w+):\s*(.*?)\s*(#.*)?$", line)
        if m:
            key, val = m.group(1), m.group(2)
            section = key
            if val.startswith("[") and val.endswith("]"):
                out[key] = [v.strip() for v in val[1:-1].split(",") if v.strip()]
            elif val:
                out[key] = int(val) if val.isdigit() else val
            continue
        m = re.match(r"^  (\w+):\s*(.*?)\s*(#.*)?$", line)
        if m and section == "paths":
            out["paths"][m.group(1)] = m.group(2)
    return out


# ---------------------------------------------------------------- model

class Lane:
    def __init__(self, cfg_path: Path | None, results: Path | None = None):
        self.cfg_path = cfg_path
        if cfg_path is not None:
            raw = load_yaml(cfg_path)
            self.name = cfg_path.stem
            paths = raw.get("paths") or {}
            self.results = Path(os.path.expanduser(str(paths.get("results"))))
            self.work = Path(os.path.expanduser(str(paths.get("work")))) if paths.get("work") else None
            self.cases = list(raw.get("cases") or [])
            self.rounds = [int(r) for r in (raw.get("rounds") or [1])]
            self.timeout = int(raw.get("timeout") or 600)
            self.debug_runs = int(raw.get("debug_runs") or 3)
            self.parallel = int(raw.get("parallel_cases") or 1)
            self.group = "tools-projects (31)" if str(raw.get("jdk")) == "11" else "study (70)"
            self.run_format = str(raw.get("run_id_format") or "r{round}")
        else:
            self.name = results.name
            self.results = results
            self.work = None
            self.cases = sorted(d.name for d in (results / "cells").glob("*") if d.is_dir())
            runs = {p.name for p in (results / "cells").glob(f"*/{MODE}/r*") if re.fullmatch(r"r\d+", p.name)}
            self.rounds = sorted(int(r[1:]) for r in runs) or [1]
            self.timeout, self.debug_runs, self.parallel = 600, 3, 1
            self.group = results.name
            self.run_format = "r{round}"
        self.log = FR / "logs" / f"{self.name}.log"
        self.done = FR / "logs" / f"{self.name}.done"

    def run_ids(self):
        return [self.run_format.format(round=r) for r in self.rounds]


def proc_cmdlines() -> list[str]:
    out = []
    for d in glob.glob("/proc/[0-9]*/cmdline"):
        try:
            out.append(Path(d).read_bytes().replace(b"\0", b" ").decode("utf-8", "replace"))
        except Exception:
            pass
    return out


def machine(results_dirs: list[Path], cmds: list[str]) -> dict:
    info: dict = {}
    try:
        l1, l5, l15 = Path("/proc/loadavg").read_text().split()[:3]
        info.update(load1=float(l1), load5=float(l5), load15=float(l15))
    except Exception:
        pass
    try:
        mem = {}
        for line in Path("/proc/meminfo").read_text().splitlines():
            k, v = line.split(":", 1)
            mem[k] = int(v.split()[0]) // 1024
        info.update(mem_total_mb=mem.get("MemTotal"), mem_available_mb=mem.get("MemAvailable"),
                    swap_used_mb=(mem.get("SwapTotal", 0) - mem.get("SwapFree", 0)))
    except Exception:
        pass
    disks = {}
    for d in results_dirs:
        try:
            disks[str(d)] = round(shutil.disk_usage(d if d.exists() else d.parent).free / 1e9, 1)
        except Exception:
            pass
    info["disk_free_gb"] = disks
    info["cpus"] = os.cpu_count()
    info["jvms"] = sum(1 for c in cmds if re.search(r"/bin/java\b", c))
    info["pi"] = sum(1 for c in cmds if re.search(r"(^|/)pi(\s|$)|pi-coding-agent|@earendil-works", c))
    info["mvn"] = sum(1 for c in cmds if "org.codehaus.plexus.classworlds.launcher" in c)
    return info


def release_cell(release: Path | None, case: str, mode: str, run_id: str) -> dict | None:
    if release is None:
        return None
    e = read_json(release / "cells" / case / mode / run_id / "eval.json")
    if not e:
        return None
    r = e.get("rq1") or {}
    v = e.get("verify") or {}
    return {"passes": v.get("passes"),
            "sabc": [r.get("recall_structure"), r.get("recall_io"), r.get("recall_state"), r.get("recall_strict")]}


def live_agent(lane: Lane, case: str, run_id: str) -> dict:
    """Turns, tool and debugger calls of a running cell from its scratch directory (if found)."""
    out: dict = {}
    if lane.work is None:
        return out
    name = neutral_name(case, MODE, run_id)
    cands = glob.glob(str(lane.work / "tg-*" / "cells" / name / "agent"))
    if not cands:
        return out
    agent = Path(max(cands, key=lambda p: os.path.getmtime(p)))
    sess = sorted(agent.glob("session/*.jsonl"), key=lambda p: p.stat().st_mtime)
    if sess:
        text = read_text(sess[-1])
        out["turns"] = text.count("\"role\":\"assistant\"")
        out["tool_calls"] = text.count("\"role\":\"toolResult\"")
    dbg = agent / "debug-log.jsonl"
    if dbg.is_file():
        calls: dict[str, int] = {}
        runs = stops = in_target = 0
        for line in read_text(dbg).splitlines():
            try:
                e = json.loads(line)
            except Exception:
                continue
            calls[e.get("tool", "?")] = calls.get(e.get("tool", "?"), 0) + 1
            runs = max(runs, int(e.get("run") or 0))
            ev = e.get("event") or {}
            if not isinstance(ev, dict):
                continue
            if ev.get("kind") == "stop":
                stops += 1
                in_target += 1 if ev.get("in_target") else 0
        out.update(debugger_calls=calls, jvm_runs=runs, stops=stops, stops_in_target=in_target)
    return out


def last_log_time(log: Path, pattern: str) -> float | None:
    t = None
    for line in read_text(log, 200_000).splitlines():
        if pattern in line:
            t = parse_ts(line.split(" ", 1)[0]) or t
    return t


def finished_row(lane: Lane, case: str, run_id: str, s: dict, e: dict | None, outcome: str | None,
                 release: Path | None) -> dict:
    u = s.get("usage") or (e or {}).get("usage") or {}
    d = s.get("debugger") or (e or {}).get("debugger") or {}
    v = (e or {}).get("verify") or {}
    r = (e or {}).get("rq1") or {}
    esc = (s.get("escape_check") or (e or {}).get("escape_check") or {}).get("status")
    secs = s.get("agent_seconds")
    row = {
        "lane": lane.name, "group": lane.group, "case": case, "project": s.get("project") or case.split("-")[0],
        "run": run_id, "status": outcome or s.get("status"), "error": (s.get("error") or "")[:200],
        "finished": s.get("finished"), "passes": v.get("passes"), "compiles": v.get("compiles"),
        "evaluated": e is not None,
        "sabc": [r.get("recall_structure"), r.get("recall_io"), r.get("recall_state"), r.get("recall_strict")] if r else None,
        "escape": esc, "turns": u.get("turns"), "tool_calls": s.get("tool_calls"), "agent_seconds": secs,
        "cell_seconds": s.get("seconds"),
        "timeout": bool(secs is not None and secs >= lane.timeout - 1),
        "jvm_runs": d.get("jvm_runs"), "run_limit": d.get("run_limit") or lane.debug_runs,
        "refused_starts": d.get("refused_starts"), "stops_in_target": d.get("stops_in_target"),
        "stops": d.get("stops"), "plan_revisions": d.get("plan_revisions"),
        "input_tokens": u.get("input_tokens"), "cache_read_tokens": u.get("cache_read_tokens"),
        "output_tokens": u.get("output_tokens"), "cost_usd": u.get("cost_usd"),
        "model": s.get("model"),
    }
    if not lane.group.startswith("tools-projects"):  # the release has the 70 study cases only
        row["release_baseline"] = release_cell(release, case, "baseline", run_id)
        row["release_dump"] = release_cell(release, case, "dump", run_id)
    return row


def lane_snapshot(lane: Lane, cmds: list[str], release: Path | None, now: float) -> dict:
    alive = None
    if lane.cfg_path is not None:
        alive = any("scripts/run.py" in c and str(lane.cfg_path.resolve()) in c for c in cmds) or \
            any("scripts/run.py" in c and str(lane.cfg_path) in c for c in cmds)
    done_code = read_text(lane.done).strip() if lane.done.is_file() else None
    started = None
    for line in read_text(lane.log, 50_000).splitlines():
        if f" start {lane.name} " in line:
            started = parse_ts(line.split(" ", 1)[0])
    counts = {"done": 0, "evaluating": 0, "running": 0, "pending": 0, "failed": 0, "infra_failed": 0}
    running, finished, cell_secs = [], [], []
    for case in lane.cases:
        st = read_json(lane.results / "status" / f"{case}.json") or {}
        stage = str(st.get("stage") or "")
        outcomes = st.get("cells") or {}
        case_log = lane.results / "logs" / f"{case}.log"
        for run_id in lane.run_ids():
            key = f"{MODE}/{run_id}"
            cell = lane.results / "cells" / case / MODE / run_id
            s = read_json(cell / "summary.json")
            e = read_json(cell / "eval.json")
            outcome = outcomes.get(key)
            active = alive is not False and (stage == f"generate {key}" or stage == f"evaluate {key}")
            if active and stage.startswith("generate"):
                t0 = last_log_time(case_log, f"generate {key}") or parse_ts(st.get("updated"))
                live = live_agent(lane, case, run_id)
                running.append({"lane": lane.name, "case": case, "run": run_id, "phase": "generation",
                                "elapsed": round(now - t0, 1) if t0 else None, "limit": lane.timeout,
                                "run_limit": lane.debug_runs, **live})
                counts["running"] += 1
                continue
            if s is None or s.get("phase") != "done" and not s.get("finished"):
                counts["pending"] += 1
                continue
            row = finished_row(lane, case, run_id, s, e, outcome, release)
            if outcome == "infra_failed":
                counts["infra_failed"] += 1
            elif s.get("status") == "failed":
                counts["failed"] += 1
            if active:  # generated, evaluation running
                t0 = parse_ts(st.get("updated"))
                running.append({"lane": lane.name, "case": case, "run": run_id, "phase": "evaluation",
                                "elapsed": round(now - parse_ts(s.get("started")), 1) if s.get("started") else None,
                                "limit": lane.timeout, "run_limit": lane.debug_runs,
                                "turns": row["turns"], "tool_calls": row["tool_calls"], "jvm_runs": row["jvm_runs"],
                                "stops_in_target": row["stops_in_target"], "stops": row["stops"]})
                counts["evaluating"] += 1
            elif e is not None or outcome == "infra_failed" or s.get("status") == "failed":
                counts["done"] += 1
            else:
                counts["evaluating"] += 1  # generated, evaluation pending
            if s.get("seconds"):
                cell_secs.append(float(s["seconds"]))
            finished.append(row)
    total = len(lane.cases) * len(lane.run_ids())
    left = total - counts["done"]
    eta = None
    if started and counts["done"]:
        eta = round(left * (now - started) / counts["done"])
    elif cell_secs:
        eta = round(left * statistics.median(cell_secs) / max(1, lane.parallel))
    errors = []
    for line in read_text(lane.log, 200_000).splitlines()[-4000:]:
        if ERR_RE.search(line):
            errors.append(line[:300])
    return {"name": lane.name, "group": lane.group, "config": str(lane.cfg_path or ""),
            "results": str(lane.results), "alive": alive, "done_code": done_code, "started": started,
            "total": total, "counts": counts, "eta_s": eta,
            "median_cell_s": round(statistics.median(cell_secs)) if cell_secs else None,
            "parallel": lane.parallel, "running": running, "finished": finished, "errors": errors[-15:]}


def aggregates(rows: list[dict]) -> dict:
    def agg(rs):
        gen = [r for r in rs if r["status"] != "infra_failed"]
        ev = [r for r in gen if r["evaluated"]]
        out = {"cells": len(rs), "generated": len(gen), "evaluated": len(ev),
               "passed": sum(1 for r in ev if r["passes"]),
               "infra_failed": sum(1 for r in rs if r["status"] == "infra_failed"),
               "failed": sum(1 for r in gen if r["status"] == "failed"),
               "timeouts": sum(1 for r in gen if r["timeout"]),
               "at_run_limit": sum(1 for r in gen if r["jvm_runs"] and r["jvm_runs"] >= (r["run_limit"] or 3)),
               "escape": sum(1 for r in gen if r["escape"] == "escape")}
        for i, lvl in enumerate("SABC"):
            vals = [r["sabc"][i] for r in ev if r["sabc"] and r["sabc"][i] is not None]
            out[f"recall_{lvl}"] = round(sum(vals) / len(vals), 3) if vals else None
        for k in ("input_tokens", "cache_read_tokens", "output_tokens", "cost_usd"):
            out[k] = round(sum(r[k] or 0 for r in gen), 4)
        for k in ("turns", "agent_seconds"):
            vals = [r[k] for r in gen if r[k] is not None]
            out[f"median_{k}"] = round(statistics.median(vals), 1) if vals else None
        return out
    by_group: dict[str, list] = {}
    by_project: dict[str, list] = {}
    for r in rows:
        by_group.setdefault(r["group"], []).append(r)
        by_project.setdefault(f'{r["group"]} / {r["project"]}', []).append(r)
    return {"all": agg(rows), "groups": {k: agg(v) for k, v in sorted(by_group.items())},
            "projects": {k: agg(v) for k, v in sorted(by_project.items())}}


class State:
    def __init__(self, lanes: list[Lane], release: Path | None):
        self.lanes, self.release = lanes, release
        self.lock = threading.Lock()
        self.cache: tuple[float, dict] | None = None

    def snapshot(self) -> dict:
        with self.lock:
            now = time.time()
            if self.cache and now - self.cache[0] < 4:
                return self.cache[1]
            cmds = proc_cmdlines()
            lanes = [lane_snapshot(l, cmds, self.release, now) for l in self.lanes]
            rows = [r for l in lanes for r in l["finished"]]
            rows.sort(key=lambda r: r.get("finished") or "", reverse=True)
            data = {"now": datetime.now(timezone.utc).isoformat(timespec="seconds"),
                    "lanes": [{k: v for k, v in l.items() if k not in ("finished", "running")} for l in lanes],
                    "running": [r for l in lanes for r in l["running"]],
                    "finished": rows, "aggregates": aggregates(rows),
                    "machine": machine(sorted({l.results for l in self.lanes}), cmds),
                    "errors": [{"lane": l["name"], "line": e} for l in lanes for e in l["errors"]]}
            self.cache = (now, data)
            return data


PAGE = r"""<!doctype html><html><head><meta charset="utf-8"><title>autonomous run</title>
<style>
body{font:13px/1.35 -apple-system,Segoe UI,Helvetica,Arial,sans-serif;margin:12px;color:#222}
h2{font-size:15px;margin:18px 0 6px}table{border-collapse:collapse;margin-bottom:6px}
td,th{border:1px solid #ddd;padding:2px 6px;text-align:right;white-space:nowrap}
th{background:#f4f4f4;cursor:pointer;position:sticky;top:0}td.l,th.l{text-align:left}
.ok{color:#080}.bad{color:#c00}.warn{color:#b60}.muted{color:#888}
.bar{width:140px;height:10px;background:#eee;display:inline-block;vertical-align:middle}
.bar>i{display:block;height:100%;background:#4a8}.bar>i.w{background:#d90}.bar>i.x{background:#c33}
#hdr span{margin-right:16px}pre{background:#f8f8f8;padding:6px;max-height:220px;overflow:auto;font-size:12px}
</style></head><body>
<div id="hdr"></div>
<h2>Lanes</h2><div id="lanes"></div>
<h2>Running cells</h2><div id="running"></div>
<h2>Aggregates</h2><div id="agg"></div>
<h2>Finished cells <span class="muted" id="nfin"></span></h2><div id="fin"></div>
<h2>Recent errors</h2><pre id="err"></pre>
<script>
let sortKey=null,sortDir=-1,last=null;
const f=(x,d=2)=>x==null?'<span class=muted>–</span>':(typeof x==='number'?(Number.isInteger(x)?x:x.toFixed(d)):x);
const k=x=>x==null?'–':(x>=1e6?(x/1e6).toFixed(2)+'M':x>=1e3?(x/1e3).toFixed(1)+'k':x);
const hm=s=>s==null?'–':(s>=3600?Math.floor(s/3600)+'h'+String(Math.floor(s%3600/60)).padStart(2,'0'):Math.floor(s/60)+'m'+String(Math.floor(s%60)).padStart(2,'0'));
const yn=b=>b==null?'<span class=muted>–</span>':(b?'<span class=ok>✓</span>':'<span class=bad>✗</span>');
const sabc=a=>!a?'<span class=muted>–</span>':a.map(x=>x==null?'–':(+x).toFixed(2)).join('/');
function table(cols,rows,id){let h='<table id="'+(id||'')+'"><tr>'+cols.map((c,i)=>'<th class="'+(c[2]||'')+'" data-i="'+i+'">'+c[0]+'</th>').join('')+'</tr>';
 for(const r of rows)h+='<tr>'+cols.map(c=>'<td class="'+(c[2]||'')+'">'+c[1](r)+'</td>').join('')+'</tr>';return h+'</table>'}
function render(d){last=d;const m=d.machine;
 document.getElementById('hdr').innerHTML='<b>'+d.now+'</b>'+
 '<span>load '+f(m.load1,1)+' / '+f(m.load5,1)+' ('+m.cpus+' cpu)</span><span>mem avail '+k((m.mem_available_mb||0)*1e6/1e6)+' MB / '+m.mem_total_mb+'</span>'+
 '<span>swap '+m.swap_used_mb+' MB</span><span>disk free '+Object.values(m.disk_free_gb||{}).map(x=>x+' GB').join(', ')+'</span>'+
 '<span>JVMs '+m.jvms+'</span><span>pi '+m.pi+'</span><span>mvn '+m.mvn+'</span>';
 document.getElementById('lanes').innerHTML=table([
  ['lane',r=>r.name,'l'],['group',r=>r.group,'l'],['process',r=>r.alive==null?'–':(r.alive?'<span class=ok>alive</span>':'<span class=muted>not running</span>'),'l'],
  ['.done',r=>r.done_code==null?'–':(r.done_code==='0'?'<span class=ok>0</span>':'<span class=bad>'+r.done_code+'</span>')],
  ['done',r=>r.counts.done+' / '+r.total],['progress',r=>{const p=r.total?r.counts.done/r.total:0;return '<span class=bar><i style="width:'+(100*p).toFixed(0)+'%"></i></span> '+(100*p).toFixed(0)+'%'}],
  ['running',r=>r.counts.running],['evaluating',r=>r.counts.evaluating],['pending',r=>r.counts.pending],
  ['failed',r=>r.counts.failed?'<span class=bad>'+r.counts.failed+'</span>':0],['infra',r=>r.counts.infra_failed?'<span class=warn>'+r.counts.infra_failed+'</span>':0],
  ['median cell',r=>hm(r.median_cell_s)],['ETA',r=>hm(r.eta_s)],['parallel',r=>r.parallel]],d.lanes);
 document.getElementById('running').innerHTML=d.running.length?table([
  ['lane',r=>r.lane,'l'],['case',r=>r.case,'l'],['run',r=>r.run],['phase',r=>r.phase,'l'],
  ['elapsed / limit',r=>{if(r.phase!=='generation')return hm(r.elapsed);const p=Math.min(1,(r.elapsed||0)/r.limit);const c=p>.9?'x':p>.7?'w':'';return '<span class=bar><i class="'+c+'" style="width:'+(100*p).toFixed(0)+'%"></i></span> '+hm(r.elapsed)+' / '+hm(r.limit)}],
  ['turns',r=>f(r.turns)],['tool calls',r=>f(r.tool_calls)],
  ['debugger calls',r=>r.debugger_calls?Object.entries(r.debugger_calls).map(([a,b])=>a.replace('debug_','')+':'+b).join(' '):'–','l'],
  ['JVM runs',r=>r.jvm_runs==null?'–':r.jvm_runs+' / '+r.run_limit],['stops in target',r=>r.stops==null?'–':r.stops_in_target+' / '+r.stops]],d.running):'<span class=muted>none</span>';
 const a=d.aggregates,ac=[['scope',r=>r[0],'l'],['cells',r=>r[1].cells],['evaluated',r=>r[1].evaluated],
  ['pass',r=>r[1].evaluated?r[1].passed+' ('+(100*r[1].passed/r[1].evaluated).toFixed(0)+'%)':'–'],
  ['S',r=>f(r[1].recall_S)],['A',r=>f(r[1].recall_A)],['B',r=>f(r[1].recall_B)],['C',r=>f(r[1].recall_C)],
  ['failed',r=>r[1].failed],['infra',r=>r[1].infra_failed],['timeouts',r=>r[1].timeouts],['at run limit',r=>r[1].at_run_limit],['escape',r=>r[1].escape],
  ['med turns',r=>f(r[1].median_turns,0)],['med agent s',r=>f(r[1].median_agent_seconds,0)],
  ['input',r=>k(r[1].input_tokens)],['cache read',r=>k(r[1].cache_read_tokens)],['output',r=>k(r[1].output_tokens)],['$',r=>f(r[1].cost_usd)]];
 document.getElementById('agg').innerHTML=table(ac,[['all',a.all],...Object.entries(a.groups)])+table(ac,Object.entries(a.projects));
 let rows=[...d.finished];if(sortKey!=null){const c=FC[sortKey];rows.sort((x,y)=>{const a=c[3]?c[3](x):null,b=c[3]?c[3](y):null;return (a>b?1:a<b?-1:0)*sortDir})}
 document.getElementById('nfin').textContent='('+rows.length+', newest first; click a header to sort)';
 document.getElementById('fin').innerHTML=table(FC,rows,'fintab');
 document.querySelectorAll('#fintab th').forEach(th=>th.onclick=()=>{const i=+th.dataset.i;sortDir=sortKey===i?-sortDir:-1;sortKey=i;render(last)});
 document.getElementById('err').textContent=d.errors.map(e=>e.lane+': '+e.line).join('\n')||'none';}
const FC=[['lane',r=>r.lane,'l',r=>r.lane],['case',r=>r.case,'l',r=>r.case],['run',r=>r.run,'',r=>r.run],
 ['status',r=>r.status==='completed'||String(r.status).startsWith('completed')?'<span class=ok>'+r.status+'</span>':(r.status==='infra_failed'?'<span class=warn>infra</span>':'<span class=bad>'+r.status+'</span>'),'l',r=>r.status],
 ['pass',r=>r.evaluated?yn(r.passes):'<span class=muted>eval…</span>','',r=>r.passes],['S/A/B/C',r=>sabc(r.sabc),'',r=>r.sabc?r.sabc[1]:-1],
 ['baseline (v2)',r=>r.release_baseline?yn(r.release_baseline.passes)+' '+sabc(r.release_baseline.sabc):'<span class=muted>–</span>','',r=>r.release_baseline?r.release_baseline.sabc[1]:-1],
 ['dump (v2)',r=>r.release_dump?yn(r.release_dump.passes)+' '+sabc(r.release_dump.sabc):'<span class=muted>–</span>','',r=>r.release_dump?r.release_dump.sabc[1]:-1],
 ['escape',r=>r.escape==='escape'?'<span class=bad>ESCAPE</span>':f(r.escape),'',r=>r.escape],['turns',r=>f(r.turns),'',r=>r.turns],
 ['agent s',r=>r.timeout?'<span class=bad>'+f(r.agent_seconds,0)+' ⏱</span>':f(r.agent_seconds,0),'',r=>r.agent_seconds],
 ['JVM runs',r=>r.jvm_runs==null?'–':r.jvm_runs+'/'+r.run_limit+(r.refused_starts?' <span class=warn>+'+r.refused_starts+' refused</span>':''),'',r=>r.jvm_runs],
 ['stops in target',r=>r.stops==null?'–':r.stops_in_target+'/'+r.stops,'',r=>r.stops_in_target],['plan rev.',r=>f(r.plan_revisions),'',r=>r.plan_revisions],
 ['input',r=>k(r.input_tokens),'',r=>r.input_tokens],['cache read',r=>k(r.cache_read_tokens),'',r=>r.cache_read_tokens],['output',r=>k(r.output_tokens),'',r=>r.output_tokens],
 ['$',r=>f(r.cost_usd,3),'',r=>r.cost_usd],['finished',r=>(r.finished||'').replace('T',' ').slice(0,19),'l',r=>r.finished],['error',r=>r.error?'<span class=bad>'+r.error.replace(/</g,'&lt;')+'</span>':'','l',r=>r.error]];
async function tick(){try{const r=await fetch('api/status');render(await r.json())}catch(e){document.getElementById('hdr').innerHTML='<span class=bad>no connection: '+e+'</span>'}}
tick();setInterval(tick,10000);
</script></body></html>"""


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--port", type=int, default=8780)
    ap.add_argument("--bind", default="127.0.0.1")
    ap.add_argument("--lanes", nargs="*", type=Path, default=None, help="lane configs (default: the full run)")
    ap.add_argument("--results", nargs="*", type=Path, default=[], help="results dirs without lane configs")
    ap.add_argument("--release", type=Path, default=DEFAULT_RELEASE)
    args = ap.parse_args()
    lanes = [Lane(p) for p in (args.lanes if args.lanes is not None else ([] if args.results else DEFAULT_LANES))]
    lanes += [Lane(None, r.expanduser()) for r in args.results]
    state = State(lanes, args.release if args.release.exists() else None)

    class H(BaseHTTPRequestHandler):
        def log_message(self, *a):
            pass

        def do_GET(self):
            if self.path.startswith("/api/status"):
                body = json.dumps(state.snapshot(), default=str).encode()
                ctype = "application/json"
            elif self.path in ("/", "/index.html"):
                body, ctype = PAGE.encode(), "text/html; charset=utf-8"
            else:
                self.send_response(404)
                self.end_headers()
                return
            self.send_response(200)
            self.send_header("Content-Type", ctype)
            self.send_header("Cache-Control", "no-store")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)

    srv = ThreadingHTTPServer((args.bind, args.port), H)
    print(f"dashboard on http://{args.bind}:{args.port}/ ({len(lanes)} lanes)", flush=True)
    srv.serve_forever()


if __name__ == "__main__":
    main()
