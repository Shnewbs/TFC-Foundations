"""One-time, checksum-guarded application of a reviewed source checkpoint."""
import base64
import hashlib
import json
import lzma
from pathlib import Path, PurePosixPath
import subprocess

EXPECTED = '13d7d7ac516cdf8ed9c466356db4b1dd5ad0c1123a0569ed010baa0442b3dcc1'
parts = [Path(f'tools/porting/.entity-checkpoint-part{i}').read_text() for i in range(3)]
compressed = base64.b64decode(''.join(parts), validate=True)
decoder = lzma.LZMADecompressor()
raw = decoder.decompress(compressed, max_length=1_000_001)
assert decoder.eof and not decoder.unused_data and len(raw) <= 1_000_000, 'Invalid bounded payload'
assert hashlib.sha256(raw).hexdigest() == EXPECTED, 'Source payload checksum mismatch'
entries = json.loads(raw)
assert isinstance(entries, list) and len(entries) == 79, 'Unexpected checkpoint size'
subprocess.run(['git', 'diff', '--quiet'], check=True)
subprocess.run(['git', 'diff', '--cached', '--quiet'], check=True)
prepared = []
seen = set()
for relative, before, after, edits in entries:
    path = PurePosixPath(relative)
    assert not path.is_absolute() and '..' not in path.parts and str(path) == relative
    assert relative == '.gitignore' or relative.startswith(('src/main/java/net/dries007/tfc/', 'tools/porting/'))
    assert relative not in seen, 'Duplicate path'
    seen.add(relative)
    target = Path(relative)
    assert not any(parent.is_symlink() for parent in [target, *target.parents]), 'Symlink not allowed'
    if before is None:
        assert not target.exists(), 'New file already exists: ' + relative
        original = b''
    else:
        assert target.is_file(), 'Missing source: ' + relative
        original = target.read_bytes()
        assert hashlib.sha256(original).hexdigest() == before, 'Changed preimage: ' + relative
    lines = original.decode('utf-8').splitlines(keepends=True)
    last_end = 0
    for start, end, replacement in edits:
        assert type(start) is int and type(end) is int and isinstance(replacement, str)
        assert last_end <= start <= end <= len(lines), 'Invalid edit bounds'
        last_end = end
    for start, end, replacement in reversed(edits):
        lines[start:end] = [replacement]
    updated = ''.join(lines).encode('utf-8')
    assert hashlib.sha256(updated).hexdigest() == after, 'Changed postimage: ' + relative
    prepared.append((target, updated))
# Validate every preimage/postimage before writing any source.
for target, updated in prepared:
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(updated)
subprocess.run(['git', 'add', '--', *(str(path) for path, _ in prepared)], check=True)
subprocess.run(['git', 'diff', '--cached', '--check'], check=True)
print(f'Applied {len(prepared)} reviewed source files; all SHA-256 preimages/postimages matched.')
print('This source application does not imply a successful mod build or playable release.')
