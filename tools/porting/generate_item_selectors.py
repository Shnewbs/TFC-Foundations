"""Generate MC 26.1.2 selectors for static models and reviewed native item adapters.

The 26.x client loads assets/tfc/items/<item>.json, not the old model
file directly. Never guess a mapping for custom loaders, predicates or tints:
those models require native 26.x item-model and client-property adapters.

This tool writes only into its dedicated build output tree. All other assets,
including the original legacy models and authored selectors, are untouched.
"""

import argparse
import json
from pathlib import Path


MODEL_KEYS_REQUIRING_MIGRATION = frozenset({'loader', 'overrides'})
FIXED_FLUID_MODEL_FIELDS = frozenset({'__comment__', 'loader', 'parent', 'fluid'})


def native_fixed_fluid_container(model: dict) -> dict | None:
    """Migrate only the fixed-fluid NeoForge bucket model supported by 26.1.2.

    The old parent neoforge:item/bucket carries vanilla bucket base and NeoForge
    fluid mask textures. Exact target DynamicFluidContainerModel$Unbaked has the
    same explicit fluid property and matching texture slots in its MapCodec.
    """
    if (model.get('loader') != 'neoforge:fluid_container' or
            model.get('parent') != 'neoforge:item/bucket' or
            not set(model).issubset(FIXED_FLUID_MODEL_FIELDS)):
        return None
    fluid = model.get('fluid')
    if not isinstance(fluid, str) or ':' not in fluid or not fluid.startswith('tfc:'):
        return None
    return {'model': {
        'type': 'neoforge:fluid_container',
        'textures': {'base': 'minecraft:item/bucket', 'fluid': 'neoforge:item/mask/bucket_fluid'},
        'fluid': fluid,
    }}


def native_layered_item(model: dict) -> dict | None:
    """Preserve reviewed TFC fluid/trim texture slots in native ItemModel codecs."""
    loader = model.get('loader')
    if loader not in ('tfc:fluid_container', 'tfc:trim'):
        return None
    if model.get('parent') != 'neoforge:item/default' or not set(model).issubset(
            {'__comment__', 'loader', 'parent', 'textures'}):
        return None
    textures = model.get('textures')
    allowed = {'particle', 'base', 'fluid', 'cover'} if loader == 'tfc:fluid_container' else {'armor', 'overlay', 'trim'}
    required = {'base', 'fluid'} if loader == 'tfc:fluid_container' else {'armor', 'trim'}
    if not isinstance(textures, dict) or not required.issubset(textures) or not set(textures).issubset(allowed):
        return None
    if any(not isinstance(value, str) or ':' not in value or value.startswith('#') for value in textures.values()):
        return None
    return {'model': {'type': loader, 'parent': model['parent'], 'textures': textures}}



REVIEWED_PROPERTIES = {'tfc:cast', 'tfc:throwing', 'tfc:damaged', 'tfc:sealed', 'tfc:heat', 'blocking'}
DISPLAY_CONTEXTS = {'none', 'gui', 'ground', 'fixed', 'head', 'firstperson_lefthand',
                    'firstperson_righthand', 'thirdperson_lefthand', 'thirdperson_righthand', 'tfc:placed_item'}


