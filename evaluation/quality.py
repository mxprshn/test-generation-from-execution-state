"""RQ3 and test-quality statistics computed from generated test sources.

* change between preliminary test and final test (mode `rendered`): size of the diff and whether the
  target calls, recorded literals, mock stubbings and checks of the preliminary test survived;
* mock statistics: number of mocks, stubbings, mocks of plain data types;
* readability (second priority, simplified): test-method length, number of assertions, tests without
  assertions, reflection, generated names (v0, mocked1), casts, fully qualified names, fixture files.
"""
from __future__ import annotations

import difflib
import re
from typing import Any

TEST_ANN = re.compile(r"@(Test|ParameterizedTest|RepeatedTest)\b")
ASSERT = re.compile(r"\b(assert\w*|verify|verifyNoMoreInteractions|verifyNoInteractions|expect\w*|fail)\s*\(")
MOCK = re.compile(r"\b(Mockito\.)?(mock|spy)\s*\(|@Mock\b|@Spy\b|@MockBean\b")
STUB = re.compile(r"\b(when|doReturn|doThrow|doAnswer|doNothing|given)\s*\(")
REFLECT = re.compile(r"ReflectionTestUtils|setAccessible\s*\(|getDeclaredField\s*\(|FieldUtils\.|Whitebox\.")
GEN_NAME = re.compile(r"\b(v\d+|mocked\d*|obj\d+|var\d+|arg\d+)\b")
CAST = re.compile(r"\(\s*[A-Z][\w.]*(<[^>]*>)?\s*\)\s*[\w(\"]")
FQN = re.compile(r"\b(?:[a-z]\w*\.){2,}[A-Z]\w*")
FIXTURE = re.compile(r"getResource|Files\.read|new File\(|Paths\.get|\.json\"|\.xml\"|\.ser\"")
STRING_LIT = re.compile(r"\"(?:[^\"\\]|\\.)*\"")
NUM_LIT = re.compile(r"(?<![\w.])-?\d+(?:\.\d+)?[LlFfDd]?(?![\w.])")
DTO_HINT = re.compile(r"\b(mock|spy)\s*\(\s*(String|Integer|Long|List|Map|Set|Optional|\w*(Dto|DTO|Entity|Model|Request|Response))\.class")


def _methods(src: str) -> list[str]:
    """Bodies of test methods (rough: from the annotation to the matching closing brace)."""
    out = []
    for m in TEST_ANN.finditer(src):
        i = src.find("{", m.end())
        if i < 0:
            continue
        depth, j = 0, i
        while j < len(src):
            if src[j] == "{":
                depth += 1
            elif src[j] == "}":
                depth -= 1
                if depth == 0:
                    break
            j += 1
        out.append(src[i:j + 1])
    return out


def _code_lines(s: str) -> list[str]:
    lines = []
    for line in s.splitlines():
        t = line.strip()
        if t and not t.startswith("//") and not t.startswith("*") and not t.startswith("/*"):
            lines.append(t)
    return lines


def mock_stats(src: str) -> dict[str, Any]:
    return {"mocks": len(MOCK.findall(src)), "stubbings": len(STUB.findall(src)),
            "data_type_mocks": len(DTO_HINT.findall(src))}


def readability(src: str) -> dict[str, Any]:
    methods = _methods(src)
    lens = [len(_code_lines(m)) for m in methods]
    asserts = [len(ASSERT.findall(m)) for m in methods]
    return {
        "test_methods": len(methods),
        "median_method_loc": sorted(lens)[len(lens) // 2] if lens else None,
        "assertions": sum(asserts),
        "tests_without_assertions": sum(1 for a in asserts if a == 0),
        "reflection": len(REFLECT.findall(src)),
        "generated_names": len(set(GEN_NAME.findall(src))),
        "casts": len(CAST.findall(src)),
        "fully_qualified_names": len(FQN.findall("\n".join(l for l in src.splitlines() if not l.lstrip().startswith(("import", "package"))))),
        "fixture_files": len(FIXTURE.findall(src)),
    }


def _literals(src: str) -> set[str]:
    return set(STRING_LIT.findall(src)) | set(NUM_LIT.findall(src))


def preservation(preliminary: str, final: str, target_method: str) -> dict[str, Any]:
    """How much of the preliminary test survived the LLM refactoring."""
    pre_lines, fin_lines = _code_lines(preliminary), _code_lines(final)
    sm = difflib.SequenceMatcher(a=pre_lines, b=fin_lines, autojunk=False)
    changed = sum(max(i2 - i1, j2 - j1) for tag, i1, i2, j1, j2 in sm.get_opcodes() if tag != "equal")
    call = re.compile(rf"\.{re.escape(target_method)}\s*\(|\b{re.escape(target_method)}\s*\(")
    pre_lit, fin_lit = _literals(preliminary), _literals(final)
    return {
        "preliminary_loc": len(pre_lines),
        "final_loc": len(fin_lines),
        "changed_lines": changed,
        "changed_share": changed / max(len(pre_lines), 1),
        "target_calls": [len(call.findall(preliminary)), len(call.findall(final))],
        "stubbings": [len(STUB.findall(preliminary)), len(STUB.findall(final))],
        "assertions": [len(ASSERT.findall(preliminary)), len(ASSERT.findall(final))],
        "test_methods": [len(_methods(preliminary)), len(_methods(final))],
        "literals_kept": len(pre_lit & fin_lit) / len(pre_lit) if pre_lit else None,
    }
