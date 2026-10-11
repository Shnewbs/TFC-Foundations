"""Reject apparently clean compiler diagnostics when compilation itself failed."""
import argparse
import json
from pathlib import Path

def verify(summary):
    if summary.get("timed_out"):
        return False, "javac timed out"
    if summary.get("exit_code") != 0:
        return False, "javac exited without success"
    if summary.get("displayed_errors") != 0:
        return False, "javac emitted compiler errors"
    if not isinstance(summary.get("source_count"), int) or summary["source_count"] <= 0:
        return False, "no compiled source files were recorded"
    return True, "diagnostic javac completed; full Gradle and runtime gates still required"

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--summary", type=Path, default=Path("port-diagnostics/main-javac-summary.json"))
    args = parser.parse_args()
    passed, message = verify(json.loads(args.summary.read_text(encoding="utf-8")))
    print(("PASS: " if passed else "FAIL: ") + message)
    raise SystemExit(0 if passed else 1)

if __name__ == "__main__":
    main()
