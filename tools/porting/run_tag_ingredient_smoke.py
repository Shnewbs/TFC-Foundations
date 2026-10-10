"""Guard holder-backed tag ingredient semantics for Minecraft 26.1.2.

This verifies actual pinned bytecode signatures and production-source routing;
it is not a game/bootstrap/JEI runtime test.
"""
from pathlib import Path
import subprocess
import unittest

ROOT=Path(__file__).resolve().parents[2]
MAIN=ROOT/'src/main/java/net/dries007/tfc'

class TagIngredientContracts(unittest.TestCase):
    def source(self,name):
        return (MAIN/name).read_text()

    def test_native_holder_set_constructor_is_available(self):
        cp=(ROOT/'port-diagnostics/classpath.txt').read_text().strip()
        s=subprocess.run(['javap','-classpath',cp,'-public','net.minecraft.world.item.crafting.Ingredient'],capture_output=True,text=True,check=True).stdout
        self.assertIn('Ingredient of(net.minecraft.core.HolderSet<net.minecraft.world.item.Item>)',s)

    def test_shared_helper_uses_tag_holder_lookup(self):
        s=self.source('common/recipes/RecipeHelpers.java')
        self.assertIn('Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tag))',s)
        self.assertIn('public static Ingredient ingredient(TagKey<Item> tag)',s)

    def test_recipe_builtins_preserve_plain_itemlike_overloads(self):
        s=self.source('util/DataGenerationHelpers.java')
        self.assertIn('public Builder input(ItemLike item, int count) {return input(Ingredient.of(item), count);}',s)
        self.assertIn('public Builder inputIsPrimary(ItemLike item) {return inputIsPrimary(Ingredient.of(item));}',s)
        self.assertIn('public Builder input(char key, ItemLike input) {return input(key, Ingredient.of(input));}',s)
        for snippet in ('input(RecipeHelpers.ingredient(item), count)', 'inputIsPrimary(RecipeHelpers.ingredient(item))', 'input(key, RecipeHelpers.ingredient(input))'):
            self.assertIn(snippet,s)

    def test_tags_and_predator_food_are_unmodified(self):
        specs={
            'common/entities/livestock/horse/TFCChestedHorse.java':'getFoodTag()',
            'common/entities/livestock/horse/TFCHorse.java':'getFoodTag()',
            'common/entities/prey/TFCRabbit.java':'getFoodTag()',
            'common/entities/ai/prey/TFCOcelot.java':'TFCTags.Items.CAT_FOOD',
            'compat/jei/category/ChiselRecipeCategory.java':'TFCTags.Items.TOOLS_CHISEL',
            'compat/jei/category/SimpleItemRecipeCategory.java':'getToolTag()',
            'compat/jei/category/WeldingRecipeCategory.java':'TFCTags.Items.WELDING_FLUX',
        }
        for name,tag in specs.items():
            s=self.source(name)
            self.assertIn('RecipeHelpers.ingredient('+tag+')',s,name)
            self.assertNotIn('Ingredient.of('+tag+')',s,name)


if __name__ == '__main__':
    unittest.main(verbosity=2)
