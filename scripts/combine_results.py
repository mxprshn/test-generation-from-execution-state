"""Put the cells of several results directories into one tree, for evaluation.aggregate.

    python3 scripts/combine_results.py OUT RESULTS_DIR [RESULTS_DIR ...]

Every <case>/<mode>/<run-id> of every input becomes a symbolic link OUT/cells/<case>/<mode>/<run-id>;
references are linked from the first input that has them. A cell present in two inputs is an error
(the same mode of a case was generated twice). Then aggregate with a config whose paths.results is OUT
and whose modes list every mode (e.g. the published v2 cells with a later autonomous run):

    python3 -m evaluation.aggregate --config combined.yaml
"""
from __future__ import annotations

import sys
from pathlib import Path


def main(argv: list[str]) -> int:
    if len(argv) < 2:
        print(__doc__, file=sys.stderr)
        return 2
    out, inputs = Path(argv[0]), [Path(a) for a in argv[1:]]
    cells = out / "cells"
    cells.mkdir(parents=True, exist_ok=True)
    seen: dict[Path, Path] = {}
    for src in inputs:
        for cell in sorted((src / "cells").glob("*/*/*")):
            if not cell.is_dir():
                continue
            rel = cell.relative_to(src / "cells")
            if rel in seen:
                print(f"{rel}: in {seen[rel]} and {src}", file=sys.stderr)
                return 1
            seen[rel] = src
            link = cells / rel
            link.parent.mkdir(parents=True, exist_ok=True)
            if link.is_symlink() or link.exists():
                link.unlink()
            link.symlink_to(cell.resolve())
    refs = out / "references"
    if not refs.exists():
        for src in inputs:
            if (src / "references").is_dir():
                refs.symlink_to((src / "references").resolve())
                break
    print(f"{len(seen)} cells from {len(inputs)} results directories in {cells}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
