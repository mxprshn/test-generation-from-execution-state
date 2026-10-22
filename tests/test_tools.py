"""Adapter of the external test generators (runner/tools): what does not need the tools installed."""
import json
import tempfile
import unittest
from pathlib import Path

import yaml

from evaluation import inputs as I
from evaluation import quality as Q
from runner import cell as G
from runner.cases import load_case
from runner.config import AGENT_MODES, NO_AGENT_MODES, TOOL_MODES, load_config
from runner.tools import common as C
from runner.tools import mtc as MTC
from runner.tools import pankti as P
from tests.test_runner import write_config

GENERATED = """package org.jsoup.nodes;

import java.io.File;
import com.thoughtworks.xstream.XStream;
import org.junit.jupiter.api.Test;
import java.util.List;

public class TestElementPanktiGen {
    XStream xStream = new XStream();

    private <T> T deserializeObjectFromFile(String serializedObjectFilePath) throws Exception {
        File serializedObjectFile = new File(getClass().getClassLoader().getResource(serializedObjectFilePath).getFile());
        return (T) xStream.fromXML(serializedObjectFile);
    }

    @Test
    public void testHasClass1() throws Exception {
        org.jsoup.nodes.Element receivingObject = deserializeObjectFromFile("org.jsoup.nodes.Element.hasClass1-receiving.xml");
        String returnedObjectStr = "<boolean>true</boolean>";
        boolean expectedObject = deserializeObjectFromString(returnedObjectStr);
        Assertions.assertEquals(true, receivingObject.hasClass("x"));
    }
}
"""


class ModesTest(unittest.TestCase):
    def test_tool_modes_are_no_agent_modes(self):
        self.assertEqual(TOOL_MODES, ("pankti", "rick", "microtestcarver"))
        for m in TOOL_MODES:
            self.assertIn(m, NO_AGENT_MODES)
            self.assertNotIn(m, AGENT_MODES)

    def test_empty_modes_means_no_agent_mode(self):
        with tempfile.TemporaryDirectory() as d:
            self.assertEqual(load_config(write_config(Path(d), modes=[], once_modes=["pankti"])).modes, [])
            self.assertEqual(load_config(write_config(Path(d))).modes, list(AGENT_MODES))

    def test_config_defaults_and_external_dir(self):
        with tempfile.TemporaryDirectory() as d:
            cfg = load_config(write_config(Path(d), once_modes=list(TOOL_MODES), modes=[]))
            self.assertEqual(cfg.tool_timeout, 2400)
            self.assertEqual(cfg.external_tools_dir, cfg.tools_dir / "external")
            cfg = load_config(write_config(Path(d), paths={"java_home": d, "external_tools": str(Path(d) / "x")},
                                           tool_timeout=60))
            self.assertEqual((cfg.external_tools_dir, cfg.tool_timeout), (Path(d) / "x", 60))

    def test_generation_inputs_of_tool_and_other_modes(self):
        """A tool cell hashes the patches and versions of the tools and its limits; the other modes keep
        their inputs (no tool field, tool_timeout not in their config hash)."""
        with tempfile.TemporaryDirectory() as d:
            case = load_case("jsoup-element-has-class")
            paths = {"java_home": d, "external_tools": str(Path(d) / "ext")}
            a = load_config(write_config(Path(d), tool_timeout=100, paths=paths))
            b = load_config(write_config(Path(d), tool_timeout=200, paths=paths))
            self.assertNotEqual(I.generation_inputs(a, case, "pankti")["config"],
                                I.generation_inputs(b, case, "pankti")["config"])
            for mode in ("baseline", "dump", "rendered", "rendered-raw", "autonomous"):
                self.assertEqual(I.generation_inputs(a, case, mode), I.generation_inputs(b, case, mode), mode)
            tool = I.generation_inputs(a, case, "rick")
            self.assertIsNone(tool["prompts"])
            self.assertIsNotNone(tool["mode_input"])
            a.external_tools_dir.mkdir(parents=True)
            (a.external_tools_dir / "versions.json").write_text(json.dumps({"pankti": {"commit": "x"}}))
            self.assertNotEqual(I.generation_inputs(a, case, "rick")["mode_input"], tool["mode_input"])

    def test_missing_tools_is_an_infrastructure_failure(self):
        s = {"status": "failed", "phase": "tool", "error": "ToolSetupError: external tools are not installed"}
        self.assertTrue(G.is_infra_failure(s, Path("/nonexistent")))
        s = {"status": "failed", "phase": "result", "error": "no test: target method has no mockable invocations"}
        self.assertFalse(G.is_infra_failure(s, Path("/nonexistent")))


