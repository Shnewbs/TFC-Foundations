"""One-time, checksum-guarded application of reviewed target-specific source."""
import hashlib
import json
import lzma
from pathlib import Path, PurePosixPath
import subprocess

BASE = 'e6e700318b97ddd1280fa0bdf66281d32f428cad'
DIGEST = '5de66925ac4cf2ee9a4e8f1fc58c853f8402303372534778c471c3b346dabb38'
PATCH_DIGEST = '6749db18cba61fae9e447b64a8aac9f751836b02719b66a88cb5b985e9219639'
ROOT = Path.cwd().resolve()


def git(*args, data=None):
    return subprocess.run(['git', *args], input=data, capture_output=True, check=True).stdout


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest() if path.is_file() else None


def main():
    if git('rev-parse', 'HEAD^').decode().strip() != BASE:
        raise SystemExit('Unexpected staging parent; refusing to apply to another checkpoint.')
    packed = b''.join((ROOT / f'tools/porting/.livestock-checkpoint-part{i}').read_bytes() for i in range(2))
    if hashlib.sha256(packed).hexdigest() != DIGEST:
        raise SystemExit('Source transport checksum mismatch.')
    decoder = lzma.LZMADecompressor(memlimit=128 * 1024 * 1024)
    raw = decoder.decompress(packed, max_length=2 * 1024 * 1024)
    if not decoder.eof or decoder.unused_data:
        raise SystemExit('Invalid or oversized transport payload.')
    payload = json.loads(raw)
    if payload['base_commit'] != BASE or len(payload['files']) != 35:
        raise SystemExit('Unexpected source manifest.')
    patch = payload['patch'].encode('utf-8')
    if hashlib.sha256(patch).hexdigest() != PATCH_DIGEST:
        raise SystemExit('Reviewed patch checksum mismatch.')
    paths = set()
    for entry in payload['files']:
        name = entry['path']
        rel = PurePosixPath(name)
        if rel.is_absolute() or '..' in rel.parts or str(rel) != name or name in paths:
            raise SystemExit('Invalid or duplicate source path.')
        if not name.startswith(('src/main/java/net/dries007/tfc/client/', 'tools/porting/')):
            raise SystemExit('Source path outside reviewed scope.')
        path = ROOT / name
        if any(p.is_symlink() for p in (path, *path.parents)) or not path.resolve().is_relative_to(ROOT):
            raise SystemExit('Symlink or path traversal rejected.')
        if path.exists() and not path.is_file():
            raise SystemExit('Source path is not a regular file.')
        if digest(path) != entry['before']:
            raise SystemExit('Source preimage mismatch: ' + name)
        paths.add(name)
    if git('diff', '--cached', '--name-only') or git('diff', '--name-only'):
        raise SystemExit('Checkout is not clean.')
    git('apply', '--check', '--index', '--whitespace=error', '-', data=patch)
    git('apply', '--index', '--whitespace=error', '-', data=patch)
    for entry in payload['files']:
        if digest(ROOT / entry['path']) != entry['after']:
            raise SystemExit('Source postimage mismatch: ' + entry['path'])
    changed = set(git('diff', '--cached', '--name-only', '-z').decode().rstrip('\0').split('\0'))
    if changed != paths:
        raise SystemExit('Staged paths do not match the reviewed manifest.')
    git('diff', '--cached', '--check')
    print('Verified and staged all 35 reviewed source files. This is not a successful mod build.')


if __name__ == '__main__':
    main()
