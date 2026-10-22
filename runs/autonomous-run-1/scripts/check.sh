#!/usr/bin/env bash
# compact overnight check (read-only)
FR=~/repos/full-run
for l in lane-a lane-b tp-lane-a tp-lane-b; do
  [ -f $FR/logs/$l.log ] || continue
  d=$( [ -f $FR/logs/$l.done ] && cat $FR/logs/$l.done || echo -)
  echo "$l done=$d last: $(tail -1 $FR/logs/$l.log | cut -c1-150)"
done
curl -s --max-time 20 127.0.0.1:8780/api/status | python3 -c '
import json,sys,time
d=json.load(sys.stdin)
for g in d.get("lanes",[]):
    print("LANE",g.get("name"),"alive",g.get("alive"),"done_code",g.get("done_code"),"total",g.get("total"),g.get("counts"),"eta_h",round((g.get("eta_s") or 0)/3600,1))
for r in d.get("running",[]):
    print("RUN",r.get("case"),r.get("run"),r.get("phase"),"elapsed",r.get("elapsed"),"turns",r.get("turns"),"jvm",r.get("jvm_runs"))
a=d.get("aggregates",{})
print("AGG",json.dumps(a.get("all",a))[:600])
m=d.get("machine",{}); print("MACH",json.dumps(m)[:300])
for e in (d.get("errors") or [])[-6:]: print("ERR",str(e)[:200])
' 2>&1 | head -40
