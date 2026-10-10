"""Checks the production native serializer migration against pinned target Java 25 APIs."""

import os
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]
RECIPES = ROOT / 'src/main/java/net/dries007/tfc/common/recipes'


class RecipeSerializerContracts(unittest.TestCase):
    def test_native_wrapper_was_removed(self):
        self.assertFalse((RECIPES / 'RecipeSerializerImpl.java').exists())
        for p in RECIPES.glob('*.java'):
            self.assertNotIn('RecipeSerializerImpl', p.read_text(), str(p))

    def test_custom_singletons_are_still_singletons(self):
        source = (RECIPES / 'TFCRecipeSerializers.java').read_text()
        for name, klass in [('food_combining', 'FoodCombiningCraftingRecipe'),
                            ('casting_crafting', 'CastingCraftingRecipe')]:
            expr = (f'register("{name}", new RecipeSerializer<>(MapCodec.unit('
                    f'{klass}.INSTANCE), StreamCodec.unit({klass}.INSTANCE)))')
            self.assertIn(expr, source)
        self.assertIn('return register(name, new RecipeSerializer<>(codec, stream));', source)
        self.assertIn('new Id<>(RECIPE_SERIALIZERS.register(name, () -> serializer))', source)

    def test_block_recipe_serializer_uses_native_record(self):
        source = (RECIPES / 'BlockRecipe.java').read_text()
        self.assertIn('return new RecipeSerializer<>(codec(factory), streamCodec(factory));', source)
        self.assertIn('BlockIngredient.CODEC.fieldOf("ingredient")', source)
        self.assertIn('Codecs.BLOCK_STATE.optionalFieldOf("result")', source)

    def test_target_native_recipe_serializer_signature_and_compile(self):
        cpfile = ROOT / 'port-diagnostics/classpath.txt'
        self.assertTrue(cpfile.is_file(), 'Pinned exact-target classpath required')
        cp = cpfile.read_text().strip()
        java_home = os.environ.get('JAVA_HOME')
        self.assertTrue(java_home, 'Java 25 environment missing')
        javap = str(Path(java_home) / 'bin/javap')
        javac = str(Path(java_home) / 'bin/javac')
        info = subprocess.run([javap, '-classpath', cp, '-public',
                               'net.minecraft.world.item.crafting.RecipeSerializer'],
                              capture_output=True, text=True, timeout=25)
        self.assertEqual(info.returncode, 0, info.stderr)
        self.assertIn('final class net.minecraft.world.item.crafting.RecipeSerializer<', info.stdout)
        self.assertIn('RecipeSerializer(com.mojang.serialization.MapCodec<T>, net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, T>);', info.stdout)
        source = '''
            import com.mojang.serialization.MapCodec;
            import net.minecraft.network.RegistryFriendlyByteBuf;
            import net.minecraft.network.codec.StreamCodec;
            import net.minecraft.world.item.crafting.Recipe;
            import net.minecraft.world.item.crafting.RecipeSerializer;
            class ExactNativeRecipeSerializerProbe {
                static <T extends Recipe<?>> RecipeSerializer<T> fromCodecs(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> stream) {
                    return new RecipeSerializer<>(codec, stream);
                }
                static <T extends Recipe<?>> RecipeSerializer<T> singleton(T instance) {
                    return new RecipeSerializer<>(MapCodec.unit(instance), StreamCodec.unit(instance));
                }
            }
        '''
        with tempfile.TemporaryDirectory() as tmp:
            f = Path(tmp) / 'ExactNativeRecipeSerializerProbe.java'
            f.write_text(source)
            compiled = subprocess.run([javac, '--release', '25', '-proc:none',
                                       '-classpath', cp, '-d', tmp, str(f)],
                                      capture_output=True, text=True, timeout=35)
            self.assertEqual(compiled.returncode, 0, compiled.stderr[:1200])
            self.assertTrue((Path(tmp) / 'ExactNativeRecipeSerializerProbe.class').is_file())


if __name__ == '__main__':
    unittest.main(verbosity=2)
