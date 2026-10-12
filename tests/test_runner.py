"""Cases, prompts, Pi command line and the cell logic that does not need a project checkout."""
import json
import os
import re
import subprocess
import tempfile
import unittest
from pathlib import Path

import yaml

from runner import agent as A
from runner import cell as G
from runner import workspace as W
from runner.cases import case_rows, load_case, load_projects
from runner.config import AGENT_MODES, ROOT, load_config
from runner.prompts import EXECUTION_INPUT, render


def write_config(tmp: Path, **extra) -> Path:
    data = {"provider": {"name": "p", "base_url": "https://example.invalid/v1", "api_key_env": "TEST_KEY"},
            "model": "m", "thinking": "medium",
            "paths": {"projects": str(tmp / "projects"), "results": str(tmp / "results"), "java_home": str(tmp)},
            **extra}
    path = tmp / "config.yaml"
    path.write_text(yaml.dump(data))
    return path


class CasesTest(unittest.TestCase):
    def test_every_case_loads_and_is_consistent(self):
        rows = case_rows()
        self.assertEqual(len(rows), 70)
        projects = load_projects()
        for row in rows:
            case = load_case(row["id"])
            self.assertEqual((case.project, case.group), (row["project"], row["group"]))
            self.assertIn(case.project, projects)
            test_class = case.scenario_test.split("#")[0].rsplit(".", 1)[-1]
            self.assertEqual(Path(case.scenario_source).stem, test_class, case.id)
            self.assertEqual((row["dump"] == "yes"), case.dump is not None, case.id)
            self.assertEqual((row["preliminary_test"] == "yes"), case.preliminary_test is not None, case.id)
            self.assertIn(case.group, ("direct", "indirect"))

    def test_repository_has_no_local_paths(self):
        pattern = re.compile(r"/Users/|/home/|/private/var/|/var/folders/|\.\./dataset")
        for path in (ROOT / "cases").rglob("*.yaml"):
            self.assertIsNone(pattern.search(path.read_text()), path)


class PromptsTest(unittest.TestCase):
    def test_neutral_scenario_and_execution_input(self):
        case = load_case("mybatis-caching-executor-query")
        commands = {n: case.commands[n] for n in ("build", "scenario", "generated_test")}
        for mode in AGENT_MODES:
            p = render(ROOT / "prompts", case, mode, commands)
            self.assertIn("Driving scenario: test org.apache.ibatis.submitted.cache.CacheTest#testplan1\n", p["task"])
            self.assertIn("Scenario test source: src/test/java/org/apache/ibatis/submitted/cache/CacheTest.java\n",
                          p["task"])
            self.assertIn("Repository: mybatis-3 at commit ", p["task"])
            self.assertNotIn("{{", p["task"] + p["system"])
            if mode != "baseline":
                self.assertIn(EXECUTION_INPUT[mode], p["task"])

    def test_effective_system_prompt_matches_pi(self):
        self.assertEqual(A.effective_system_prompt("S", Path("/w/x")), "S\nCurrent working directory: /w/x")

    def test_baseline_block_states_only_the_absence_of_data(self):
        text = (ROOT / "prompts" / "task-baseline.txt").read_text()
        self.assertEqual(text, "Condition: source code only.\n\nNo execution data is provided.\n")

    def test_dump_prompt_describes_split_files(self):
        text = (ROOT / "prompts" / "task-dump.txt").read_text()
        self.assertIn("one JSON file per recorded execution in {{execution_input}}, in recording order", text)
        self.assertEqual(EXECUTION_INPUT["dump"], "carving/executions/")

    def test_rendered_prompt_allows_checks_on_recorded_values(self):
        text = (ROOT / "prompts" / "task-rendered.txt").read_text()
        self.assertIn("You may add checks only on values already recorded", text)
        self.assertIn("do not remove checks", text)


