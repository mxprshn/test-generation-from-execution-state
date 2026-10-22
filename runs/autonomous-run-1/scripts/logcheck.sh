#!/usr/bin/env bash
# one-line check appended to NIGHT-LOG.md + printed
FR=~/repos/full-run; cd $FR
line=$(curl -s --max-time 30 127.0.0.1:8780/api/status | python3 -c '
import json,sys
d=json.load(sys.stdin); out=[]
for g in d["lanes"]:
    c=g["counts"]; 
    if not g["alive"] and not c["done"] and g["done_code"] is None: continue
    out.append("%s %s/%s run%d fail%d infra%d alive=%s rc=%s"%(g["name"],c["done"],g["total"],c["running"]+c["evaluating"],c["failed"],c["infra_failed"],g["alive"],g["done_code"]))
a=d["aggregates"]; a=a.get("all",a); m=d["machine"]
out.append("passed %s/%s timeouts %s runlimit %s escape %s $%.2f"%(a.get("passed"),a.get("evaluated"),a.get("timeouts"),a.get("at_run_limit"),a.get("escape"),a.get("cost_usd") or 0))
out.append("load %.1f mem %dMB swap %d disk %s jvms %d"%(m["load1"],m["mem_available_mb"],m["swap_used_mb"],min(m["disk_free_gb"].values()),m["jvms"]))
long=[r["case"]+"/"+r["run"]+":"+str(int(r["elapsed"])) for r in d["running"] if (r.get("elapsed") or 0)>1500]
if long: out.append("LONG "+",".join(long))
print("; ".join(out))' 2>&1)
inf=$(python3 infra_scan.py 2>&1 | grep -v "^  moved" | awk '{print $1}' | tr '\n' ' ')
echo "- $(date -Is) check: $line; stream-cut cells: ${inf:-none}" | tee -a NIGHT-LOG.md
