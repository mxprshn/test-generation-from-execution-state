"""Escape check: the rules on generated tests, agent tool calls and guard denials."""
import json
import tempfile
import unittest
from pathlib import Path

from runner import escape_check as X

TEST = "src/test/java/a/FooCarvedTest.java"


def diff(code: str, path: str = TEST) -> str:
    lines = code.splitlines()
    return (f"diff --git a/{path} b/{path}\nnew file mode 100644\n--- /dev/null\n+++ b/{path}\n"
            f"@@ -0,0 +1,{len(lines)} @@\n" + "\n".join("+" + l for l in lines) + "\n")


def kinds(code: str) -> set[str]:
    return {f["kind"] for f in X.check_code(code, TEST)}


def cell_with(tmp: Path, code: str | None = None, calls: list[dict] | None = None,
              denied: list[dict] | None = None) -> Path:
    cell = tmp / "cell"
    (cell / "artifacts").mkdir(parents=True)
    if code is not None:
        (cell / "result.diff").write_text(diff(code))
    if calls is not None:
        events = [{"type": "message_end", "message": {"role": "assistant", "content": [
            {"type": "toolCall", "id": f"c{i}", "name": c["name"], "arguments": c["arguments"]}
            for i, c in enumerate(calls)]}}]
        (cell / "artifacts" / "session.jsonl").write_text("\n".join(json.dumps(e) for e in events))
    if denied is not None:
        (cell / "artifacts" / "denied.jsonl").write_text("\n".join(json.dumps(d) for d in denied))
    return cell


CLEAN = """
package a;
import java.nio.file.*;
class FooCarvedTest {
  // reads src/test/resources/input.txt; the recording in carving/call-dumps.json is not used
  @Test void t() throws Exception {
    String text = Files.readString(Paths.get("src/test/resources/a/input.txt"));
    var url = getClass().getResource("/a/input.txt");
    Node parent = node.getParent();
    assertEquals("x", new Foo().bar(text));
  }
}
"""


class CodeRules(unittest.TestCase):
    def test_clean_test(self):
        self.assertEqual(kinds(CLEAN), set())

    def test_reads_call_dump(self):
        self.assertIn("reads mode input", kinds('String s = Files.readString(Path.of("carving/call-dumps.json"));'))

    def test_reads_preliminary_test(self):
        self.assertIn("reads mode input", kinds('Path p = Path.of("carving", "PreliminaryTest.java.txt");'))

    def test_comment_mentioning_input_is_not_flagged(self):
        self.assertEqual(kinds("/** Replays the executions in carving/call-dumps.json. */ class T {}"), set())

    def test_string_with_slashes_is_not_a_comment(self):
        self.assertIn("reads mode input", kinds('String u = "http://x"; Path p = Path.of("carving/call-dumps.json");'))

    def test_absolute_path(self):
        self.assertIn("absolute path", kinds('new File("/home/user/results/x.json")'))

    def test_leaves_working_directory(self):
        self.assertIn("leaves working directory", kinds('Path.of("../other/file")'))

    def test_walks_up_from_working_directory(self):
        code = ('Path d = Paths.get("").toAbsolutePath();\n'
                'while (d != null && !Files.exists(d.resolve("x"))) { d = d.getParent(); }')
        self.assertIn("walks up from the working directory", kinds(code))

    def test_getparent_on_other_objects_is_not_flagged(self):
        self.assertEqual(kinds("DetailAST p = ast.getParent(); Element e = el.getParent();"), set())

    def test_environment(self):
        self.assertIn("environment or user location", kinds('String h = System.getenv("HOME");'))
        self.assertIn("environment or user location", kinds('String h = System.getProperty("user.home");'))

    def test_process(self):
        self.assertIn("runs a process", kinds('new ProcessBuilder("ls").start();'))
        self.assertIn("runs a process", kinds('Runtime.getRuntime().exec("ls");'))

    def test_listing_from_working_directory(self):
        self.assertIn("lists the file system", kinds('Files.walk(Paths.get("")).count();'))

    def test_path_inside_workspace_is_not_flagged(self):
        self.assertEqual(kinds('Path p = Path.of("src/test/resources/../resources/a.txt");'), set())


class ToolCalls(unittest.TestCase):
    ROOTS = ["/srv/exp/results", "/srv/exp/repo/cases", "/home/u"]

    def check(self, *calls):
        return {f["kind"] for f in X.check_tool_calls(list(calls), self.ROOTS)}

    def test_relative_paths_are_clean(self):
        self.assertEqual(self.check({"name": "read", "arguments": {"path": "src/main/java/a/Foo.java"}},
                                    {"name": "grep", "arguments": {"pattern": "foo", "path": "src"}}), set())

    def test_absolute_path(self):
        self.assertIn("tool path outside the workspace",
                      self.check({"name": "read", "arguments": {"path": "/srv/exp/results/x.json"}}))

    def test_parent_escape(self):
        self.assertIn("tool path outside the workspace", self.check({"name": "find", "arguments": {"path": "../.."}}))

    def test_harness_root_in_command(self):
        self.assertIn("tool argument names a harness location",
                      self.check({"name": "bash", "arguments": {"command": "cat /srv/exp/repo/cases/x/case.yaml"}}))


class Cell(unittest.TestCase):
    def test_clean_cell(self):
        with tempfile.TemporaryDirectory() as t:
            cell = cell_with(Path(t), CLEAN, [{"name": "read", "arguments": {"path": "pom.xml"}}], [])
            self.assertEqual(X.check_cell(cell, ["/srv/exp"]), {"status": "clean", "findings": []})

    def test_guard_denial_flags(self):
        with tempfile.TemporaryDirectory() as t:
            cell = cell_with(Path(t), CLEAN, [], [{"kind": "read", "path": "/srv/exp/cases", "reason": "outside"}])
            r = X.check_cell(cell, ["/srv/exp"])
            self.assertEqual(r["status"], "flagged")
            self.assertEqual(r["findings"][0]["kind"], "denied by the guard")

    def test_test_reading_dump_flags(self):
        with tempfile.TemporaryDirectory() as t:
            cell = cell_with(Path(t), 'var s = Files.readString(Path.of("carving/call-dumps.json"));')
            r = X.check_cell(cell, [])
            self.assertEqual(r["status"], "flagged")
            self.assertTrue(r["findings"][0]["where"].startswith(TEST + ":"))

    def test_only_added_lines_are_checked(self):
        d = (f"--- a/{TEST}\n+++ b/{TEST}\n@@ -1,2 +1,2 @@\n-String h = System.getenv(\"HOME\");\n+String h = \"x\";\n")
        self.assertEqual(X.added_code(d), {TEST: 'String h = "x";'})


if __name__ == "__main__":
    unittest.main()