class PanktiSourceTest(unittest.TestCase):
    def test_fix_source_renames_imports_and_drops_unused(self):
        src, added = P.fix_source(GENERATED, "TestElementPanktiGen", "ElementHasClassCarvedTest")
        self.assertIn("public class ElementHasClassCarvedTest", src)
        self.assertNotIn("TestElementPanktiGen", src)
        self.assertIn("import org.junit.jupiter.api.Assertions;", src)
        self.assertEqual(added, ["org.junit.jupiter.api.Assertions"])
        self.assertNotIn("import java.util.List;", src)
        self.assertNotIn("expectedObject", src)
        self.assertNotIn("returnedObjectStr", src)
        self.assertIn("\n    <T> T deserializeObjectFromFile(", src)
        self.assertIn('deserializeObjectFromFile("org.jsoup.nodes.Element.hasClass1-receiving.xml")', src)

    def test_rick_mocks_and_matchers_get_imports(self):
        src = ("package a.b;\n\nimport org.mockito.Mockito;\n\npublic class T {\n"
               "  void t() { c.d.Reader mockReader = Mockito.mock(Reader.class); when(x.f(any(), eq(1))); }\n}\n")
        out, added = P.add_missing_imports(src)
        self.assertIn("c.d.Reader", added)
        self.assertIn("static org.mockito.ArgumentMatchers.*", added)
        self.assertIn("import c.d.Reader;", out)

    def test_select_target_row_matches_erased_parameters(self):
        with tempfile.TemporaryDirectory() as d:
            f = Path(d) / "extracted-methods-x.csv"
            f.write_text("visibility,parent-FQN,method-name,param-list,has-mockable-invocations\n"
                         "public,a.B,m,[java.util.List<java.lang.String>],false\n"
                         "public,a.B,m,[],true\n"
                         "public,a.B,m,\"[int, char[]]\",true\n")
            row = P.select_target_row(f, {"class": "a.B", "method": "m", "signature": "(java.util.List<java.lang.String>) -> void"})
            self.assertEqual(row["has-mockable-invocations"], "false")
            row = P.select_target_row(f, {"class": "a.B", "method": "m", "signature": "(int, char[]) -> void"})
            self.assertEqual(row["param-list"], "[int, char[]]")
            self.assertIsNone(P.select_target_row(f, {"class": "a.B", "method": "m", "signature": "(long) -> void"}))


