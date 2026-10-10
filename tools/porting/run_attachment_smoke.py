"""Exercise the actual 26.1.2 attachment value-IO bridge without game stubs."""
import os
from pathlib import Path
import shutil
import subprocess

ROOT = Path('port-diagnostics')
CP = (ROOT / 'classpath.txt').read_text().strip()
CLASSES = ROOT / 'attachment-smoke-classes'
EMPTY = ROOT / 'attachment-smoke-empty-sourcepath'
shutil.rmtree(CLASSES, ignore_errors=True)
CLASSES.mkdir(parents=True)
EMPTY.mkdir(parents=True, exist_ok=True)
JAVA_BIN = Path(os.environ.get('JAVA_HOME', '')) / 'bin' if os.environ.get('JAVA_HOME') else Path('')

def execute(binary, *args):
    command = [str(JAVA_BIN / binary), *map(str, args)]
    result = subprocess.run(command, text=True, capture_output=True, timeout=100)
    if result.returncode:
        print(result.stdout + result.stderr)
        raise SystemExit(result.returncode)
    return result.stdout + result.stderr

execute('javac', '--release', '25', '-proc:none', '-classpath', CP, '-sourcepath', EMPTY,
        '-d', CLASSES, 'src/main/java/net/dries007/tfc/util/AttachmentValueIO.java',
        'tools/porting/AttachmentValueIOSmoke.java')
print(execute('java', '-ea', '-classpath', str(CLASSES) + os.pathsep + CP, 'AttachmentValueIOSmoke').strip())

source = Path('src/main/java/net/dries007/tfc/common/TFCAttachments.java').read_text()
assert source.count('new IAttachmentSerializer<') == 2
assert 'IAttachmentSerializer<CompoundTag,' not in source
assert source.count('AttachmentValueIO.read(input)') == 2
assert source.count('AttachmentValueIO.write(output,') == 2
assert 'ValueInput input' in source and 'ValueOutput output' in source
print('PASS: 2 production serializers use the target native attachment API and preserve root NBT layout.')