class ConfigAndPiTest(unittest.TestCase):
    def test_config_defaults_and_pi_argv(self):
        with tempfile.TemporaryDirectory() as d:
            tmp = Path(d)
            cfg = load_config(write_config(tmp, paths={"java_home": d, "pi": "/bin/echo",
                                                        "results": str(tmp / "r")}))
            self.assertEqual((cfg.timeout, cfg.rounds, cfg.run_id(2)), (600, [1], "r2"))
            argv = A.pi_argv(cfg, {"system": "S", "task": "T"}, tmp / "s")
            for flag in ("--no-extensions", "--no-skills", "--no-context-files", "--no-prompt-templates"):
                self.assertIn(flag, argv)
            self.assertEqual(argv[argv.index("--provider") + 1], "p")
            self.assertEqual(argv[-1], "T")
            self.assertTrue(argv[argv.index("-e") + 1].endswith("pi/provider.ts"))

    def test_agent_env_passes_only_the_selected_key(self):
        with tempfile.TemporaryDirectory() as d:
            cfg = load_config(write_config(Path(d)))
            os.environ["TEST_KEY"] = "secret"
            try:
                env = A.agent_env(cfg, "TEST_KEY")
            finally:
                os.environ.pop("TEST_KEY")
            self.assertEqual(env["TESTGEN_API_KEY"], "secret")
            self.assertNotIn("TEST_KEY", env)
            self.assertEqual(env["TESTGEN_BASE_URL"], "https://example.invalid/v1")

    def test_command_env_is_an_allowlist(self):
        with tempfile.TemporaryDirectory() as d:
            cfg = load_config(write_config(Path(d), env={"MAVEN_OPTS": "-Xmx1g"}))
            saved = dict(os.environ)
            os.environ.update({"TEST_KEY": "secret", "OPENAI_API_KEY": "s2", "AWS_SECRET_ACCESS_KEY": "s3",
                               "LC_ALL": "C", "TESTGEN_API_KEY": "s4"})
            try:
                env = cfg.command_env()
            finally:
                os.environ.clear()
                os.environ.update(saved)
            for name in ("TEST_KEY", "OPENAI_API_KEY", "AWS_SECRET_ACCESS_KEY", "TESTGEN_API_KEY"):
                self.assertNotIn(name, env)
            self.assertEqual(env["LC_ALL"], "C")
            self.assertEqual(env["MAVEN_OPTS"], "-Xmx1g")
            self.assertTrue(env["PATH"].startswith(str(Path(d) / "bin")))
            self.assertNotIn("secret", json.dumps(env))

    def test_work_dir_outside_results_and_repository(self):
        with tempfile.TemporaryDirectory() as d:
            cfg = load_config(write_config(Path(d)))
            work = cfg.work_dir
            self.assertNotIn(cfg.results_dir, [work, *work.parents])
            self.assertNotIn(ROOT, [work, *work.parents])
            from runner.config import ConfigError
            with self.assertRaises(ConfigError):
                load_config(write_config(Path(d), paths={"java_home": d, "results": str(Path(d) / "r"),
                                                         "work": str(Path(d) / "r" / "w")}))

    def test_neutral_directory_names(self):
        from runner.config import neutral_name
        name = neutral_name("jsoup-normalise-whitespace", "dump", "r1")
        self.assertRegex(name, r"^w[0-9a-f]{12}$")
        self.assertNotEqual(name, neutral_name("jsoup-normalise-whitespace", "baseline", "r1"))


