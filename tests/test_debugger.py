"""The autonomous mode: pi/debugger.ts and its JDI server, the journal, the wiring of the mode.

The node checks need node and an installed Pi package (as tests/test_runner.py); the integration
check also needs JDK 21, Maven and the Maven repository of the subject projects (TESTGEN_M2, default
~/.m2/repository) to build tests/debugger/sample offline. Missing tools skip the check."""
import json
import os
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

from runner import agent as A
from runner import debugger as D
from runner import escape_check as X
from runner.cases import load_case
from runner.config import AGENT_MODES, ROOT, load_config
from runner.prompts import render
from tests.test_runner import PiExtensionsTest, write_config


def node_run(*args: str, timeout: int = 120) -> dict:
    pi = PiExtensionsTest.pi_package()
    if pi is None or shutil.which("node") is None:
        raise unittest.SkipTest("node and an installed Pi package are needed")
    r = subprocess.run(["node", str(ROOT / "tests" / "node" / "debugger.mjs"), str(pi), str(ROOT), *args],
                       capture_output=True, text=True, timeout=timeout)
    if r.returncode != 0:
        raise AssertionError(r.stderr[-3000:])
    return json.loads(r.stdout)


class ModeWiringTest(unittest.TestCase):
    def test_autonomous_mode_prompt_and_argv(self):
        self.assertIn("autonomous", AGENT_MODES)
        case = load_case("jsoup-token-queue-chomp-balanced")
        commands = {n: case.commands[n] for n in ("build", "scenario", "generated_test")}
        p = render(ROOT / "prompts", case, "autonomous", commands)
        self.assertIn("Condition: debugger.", p["task"])
        self.assertIn("run the scenario again", p["task"])
        self.assertLess(len((ROOT / "prompts" / "task-autonomous.txt").read_text()), 700)
        with tempfile.TemporaryDirectory() as d:
            cfg = load_config(write_config(Path(d), paths={"java_home": d, "pi": "/bin/echo", "results": d + "/r"}))
            plain = A.pi_argv(cfg, {"system": "S", "task": "T"}, Path(d))
            debug = A.pi_argv(cfg, {"system": "S", "task": "T"}, Path(d), debugger=True)
        self.assertFalse(any(a.endswith("debugger.ts") for a in plain))
        self.assertTrue(any(a.endswith("pi/debugger.ts") for a in debug))
        self.assertEqual(debug[debug.index("-t") + 1].split(","), list(A.BUILTIN_TOOLS + A.DEBUGGER_TOOLS))
        self.assertEqual(plain[plain.index("-t") + 1].split(","), list(A.BUILTIN_TOOLS))

    def test_journal_summary_and_expressions(self):
        rows = [
            {"tool": "set_breakpoint", "ok": True, "args": {"method": "a.B#c", "on": "exit", "condition": "x > 1"}},
            {"tool": "debug_start", "ok": True, "event": {"kind": "start"}},
            {"tool": "resume", "ok": True, "event": {"kind": "stop", "in_target": True}},
            {"tool": "inspect", "ok": True, "args": {"expr": "this.items.size()"}, "answer_chars": 10},
            {"tool": "set_breakpoint", "ok": True, "args": {"file": "F.java", "code": "x"}},
            {"tool": "remove_breakpoint", "ok": False, "args": {"id": 9}},
            {"tool": "resume", "ok": True, "event": {"kind": "stop", "in_target": False}},
            {"tool": "resume", "ok": True, "event": {"kind": "terminated"}},
            {"tool": "debug_start", "ok": True, "event": {"kind": "start"}},
            {"event": "run_end", "reason": "bash"},
        ]
        s = D.journal_summary(rows)
        self.assertEqual((s["jvm_runs"], s["stops"], s["stops_in_target"], s["plan_revisions"]), (2, 2, 1, 1))
        self.assertEqual(s["calls"], {"debug_start": 2, "inspect": 1, "remove_breakpoint": 1, "resume": 3,
                                      "set_breakpoint": 2})
        self.assertEqual([e for _, e in D.expressions(rows)], ["x > 1", "this.items.size()"])

    def test_escape_check_reads_debugger_expressions(self):
        rows = [{"tool": "inspect", "args": {"expr": "java.nio.file.Files.readString(p)"}},
                {"tool": "inspect", "args": {"expr": "this.seen.get(0)"}},
                {"tool": "set_breakpoint", "args": {"condition": "Runtime.getRuntime() != null"}}]
        found = X.check_debugger(rows)
        self.assertEqual([f["where"] for f in found], ["debug-log 1 (inspect)", "debug-log 3 (set_breakpoint)"])
        self.assertEqual(X.finding_class(found[0]["kind"]), "escape")

    def test_ports_are_leased_once(self):
        with tempfile.TemporaryDirectory() as d:
            cfg = load_config(write_config(Path(d), paths={"java_home": d, "results": d + "/r", "work": d + "/w",
                                                            "debug_ports": "5190-5191"}))
            with D.lease_port(cfg) as a, D.lease_port(cfg) as b:
                self.assertNotEqual(a, b)
                self.assertTrue({a, b} <= {5190, 5191})

    def test_server_source_is_attach_only(self):
        src = D.SERVER_SRC.read_text()
        self.assertIn("be84732", src)
        for gone in ("redefineClasses", "\"launch\"", "ProcessBuilder", "StepRequest", "invokeStaticMethod"):
            self.assertNotIn(gone, src)
        self.assertIn("allowedCall", src)


