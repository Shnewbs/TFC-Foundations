"""One-time checksum/ancestry guarded source migration for 26.1.2."""
import base64
import hashlib
import json
import lzma
import subprocess
from pathlib import Path, PurePosixPath

BASE = "d7691430d4c615b9df293d1e47f457f5dbe4926e"
PACKED_SHA = "c939f1b42d8d8bc6384212b53201d6abbeaa2a906cdc5b87eb3d15ca0808c3a0"
RAW_SHA = "6d47cdd595d0daadc8f9c2413847759955dd47e6f51b4e2bcf326b52870c070c"
PATCH_SHA = "1db4914a4393c090479583cc7610fe3ffeefa1d7794a6b5790bc4b7dcf8f0f67"
ROOT = Path.cwd().resolve()


def git(*args, data=None):
    return subprocess.run(["git", *args], input=data, check=True, capture_output=True).stdout


def sha(data):
    return hashlib.sha256(data).hexdigest()


def main():
    if git("rev-parse", "HEAD^").decode().strip() != BASE:
        raise SystemExit("Unexpected staging parent; refuse to apply unrelated source.")
    encoded = "".join(
        (ROOT / f"tools/porting/.attachment-food-runtime-part{i}").read_text().strip()
        for i in range(5)
    )
    packed = base64.b64decode(encoded, validate=True)
    if sha(packed) != PACKED_SHA:
        raise SystemExit("Source transport checksum mismatch.")
    decoder = lzma.LZMADecompressor(memlimit=128 * 1024 * 1024)
    raw = decoder.decompress(packed, max_length=2 * 1024 * 1024)
    if not decoder.eof or decoder.unused_data or sha(raw) != RAW_SHA:
        raise SystemExit("Source payload checksum/size mismatch.")
    payload = json.loads(raw)
    if payload["base_commit"] != BASE or len(payload["files"]) != 17:
        raise SystemExit("Unexpected source manifest.")
    patch = payload["patch"].encode("utf-8")
    if sha(patch) != PATCH_SHA:
        raise SystemExit("Reviewed patch checksum mismatch.")
    seen = set()
    for entry in payload["files"]:
        name = entry["path"]
        rel = PurePosixPath(name)
        if rel.is_absolute() or ".." in rel.parts or str(rel) != name or name in seen:
            raise SystemExit("Invalid or duplicate source path: " + name)
        if not name.startswith(("src/main/java/net/dries007/tfc/",
                                "src/main/resources/tfc.mixins.json",
                                "tools/porting/")):
            raise SystemExit("Unexpected source path: " + name)
        path = ROOT / name
        if any(parent.is_symlink() for parent in (path, *path.parents)):
            raise SystemExit("Source symlink rejected: " + name)
        if not path.resolve().is_relative_to(ROOT):
            raise SystemExit("Source path traversal: " + name)
        before = sha(path.read_bytes()) if path.is_file() else None
        if before != entry["before"]:
            raise SystemExit("Source preimage mismatch: " + name)
        seen.add(name)
    if git("diff", "--name-only") or git("diff", "--cached", "--name-only"):
        raise SystemExit("Staging checkout must have no preexisting changes.")
    git("apply", "--check", "--index", "--whitespace=error", "-", data=patch)
    git("apply", "--index", "--whitespace=error", "-", data=patch)
    for entry in payload["files"]:
        path = ROOT / entry["path"]
        if not path.is_file() or sha(path.read_bytes()) != entry["after"]:
            raise SystemExit("Source postimage mismatch: " + entry["path"])
    changed = set(git("diff", "--cached", "--name-only", "-z").decode().rstrip("\0").split("\0"))
    if changed != seen:
        raise SystemExit("Changed paths differ from reviewed manifest.")
    git("diff", "--cached", "--check")
    print("Verified 17 source/test files and complete pre/postimage checks.")
    print("This application is not evidence of a successful mod build.")


if __name__ == "__main__":
    main()
