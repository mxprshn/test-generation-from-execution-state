"""scripts/export_results.py: what is published, how sessions are cleaned, redaction, determinism."""
import importlib.util
import json
import tarfile
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location("export_results", ROOT / "scripts" / "export_results.py")
X = importlib.util.module_from_spec(spec)
spec.loader.exec_module(X)


def write(path: Path, text: str = "x") -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text)


def session_lines(cut_off: bool = False) -> list[str]:
    user = {"role": "user", "content": [{"type": "text", "text": "write a test"}], "timestamp": 1}
    call = {"type": "toolCall", "id": "c1", "name": "read", "arguments": {"path": "A.java"}}
    assistant = {"role": "assistant", "timestamp": 2, "stopReason": "toolUse",
                 "usage": {"input": 10, "output": 5, "cost": {"total": 0.01}},
                 "content": [{"type": "thinking", "thinking": "read the class", "thinkingSignature": "opaque"},
                             call]}
    result = {"role": "toolResult", "toolCallId": "c1", "toolName": "read", "isError": False, "timestamp": 3,
              "content": [{"type": "text", "text": "class A {}"}]}
    ev = [{"type": "session", "version": 3, "cwd": "/root/ws"}, {"type": "agent_start"}, {"type": "turn_start"},
          {"type": "message_start", "message": user}, {"type": "message_end", "message": user},
          {"type": "message_start", "message": {**assistant, "content": []}},
          {"type": "message_update", "assistantMessageEvent": {"type": "thinking_delta", "delta": "read"}},
          {"type": "message_update", "assistantMessageEvent": {"type": "toolcall_delta", "delta": "{\"pa"}},
          {"type": "message_end", "message": assistant},
          {"type": "tool_execution_start", "toolCallId": "c1", "toolName": "read", "args": {"path": "A.java"}},
          {"type": "tool_execution_update", "toolCallId": "c1", "partialResult": {"content": []}},
          {"type": "tool_execution_end", "toolCallId": "c1", "toolName": "read", "isError": False,
           "result": {"content": result["content"]}},
          {"type": "message_start", "message": result}, {"type": "message_end", "message": result},
          {"type": "turn_end", "message": assistant, "toolResults": [result]}]
    if cut_off:
        ev += [{"type": "message_start", "message": {"role": "assistant", "content": []}},
               {"type": "message_update", "assistantMessageEvent": {"type": "text_delta", "delta": "half"}}]
    else:
        ev += [{"type": "agent_end", "messages": [user, assistant, result]}, {"type": "agent_settled"}]
    return [json.dumps(e) for e in ev]


class SessionTest(unittest.TestCase):
    def test_conversation_is_kept(self):
        raw = session_lines()
        clean = X.clean_session(raw)
        self.assertEqual(X.conversation(clean), X.conversation(raw))
        types = [json.loads(x)["type"] for x in clean]
        for gone in ("message_update", "tool_execution_update", "message_start"):
            self.assertNotIn(gone, types)
        self.assertNotIn("thinkingSignature", "\n".join(clean))
        self.assertIn("read the class", "\n".join(clean))
        self.assertEqual("\n".join(clean).count("class A {}"), 1)  # tool result kept once
        end = [json.loads(x) for x in clean if json.loads(x)["type"] == "agent_end"][0]
        self.assertEqual([m["role"] for m in end["messages"]], ["user", "assistant", "toolResult"])
        self.assertNotIn("content", end["messages"][1])
        self.assertEqual(end["messages"][1]["usage"]["input"], 10)

    def test_cut_off_message_keeps_its_stream(self):
        raw = session_lines(cut_off=True)
        clean = X.clean_session(raw)
        self.assertEqual(X.conversation(clean), X.conversation(raw))
        self.assertTrue(X.conversation(clean)["cut_off"])
        self.assertIn("half", clean[-1])


class ExportTest(unittest.TestCase):
    def test_archive_contents_redaction_determinism(self):
        with tempfile.TemporaryDirectory() as d:
            res, arc = Path(d) / "res", Path(d) / "a.tar.gz"
            cell = res / "cells" / "c1" / "dump" / "r1"
            write(cell / "summary.json", json.dumps({"workspace": "/srv/x/work/ws/w0", "key": "GATEWAY_KEY"}))
            write(cell / "eval.json", "{}")
            write(cell / "summary.json.pre-migration", "{}")
            write(cell / "artifacts" / "session.jsonl", "\n".join(session_lines()))
            write(cell / "eval-artifacts" / "trace" / "trace-1.jsonl", "/srv/x/work/ws/w0/A.java")
            write(res / "cells" / "c1" / "dump" / "r1.incomplete-1" / "summary.json", "{}")
            write(res / "cells" / "c1" / "dump" / "r1.infra-2" / "summary.json", "{}")
            write(res / "references" / "c1" / "run1.json", "{}")
            write(res / "report" / "report.md", "r")
            for stamp in ("20260101T000000", "20260101T000100"):
                write(res / f"config-{stamp}.yaml", "base_url: https://gw.example/v1\napi_key_env: GATEWAY_KEY\n")
                write(res / f"start-{stamp}.json", json.dumps({"cases": ["c1"]}))
            extra = Path(d) / "moved.json"
            write(extra, "{}")
            pairs = [("/srv/x", "<root>"), ("https://gw.example/v1", "https://<gateway>/v1"),
                     ("GATEWAY_KEY", "TESTGEN_API_KEY")]
            stats = X.export(res, arc, [extra], {}, pairs)
            with tarfile.open(arc) as tar:
                names = tar.getnames()
                body = {n: tar.extractfile(n).read().decode() for n in names}
                self.assertTrue(all(m.mtime == X.MTIME and m.uid == 0 for m in tar.getmembers()))
            self.assertEqual(names, sorted(names))
            for want in ("cells/c1/dump/r1/summary.json", "cells/c1/dump/r1/artifacts/session.jsonl",
                         "cells/c1/dump/r1/eval-artifacts/trace/trace-1.jsonl", "references/c1/run1.json",
                         "report/report.md", "provenance/runs.jsonl", "provenance/moved.json"):
                self.assertIn(want, names)
            for gone in ("pre-migration", "incomplete", "infra"):
                self.assertFalse([n for n in names if gone in n], gone)
            text = "\n".join(body.values())
            for secret in ("/srv/x", "gw.example", "GATEWAY_KEY"):
                self.assertNotIn(secret, text)
            self.assertIn("<root>/work/ws/w0/A.java", body["cells/c1/dump/r1/eval-artifacts/trace/trace-1.jsonl"])
            self.assertEqual(stats["configs"], 1)
            self.assertEqual(len([n for n in names if n.startswith("provenance/config-")]), 1)
            self.assertEqual(len(body["provenance/runs.jsonl"].splitlines()), 2)
            self.assertLess(stats["session_bytes_after"], stats["session_bytes_before"])
            again = X.export(res, Path(d) / "b.tar.gz", [extra], {}, pairs)
            self.assertEqual(stats["archive_sha256"], again["archive_sha256"])


if __name__ == "__main__":
    unittest.main()
