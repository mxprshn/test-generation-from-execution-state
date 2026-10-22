"""List failed/timed-out cells whose agent session ended on a provider error the frozen harness does not
recognise (e.g. "The upstream stream failed before completing the response. Please retry.").
Same rule as runner/cell.py is_infra_failure(ended_on_gateway_error): the LAST assistant message ended with
stopReason=error carrying a provider/gateway failure. Read-only unless --move (moves the cell aside to
<run>.infra-stream-<ts>, the harness naming for infra retries; the lane rerun regenerates it)."""
import json, sys, glob, os, time, shutil, re
PAT = re.compile(r"upstream stream failed|Please retry|stream (was )?(closed|terminated|interrupted)|terminated", re.I)
move = "--move" in sys.argv
roots = [a for a in sys.argv[1:] if not a.startswith("--")] or ["/<home>/repos/results/autonomous-v2", "/<home>/repos/results/autonomous-tools-projects"]
for root in roots:
    for sp in sorted(glob.glob(f"{root}/cells/*/autonomous/r[0-9]/summary.json")):
        s = json.load(open(sp)); cell = os.path.dirname(sp)
        if s.get("status") not in ("failed", "timed_out"):
            continue
        last = None
        for line in open(os.path.join(cell, "artifacts", "session.jsonl"), errors="replace"):
            try: e = json.loads(line)
            except Exception: continue
            if e.get("type") == "message_end" and (e.get("message") or {}).get("role") == "assistant":
                last = e["message"]
        if not last or last.get("stopReason") != "error":
            continue
        msg = str(last.get("errorMessage") or "")
        if not PAT.search(msg):
            continue
        print(cell.split("/cells/")[1], s.get("status"), "tool_calls", s.get("tool_calls"), "|", msg[:120])
        if move:
            dst = f"{cell}.infra-stream-{int(time.time())}"
            shutil.move(cell, dst); print("  moved ->", dst)