class CellLogicTest(unittest.TestCase):
    def test_retarget_preliminary_test(self):
        src = "package a.b;\nclass XReproducedTests { XReproducedTests x; }"
        out = G.retarget_test_class(src, "m/src/test/java/org/x/YCarvedTest.java")
        self.assertIn("package org.x;", out)
        self.assertIn("class YCarvedTest { YCarvedTest x; }", out)

    def test_eval_commands_skip_format_checks_agent_commands_unchanged(self):
        from runner.cases import eval_command
        case = load_case("klaw-util-methods-get-user-name")
        for name in ("build", "generated_test", "scenario"):
            argv = eval_command(case, name)
            self.assertEqual(argv[0], case.commands[name][0])
            self.assertIn("-Dspotless.check.skip=true", argv)
            self.assertNotIn("-Dspotless.check.skip=true", case.commands[name])
            self.assertEqual([a for a in argv if not a.startswith("-Dspotless")], case.commands[name])
        jsoup = load_case("jsoup-normalise-whitespace")
        self.assertEqual(eval_command(jsoup, "build"), jsoup.commands["build"])
        self.assertFalse(hasattr(G, "spotless_apply_argv"))

    def test_infrastructure_failures(self):
        cell = Path("/nonexistent")
        self.assertTrue(G.is_infra_failure({"status": "failed", "ended_on_gateway_error": True}, cell))
        self.assertTrue(G.is_infra_failure({"status": "timed_out", "gateway_errors": 3}, cell))
        self.assertFalse(G.is_infra_failure({"status": "timed_out", "gateway_errors": 1}, cell))
        self.assertTrue(G.is_infra_failure({"status": "failed", "phase": "scenario-check"}, cell))
        self.assertFalse(G.is_infra_failure({"status": "failed", "phase": "agent", "tool_calls": 5,
                                             "agent_exit_code": 1}, cell))
        self.assertFalse(G.is_infra_failure({"status": "completed"}, cell))
        self.assertFalse(G.is_infra_failure({"status": "method_failed"}, cell))

    def test_session_usage_and_gateway_errors(self):
        events = [
            {"type": "message_end", "message": {"role": "assistant", "usage": {"input": 10, "output": 2,
                                                                                "cost": {"total": 0.5}}}},
            {"type": "message_end", "message": {"role": "assistant", "stopReason": "error",
                                                "errorMessage": "502 upstream_empty_response"}},
        ]
        u = A.usage(events)
        self.assertEqual((u["turns"], u["input_tokens"], u["cost_usd"]), (2, 10, 0.5))
        self.assertEqual(A.gateway_errors(events), (1, True))

    def test_final_failed_commands(self):
        entries = [{"event": "end", "name": "build", "succeeded": False},
                   {"event": "end", "name": "build", "succeeded": True},
                   {"event": "end", "name": "scenario", "succeeded": False}]
        self.assertEqual(G.final_failed_commands(entries), ["scenario"])


class WorkspaceTest(unittest.TestCase):
    def test_split_dump_is_lossless_and_keeps_duplicates(self):
        with tempfile.TemporaryDirectory() as d:
            src = Path(d) / "call-dumps.json"
            data = [{"calls": [{"a": 1}]}, {"calls": []}, {"calls": [{"a": 1}]}]
            src.write_text(json.dumps(data))
            names = W.split_dump(src, Path(d) / "executions")
            self.assertEqual(names, ["0001.json", "0002.json", "0003.json"])
            self.assertEqual(W.join_dump(Path(d) / "executions"), data)

    def test_split_dump_on_all_cases(self):
        with tempfile.TemporaryDirectory() as d:
            n = 0
            for row in case_rows():
                case = load_case(row["id"])
                if case.dump is None:
                    continue
                out = Path(d) / case.id
                W.split_dump(case.dump, out)
                self.assertEqual(W.join_dump(out), json.loads(case.dump.read_text()), case.id)
                n += 1
            self.assertGreater(n, 50)

    def test_split_dump_widens_numbers_past_9999(self):
        with tempfile.TemporaryDirectory() as d:
            src = Path(d) / "call-dumps.json"
            src.write_text(json.dumps([{}] * 10000))
            names = W.split_dump(src, Path(d) / "e")
            self.assertEqual((names[0], names[-1]), ("00001.json", "10000.json"))

    def test_export_hide_and_diff(self):
        with tempfile.TemporaryDirectory() as d:
            repo = Path(d) / "repo"
            repo.mkdir()
            (repo / "src").mkdir()
            (repo / "src" / "A.java").write_text("class A {}\n")
            (repo / "src" / "ATest.java").write_text("class ATest {}\n")
            for args in (["init", "-q"], ["add", "."], ["-c", "user.name=t", "-c", "user.email=t@t", "commit", "-qm", "x"]):
                subprocess.run(["git", "-C", str(repo), *args], check=True)
            commit = W.git(repo, "rev-parse", "HEAD").stdout.strip()
            ws = W.export_commit(repo, commit, Path(d) / "ws")
            self.assertEqual(W.hide_paths(ws, ["src/ATest.java", "src/Missing.java"]), ["src/ATest.java"])
            before = W.snapshot(ws)
            (ws / "src" / "ACarvedTest.java").write_text("class ACarvedTest {}\n")
            (ws / "target").mkdir()
            (ws / "target" / "x.class").write_text("")
            self.assertEqual(W.unexpected_changes(before, W.snapshot(ws), {"src/ACarvedTest.java"}), [])
            diff = W.result_diff(repo, commit, {"src/ACarvedTest.java": ws / "src" / "ACarvedTest.java"},
                                 Path(d) / "result")
            self.assertIn("+class ACarvedTest {}", diff)


