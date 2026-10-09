"""Compile real particle/save classes and exercise real NBT-backed value I/O."""
import json
import os
from pathlib import Path
import shutil
import subprocess

PARTICLES = ('AnimatedParticle', 'BubbleColumnDownParticle', 'BubbleColumnUpParticle',
             'BubbleParticle', 'SparkParticle', 'SleepParticle', 'GlintParticleProvider')


def main():
    out = Path('port-diagnostics')
    cp_file = out / 'classpath.txt'
    if not cp_file.is_file():
        raise SystemExit('Resolve the exact target classpath before running this suite.')
    cp = cp_file.read_text().strip()
    classes = out / 'visual-save-smoke-classes'
    empty = out / 'visual-save-empty-sourcepath'
    shutil.rmtree(classes, ignore_errors=True)
    classes.mkdir(parents=True)
    empty.mkdir(exist_ok=True)
    root = Path('src/main/java/net/dries007/tfc')
    tools = Path(os.environ['JAVA_HOME']) / 'bin' if os.environ.get('JAVA_HOME') else None

    def tool(name):
        return str(tools / name) if tools else name

    sources = [root / 'util/NbtHelpers.java', root / 'util/ValueIoHelpers.java',
               root / 'common/blockentities/TFCBlockEntity.java', Path('tools/porting/SaveIoSmoke.java')]
    sources += [root / 'client/particle' / (name + '.java') for name in PARTICLES]
    commands = [
        [tool('javac'), '--release', '25', '-proc:none', '-classpath', cp,
         '-sourcepath', str(empty), '-d', str(classes), *map(str, sources)],
        [tool('java'), '-ea', '-classpath', str(classes) + os.pathsep + cp, 'SaveIoSmoke'],
    ]
    log = out / 'visual-save-smoke.log'
    with log.open('w') as output:
        for command in commands:
            output.write('Running ' + Path(command[0]).name + ' against the exact target classpath.\n')
            output.flush()
            result = subprocess.run(command, stdout=output, stderr=subprocess.STDOUT, text=True, timeout=90)
            if result.returncode:
                print(log.read_text())
                return result.returncode
        # Inspect bytecode without initializing a Minecraft world or client renderer.
        result = subprocess.run([tool('javap'), '-c', '-p', '-classpath', str(classes) + os.pathsep + cp,
                                 'net.dries007.tfc.common.blockentities.TFCBlockEntity',
                                 'net.dries007.tfc.util.ValueIoHelpers',
                                 *('net.dries007.tfc.client.particle.' + name for name in PARTICLES)],
                                text=True, capture_output=True, timeout=90)
        (out / 'visual-save-bytecode.txt').write_text(result.stdout + result.stderr)
        if result.returncode:
            return result.returncode
        contracts = (
            'BlockEntity.saveAdditional:(Lnet/minecraft/world/level/storage/ValueOutput;)V',
            'BlockEntity.loadAdditional:(Lnet/minecraft/world/level/storage/ValueInput;)V',
            'BlockEntity.onDataPacket:(Lnet/minecraft/network/Connection;Lnet/minecraft/world/level/storage/ValueInput;)V',
            'BlockEntity.handleUpdateTag:(Lnet/minecraft/world/level/storage/ValueInput;)V',
        )
        for contract in contracts:
            if contract not in result.stdout:
                raise AssertionError('Superclass lifecycle delegation missing: ' + contract)
        if 'FluidTank.serialize:(Lnet/minecraft/world/level/storage/ValueOutput;)V' not in result.stdout:
            raise AssertionError('Tank writer must delegate to the format-preserving native serializer.')
        for name in PARTICLES:
            if not (classes / 'net/dries007/tfc/client/particle' / (name + '.class')).is_file():
                raise AssertionError('Production particle class was not compiled: ' + name)
        particle_sources = list((root / 'client/particle').glob('*.java'))
        for source in particle_sources:
            if any(token in source.read_text() for token in ('TextureSheetParticle', 'getRenderType()', 'pickSprite(')):
                raise AssertionError('Legacy particle API in ' + str(source))
        for source in (root / 'common/blockentities').rglob('*.java'):
            if any(token in source.read_text() for token in ('saveAdditional(CompoundTag', 'loadAdditional(CompoundTag', 'INBTSerializable')):
                raise AssertionError('Legacy block-entity save API in ' + str(source))
        output.write('PASS: seven standalone production particle classes and the base block entity compile.\n')
        output.write('PASS: four superclass save/sync delegations verified in emitted bytecode.\n')
        output.write('PASS: tank writer delegates to the native FluidTank serializer.\n')
        output.write('NOT RUN: client rendering, world save/reload, and registry-backed item/fluid/name round trips.\n')
    report = {'particle_classes_compiled': len(PARTICLES), 'nbt_valueio_checks': 49,
              'superclass_delegations_checked': len(contracts), 'native_tank_writer_delegation_checked': True, 'exit_code': 0,
              'scope': 'Standalone compilation, NBT ValueIO utility execution, and static bytecode contracts; not a full build or gameplay test.'}
    (out / 'visual-save-smoke.json').write_text(json.dumps(report, indent=2) + '\n')
    print(log.read_text())
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
