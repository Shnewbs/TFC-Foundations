"""Negative regression tests ensure the source guards detect the known lost-save bugs."""
from pathlib import Path
import shutil
import tempfile
import unittest

from run_entity_smoke import method_body, source_contracts


class EntityContractsTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.entities = self.root / 'src/main/java/net/dries007/tfc/common/entities'
        shutil.copytree('src/main/java/net/dries007/tfc/common/entities', self.entities)
        target = self.root / 'tools/porting/entity-save-contracts.json'
        target.parent.mkdir(parents=True)
        shutil.copyfile('tools/porting/entity-save-contracts.json', target)

    def test_current_source(self):
        self.assertEqual(62, source_contracts(self.root))

    def test_discarded_plucking_cooldown_is_rejected(self):
        for relative in ('livestock/OviparousAnimal.java', 'prey/WingedPrey.java'):
            with self.subTest(relative=relative):
                path = self.entities / relative
                original = path.read_text()
                path.write_text(original.replace('lastPlucked = EntityHelpers.getLongOrDefault', 'EntityHelpers.getLongOrDefault'))
                with self.assertRaisesRegex(AssertionError, 'cooldown must be assigned'):
                    source_contracts(self.root)
                path.write_text(original)

    def test_lost_save_key_is_rejected(self):
        path = self.entities / 'livestock/MammalProperties.java'
        path.write_text(path.read_text().replace('putLong("pregnant"', 'putLong("pregnancy_renamed"'))
        with self.assertRaisesRegex(AssertionError, 'Lost literal save keys'):
            source_contracts(self.root)

    def test_stale_owner_is_rejected(self):
        path = self.entities / 'livestock/pet/TamableMammal.java'
        path.write_text(path.read_text().replace('setOwnerUUID(tag.read("Owner", UUIDUtil.CODEC).orElse(null));',
                                                 'tag.read("Owner", UUIDUtil.CODEC).ifPresent(this::setOwnerUUID);'))
        with self.assertRaisesRegex(AssertionError, 'clearing missing owner'):
            source_contracts(self.root)

    def test_stale_genes_are_rejected(self):
        path = self.entities / 'livestock/MammalProperties.java'
        path.write_text(path.read_text().replace('setGenes(nbt.read("genes", CompoundTag.CODEC).orElse(null));',
                                                 'nbt.read("genes", CompoundTag.CODEC).ifPresent(this::setGenes);'))
        with self.assertRaisesRegex(AssertionError, 'Missing genes must clear'):
            source_contracts(self.root)

    def test_masked_braces_preserve_method_boundaries(self):
        source = '''void readAdditionalSaveData(ValueInput input) {
            // } is not the method boundary
            consume("{literal}"); /* } */ if (flag) { consume('}'); }
        }
        void after() {}'''
        parameter, body = method_body(source, 'readAdditionalSaveData', 'ValueInput')
        self.assertEqual('input', parameter)
        self.assertIn("consume('}')", body)
        self.assertNotIn('void after', body)


if __name__ == '__main__':
    unittest.main()
