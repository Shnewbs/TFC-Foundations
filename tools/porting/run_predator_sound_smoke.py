"""Check migrated classic sound providers and real predator schedule math."""
from pathlib import Path
import os
import re
import shutil
import subprocess

ROOT = Path.cwd()
OUT = ROOT / 'port-diagnostics' / 'predator-sound-smoke-classes'
SRC = ROOT / 'src/main/java/net/dries007/tfc'
JAVA = Path(os.environ.get('JAVA_HOME', '')) / 'bin'
JAVAC = str(JAVA / 'javac') if (JAVA / 'javac').is_file() else 'javac'
JAVA_EXE = str(JAVA / 'java') if (JAVA / 'java').is_file() else 'java'
JAVAP = str(JAVA / 'javap') if (JAVA / 'javap').is_file() else 'javap'
CLASSPATH = (ROOT / 'port-diagnostics/classpath.txt').read_text().strip()


def require(expr, message):
    if not expr:
        raise AssertionError(message)


def api_signature(class_name, required):
    api = subprocess.check_output([JAVAP, '-classpath', CLASSPATH, '-public', class_name], text=True)
    for member in required:
        require(member in api, class_name + ' missing expected member: ' + member)


def main():
    sounds = (SRC / 'client/TFCSounds.java').read_text()
    brain = (SRC / 'common/entities/ai/TFCBrain.java').read_text()
    core = (SRC / 'TerraFirmaCraft.java').read_text()
    for label in ('PIG', 'COW', 'CHICKEN', 'CAT'):
        require(re.search(r'EntityId ' + label + r'\s*=\s*classic' + label.title() + r'Sounds\(\);', sounds),
                'The ' + label + ' sound source must use the classic variant')
    for name in ('PIG', 'COW', 'CHICKEN', 'CAT'):
        require('SoundEvents.' + name + '_SOUNDS.get(' in sounds, 'Missing native sound variant: ' + name)
    require('CHICKEN_STEP.value()' in sounds, 'Cat step supplier must unwrap the holder')
    require('SoundEvents.PIG_AMBIENT' not in sounds and 'SoundEvents.COW_AMBIENT' not in sounds,
            'Removed vanilla ambient sounds must not be referenced')
    for word in ('ScheduleBuilder', 'Registries.SCHEDULE', 'registerSchedule', 'SCHEDULES.register'):
        require(word not in brain + core, 'Removed schedule API still referenced: ' + word)
    require('PredatorScheduleMath.shouldHunt(diurnal, overworldClockTime)' in brain,
            'TFCBrain must delegate to the tested schedule math')
    paths = [
        SRC / 'common/entities/predator/Predator.java',
        *[SRC / 'common/entities/ai/predator' / f for f in
          ('PredatorAi.java', 'AmphibiousPredatorAi.java', 'PackPredatorAi.java', 'PredatorBehaviors.java')]
    ]
    for path in paths:
        body = path.read_text()
        require('TFCBrain.updatePredatorActivity(' in body, str(path) + ' skips custom predator activity')
        require('setSchedule(' not in body and 'updateActivityFromSchedule(' not in body,
                str(path) + ' mixes vanilla villager activity with predator scheduling')
    behaviors = (SRC / 'common/entities/ai/predator/PredatorBehaviors.java').read_text()
    require('other != predator' in behaviors and 'TargetingConditions.DEFAULT.test(server, predator, other)' in behaviors,
            'Native nearby-disturbance query must preserve target rules and exclude self')
    require('EntityTypeTest.forClass(LivingEntity.class)' in behaviors and 'hasNearbyDisturbance(entity)' in behaviors,
            'Predator disturbance detection must use the target entity query')
    api_signature('net.minecraft.world.level.Level', ['getOverworldClockTime()'])
    api_signature('net.minecraft.world.entity.ai.Brain', ['setActiveActivityIfPossible('])
    api_signature('net.minecraft.sounds.SoundEvents', ['PIG_SOUNDS', 'COW_SOUNDS', 'CHICKEN_SOUNDS', 'CAT_SOUNDS'])
    shutil.rmtree(OUT, ignore_errors=True)
    OUT.mkdir(parents=True)
    subprocess.run([JAVAC, '--release', '25', '-sourcepath', '', '-d', str(OUT),
                    str(SRC / 'common/entities/ai/PredatorScheduleMath.java'),
                    'tools/porting/PredatorScheduleSmoke.java'], check=True, timeout=40)
    subprocess.run([JAVA_EXE, '-cp', str(OUT), 'PredatorScheduleSmoke'], check=True, timeout=45)
    print('PASS: migrated sound API declarations, predator call sites and native target signatures.')


if __name__ == '__main__':
    main()
