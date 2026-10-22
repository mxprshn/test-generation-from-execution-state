#!/usr/bin/env bash
# Start the live status page of the full run (idempotent): dashboard.sh [stop|status] [extra args of dashboard.py]
# Serves on 127.0.0.1:8780 only. Open it from your machine through a tunnel:
#   ssh -N -L 8780:127.0.0.1:8780 <user>@<host>     then  http://localhost:8780
FR=/<home>/repos/full-run
PID=$FR/logs/dashboard.pid; LOG=$FR/logs/dashboard.log
mkdir -p "$FR/logs"
running() { [ -f "$PID" ] && kill -0 "$(cat "$PID")" 2>/dev/null; }
case "${1:-start}" in
  stop)   if running; then kill "$(cat "$PID")"; echo stopped; else echo "not running"; fi; rm -f "$PID"; exit 0 ;;
  status) if running; then echo "running, pid $(cat "$PID")"; else echo "not running"; fi; exit 0 ;;
  start)  shift || true ;;
  *)      ;;
esac
if running; then echo "already running, pid $(cat "$PID")"; exit 0; fi
nohup python3 "$FR/dashboard.py" "$@" > "$LOG" 2>&1 &
echo $! > "$PID"
sleep 1
if running; then echo "started, pid $(cat "$PID"): $(head -1 "$LOG")"; else echo "failed:"; cat "$LOG"; rm -f "$PID"; exit 1; fi
