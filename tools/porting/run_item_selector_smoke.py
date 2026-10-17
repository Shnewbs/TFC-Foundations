"""Offline source and resource tests for native Minecraft 26.x item selectors.

No Minecraft, NeoForge, or game stubs are used; this verifies generated resource
structure and proves special legacy models are NOT silently flattened.
"""

import json
from pathlib import Path
import tempfile
import math
import unittest

from generate_item_selectors import generate, has_legacy_tint, scan_models, native_layered_item, native_special_item

PROJECT = Path(__file__).resolve().parents[2]
LEGACY = PROJECT / 'src/main/resources/assets/tfc/models/item'


class ItemSelectorGenerationTests(unittest.TestCase):
    def test_real_source_is_exhaustive_and_special_models_remain_unmodified(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / 'resources'
            report = Path(tmp) / 'report.json'
            s = generate(LEGACY, out, (), report)
            self.assertEqual(s['source_count'], 5614)
            self.assertEqual(s['generated_count'], 5613)
            self.assertEqual(s['generated_static_count'], 5317)
            self.assertEqual(s['generated_fixed_fluid_count'], 66)
            self.assertEqual(s['generated_layered_count'], 60)
            self.assertEqual(s['generated_special_count'], 170)
            self.assertEqual(s['manual_migration_count'], 1)
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
                elif model.get('loader') in ('tfc:fluid_container', 'tfc:trim'):
                    self.assertEqual(generated_model, {'type': model['loader'], 'parent': model['parent'], 'textures': model['textures']})
                elif native_special_item(rel, model, LEGACY) is not None:
                    self.assertEqual(generated_model, native_special_item(rel, model, LEGACY)['model'])
                else:
                    self.assertEqual(generated_model, {
                        'type': 'minecraft:model', 'model': 'tfc:item/' + rel,
                    })
            for special, reasons in s['manual_migrations'].items():
                self.assertTrue(reasons)
                self.assertFalse((out / 'assets/tfc/items' / (special + '.json')).exists())
            self.assertNotIn('wooden_bucket', s['manual_migrations'])
            self.assertNotIn('bucket/beer', s['manual_migrations'])
            self.assertNotIn('powderkeg', s['manual_migrations'])
            self.assertIn('tintindex', s['manual_migrations']['grass_inv'])

    def test_predicate_and_context_semantics_match_originals(self):
        contexts = ('none', 'gui', 'ground', 'fixed', 'head', 'firstperson_lefthand',
                    'firstperson_righthand', 'thirdperson_lefthand', 'thirdperson_righthand')
        properties = ('tfc:cast', 'tfc:throwing', 'tfc:damaged', 'tfc:sealed', 'tfc:heat', 'blocking')
        values = (-1, 0, 0.001, 0.2999, 0.3, 0.3999, 0.4, 0.4999, 0.5, 0.7499, 0.75, 0.8999, 0.9, 0.9999, 1, 2)
        checks = 0
        def native(node, context, value):
            if node['type'] == 'minecraft:range_dispatch':
                branches = [e['model'] for e in node['entries'] if value >= e['threshold']]
                return native(branches[-1] if branches else node['fallback'], context, value)
            if node['type'] == 'minecraft:select':
                return native(next((c['model'] for c in node['cases'] if c['when'] == context), node['fallback']), context, value)
            if node['type'] == 'minecraft:special':
                return node['base']
            return node['model']
        def legacy(identifier, model, context, value):
            for override in reversed(model.get('overrides', [])):
                if all(value >= v for v in override['predicate'].values()):
                    target = override['model']
                    if target.startswith('tfc:item/'):
                        child = json.loads((LEGACY / (target.removeprefix('tfc:item/') + '.json')).read_text())
                        return legacy(target, child, context, value)
                    return target
            if model.get('loader') == 'neoforge:separate_transforms':
                target = model['perspectives'].get(context, model['base'])['parent']
                child = json.loads((LEGACY / (target.removeprefix('tfc:item/') + '.json')).read_text())
                return legacy(target, child, context, value)
            return identifier
        for path in LEGACY.rglob('*.json'):
            model = json.loads(path.read_text())
            if not model.get('overrides') and model.get('loader') != 'neoforge:separate_transforms':
                continue
            item = path.relative_to(LEGACY).with_suffix('').as_posix()
            converted = native_special_item(item, model, LEGACY)
            self.assertIsNotNone(converted, item)
            for context in contexts:
                for value in values:
                    self.assertEqual(native(converted['model'], context, value), legacy('tfc:item/' + item, model, context, value), (item, context, value))
                    checks += 1
        self.assertEqual(checks, 14544)

    def test_chest_and_javelin_specials_preserve_materials(self):
        for path in LEGACY.glob('wood/*chest/*.json'):
            item = path.relative_to(LEGACY).with_suffix('').as_posix()
            node = native_special_item(item, json.loads(path.read_text()), LEGACY)['model']
            self.assertEqual(node['base'], 'tfc:item/' + item)
            kind = 'trapped' if '/trapped_chest/' in item else 'normal'
            self.assertEqual(node['model'], {'type': 'minecraft:chest', 'texture': 'tfc:' + kind + '/' + path.stem})
        for item, material in [('metal/javelin/copper_in_hand', 'copper'), ('stone/javelin/metamorphic_throwing_base', 'stone'), ('obsidian_javelin_in_hand', 'obsidian')]:
            model = json.loads((LEGACY / (item + '.json')).read_text())
            node = native_special_item(item, model, LEGACY)['model']
            self.assertEqual(node['model'], {'type': 'tfc:javelin', 'texture': 'tfc:textures/entity/projectiles/' + material + '_javelin.png'})

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

    def test_layered_codecs_reject_unknown_fields_and_preserve_slots(self):
        fluid = {'loader': 'tfc:fluid_container', 'parent': 'neoforge:item/default',
                 'textures': {'base': 'tfc:item/base', 'fluid': 'tfc:item/mask', 'cover': 'tfc:item/cover'}}
        self.assertEqual(native_layered_item(fluid)['model']['textures'], fluid['textures'])
        for key, value in [('display', {}), ('overrides', []), ('flip_gas', True)]:
            self.assertIsNone(native_layered_item(dict(fluid, **{key: value})))
        self.assertIsNone(native_layered_item(dict(fluid, textures={'base': '#missing', 'fluid': 'tfc:item/mask'})))
        trim = {'loader': 'tfc:trim', 'parent': 'neoforge:item/default',
                'textures': {'armor': 'minecraft:item/leather_boots', 'overlay': 'minecraft:item/leather_boots_overlay', 'trim': 'tfc:item/boots_trim'}}
        self.assertEqual(native_layered_item(trim)['model']['textures'], trim['textures'])

    def test_vanilla_trim_overrides_have_native_selectors(self):
        source = PROJECT / 'src/main/resources/assets/minecraft/models/item'
        count = 0
        for legacy in source.glob('*.json'):
            model = json.loads(legacy.read_text())
            if model.get('loader') != 'tfc:trim':
                continue
            selector = json.loads((PROJECT / 'src/main/resources/assets/minecraft/items' / legacy.name).read_text())['model']
            self.assertEqual(selector['type'], 'tfc:trim')
            self.assertEqual(selector['parent'], model['parent'])
            self.assertEqual(selector['textures'], model['textures'])
            self.assertEqual(selector.get('tints', []), [{'type': 'minecraft:dye', 'default': 10511680}] if legacy.stem.startswith('leather_') else [])
            count += 1
        self.assertEqual(count, 8)

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
