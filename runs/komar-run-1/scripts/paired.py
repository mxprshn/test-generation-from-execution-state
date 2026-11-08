"""komar against autonomous and baseline, paired by case, with the functions of evaluation.aggregate
(case_scores: mean over rounds; ST.wilcoxon, ST.bootstrap_ci). Usage from the repository:
python3 paired.py <combined config 70> <combined config 31>"""
import statistics
import sys

sys.path.insert(0, ".")
from evaluation import aggregate as AG
from runner.config import load_config


def rows(cfg_path):
    return AG.load(load_config(cfg_path))[0]


def paired(scores, a, b, level):
    cids = sorted(c for m, c in scores if m == a and (b, c) in scores)
    d = [scores[(a, c)][level] - scores[(b, c)][level] for c in cids
         if level in scores[(a, c)] and level in scores[(b, c)]]
    w = AG.ST.wilcoxon(d)
    lo, hi = AG.ST.bootstrap_ci(d)
    return (len(d), statistics.mean(scores[(a, c)][level] for c in cids), statistics.mean(scores[(b, c)][level] for c in cids),
            statistics.mean(d), lo, hi, sum(x > 0 for x in d), sum(x == 0 for x in d), sum(x < 0 for x in d), w["p"])


for group, path in (("70", sys.argv[1]), ("31", sys.argv[2])):
    recs = rows(path)
    scores = AG.case_scores(recs)
    for other in ("autonomous", "baseline"):
        if not any(m == other for m, _ in scores):
            continue
        for level, short in AG.LEVELS:
            n, ma, mb, diff, lo, hi, w, t, l, p = paired(scores, "komar", other, level)
            print(f"{group} komar vs {other} {short}: n={n} {ma:.3f} vs {mb:.3f} diff {diff:+.3f} "
                  f"CI [{lo:+.3f},{hi:+.3f}] W/T/L {w}/{t}/{l} p={p if p is None else format(p, ".3g")}")
