"""Expose the original TFC armor art through Minecraft 26.1.2 equipment assets."""
import argparse
import json
from pathlib import Path
import re
import shutil


def generate(project: Path, output: Path):
    source = project / 'src/main/resources/assets/tfc/textures'
    materials = re.findall(r'public static final Id \w+ = register\("([a-z_]+)"',
        (project / 'src/main/java/net/dries007/tfc/common/TFCArmorMaterials.java').read_text())
    if not materials:
        raise ValueError('No registered armor materials found')
    expected = set()
    for material in materials:
        layers = {}
        for layer, original in (
            ('humanoid', f'models/armor/{material}_layer_1.png'),
            ('humanoid_leggings', f'models/armor/{material}_layer_2.png'),
            ('horse_body', f'entity/horse/armor/horse_armor_{material}.png'),
        ):
            target = output / f'assets/tfc/textures/entity/equipment/{layer}/{material}.png'
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source / original, target)
            expected.add(target)
            layers[layer] = [{'texture': f'tfc:{material}'}]
        target = output / f'assets/tfc/equipment/{material}.json'
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps({'layers': layers}, indent=2) + '\n')
        expected.add(target)
    # This directory is dedicated to this generator; remove obsolete generated aliases only.
    for target in output.rglob('*'):
        if target.is_file() and target not in expected:
            target.unlink()
    print(f'Generated {len(materials)} equipment definitions and {len(materials) * 3} unchanged armor texture aliases.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    generate(Path(__file__).resolve().parents[2], args.output.resolve())