class PiExtensionsTest(unittest.TestCase):
    def test_guard_confines_reads_writes_and_commands(self):
        source = (ROOT / "pi" / "guard.ts").read_text()
        for tool in ('"read"', '"grep"', '"find"', '"ls"', '"write"', '"edit"', '"bash"'):
            self.assertIn(f"event.toolName === {tool}", source)
        self.assertIn("realpathSync", source)

    def test_provider_reads_only_environment(self):
        source = (ROOT / "pi" / "provider.ts").read_text()
        self.assertIn("TESTGEN_BASE_URL", source)
        self.assertIn("delete process.env.TESTGEN_API_KEY", source)
        code = "\n".join(line for line in source.splitlines() if not line.lstrip().startswith(("*", "/*", "//")))
        self.assertNotRegex(code, r"https?://")

    @staticmethod
    def pi_package() -> Path | None:
        import shutil
        candidates = [ROOT / ".tools" / "pi" / "lib" / "node_modules" / "@earendil-works" / "pi-coding-agent"]
        pi = shutil.which("pi")
        if pi:
            p = Path(pi).resolve()
            for parent in p.parents:
                if (parent / "package.json").is_file() and parent.name == "pi-coding-agent":
                    candidates.append(parent)
                    break
        return next((c for c in candidates if (c / "node_modules" / "jiti").is_dir()), None)

    def test_guard_and_provider_behaviour(self):
        """The real extension code under node: command matching, environment of commands, key escaping."""
        import shutil
        pi = self.pi_package()
        if pi is None or shutil.which("node") is None:
            self.skipTest("node and an installed Pi package are needed")
        r = subprocess.run(["node", str(ROOT / "tests" / "node" / "extensions.mjs"), str(pi), str(ROOT)],
                           capture_output=True, text=True, timeout=120)
        self.assertEqual(r.returncode, 0, r.stderr[-2000:])
        out = json.loads(r.stdout)
        self.assertEqual(out["exact"], "build")
        self.assertEqual(out["extraSpaces"], "build")
        self.assertEqual(out["quotedHash"], "scenario")
        self.assertEqual(out["unquotedHash"], "scenario")
        for bypass in ("bypassRedirect", "redirect", "background", "chained", "piped", "newline", "substitution"):
            self.assertIsNone(out[bypass], bypass)
        self.assertEqual(out["env"], {"PATH": "/bin", "HOME": "/h"})
        self.assertEqual(out["literal"], ["plain-key", "a$$B", "$!rm"])


if __name__ == "__main__":
    unittest.main()
