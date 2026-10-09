"""One-time, checksum-guarded transfer of the locally reviewed source checkpoint."""
import base64
import gzip
import hashlib
import json
from pathlib import Path
import subprocess

ROOT = Path.cwd().resolve()
PARTS = [Path('tools/porting/.checkpoint-part0'), Path('tools/porting/.checkpoint-part1')]
EXPECTED = 'a38462d70acb0b9b0a173abec7b6be3236a5d11f79802ac80008bd6e56ada302'


def digest(entries):
    result = hashlib.sha256()
    for path, data in entries:
        name = path.encode()
        result.update(len(name).to_bytes(4, 'big'))
        result.update(name)
        result.update(bytes([data is not None]))
        data = data or b''
        result.update(len(data).to_bytes(8, 'big'))
        result.update(data)
    return result.hexdigest()


def decode_parts(raw0, raw1):
    # Two known transfer transcription defects are repaired before verification.
    # Neither the encoded plan nor any source is trusted until its digest matches.
    a = base64.b64encode(raw0).decode().rstrip('=')
    b = base64.b64encode(raw1).decode().rstrip('=')
    if a.count('PD6ka6F6J6JIL8VA') != 1 or b.count('D/4SoEgV5GFv') != 1:
        raise SystemExit('Unexpected checkpoint transport bytes; no source modified.')
    a = a.replace('PD6ka6F6J6JIL8VA', 'PD6ka6F6JIL8VA')
    a = a[:-4] + 'TzAL'
    b = b.replace('D/4SoEgV5GFv', 'D/4SoEgVGFv') + 'A'
    payload = base64.b64decode(a, validate=True) + base64.b64decode(b, validate=True)
    if hashlib.sha256(payload).hexdigest() != EXPECTED:
        raise SystemExit('Checkpoint checksum mismatch; no source modified.')
    return json.loads(gzip.decompress(payload))


def apply(plan, root):
    before, after = [], []
    seen = set()
    for name, existed, edits in plan['files']:
        path = Path(name)
        if path.is_absolute() or '..' in path.parts or name in seen:
            raise SystemExit('Unsafe or duplicate checkpoint path.')
        if not name.startswith(('src/', 'tools/porting/', 'docs/foundations/')) and name not in ('PROJECT_STATUS.md', 'build.gradle.kts'):
            raise SystemExit('Unexpected checkpoint path: ' + name)
        seen.add(name)
        target = root / path
        if not target.resolve().is_relative_to(root) or target.is_symlink():
            raise SystemExit('Unsafe path resolution: ' + name)
        if target.exists() != existed:
            raise SystemExit('Source existence changed: ' + name)
        data = target.read_bytes() if existed else None
        lines = (data or b'').decode().splitlines(keepends=True)
        last = 0
        for start, end, replacement in edits:
            if not 0 <= last <= start <= end <= len(lines) or not isinstance(replacement, str):
                raise SystemExit('Invalid edit spans: ' + name)
            last = end
        for start, end, replacement in reversed(edits):
            lines[start:end] = replacement.splitlines(keepends=True)
        before.append((name, data))
        after.append((name, ''.join(lines).encode()))
    if digest(before) != plan['before_sha256'] or digest(after) != plan['after_sha256']:
        raise SystemExit('Source checkpoint mismatch; nothing was written.')
    for name, data in after:
        target = root / name
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    print('Applied', len(after), 'reviewed files. Source SHA-256:', plan['after_sha256'])
    return [name for name, _ in after]


if __name__ == '__main__':
    plan = decode_parts(*(p.read_bytes() for p in PARTS))
    changed = apply(plan, ROOT)
    subprocess.run(['git', 'diff', '--check'], check=True)
    subprocess.run(['git', 'add', '-f', '--', *changed], check=True)
