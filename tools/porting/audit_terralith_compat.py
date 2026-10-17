"""Audit an externally installed Terralith JAR against TFC resources; never copy pack content."""
import argparse
from collections import Counter
import hashlib
import json
from pathlib import Path
from zipfile import ZipFile


def audit(project: Path, external: Path):
    owned = set()
    for root in (project / 'src/main/resources', project / 'src/generated/resources'):
        owned.update(path.relative_to(root).as_posix() for path in (root / 'data').rglob('*.json'))
    with ZipFile(external) as archive:
        base = {name for name in archive.namelist() if name.startswith('data/') and name.endswith('.json')}
        kinds = Counter('/'.join(name.split('/')[2:4]) for name in base if '/worldgen/' in name)
        overrides = sorted(base & owned)
        terrain = sorted(name for name in base if '/worldgen/noise_settings/' in name or '/dimension/' in name)
        features = sorted(name for name in base if '/worldgen/configured_feature/' in name and '/tags/' not in name)
        types = Counter()
        block_names = set()

        def visit(value):
            if isinstance(value, dict):
                if isinstance(value.get('Name'), str):
                    block_names.add(value['Name'])
                for child in value.values():
                    visit(child)
            elif isinstance(value, list):
                for child in value:
                    visit(child)

        for name in features:
            definition = json.loads(archive.read(name))
            types[definition.get('type', '<missing>')] += 1
            visit(definition)
        metadata = json.loads(archive.read('pack.mcmeta'))
    return {
        'external_file': external.name,
        'external_sha256': hashlib.sha256(external.read_bytes()).hexdigest(),
        'pack_id': metadata['pack'].get('id'),
        'base_data_resources': len(base),
        'registry_counts': dict(sorted(kinds.items())),
        'exact_tfc_resource_collisions': overrides,
        'external_generator_resources': terrain,
        'configured_features': len(features),
        'configured_feature_types': dict(sorted(types.items())),
        'literal_block_palette_count': len(block_names),
        'literal_vanilla_ore_blocks': sorted(name for name in block_names if name.endswith('_ore')),
        'status': 'audit only; no Terralith content copied or injected into TFC',
        'limits': [
            'No overlap in filenames does not prove compatible terrain or progression.',
            'Conditional overlays, mixins and transitive feature references require separate runtime review.',
            'TFC owns geology, ore distribution, soils, climate, rivers, crops, forests and survival loot.',
            'Feature geometry must be adapted by an original compatibility layer before enabling it.',
        ],
    }


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('terralith_jar', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    result = audit(Path(__file__).resolve().parents[2], args.terralith_jar)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps({key: result[key] for key in ('external_file', 'base_data_resources', 'configured_features', 'literal_block_palette_count', 'status')}))