class DebuggerHelpersTest(unittest.TestCase):
    def test_line_matching_frames_values(self):
        out = node_run()
        self.assertEqual(out["unique"], {"line": 7, "text": "int g() {"})
        self.assertEqual(out["trimmed"]["line"], 4)
        self.assertIn("lines 5, 8", out["duplicate"]["error"])
        self.assertIn("with line", out["duplicate"]["error"])
        self.assertEqual(out["duplicateWithLine"]["line"], 8)
        self.assertIn("the code is at lines 5, 8", out["wrongLine"]["error"])
        self.assertIn("no line", out["missing"]["error"])
        self.assertEqual(out["partial"]["line"], 10)
        self.assertIn("200", out["tooLong"]["error"])
        self.assertIn("single source line", out["multiLine"]["error"])
        self.assertEqual(out["className"], "a.b.X")
        self.assertEqual(out["method"]["argTypes"], ["int", "java.lang.String"])
        self.assertIsNone(out["methodAny"]["argTypes"])
        self.assertEqual(out["ctor"]["method"], "<init>")
        self.assertIn("error", out["badMethod"])
        self.assertEqual(out["frames"], {"shown": ["T.m:3", "U.u0:0", "U.u1:1", "U.u2:2", "U.u3:3"], "more": 3})
        v = out["values"]
        self.assertEqual((v["prim"], v["char"], v["null"]), ("5", "\x27(\x27", "null"))
        self.assertEqual(v["str"], "\"a\\\"b\\n\"")
        self.assertIn("3 of 5000 characters", v["cutStr"])
        self.assertEqual(v["list"], "ArrayList@9(size 25) [\"x\", …+24 more]")
        self.assertEqual(v["obj"], "T@3 {n=1, o=U@4, …+2 more fields}")
        self.assertLessEqual(out["fit"]["length"], 8000)
        self.assertIn("this = T@3 {n=1}", out["stopLocals"])
        self.assertIn("u = U@4 {\n    s=\"zzz", out["stopLocals"])
        self.assertIn("this = T@3 {n=1}", out["stopLocalsCapped"])
        self.assertIn("u = U@4\n", out["stopLocalsCapped"])
        self.assertEqual(out["outside"], [True, True, True, False, False, True])


