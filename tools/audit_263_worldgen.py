#!/usr/bin/env python3
"""Inventory Minecraft 26.3 worldgen migration blockers without changing gameplay.

Usage: python3 tools/audit_263_worldgen.py [repository-root]
Exit 0 means the scan completed, NOT that the port compiles or runs.
"""
from __future__ import annotations

import collections
import json
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java"
RESOURCES = (ROOT / "src/main/resources", ROOT / "src/generated/resources")

# Only match APIs whose 26.3 removal/change is documented. These are migration
# work items, not substitutions: features, configs and codecs must be redesigned.
CHECKS = {
    "removed feature configurations package": "net.minecraft.world.level.levelgen.feature.configurations",
    "removed FeaturePlaceContext": "FeaturePlaceContext",
    "removed ConfiguredFeature": "ConfiguredFeature",
    "removed ConfiguredWorldCarver": "ConfiguredWorldCarver",
    "removed CarvingContext": "CarvingContext",
    "old density function namespace": "net.minecraft.world.level.levelgen.DensityFunction",
    "old configured-feature registry key": "Registries.CONFIGURED_FEATURE",
    "old feature type registry": "BuiltInRegistries.FEATURE",
}

def main() -> int:
    if not JAVA.is_dir():
        print(f"Missing Java sources: {JAVA}", file=sys.stderr)
        return 2

    counts = collections.Counter()
    locations: dict[str, list[str]] = collections.defaultdict(list)
    for path in sorted(JAVA.rglob("*.java")):
        content = path.read_text(encoding="utf-8")
        # Ignore comments when possible; conservative scan may overcount words
        # in documentation. Each hit counts a FILE, never total API invocations.
        for name, needle in CHECKS.items():
            if needle in content:
                counts[name] += 1
                locations[name].append(str(path.relative_to(ROOT)))

    legacy_json = []
    scanned_json = 0
    for base in RESOURCES:
        if not base.exists():
            continue
        for path in sorted(base.rglob("*.json")):
            if "/worldgen/configured_feature/" not in path.as_posix():
                continue
            scanned_json += 1
            try:
                data = json.loads(path.read_text(encoding="utf-8"))
            except (OSError, json.JSONDecodeError) as exc:
                print(f"Invalid JSON: {path}: {exc}", file=sys.stderr)
                return 2
            legacy_json.append(str(path.relative_to(ROOT)))
            if not isinstance(data, dict):
                print(f"Invalid legacy feature document: {path}", file=sys.stderr)
                return 2

    print("Minecraft 26.3 worldgen migration inventory (not a build validation)")
    print(f"Java source files scanned: {sum(1 for _ in JAVA.rglob('*.java'))}")
    for name in CHECKS:
        print(f"\n{name}: {counts[name]} file(s)")
        for path in locations[name][:20]:
            print(f"  {path}")
        if len(locations[name]) > 20:
            print(f"  ... {len(locations[name]) - 20} more")
    print(f"\nLegacy worldgen/configured_feature JSON files: {scanned_json}")
    for path in legacy_json[:30]:
        print(f"  {path}")
    if len(legacy_json) > 30:
        print(f"  ... {len(legacy_json) - 30} more")
    print("\nDo not bulk-rename old features: Minecraft 26.3 merges configuration into Feature,")
    print("moves worldgen datapack entries, and requires per-feature codecs/placement updates.")
    print("Scan succeeded; compiler, resource generation, runtime and gameplay gates remain open.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
