"""Record public signatures from the exact resolved target classpath, not a guessed version."""
import os
from pathlib import Path
import subprocess
import zipfile

OUT = Path('port-diagnostics')
CLASSPATH = OUT / 'classpath.txt'
NAMES = {
    'LootContext', 'LootParams', 'LootItemCondition', 'LootItemFunction',
    'LootItemConditionalFunction', 'NumberProvider', 'NumberProviders',
    'LootContextUser', 'ValidationContext', 'Validatable', 'ContextKey',
    'ContextKeySet', 'DeferredRegister', 'DeferredHolder', 'Registry',
    'LootContextParams', 'LootContextParamSets', 'NullMarked', 'Nullable',
    'EnumProperty', 'ContainerInput', 'TeleportTransition', 'IdentifierException',
}


def main():
    if not CLASSPATH.is_file():
        raise SystemExit('Target classpath unavailable; API inspection did not run.')
    cp = CLASSPATH.read_text().strip()
    classes = set()
    for entry in cp.split(os.pathsep):
        path = Path(entry)
        if path.suffix != '.jar' or not path.is_file():
            continue
        with zipfile.ZipFile(path) as jar:
            for name in jar.namelist():
                if name.startswith(('net/minecraft/', 'net/neoforged/', 'org/jspecify/')) and name.endswith('.class'):
                    classes.add(name[:-6].replace('/', '.'))
    (OUT / 'target-classes.txt').write_text('\n'.join(sorted(classes)) + '\n')
    selected = sorted(name for name in classes if name.rsplit('.', 1)[-1] in NAMES)
    result = subprocess.run(['javap', '-classpath', cp, '-public', *selected], text=True, capture_output=True, timeout=90)
    (OUT / 'target-api.txt').write_text(result.stdout + result.stderr)
    print(f'Inspected {len(selected)} API types from {len(classes)} target classes; javap exit {result.returncode}.')
    if result.returncode:
        raise SystemExit(result.returncode)
    # A separate process checks actual production classes; no Minecraft or TFC stubs.
    failed = False
    for probe in ('run_loot_smoke.py', 'run_common_smoke.py', 'run_visual_save_smoke.py', 'run_entity_smoke.py', 'run_model_render_smoke.py', 'run_livestock_render_smoke.py', 'run_mechanical_boat_smoke.py', 'run_seasonal_block_smoke.py', 'run_snapshot_block_smoke.py', 'run_worldgen_api_smoke.py', 'run_item_registration_smoke.py', 'run_predator_sound_smoke.py', 'run_item_selector_smoke.py', 'run_recipe_serializer_smoke.py', 'run_selected_slot_smoke.py', 'run_sound_event_holder_smoke.py', 'run_tool_material_smoke.py'):
        smoke = subprocess.run(['python3', 'tools/porting/' + probe], timeout=360)
        failed |= smoke.returncode != 0
    raise SystemExit(1 if failed else 0)


if __name__ == '__main__':
    main()