class DebuggerIntegrationTest(unittest.TestCase):
    """The five tools on tests/debugger/sample: line, entry and exit breakpoints, the exit by an
    exception, inspect with the method whitelist, a second run, the JVM stopped before bash."""

    def test_tools_on_the_sample_project(self):
        java_home = Path(os.environ.get("JAVA_HOME", ""))
        m2 = Path(os.environ.get("TESTGEN_M2", Path.home() / ".m2" / "repository"))
        if not (java_home / "bin" / "javac").is_file() or shutil.which("mvn") is None or not m2.is_dir():
            self.skipTest("JDK 21, Maven and a Maven repository with JUnit 5.10.3 are needed")
        with tempfile.TemporaryDirectory() as d:
            tmp = Path(d)
            ws = tmp / "ws"
            shutil.copytree(ROOT / "tests" / "debugger" / "sample", ws)
            classes = tmp / "server"
            classes.mkdir()
            subprocess.run([str(java_home / "bin" / "javac"), "-Xlint:-options", "-d", str(classes), str(D.SERVER_SRC)],
                           check=True)
            agent = tmp / "agent"
            agent.mkdir()
            config = agent / "debugger.json"
            config.write_text(json.dumps({
                "workspace": str(ws), "java": str(java_home / "bin" / "java"), "server_classes": str(classes),
                "scenario": ["mvn", "-q", "-o", f"-Dmaven.repo.local={m2}", "-Dtest=CounterScenarioTest", "test"],
                "port": 5199, "command_env": ["PATH", "JAVA_HOME", "HOME"], "start_timeout": 180,
                "scenario_log_dir": str(agent), "target": {"class": "demo.Counter", "method": "add"}}))
            os.environ["DEBUGGER_LOG"] = str(agent / "log.jsonl")
            os.environ["DEBUGGER_PIDS"] = str(agent / "pids")
            try:
                out = node_run(str(config), timeout=600)
            finally:
                os.environ.pop("DEBUGGER_LOG")
                os.environ.pop("DEBUGGER_PIDS")
            journal = D.read_journal(agent / "log.jsonl")
        self.assertEqual(out["tools"], list(A.DEBUGGER_TOOLS))
        self.assertIn("call debug_start", out["noSession"])
        self.assertIn("Counter.java:15", out["bpLine"])
        self.assertIn("call set_breakpoint again with line", out["bpDuplicate"])
        self.assertIn("run #1", out["start"])
        self.assertIn("stopped at breakpoint #1 (hit 1)", out["stop1"])
        self.assertIn("word = \"hello\"", out["stop1"])
        self.assertIn("this = Counter@", out["stop1"])
        self.assertIn("total=0", out["stop1"])
        self.assertIn("total=0", out["inspectThis"])
        self.assertEqual(out["inspectSize"], "1")
        self.assertIn("method calls on project classes are not allowed", out["inspectProjectCall"])
        self.assertIn("c = Counter@", out["inspectFrame1"])
        self.assertIn("stopped at exit #2 (hit 1)", out["stop2"])
        self.assertIn("returned = 5", out["stop2"])
        self.assertIn("returned = 10", out["stop4"])
        self.assertIn("stopped at entry #3", out["stop5"])
        self.assertIn("word = null", out["stop5"])
        self.assertIn("exception java.lang.IllegalArgumentException: \"word is null\" thrown out of demo.Counter#add",
                      out["stop6"])
        self.assertIn("terminated: run #1 ended, 6 stops", out["stop7"])
        self.assertIn("run #2", out["restart"])
        self.assertIn("returned = 5", out["stopAfterRestart"])
        self.assertIn("stopped at entry #4 (hit 1) of demo.Counter#twice", out["stopStatic"])
        self.assertIn("x = 2", out["stopStatic"])
        self.assertNotIn("this =", out["stopStatic"])
        self.assertEqual(out["inspectStaticThis"], "static method: no this")
        self.assertIn("call debug_start", out["afterBash"])
        s = D.journal_summary(journal)
        self.assertEqual((s["jvm_runs"], s["stops"], s["stops_in_target"]), (2, 8, 6))
        self.assertTrue(any(r.get("event") == "run_end" and r.get("reason") == "bash" for r in journal))
        for answer in out.values():
            if isinstance(answer, str):
                self.assertLess(len(answer), 8500)


if __name__ == "__main__":
    unittest.main()
