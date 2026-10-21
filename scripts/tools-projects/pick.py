"""pick.py SCAN.tsv TEST_SRC_ROOTS... -> candidate (target, test, hits) where the test method body does not name the target."""
import re, sys, os, collections

tsv, roots = sys.argv[1], sys.argv[2:]
rows = []
for line in open(tsv):
    if line.startswith("CAPPED"):
        continue
    n, rest = line.rstrip("\n").split("\t", 1)
    target, test = rest.split(" <- ")
    rows.append((int(n), target, test))

def body(test):
    cls, meth = test.split("#")
    rel = cls.replace(".", "/") + ".java"
    for r in roots:
        p = os.path.join(r, rel)
        if os.path.exists(p):
            src = open(p, encoding="utf-8", errors="replace").read()
            m = re.search(r"void\s+" + re.escape(meth) + r"\s*\(", src)
            if not m:
                return src, src
            i = src.index("{", m.end())
            depth, j = 0, i
            while j < len(src):
                if src[j] == "{": depth += 1
                elif src[j] == "}":
                    depth -= 1
                    if depth == 0: break
                j += 1
            return src[m.start():j + 1], src
    return None, None

by = collections.defaultdict(list)
for n, target, test in rows:
    if test in ("?", "ERR") or " static" in target or " nonpublic" in target:
        continue
    if not (1 <= n <= 13):
        continue
    mname = target.split("#")[1].split("(")[0]
    b, whole = body(test)
    if b is None:
        continue
    if re.search(r"\b" + re.escape(mname) + r"\s*\(", b):
        continue
    named_in_class = bool(re.search(r"\b" + re.escape(mname) + r"\s*\(", whole))
    by[target].append((n, test, named_in_class))
for t, lst in sorted(by.items()):
    lst.sort(key=lambda x: (x[2], -x[0]))
    print(t)
    for n, test, nic in lst[:6]:
        print(f"   {n:3d}  {test}{'  (class names it elsewhere)' if nic else ''}")
