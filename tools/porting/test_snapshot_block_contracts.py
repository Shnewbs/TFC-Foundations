"""Mutations exercise the actual source/asset guards, not a simulated game or renderer."""
import json
import os
from pathlib import Path
import shutil
import tempfile
import unittest
import run_snapshot_block_smoke as guard


class SnapshotContractTests(unittest.TestCase):
    def setUp(self):
        self.root = Path.cwd()
        self.temp = tempfile.TemporaryDirectory()
        self.contracts = json.loads(guard.CONTRACTS.read_text())
        files = set(self.contracts['unchanged_assets']) | set(self.contracts['blockstates']) | guard.GUARDED_FILES
        for name in files:
            target = Path(self.temp.name) / name
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(self.root / name, target)
        os.chdir(self.temp.name)

    def tearDown(self):
        os.chdir(self.root)
        self.temp.cleanup()

    def change(self, name, before, after):
        path = Path(name)
        source = path.read_text()
        self.assertIn(before, source)
        path.write_text(source.replace(before, after))

    def test_valid(self):
        self.assertEqual(guard.validate_contracts(self.contracts), 4)

    def test_obsolete_interface_rejected(self):
        (guard.MODELS / 'IStaticBakedModel.java').write_text('// obsolete interface restored')
        with self.assertRaisesRegex(AssertionError, 'Obsolete baked-model interface'):
            guard.validate_contracts(self.contracts)

    def test_dynamic_dispatch_required(self):
        self.change('src/main/resources/assets/tfc/blockstates/ingot_pile.json', ',\n      "type": "tfc:dynamic"', '')
        with self.assertRaisesRegex(AssertionError, 'bypasses dynamic dispatch'):
            guard.validate_contracts(self.contracts)

    def test_multipart_rotation_preserved(self):
        self.change('src/main/resources/assets/tfc/blockstates/mold_table.json', '"y": 270', '"y": 180')
        with self.assertRaisesRegex(AssertionError, 'rotations'):
            guard.validate_contracts(self.contracts)

    def test_standalone_registration_required(self):
        self.change('src/main/java/net/dries007/tfc/client/ClientEventHandler.java', 'bus.addListener(MoldTableBlockModel::registerStandaloneModels);', '')
        with self.assertRaisesRegex(AssertionError, 'registration listener'):
            guard.validate_contracts(self.contracts)

    def test_defensive_copy_required(self):
        self.change(str(guard.BLOCK_ENTITIES / 'BlockEntityModelData.java'), 'textures = List.copyOf(textures);', '// mutable list retained')
        with self.assertRaisesRegex(AssertionError, 'Immutable snapshot'):
            guard.validate_contracts(self.contracts)

    def test_live_world_read_rejected(self):
        path = guard.MODELS / 'SnapshotBlockStateModel.java'
        path.write_text(path.read_text() + '\n// regression: level.getBlockEntity(pos)\n')
        with self.assertRaisesRegex(AssertionError, 'reads live block entity'):
            guard.validate_contracts(self.contracts)

    def test_load_refresh_required(self):
        self.change(str(guard.BLOCK_ENTITIES / 'IngotPileBlockEntity.java'), 'super.loadAdditional(tag);\n        BlockEntityModelData.refresh(this);', 'super.loadAdditional(tag);')
        with self.assertRaisesRegex(AssertionError, 'Load no longer refreshes'):
            guard.validate_contracts(self.contracts)

    def test_frozen_geometry_required(self):
        self.change('tools/porting/PileLegacyReference.java', '(minX + 7)', '(minX + 8)')
        with self.assertRaisesRegex(AssertionError, 'oracle changed'):
            guard.validate_contracts(self.contracts)

    def test_save_layout_preserved(self):
        self.change(str(guard.BLOCK_ENTITIES / 'ScrapingBlockEntity.java'), 'nbt.putShort("positions", positions);', 'nbt.putShort("tiles", positions);')
        with self.assertRaisesRegex(AssertionError, 'save writer'):
            guard.validate_contracts(self.contracts)

    def test_item_needs_own_adapter(self):
        path = Path('src/main/resources/assets/tfc/models/item/invalid_pile.json')
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps({'parent': 'tfc:block/ingot_pile'}))
        with self.assertRaisesRegex(AssertionError, 'ItemModel adapter'):
            guard.validate_contracts(self.contracts)

    def test_reload_catalog_replaced(self):
        self.change(str(guard.MODELS / 'MoldTableBlockModel.java'), 'discovered = Map.copyOf(catalog);', '// keep previous discovery catalog')
        with self.assertRaisesRegex(AssertionError, 'reload/dependency'):
            guard.validate_contracts(self.contracts)


if __name__ == '__main__':
    unittest.main()
