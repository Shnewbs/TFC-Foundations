"""Mutation tests for the real seasonal asset/registration guards; no game runtime is simulated."""
import json
import os
from pathlib import Path
import shutil
import tempfile
import unittest

import run_seasonal_block_smoke as guard


class SeasonalContractTests(unittest.TestCase):
    def setUp(self):
        self.root = Path.cwd()
        self.temp = tempfile.TemporaryDirectory()
        self.contracts = json.loads(guard.CONTRACTS.read_text())
        files = set(self.contracts['unchanged_model_assets']) | set(self.contracts['blockstates'])
        files |= {
            'tools/porting/SeasonalLegacyReference.java',
            'src/main/java/net/dries007/tfc/client/ClientEventHandler.java',
            *(str(guard.MODEL_ROOT / (name + '.java')) for name in ('BlockModelRegistration', 'PlantBlockModel', 'LeavesBlockModel')),
        }
        for filename in files:
            target = Path(self.temp.name) / filename
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(self.root / filename, target)
        os.chdir(self.temp.name)

    def tearDown(self):
        os.chdir(self.root)
        self.temp.cleanup()

    def test_valid_checkpoint(self):
        self.assertEqual(guard.validate_assets(self.contracts), (178, 245))

    def test_missing_dispatch_rejected(self):
        path = Path('src/main/resources/assets/tfc/blockstates/wood/leaves/oak.json')
        data = json.loads(path.read_text())
        del data['variants']['']['type']
        path.write_text(json.dumps(data))
        with self.assertRaisesRegex(AssertionError, 'bypasses dynamic dispatch'):
            guard.validate_assets(self.contracts)

    def test_weight_change_rejected(self):
        path = Path('src/main/resources/assets/tfc/blockstates/wood/leaves/chestnut.json')
        data = json.loads(path.read_text())
        data['multipart'][1]['apply'][0]['weight'] += 1
        path.write_text(json.dumps(data))
        with self.assertRaisesRegex(AssertionError, 'weights'):
            guard.validate_assets(self.contracts)

    def test_item_requires_separate_adapter(self):
        path = Path('src/main/resources/assets/tfc/models/item/new_seasonal_item.json')
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps({'parent': 'tfc:block/wood/leaves/oak_dynamic'}))
        with self.assertRaisesRegex(AssertionError, 'ItemModel adapter'):
            guard.validate_assets(self.contracts)

    def test_registration_listener_required(self):
        path = Path('src/main/java/net/dries007/tfc/client/ClientEventHandler.java')
        path.write_text(path.read_text().replace('bus.addListener(BlockModelRegistration::registerBlockStateModels);', ''))
        with self.assertRaisesRegex(AssertionError, 'registration listener'):
            guard.validate_assets(self.contracts)

    def test_frozen_oracle_guard(self):
        path = Path('tools/porting/SeasonalLegacyReference.java')
        path.write_text(path.read_text().replace('return 3;', 'return 2;'))
        with self.assertRaisesRegex(AssertionError, 'oracle changed'):
            guard.validate_assets(self.contracts)


if __name__ == '__main__':
    unittest.main()
