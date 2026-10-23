"""Post-hoc code-style analysis (evaluation/code_style.py) on tiny fixtures."""
import math
import tempfile
import unittest
from pathlib import Path

from evaluation import code_style as S

TOOL_TEST = '''package p;
import com.thoughtworks.xstream.XStream;
public class ACarvedTest {
    static XStream xStream = new XStream();
    <T> T deserializeObjectFromFile(String f) throws Exception {
        return (T) xStream.fromXML(f); // load
    }
    private void insertMock(A a) throws Exception {
        java.lang.reflect.Field f = A.class.getDeclaredField("b");
        f.setAccessible(true);
    }
    @Test
    public void testA1() throws Exception {
        A a = deserializeObjectFromFile("a.xml");
        insertMock(a);
        assertEquals(1, a.run("x{"));
    }
    @Test
    @DisplayName("plain")
    public void testA2() {
        assertEquals(2, new A().run("y"));
    }
}
'''

AGENT_TEST = '''package p;
import org.junit.jupiter.api.BeforeEach;
class ATest {
    private A a;
    @BeforeEach
    void setUp() { a = new A(); }
    @Test
    void runsOnce() { assertEquals(1, a.run("x")); }
    @ParameterizedTest
    void runsTwice() { assertEquals(2, a.run("y")); }
}
'''


class Tokens(unittest.TestCase):
    def test_literals_comments_imports(self):
        t = S.toks('package p;\nimport x.Y;\n// c\nint a = 42; String s = "q"; char c = \'z\'; /* b */ a += 1;')
        self.assertEqual(t, ["int", "a", "=", "<NUM>", ";", "String", "s", "=", "<STR>", ";", "char", "c", "=",
                             "<CHR>", ";", "a", "+=", "<NUM>", ";"])

    def test_comment_markers_inside_strings_kept(self):
        self.assertEqual(S.toks('s = "a//b";'), ["s", "=", "<STR>", ";"])


class Model(unittest.TestCase):
    def test_probabilities_and_unknown_tokens(self):
        docs = [S.toks("a.run(1); a.run(2); b.stop();")] * 3
        lm = S.KN3(docs)
        seen = lm.xent(S.toks("a.run(3);"))
        odd = lm.xent(S.toks("zz.qq(3);"))
        self.assertGreater(odd, seen)
        self.assertEqual(lm.m("never"), "<UNK>")
        total = sum(lm.p3("<s>", "<s>", x) for x in list(lm.cont) + ["<UNK>"])
        self.assertLessEqual(total, 1.0 + 1e-9)
        self.assertTrue(math.isfinite(seen))
        self.assertIsNone(lm.xent([]))


class Methods(unittest.TestCase):
    def test_test_methods_and_names(self):
        ms = S.test_methods(TOOL_TEST)
        self.assertEqual([n for n, _ in ms], ["testA1", "testA2"])
        self.assertIn('a.run("x{")', ms[0][1])
        self.assertEqual([n for n, _ in S.test_methods(AGENT_TEST)], ["runsOnce", "runsTwice"])

    def test_reflection_and_xml_through_helpers(self):
        self.assertEqual(S.reflection_xml(TOOL_TEST), [("testA1", True, True), ("testA2", False, False)])
        self.assertEqual(S.reflection_xml(AGENT_TEST), [("runsOnce", False, False), ("runsTwice", False, False)])

    def test_setup_counts_for_every_test(self):
        src = AGENT_TEST.replace("a = new A();", "a = new A(); ReflectionTestUtils.setField(a, \"b\", 1);")
        self.assertEqual(S.reflection_xml(src), [("runsOnce", True, False), ("runsTwice", True, False)])


class Data(unittest.TestCase):
    def test_hidden_paths_and_test_roots(self):
        self.assertTrue(S.hidden("src/test/java/a/sub/X.java", ["src/test/java/a/sub"]))
        self.assertFalse(S.hidden("src/test/java/a/subx/X.java", ["src/test/java/a/sub"]))
        files = ["src/test/java/A.java", "m/src/test/java11/B.java", "tst/C.java", "src/test/resources/D.java",
                 "src/main/java/E.java", "src/test/java/FCarvedTest.java", "src/test/java/G.groovy"]
        self.assertEqual(S.test_sources(files), ["src/test/java/A.java", "m/src/test/java11/B.java", "tst/C.java"])

    def test_cell_test_and_object_files(self):
        diff = ("diff --git a/pom.xml b/pom.xml\n+<x/>\n"
                "diff --git a/src/test/java/p/ACarvedTest.java b/src/test/java/p/ACarvedTest.java\n+class A {}\n"
                "diff --git a/src/test/resources/a.xml b/src/test/resources/a.xml\n+<a/>\n")
        with tempfile.TemporaryDirectory() as d:
            cell = Path(d)
            self.assertIsNone(S.cell_test(cell, "src/test/java/p/ACarvedTest.java"))
            (cell / "result.diff").write_text(diff)
            self.assertEqual(S.cell_test(cell, "src/test/java/p/ACarvedTest.java"), "class A {}")
            self.assertEqual(S.object_files(cell), 1)


class Tables(unittest.TestCase):
    def test_summary_and_dominance(self):
        def row(case, mode, nat, refl=False, xml=False, passes=True):
            return {"group": "70", "project": "p", "case": case, "mode": mode, "run": "r1", "file": 0,
                    "method": "m", "index": 0, "naturalness": nat, "class_naturalness": nat, "reflection": refl,
                    "xml": xml, "passes": passes}
        rows = [row("c1", "autonomous", 3.0), row("c1", "pankti", 5.0, xml=True), row("c1", "baseline", 2.0),
                row("c2", "autonomous", 3.0), row("c2", "pankti", 2.5, refl=True, passes=False)]
        by = {r["mode"]: r for r in S.summarise(rows, ("group", "mode"))}
        self.assertEqual(by["pankti"]["test_methods"], 2)
        self.assertEqual(by["pankti"]["xml_pct"], 50.0)
        self.assertEqual(by["pankti"]["reflection_methods"], 1)
        self.assertEqual(by["pankti"]["passing_test_methods"], 1)
        dom = {r["mode"]: r for r in S.dominance(rows)}
        self.assertEqual(dom["pankti"]["less_natural_than_autonomous"], 1)
        self.assertEqual(dom["pankti"]["more_natural_than_autonomous"], 1)
        self.assertEqual(dom["pankti"]["less_natural_than_every_agent_mode"], 1)


if __name__ == "__main__":
    unittest.main()
