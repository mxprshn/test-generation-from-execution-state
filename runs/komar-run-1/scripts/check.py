"""One progress line for RUN-LOG.md: cells, statuses, passing, timeouts, agent seconds, cost, gateway errors, load."""
import datetime, glob, json, os, statistics, subprocess

R = "/<root>/results"
KR = "/<root>/komar-run"
rows = []
for g in ("komar-v2", "komar-tools-projects"):
    for p in glob.glob(f"{R}/{g}/cells/*/komar/r*/summary.json"):
        try:
            s = json.load(open(p))
        except Exception:
            continue
        e = {}
        ep = p.replace("summary.json", "eval.json")
        if os.path.exists(ep):
            try:
                e = json.load(open(ep))
            except Exception:
                pass
        rows.append((p.split("/")[-4], p.split("/")[-2], s, e))
st = {}
for _, _, s, _ in rows:
    st[s.get("status")] = st.get(s.get("status"), 0) + 1
ev = [r for r in rows if r[3]]
passed = sum(1 for r in ev if (r[3].get("verify") or {}).get("passes"))
secs = sorted(s["agent_seconds"] for _, _, s, _ in rows if isinstance(s.get("agent_seconds"), (int, float)))
cost = sum((s.get("usage") or {}).get("cost_usd") or 0 for _, _, s, _ in rows)
gw = [f"{c}/{r}" for c, r, s, _ in rows if s.get("ended_on_gateway_error") or s.get("ended_on_provider_client_error")]
to = [f"{c}/{r}" for c, r, s, _ in rows if s.get("status") == "timed_out"]
mem = int(open("/proc/meminfo").read().split("MemAvailable:")[1].split()[0]) // 1024
queue = sum(1 for _ in open(f"{KR}/queue.txt"))
jvms = subprocess.run("pgrep -c java", shell=True, capture_output=True, text=True).stdout.strip()
med = f"{statistics.median(secs):.0f}" if secs else "-"
print(f"- {datetime.datetime.now(datetime.timezone.utc).isoformat(timespec='seconds')} check: cells {len(rows)}/202 "
      f"{st}; evaluated {len(ev)}, passing {passed}; agent s median {med} max {secs[-1] if secs else '-'}; "
      f"cost ${cost:.2f}; queue {queue} target {open(KR + '/target').read().strip()}; "
      f"load {os.getloadavg()[0]:.1f} mem {mem}MB jvms {jvms}; timeouts {to}; gateway-ended {gw}")