def native_special_item(item_id: str, model: dict, source: Path, seen: frozenset[str] = frozenset()) -> dict | None:
    """Convert the inspected legacy transforms/predicates without altering source models.

    Every branch is a native ItemModel; built-in entity parents are never treated
    as ordinary cuboid geometry. Unknown loaders/properties remain explicit gaps.
    """
    if item_id in seen:
        raise ValueError(f'Cyclic special item model: {item_id}')
    seen = seen | {item_id}

    def reference(identifier):
        if not isinstance(identifier, str):
            return None
        if identifier.startswith('tfc:item/'):
            key = identifier.removeprefix('tfc:item/')
            path = source / (key + '.json')
            if not path.is_file():
                return None
            if path.is_file():
                child = json.loads(path.read_text(encoding='utf-8'))
                converted = native_special_item(key, child, source, seen)
                if converted is not None:
                    return converted['model']
                if child.get('loader') or child.get('overrides'):
                    return None
        return {'type': 'minecraft:model', 'model': identifier}

    if item_id == 'pan/filled' and model.get('parent') == 'tfc:item/entity_with_transforms':
        return {'model': {'type': 'tfc:pan'}}
    if (item_id.startswith(('wood/chest/', 'wood/trapped_chest/')) and
            model.get('parent') in ('item/chest', 'minecraft:item/chest')):
        folder = 'trapped' if item_id.startswith('wood/trapped_chest/') else 'normal'
        return {'model': {'type': 'minecraft:special', 'base': 'tfc:item/' + item_id,
                          'model': {'type': 'minecraft:chest', 'texture': f'tfc:{folder}/{item_id.split("/")[-1]}'}}}
    if model.get('parent') in ('item/trident_in_hand', 'item/trident_throwing',
                               'minecraft:item/trident_in_hand', 'minecraft:item/trident_throwing'):
        if 'javelin' not in item_id:
            return None
        material = item_id.split('/')[-1].removesuffix('_in_hand').removesuffix('_throwing_base')
        material = material if item_id.startswith('metal/javelin/') else 'obsidian' if item_id.startswith('obsidian_javelin') else 'stone'
        return {'model': {'type': 'minecraft:special', 'base': 'tfc:item/' + item_id,
                          'model': {'type': 'tfc:javelin', 'texture': f'tfc:textures/entity/projectiles/{material}_javelin.png'}}}

    if model.get('loader') == 'neoforge:separate_transforms':
        if not set(model).issubset({'__comment__', 'loader', 'base', 'perspectives', 'textures', 'gui_light', 'overrides'}):
            return None
        base, perspectives = model.get('base'), model.get('perspectives')
        if not isinstance(base, dict) or set(base) != {'parent'} or not isinstance(perspectives, dict):
            return None
        fallback = reference(base['parent'])
        if fallback is None:
            return None
        cases = []
        for context, part in perspectives.items():
            if context not in DISPLAY_CONTEXTS or not isinstance(part, dict) or set(part) != {'parent'}:
                return None
            child = reference(part['parent'])
            if child is None:
                return None
            cases.append({'when': context, 'model': child})
        result = {'type': 'minecraft:select', 'property': 'minecraft:display_context', 'cases': cases, 'fallback': fallback}
    elif model.get('loader'):
        return None
    else:
        result = {'type': 'minecraft:model', 'model': 'tfc:item/' + item_id}

    overrides = model.get('overrides', [])
    if overrides:
        # Only one reviewed numeric property per model, with ascending thresholds,
        # has the same last-match semantics as a native range select.
        entries, property_id = [], None
        for override in overrides:
            if set(override) != {'predicate', 'model'} or len(override['predicate']) != 1:
                return None
            key, threshold = next(iter(override['predicate'].items()))
            if key not in REVIEWED_PROPERTIES or (property_id is not None and key != property_id):
                return None
            if not isinstance(threshold, (int, float)) or (entries and threshold <= entries[-1]['threshold']):
                return None
            property_id = key
            child = reference(override['model'])
            if child is None:
                return None
            entries.append({'threshold': threshold, 'model': child})
        result = {'type': 'minecraft:range_dispatch', 'property': 'tfc:blocking' if property_id == 'blocking' else property_id,
                  'entries': entries, 'fallback': result}
    elif model.get('loader') != 'neoforge:separate_transforms':
        return None
    return {'model': result}

def has_legacy_tint(model: object) -> bool:
    """Older built-in model face tint-index cannot be inferred from an item key."""
    if isinstance(model, dict):
        return any(k == 'tintindex' or has_legacy_tint(v) for k, v in model.items())
    if isinstance(model, list):
        return any(has_legacy_tint(v) for v in model)
    return False


