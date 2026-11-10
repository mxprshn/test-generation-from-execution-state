# komar-compact-run-1

- Tag `komar-compact-run-1` = `5a07de8` (branch komar-compact): mode komar-compact, Komar 03f4534 (compact-view; .komar view + callee of call-site hits), binary sha256 efc4601b9a9b, agent jar sha256 fa863386ef90.
  The agent reads only komar/NAME.komar (read; sed -n 'A,Bp' / cut -c A-B of one view accepted by the guard, not in the prompt); no jq, no access to *.jsonl.
- Settings of komar-run-1b: OpenAI/gpt-6.1-sol, thinking medium, 600 s, command_timeout 900, Komar run 300 s, at most 3 runs; verify/rq1/quality, no PIT.
- Cells: 5 cases x r1,r2 = 10 (group 70: klaw approve-new-user-requests, checkstyle indentation-visit, mybatis caching-executor-query; group 31: lab calculate-price, pdfbox font-descriptor-is-fixed-pitch). 4 workers, one case (both rounds) per worker, never two cells of one project at a time (scripts/worker.sh).
- Results: ~/repos/results/komar-compact/{v2,tp}; references copied (identical to v2-release / tools-tools-projects).
- Fairness: completed cells are never deleted or rerun; infrastructure failures may be rerun, documented here, the old cell kept aside.

## Checks and incidents
- 2026-11-10T11:45:36+00:00 start: 4 workers (klaw, checkstyle, lab, mybatis; pdfbox queued), model probe 200 on both keys.
- 2026-11-10T12:25:23+00:00 end: 10/10 cells, all workers exit 0 (11:43-12:18 UTC). 2 timeouts (mybatis-caching-executor-query r1, r2; agent 600 s) kept as results. No infra failures, no reruns, no orphan komar processes. Report: results/komar-compact/REPORT.md, comparison.json.
