# komar-compact-run-1

- Every cell was generated and evaluated on tag `komar-compact-run-1` = `5a07de8a8d64dfab901f300beb6b36cb1180eaca`
  (branch `komar-compact`): mode `komar-compact` = mode `komar` with Komar
  `03f4534a0fecd75a1e7c097c88fb01922200a92f` (github.com/mxprshn/komar, branch `compact-view`: `komar run` also
  writes a compact view `NAME.komar`; call-site hits record the called method) and only the view for the agent.
  Komar binary sha256 `efc4601b9a9b6cfe1ed3c351cfab028a6c545f70349e3985a047f7d02fdacf26`, agent jar sha256
  `fa863386ef903171b924bb1a15762c2c35926dbac97bd641abed95a6be37c678` (`komar-versions.json`).
- The agent reads `komar/NAME.komar` with the read tool (`prompts/task-komar-compact.txt`, diff against
  `task-komar.txt` in `prompt.diff`); the guard also accepts `sed -n 'A,Bp'` and `cut -c A-B` of one view
  (not in the prompt; the read tool suggests sed for over-long lines); jq and any access to `komar/*.jsonl`
  are refused. Parent commit `2fd5999` adds the mode; `5a07de8` adds the sed/cut forms.
- Settings of komar-run-1b: `OpenAI/gpt-6.1-sol`, thinking medium, 600 s per cell, command_timeout 900,
  Komar run bounded by 300 s, at most 3 runs; evaluation verify, rq1, quality; no PIT.
- Cells: 5 cases x rounds 1, 2 = 10: group 70 klaw-users-teams-controller-service-approve-new-user-requests,
  checkstyle-indentation-visit, mybatis-caching-executor-query; group 31 lab-calculate-price-handler-handle,
  pdfbox-font-descriptor-is-fixed-pitch. 4 workers, one case (both rounds) per worker, never two cells of one
  project at a time (`scripts/worker.sh`, `logs/dispatch.log`). References copied from v2-release and
  tools-tools-projects (identical).
- Run 2026-11-10 11:43-12:18 UTC; 10/10 cells completed or timed out (2 timeouts: mybatis r1, r2), no
  infrastructure failure, no rerun.
- Comparison with the same cases and rounds of komar-run-1b (`komar`) and autonomous-run-1 (`autonomous`):
  `REPORT.md` (Russian), `comparison.json`, `analysis/compare.py`. Two rounds per case: the differences are
  indicative only. komar-compact differs from komar by two changes (the view and callee recording).
- Results are not published as a release. sha256 of the report files in the results directory:
  1bcf097b46cd2128af2cdeafd17977ed31f45ec60d025ffb1010b77627e9aa56  REPORT.md
  05a21a2c36a926b9e2d2897a73e58a54aed14cc5c5f26f938f6070a72b00cfbd  comparison.json
  e371dd23035482fc278157c63640df1a94c9ef1089e3139d41ab2ba297a4ff70  comparison-cells.json
