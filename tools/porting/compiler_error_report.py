"""Turn a captured javac log into a file-by-file report."""
import argparse
from collections import Counter
import json
from pathlib import Path
import re

PATTERN = re.compile(r"^\s*(?P<file>.+?[.]java):(?P<line>[0-9]+): error: (?P<message>.*)$", re.MULTILINE)

def analyze(text):
    errors = [dict(file=m.group("file").replace(chr(92), "/"), line=int(m.group("line")), message=m.group("message")) for m in PATTERN.finditer(text)]
    counts = Counter(item["file"] for item in errors)
    return {"total": len(errors), "files": [{"file": p, "errors": n} for p, n in sorted(counts.items(), key=lambda x: (-x[1], x[0]))], "errors": errors}

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("log", type=Path, help="javac diagnostic log")
    parser.add_argument("--output", type=Path, default=Path("port-diagnostics/main-javac-errors.json"))
    args = parser.parse_args()
    result = analyze(args.log.read_text(encoding="utf-8", errors="replace"))
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2) + chr(10), encoding="utf-8")
    print(f"Wrote {result['total']} diagnostics across {len(result['files'])} files to {args.output}")

if __name__ == "__main__":
    main()
