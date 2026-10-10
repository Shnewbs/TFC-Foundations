"""Exact-target worldgen API checks: source contracts + actual JDK 25 class signatures.

These are NOT mod loading, registered world generation, or gameplay tests.
"""

import os
from pathlib import Path
import subprocess
import unittest

ROOT = Path('src/main/java/net/dries007/tfc/world')
CLASSPATH = Path('port-diagnostics/classpath.txt')


def source(name: str) -> str:
    return (ROOT / name).read_text()


class WorldgenApiContracts(unittest.TestCase):
    def test_structure_generation_dimension_forwarding(self):
        body = source('TFCChunkGenerator.java')
        self.assertIn('ResourceKey<net.minecraft.world.level.Level> dimension)', body)
        self.assertIn('chunkDataGenerator.generate(chunk);', body)
        self.assertIn('super.createStructures(dynamicRegistry, structureState, structureFeatureManager, chunk, templateManager, dimension)', body)

    def test_noise_uses_target_resource_identifier(self):
        body = source('noise/NoiseSampler.java')
        self.assertIn('holder.noiseData().unwrapKey().orElseThrow().identifier()', body)

    def test_registry_codec_rejects_missing_values(self):
        body = source('Codecs.java')
        self.assertIn('registry.containsKey(id) ? DataResult.success(registry.getValue(id))', body)
        self.assertIn('registry.containsValue(value) ? DataResult.success(registry.getKey(value))', body)

    def test_biome_extension_is_optional_not_defaulted(self):
        body = source('biome/TFCBiomes.java')
        self.assertIn('getResourceKey(biome)', body)
        self.assertIn('.flatMap(key -> REGISTRY.getOptional(key.identifier()))', body)
        self.assertIn('.orElse(null)', body)

    def test_erosion_uses_native_nullable_post_process_position(self):
        body = source('feature/ErosionFeature.java')
        self.assertIn('final BlockPos postProcessPos = state.getPostProcessPos(level, pos)', body)
        self.assertIn('if (postProcessPos != null)', body)
        self.assertIn('chunk.markPosForPostprocessing(postProcessPos)', body)

    def test_conditional_features_include_then_branch(self):
        body = source('feature/IfThenConfig.java')
        self.assertIn('public Stream<Holder<ConfiguredFeature<?, ?>>> getSubFeatures()', body)
        self.assertIn('Stream.concat(ifFeature.value().getFeatures(), thenFeature.value().getFeatures())', body)
        self.assertIn('PlacedFeature.CODEC.fieldOf("if")', body)
        self.assertIn('PlacedFeature.CODEC.fieldOf("then")', body)

    def test_fluid_feature_provides_worldgen_level(self):
        body = source('feature/plant/BlockWithFluidFeature.java')
        self.assertIn('.getState(level, context.random(), pos)', body)
        self.assertIn('FluidHelpers.fillWithFluid', body)

    def test_random_property_provider_uses_new_callback(self):
        body = source('stateprovider/RandomPropertyProvider.java')
        self.assertIn('getState(net.minecraft.world.level.WorldGenLevel level, RandomSource random, BlockPos pos)', body)
        self.assertIn('return propertySetter.apply(random);', body)

    def test_resolved_target_signatures(self):
        self.assertTrue(CLASSPATH.is_file(), 'Exact target classpath is not available')
        jdk = Path(os.environ['JAVA_HOME']) / 'bin' / 'javap' if os.environ.get('JAVA_HOME') else 'javap'
        names = [
            'net.minecraft.resources.ResourceKey',
            'net.minecraft.core.Registry',
            'net.minecraft.world.level.chunk.ChunkGenerator',
            'net.minecraft.world.level.levelgen.placement.PlacedFeature',
            'net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider',
            'net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase',
        ]
        result = subprocess.run([str(jdk), '-classpath', CLASSPATH.read_text().strip(), '-public', *names],
                                capture_output=True, text=True, timeout=40)
        self.assertEqual(result.returncode, 0, result.stderr[:500])
        signatures = result.stdout
        for fragment in (
            'net.minecraft.resources.Identifier identifier()',
            'java.util.Optional<T> getOptional(net.minecraft.resources.Identifier)',
            'net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>',
            'java.util.stream.Stream<net.minecraft.core.Holder<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>>> getFeatures()',
            'getState(net.minecraft.world.level.WorldGenLevel, net.minecraft.util.RandomSource, net.minecraft.core.BlockPos)',
            'getPostProcessPos(net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos)',
        ):
            self.assertIn(fragment, signatures)


if __name__ == '__main__':
    unittest.main(verbosity=2)