def scan_models(source: Path, authored_roots: tuple[Path, ...]):
    if not source.is_dir():
        raise ValueError(f'Missing legacy item-model directory: {source}')
    desired: dict[Path, str] = {}
    skipped: dict[str, list[str]] = {}
    generated_fluid_count = 0
    generated_layered_count = 0
    generated_special_count = 0
    for original in sorted(source.rglob('*.json')):
        if original.is_symlink():
            raise ValueError(f'Unexpected symlink in legacy item models: {original}')
        relative = original.relative_to(source)
        model = json.loads(original.read_text(encoding='utf-8'))
        if not isinstance(model, dict):
            raise ValueError(f'Expected JSON object: {original}')
        item_id = relative.with_suffix('').as_posix()
        reasons = []
        if any((root / relative).is_file() for root in authored_roots):
            reasons.append('existing_selector')
        fluid_definition = native_fixed_fluid_container(model)
        layered_definition = native_layered_item(model)
        special_definition = native_special_item(item_id, model, source)
        if fluid_definition is None and layered_definition is None and special_definition is None:
            reasons.extend(sorted(MODEL_KEYS_REQUIRING_MIGRATION.intersection(model)))
        if has_legacy_tint(model):
            reasons.append('tintindex')
        if reasons:
            skipped[item_id] = reasons
            continue
        if fluid_definition is not None:
            definition = fluid_definition
            generated_fluid_count += 1
        elif layered_definition is not None:
            definition = layered_definition
            generated_layered_count += 1
        elif special_definition is not None:
            definition = special_definition
            generated_special_count += 1
        else:
            definition = {'model': {'type': 'minecraft:model', 'model': f'tfc:item/{item_id}'}}
        desired[relative] = json.dumps(definition, indent=2) + '\n'
    return desired, skipped, generated_fluid_count, generated_layered_count, generated_special_count


def generate(source: Path, output: Path, authored_roots: tuple[Path, ...], report: Path | None):
    desired, skipped, generated_fluid_count, generated_layered_count, generated_special_count = scan_models(source, authored_roots)
    items_root = output / 'assets' / 'tfc' / 'items'
    if output.resolve() == source.resolve() or source.resolve().is_relative_to(output.resolve()):
        raise ValueError('Output must not overwrite the source model directory')
    # Never remove author-maintained files. This directory is dedicated to outputs
    # from this generator and no other resources may be placed under it.
    expected = set(desired)
    if items_root.is_dir():
        for stale in items_root.rglob('*.json'):
            if stale.is_symlink():
                raise ValueError(f'Unexpected symlink in output: {stale}')
            if stale.relative_to(items_root) not in expected:
                stale.unlink()
    for relative, contents in desired.items():
        target = items_root / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        if not target.exists() or target.read_text(encoding='utf-8') != contents:
            target.write_text(contents, encoding='utf-8')

    summary = {
        'source_count': len(desired) + len(skipped),
        'generated_count': len(desired),
        'generated_static_count': len(desired) - generated_fluid_count - generated_layered_count - generated_special_count,
        'generated_special_count': generated_special_count,
        'generated_fixed_fluid_count': generated_fluid_count,
        'generated_layered_count': generated_layered_count,
        'manual_migration_count': len(skipped),
        'manual_migrations': dict(sorted(skipped.items())),
    }
    if report is not None:
        report.parent.mkdir(parents=True, exist_ok=True)
        report.write_text(json.dumps(summary, indent=2, sort_keys=True) + '\n', encoding='utf-8')
    print(f"Generated {len(desired) - generated_fluid_count - generated_layered_count - generated_special_count} static and "
          f"{generated_fluid_count} native fixed-fluid TFC item definitions; "
          f"{generated_layered_count} native TFC fluid/trim definitions; {generated_special_count} native special/predicate definitions; {len(skipped)} models still need 26.x adapters.")
    return summary


def main():
    root = Path(__file__).resolve().parents[2]
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source', type=Path,
                        default=root / 'src/main/resources/assets/tfc/models/item')
    parser.add_argument('--output', type=Path, required=True,
                        help='dedicated generated resources root (contains assets/tfc/items)')
    parser.add_argument('--authored', type=Path, action='append', default=None,
                        help='author-maintained assets/tfc/items root; these override generation')
    parser.add_argument('--report', type=Path, help='JSON report outside packaged resources')
    args = parser.parse_args()
    authored = tuple(args.authored) if args.authored is not None else (
        root / 'src/main/resources/assets/tfc/items',
        root / 'src/generated/resources/assets/tfc/items',
    )
    generate(args.source, args.output, authored, args.report)


if __name__ == '__main__':
    main()
