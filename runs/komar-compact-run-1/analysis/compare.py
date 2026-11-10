import json, glob, os, statistics as st, re
R = os.path.expanduser("~/repos/results")
CASES = [("v2","klaw-users-teams-controller-service-approve-new-user-requests","klaw"),("v2","checkstyle-indentation-visit","checkstyle"),
         ("v2","mybatis-caching-executor-query","mybatis"),("tp","lab-calculate-price-handler-handle","lab"),("tp","pdfbox-font-descriptor-is-fixed-pitch","pdfbox")]
SRC = {"komar-compact": {"v2": "komar-compact/v2", "tp": "komar-compact/tp"},
       "komar": {"v2": "komar-v2", "tp": "komar-tools-projects"},
       "autonomous": {"v2": "autonomous-v2", "tp": "autonomous-tools-projects"}}
L = [("S","recall_structure"),("A","recall_io"),("B","recall_state"),("C","recall_strict")]
DBG = ("inspect","resume","set_breakpoint","debug_start","remove_breakpoint")
def session(cell):
    calls, res, seen = {}, [], set()
    for l in open(f"{cell}/artifacts/session.jsonl", errors="replace"):
        try: r = json.loads(l)
        except Exception: continue
        m = r.get("message") or {}
        if m.get("role") == "assistant" and isinstance(m.get("content"), list):
            for c in m["content"]:
                if c.get("type") == "toolCall": calls[c.get("id")] = (c.get("name"), c.get("arguments") or {})
        if m.get("role") == "toolResult" and m.get("toolCallId") not in seen:
            seen.add(m.get("toolCallId"))
            txt = "".join(x.get("text","") for x in m.get("content",[]) if isinstance(x, dict))
            n, a = calls.get(m.get("toolCallId"), (m.get("toolName"), {}))
            res.append((n, a, txt, bool(m.get("isError"))))
    return res
out = []
for g, case, short in CASES:
    for mode, dirs in SRC.items():
        for rnd in ("r1","r2"):
            cell = f"{R}/{dirs[g]}/cells/{case}/{mode}/{rnd}"
            s = json.load(open(cell + "/summary.json")); e = json.load(open(cell + "/eval.json"))
            u = s.get("usage") or {}; q = e.get("rq1") or {}; v = e.get("verify") or {}; k = s.get("komar") or {}
            tr = session(cell)
            cmd = lambda a: (a.get("command") or "") if isinstance(a, dict) else ""
            pth = lambda a: (a.get("path") or "") if isinstance(a, dict) else ""
            B = lambda xs: sum(len(t.encode()) for *_, t, _ in xs)
            view = [x for x in tr if (x[0]=="read" and pth(x[1]).endswith(".komar")) or (x[0]=="bash" and re.match(r"^(sed|cut) ", cmd(x[1])))]
            jq = [x for x in tr if x[0]=="bash" and cmd(x[1]).lstrip().startswith("jq")]
            dbg = [x for x in tr if x[0] in DBG]
            krun = [x for x in tr if x[0]=="bash" and cmd(x[1]).startswith("komar run")]
            trunc_reads = sum(1 for x in view if x[0]=="read" and re.search(r"Use offset=|\[Showing lines", x[2]))
            cont_reads = sum(1 for x in view if x[0]=="read" and (x[1].get("offset") or 1) > 1)
            out.append(dict(case=short, group=g, mode=mode, round=rnd, status=s.get("status"), passes=bool(v.get("passes")),
                **{n: (q.get(f) or 0) for n, f in L},
                input=u.get("input_tokens",0), cache=u.get("cache_read_tokens",0), output=u.get("output_tokens",0),
                turns=u.get("turns",0), tool_calls=s.get("tool_calls",0), cost=round(u.get("cost_usd",0),4), agent_s=s.get("agent_seconds"),
                komar_runs=k.get("runs"), spec_errors=k.get("spec_errors"), hits=k.get("hits"),
                view_written=k.get("view_bytes"), jsonl_written=k.get("output_bytes"),
                data_read=B(view)+B(jq)+B(dbg), view_read=B(view), view_calls=len(view), jq_read=B(jq), jq_calls=len(jq),
                dbg_read=B(dbg), dbg_calls=len(dbg), komar_run_output=B(krun),
                tool_result_total=B(tr), view_truncated_reads=trunc_reads, view_continued_reads=cont_reads,
                sed_cut=sum(1 for x in view if x[0]=="bash"),
                long_line_hint=sum(1 for x in view if "Use bash: sed" in x[2])))
json.dump(out, open(os.path.expanduser("~/repos/results/komar-compact/comparison-cells.json"),"w"), indent=1)
print("case mode r status pass S A B C in cache out turns calls $ runs specerr | dataRead viewRead jqRead dbgRead komarRunOut total | trunc cont sed")
for r in out:
    print(r["case"], r["mode"][:9], r["round"], r["status"][:9], int(r["passes"]), *[round(r[x],2) for x in "SABC"], r["input"], r["cache"], r["output"], r["turns"], r["tool_calls"], r["cost"], r["komar_runs"], r["spec_errors"], "|", r["data_read"], r["view_read"], r["jq_read"], r["dbg_read"], r["komar_run_output"], r["tool_result_total"], "|", r["view_truncated_reads"], r["view_continued_reads"], r["sed_cut"])
print()
for mode in SRC:
    rs = [r for r in out if r["mode"]==mode]
    md = lambda f: st.median(r[f] for r in rs)
    print(f"{mode:13s} pass {sum(r['passes'] for r in rs)}/10 timeouts {sum(r['status']=='timed_out' for r in rs)} S/A/B/C " + "/".join(f"{st.mean(r[x] for r in rs):.2f}" for x in "SABC")
          + f" | sum in {sum(r['input'] for r in rs)} cache {sum(r['cache'] for r in rs)} out {sum(r['output'] for r in rs)} turns {sum(r['turns'] for r in rs)} calls {sum(r['tool_calls'] for r in rs)} ${sum(r['cost'] for r in rs):.2f}"
          + f" | data read sum {sum(r['data_read'] for r in rs)} median {md('data_read')} | tool results total {sum(r['tool_result_total'] for r in rs)}")
