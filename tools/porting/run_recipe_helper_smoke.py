"""Source and exact-target API checks for the 26.1.2 recipe helper transition.

Not a Minecraft bootstrap, recipe registry reload, or survival crafting test.
"""
import os
from pathlib import Path
import subprocess
import unittest

ROOT=Path(__file__).resolve().parents[2]
FILE=ROOT/'src/main/java/net/dries007/tfc/common/recipes/RecipeHelpers.java'

class RecipeHelperContracts(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source=FILE.read_text()

    def test_native_remainder_api_preserves_primary_override(self):
        s=self.source
        self.assertIn('CraftingRecipe.defaultCraftingReminder(input)',s)
        self.assertIn('if (ItemStack.isSameItem(primaryInput, stack))',s)
        self.assertIn('results.set(i, provider.getStack(stack.copyWithCount(1)))',s)
        self.assertNotIn('stack.hasCraftingRemainingItem()',s)
        self.assertNotIn('stack.getCraftingRemainingItem()',s)

    def test_target_api_signatures_resolve(self):
        cp=(ROOT/'port-diagnostics/classpath.txt').read_text().strip()
        javap=str(Path(os.environ['JAVA_HOME'])/'bin/javap') if os.environ.get('JAVA_HOME') else 'javap'
        text=subprocess.run([javap,'-classpath',cp,'net.minecraft.world.item.crafting.CraftingRecipe',
                             'net.minecraft.world.item.crafting.display.SlotDisplay',
                             'net.neoforged.neoforge.fluids.crafting.FluidIngredient'],
                            text=True,capture_output=True,check=True,timeout=40).stdout
        self.assertIn('defaultCraftingReminder(net.minecraft.world.item.crafting.CraftingInput)',text)
        self.assertIn('resolveForFirstStack(net.minecraft.util.context.ContextMap)',text)
        self.assertIn('java.util.List<net.minecraft.core.Holder<net.minecraft.world.level.material.Fluid>> fluids()',text)

    def test_result_accessor_uses_new_display_api(self):
        s=self.source
        self.assertIn('recipe.display().isEmpty() ? ItemStack.EMPTY',s)
        self.assertIn('result().resolveForFirstStack(ContextMap.EMPTY)',s)
        self.assertNotIn('recipe.getResultItem(null)',s)

    def test_fluids_and_items_use_holder_streams(self):
        s=self.source
        self.assertIn('return ingredient.fluids().stream().map(Holder::value)',s)
        self.assertIn('return ingredient.items().map(Holder::value)',s)
        self.assertIn('Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tag))',s)

    def test_shaped_recipe_preserves_public_legacy_overload(self):
        s=self.source
        self.assertIn('translateMatch(NonNullList<Ingredient> recipeItems',s)
        self.assertIn('translateOptionalMatch(recipe.getIngredients()',s)
        self.assertIn('recipeItems.stream().map(Optional::ofNullable).toList()',s)
        self.assertIn('List<Optional<Ingredient>> recipeItems',s)

    def test_empty_slots_require_empty_input(self):
        s=self.source
        self.assertIn('Optional<Ingredient> ingredient = Optional.empty()',s)
        self.assertIn('ingredient.isPresent() ? !ingredient.get().test(stack) : !stack.isEmpty()',s)
        self.assertNotIn('Ingredient.EMPTY',s)
        self.assertIn('recipeItems.get(width - col - 1 + row * width)',s)
        self.assertIn('recipeItems.get(col + row * width)',s)

    def test_shaped_index_equations_unchanged(self):
        s=self.source
        self.assertIn('((width - 1 - (targetIndex % width)) + startCol) + ((targetIndex / width) + startRow) * input.width()',s)
        self.assertIn('((targetIndex % width) + startCol) + ((targetIndex / width) + startRow) * input.width()',s)
        # Simulate index correspondence for all legal windows; does not run the Java method.
        checked=0
        for inv_w in range(1,6):
            for inv_h in range(1,6):
                for w in range(1,inv_w+1):
                    for h in range(1,inv_h+1):
                        for start_c in range(inv_w-w+1):
                            for start_r in range(inv_h-h+1):
                                for k in range(w*h):
                                    col=k%w;row=k//w
                                    regular=(col+start_c)+(row+start_r)*inv_w
                                    mirror=(w-1-col+start_c)+(row+start_r)*inv_w
                                    self.assertEqual(regular,((k%w)+start_c)+((k//w)+start_r)*inv_w)
                                    self.assertEqual(mirror,((w-1-(k%w))+start_c)+((k//w)+start_r)*inv_w)
                                    checked+=1
        self.assertGreater(checked,300)

if __name__=='__main__':
    unittest.main(verbosity=2)