class PlacementTest(unittest.TestCase):
    def _ctx(self, ws: Path, case_id: str) -> C.ToolContext:
        return C.ToolContext(cfg=None, case=load_case(case_id), workspace=ws, work=ws.parent / "work", env={},
                             scenario=["mvn", "-q", "test"])

    def test_pankti_place_outputs_installs_class_objects_and_dependencies(self):
        with tempfile.TemporaryDirectory() as d:
            ws = Path(d) / "ws"
            (ws / "src/test/java").mkdir(parents=True)
            (ws / "pom.xml").write_text("<project>\n<modelVersion>4.0.0</modelVersion>\n<dependencies>\n"
                                        "    <dependency><artifactId>junit-jupiter</artifactId></dependency>\n"
                                        "</dependencies>\n</project>\n")
            out = Path(d) / "out"
            (out / "generated/org/jsoup/nodes").mkdir(parents=True)
            (out / "generated/org/jsoup/nodes/TestElementPanktiGen.java").write_text(GENERATED)
            (out / "object-data").mkdir()
            (out / "object-data/org.jsoup.nodes.Element.hasClass1-receiving.xml").write_text("<o/>")
            (out / "object-data/unused-receiving.xml").write_text("<o/>")
            ctx = self._ctx(ws, "jsoup-element-has-class")
            placed = P.place_outputs(ctx, "pankti", out)
            self.assertTrue(placed["generated"])
            test = ws / ctx.case.output_test_file
            self.assertTrue(test.is_file())
            self.assertIn("class ElementHasClassCarvedTest", test.read_text())
            self.assertIn("src/test/resources/org.jsoup.nodes.Element.hasClass1-receiving.xml", placed["support_files"])
            self.assertNotIn("src/test/resources/unused-receiving.xml", placed["support_files"])
            self.assertIn("pom.xml", placed["support_files"])
            self.assertIn("<artifactId>xstream</artifactId>", (ws / "pom.xml").read_text())
            self.assertEqual(placed["object_files"], 1)

    def test_no_generated_class(self):
        with tempfile.TemporaryDirectory() as d:
            ws = Path(d) / "ws"
            ws.mkdir()
            (Path(d) / "out/generated").mkdir(parents=True)
            self.assertEqual(P.place_outputs(self._ctx(ws, "jsoup-element-has-class"), "pankti", Path(d) / "out"),
                             {"generated": False, "support_files": []})

    def test_mtc_fix_source(self):
        src = ("package org.jsoup.nodes;\n\nimport org.jsoup.nodes.Element;\nimport java.util.Map;\n\n"
               "public class ElementTest {\n  Element e;\n}\n")
        out = MTC.fix_source(src, "org.jsoup.nodes.Element", "ElementHasClassCarvedTest")
        self.assertIn("public class ElementHasClassCarvedTest", out)
        self.assertNotIn("import org.jsoup.nodes.Element;", out)
        self.assertNotIn("import java.util.Map;", out)

    def test_record_argv_puts_the_agent_in_the_forked_jvm(self):
        with tempfile.TemporaryDirectory() as d:
            ctx = C.ToolContext(cfg=None, case=load_case("jsoup-element-has-class"), workspace=Path(d),
                                work=Path(d), env={}, scenario=["mvn", "-q", "-o", "-Dtest=X", "test"])
            argv = C.record_argv(ctx, Path("/jdk17"), "-javaagent:/g.jar", ["-Djacoco.skip"])
            self.assertEqual(argv[0], "mvn")
            self.assertNotIn("-q", argv)
            self.assertIn("-Djvm=/jdk17/bin/java", argv)
            self.assertIn("-Dmaven.surefire.debug=-javaagent:/g.jar", argv)
            self.assertEqual(argv[-3:], ["-o", "-Dtest=X", "test"])


class ReasonTest(unittest.TestCase):
    def test_first_exception_after_mark(self):
        with tempfile.TemporaryDirectory() as d:
            log = Path(d) / "tool.log"
            log.write_text("old java.lang.IllegalStateException: before\n")
            mark = log.stat().st_size
            with log.open("a") as f:
                f.write("[INFO] x\njava.lang.NullPointerException: Cannot invoke \"a.b()\"\n\tat x.Y.z(Y.java:1)\n")
            self.assertEqual(C.first_exception(log, mark), 'java.lang.NullPointerException: Cannot invoke "a.b()"')
            self.assertIsNone(C.first_exception(Path(d) / "missing.log"))


class QualityOfToolCellsTest(unittest.TestCase):
    def test_added_by_file(self):
        diff = ("diff --git a/pom.xml b/pom.xml\n+++ b/pom.xml\n+<dep/>\n"
                "diff --git a/src/test/java/A.java b/src/test/java/A.java\n+++ b/src/test/java/A.java\n+class A {}\n"
                "diff --git a/src/test/resources/o.xml b/src/test/resources/o.xml\n+++ b/src/test/resources/o.xml\n+<o/>\n")
        files = Q.added_by_file(diff)
        self.assertEqual(files, {"pom.xml": "<dep/>", "src/test/java/A.java": "class A {}",
                                 "src/test/resources/o.xml": "<o/>"})


if __name__ == "__main__":
    unittest.main()
