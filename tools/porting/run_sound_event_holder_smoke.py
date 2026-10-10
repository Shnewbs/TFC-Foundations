"""Target sound holder typing and concrete playback call-site contracts."""
import os
from pathlib import Path
import subprocess
import unittest

ROOT=Path(__file__).resolve().parents[2]
SRC=ROOT/'src/main/java/net/dries007/tfc'
JAVAP=str(Path(os.environ.get('JAVA_HOME',''))/'bin/javap') if os.environ.get('JAVA_HOME') else 'javap'

CALLS={
    'common/blockentities/QuernBlockEntity.java':['ITEM_BREAK'],
    'common/blockentities/rotation/TripHammerBlockEntity.java':['ITEM_BREAK'],
    'common/blocks/rotation/GearBoxBlock.java':['ITEM_BREAK'],
    'common/entities/prey/Pest.java':['GENERIC_EAT'],
    'common/entities/misc/TFCFishingHook.java':['ITEM_BREAK','GENERIC_EAT'],
    'common/items/GlassBlowpipeItem.java':['ITEM_BREAK'],
    'util/data/Drinkable.java':['GENERIC_DRINK'],
}

class TargetSoundContracts(unittest.TestCase):
    def test_holder_types_resolved_against_pinned_target(self):
        cp=(ROOT/'port-diagnostics/classpath.txt')
        self.assertTrue(cp.is_file())
        output=subprocess.run([JAVAP,'-classpath',cp.read_text().strip(),'-public',
                               'net.minecraft.sounds.SoundEvents'],check=True,
                              text=True,capture_output=True,timeout=15).stdout
        for name in ('ITEM_BREAK','GENERIC_EAT','GENERIC_DRINK'):
            self.assertIn('Holder$Reference<net.minecraft.sounds.SoundEvent> '+name+';', output)
        self.assertIn('public static final net.minecraft.sounds.SoundEvent ENCHANTMENT_TABLE_USE;',output)

    def test_sound_holder_playback_values_are_extracted(self):
        for file,names in CALLS.items():
            s=(SRC/file).read_text()
            for name in names:
                self.assertEqual(s.count('SoundEvents.'+name+'.value()'),1,(file,name))
                self.assertNotIn('SoundEvents.'+name+',',s,(file,name))
        self.assertIn('broken ? SoundEvents.ITEM_BREAK.value() : SoundEvents.ENCHANTMENT_TABLE_USE',
                      (SRC/'common/items/GlassBlowpipeItem.java').read_text())

if __name__=='__main__':
    unittest.main(verbosity=2)
