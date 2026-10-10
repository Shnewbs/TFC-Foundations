"""Generate MC 26.1.2 item model selectors for unambiguous, static TFC models.

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
    for original in sorted(source.rglob('*.json')):
        if original.is_symlink():
            raise ValueError(f'Unexpected symlink in legacy item models: {original}')
        relative = original.relative_to(source)
        model = json.loads(original.read_text(encoding='utf-8'))
        if not isinstance(model, dict):
            raise ValueError(f'Expected JSON object: {original}')
        item_id = relative.with_suffix('').as_posix()
        reasons = sorted(MODEL_KEYS_REQUIRING_MIGRATION.intersection(model))
        if has_legacy_tint(model):
            reasons.append('tintindex')
        if any((root / relative).is_file() for root in authored_roots):
            reasons.append('existing_selector')
        if reasons:
            skipped[item_id] = reasons
            continue
        definition = {'model': {'type': 'minecraft:model', 'model': f'tfc:item/{item_id}'}}
        desired[relative] = json.dumps(definition, indent=2) + '\n'
    return desired, skipped


def generate(source: Path, output: Path, authored_roots: tuple[Path, ...], report: Path | None):
    desired, skipped = scan_models(source, authored_roots)
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
        'manual_migration_count': len(skipped),
        'manual_migrations': dict(sorted(skipped.items())),
    }
    if report is not None:
        report.parent.mkdir(parents=True, exist_ok=True)
        report.write_text(json.dumps(summary, indent=2, sort_keys=True) + '\n', encoding='utf-8')
    print(f"Generated {len(desired)} static TFC item definitions; "
          f"{len(skipped)} special models still require manual 26.x adapters.")
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
