"""Offline source and resource tests for native Minecraft 26.x item selectors.

No Minecraft, NeoForge, or game stubs are used; this verifies generated resource
structure and proves special legacy models are NOT silently flattened.
"""

import json
from pathlib import Path
import tempfile
import unittest

from generate_item_selectors import generate, has_legacy_tint, scan_models

PROJECT = Path(__file__).resolve().parents[2]
LEGACY = PROJECT / 'src/main/resources/assets/tfc/models/item'


class ItemSelectorGenerationTests(unittest.TestCase):
    def test_real_source_is_exhaustive_and_special_models_remain_unmodified(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / 'resources'
            report = Path(tmp) / 'report.json'
            s = generate(LEGACY, out, (), report)
            self.assertEqual(s['source_count'], 5597)
            self.assertEqual(s['generated_count'], 5435)
            self.assertEqual(s['generated_static_count'], 5369)
            self.assertEqual(s['generated_fixed_fluid_count'], 66)
            self.assertEqual(s['manual_migration_count'], 162)
            self.assertTrue(report.is_file())
            self.assertEqual(s, json.loads(report.read_text()))
            generated = list((out / 'assets/tfc/items').rglob('*.json'))
            self.assertEqual(len(generated), s['generated_count'])
            # The generated IDs and model paths remain unchanged, including
            # nested metal and food item identifiers.
            for selector in generated:
                rel = selector.relative_to(out / 'assets/tfc/items').with_suffix('').as_posix()
                legacy = LEGACY / (rel + '.json')
                self.assertTrue(legacy.is_file())
                model = json.loads(legacy.read_text())
                generated_model = json.loads(selector.read_text())['model']
                if model.get('loader') == 'neoforge:fluid_container':
                    self.assertEqual(generated_model, {
                        'type': 'neoforge:fluid_container',
                        'textures': {
                            'base': 'minecraft:item/bucket',
                            'fluid': 'neoforge:item/mask/bucket_fluid',
                        },
                        'fluid': model['fluid'],
                    })
                else:
                    self.assertEqual(generated_model, {
                        'type': 'minecraft:model', 'model': 'tfc:item/' + rel,
                    })
            for special, reasons in s['manual_migrations'].items():
                self.assertTrue(reasons)
                self.assertFalse((out / 'assets/tfc/items' / (special + '.json')).exists())
            self.assertIn('loader', s['manual_migrations']['wooden_bucket'])
            self.assertNotIn('bucket/beer', s['manual_migrations'])
            self.assertIn('overrides', s['manual_migrations']['powderkeg'])
            self.assertIn('tintindex', s['manual_migrations']['grass_inv'])

    def test_multiple_runs_are_deterministic_and_remove_stale_generated_files(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            src = root / 'models'
            src.mkdir()
            (src / 'plain.json').write_text('{"parent":"item/generated"}')
            (src / 'dynamic.json').write_text('{"loader":"tfc:fluid_container"}')
            out = root / 'out'
            s1 = generate(src, out, (), None)
            self.assertEqual((s1['generated_count'], s1['manual_migration_count']), (1, 1))
            original = (out / 'assets/tfc/items/plain.json').read_bytes()
            generate(src, out, (), None)
            self.assertEqual((out / 'assets/tfc/items/plain.json').read_bytes(), original)
            (src / 'plain.json').unlink()
            generate(src, out, (), None)
            self.assertFalse((out / 'assets/tfc/items/plain.json').exists())
            self.assertFalse((out / 'assets/tfc/items/dynamic.json').exists())

    def test_authored_selector_is_never_overwritten(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            src = root / 'models'
            src.mkdir()
            (src / 'known.json').write_text('{"parent":"item/generated"}')
            authored = root / 'authored'
            authored.mkdir()
            (authored / 'known.json').write_text('custom authored model')
            out = root / 'out'
            report = generate(src, out, (authored,), None)
            self.assertEqual(report['manual_migrations']['known'], ['existing_selector'])
            self.assertEqual((authored / 'known.json').read_text(), 'custom authored model')
            self.assertFalse((out / 'assets/tfc/items/known.json').exists())

    def test_custom_loader_override_and_tint_all_require_real_migrations(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            src = root / 'models'
            src.mkdir()
            (src / 'fluid.json').write_text('{"loader":"tfc:fluid_container"}')
            (src / 'fired.json').write_text('{"overrides": [{"predicate":{"tfc:heat":0.1},"model":"tfc:item/fire"}]}')
            (src / 'grass.json').write_text('{"elements": [{"faces": {"north": {"tintindex":0}}}]}')
            (src / 'static.json').write_text('{}')
            s = generate(src, root / 'out', (), None)
            self.assertEqual(s['generated_count'], 1)
            self.assertEqual(s['manual_migration_count'], 3)
            self.assertTrue(has_legacy_tint({'elements':[{'faces':{'north':{'tintindex':0}}}]}))
            self.assertFalse(has_legacy_tint({'layers':[{'tint':'#ff00ff'}]}))

    def test_native_fixed_fluid_codec_is_available_on_pinned_neoforge(self):
        import os
        import subprocess
        cp = PROJECT / 'port-diagnostics/classpath.txt'
        self.assertTrue(cp.is_file(), 'Exact-target NeoForge classpath missing')
        java_home = os.environ.get('JAVA_HOME')
        javap = str(Path(java_home)/'bin/javap') if java_home else 'javap'
        result = subprocess.run([
            javap, '-classpath', cp.read_text().strip(), '-p', '-c',
            'net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel$Unbaked'
        ], capture_output=True, text=True, timeout=40)
        self.assertEqual(result.returncode, 0, result.stderr[:400])
        self.assertIn('MapCodec<', result.stdout)
        for field in ('String textures', 'String fluid', 'String flip_gas',
                      'String cover_is_mask', 'String apply_fluid_luminosity'):
            self.assertIn(field, result.stdout)

    def test_gradle_resource_wiring_and_ci_validation(self):
        script = (PROJECT / 'build.gradle.kts').read_text()
        self.assertIn('generateTfcItemSelectors', script)
        self.assertIn('dependsOn(generateTfcItemSelectors)', script)
        self.assertIn('from(generatedItemSelectorsRoot)', script)
        self.assertIn('src/main/resources/assets/tfc/models/item', script)
        inspector = (PROJECT / 'tools/porting/inspect_api.py').read_text()
        self.assertIn('run_item_selector_smoke.py', inspector)


if __name__ == '__main__':
    unittest.main(verbosity=2)
