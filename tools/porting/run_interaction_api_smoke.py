"""Source/API contract checks for the 26.1.2 right-click interaction migration.

Not a Minecraft bootstrap or in-game inventory round-trip test.
"""
import hashlib
import re
import subprocess
import unittest
from pathlib import Path

SOURCE = Path('src/main/java/net/dries007/tfc/util/InteractionManager.java')
KEYED_INGREDIENT = Path('src/main/java/net/dries007/tfc/common/recipes/ingredients/KeyedIngredient.java')
# Accepted upstream 08bda4bd: shelf rollback and failed pot placement result.
BEFORE_HASH = '13f5d915883421fc41e3589b9b9090807cb8baaa4dd071fbbc18ac5e2359ee51'
TAGS = (
    'TFCTags.Items.THATCH_BED_HIDES', 'TFCTags.Items.LOG_PILE_LOGS',
    'Tags.Items.INGOTS', 'TFCTags.Items.DOUBLE_INGOTS',
    'TFCTags.Items.USABLE_IN_MOLD_TABLE', 'TFCTags.Items.SALAD_BOWLS',
)


class InteractionApiContracts(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source = SOURCE.read_text()

    def test_all_six_tag_interactions_still_use_lazy_holder_backed_tags(self):
        self.assertEqual(self.source.count('KeyedIngredient.of(TFCTags.') + self.source.count('KeyedIngredient.of(Tags.'), 6)
        for tag in TAGS:
            self.assertIn(f'KeyedIngredient.of({tag})', self.source)
            self.assertNotRegex(self.source, r'(?<![\w.])Ingredient\.of\(' + re.escape(tag) + r'\)')
        helpers = KEYED_INGREDIENT.read_text()
        self.assertIn('() -> BuiltInRegistries.ITEM.getOrThrow(tag).stream().map(Holder::value).toList()', helpers)

    def test_optional_block_entity_interaction_fallthrough(self):
        self.assertEqual(self.source.count('.<InteractionResult>map('), 2)
        self.assertEqual(self.source.count('}).orElse(InteractionResult.PASS);'), 2)
        self.assertIn('return result == InteractionResult.PASS ? Optional.empty() : Optional.of(result);', self.source)
        self.assertEqual(self.source.count('return result == InteractionResult.PASS ? Optional.empty() : Optional.of(result);'), 2)

    def test_writes_native_knapping_identifier_without_altering_type(self):
        self.assertIn('buffer.writeIdentifier(KnappingType.MANAGER.getIdOrThrow(type))', self.source)
        self.assertNotIn('writeResourceLocation(', self.source)

    def test_damage_goes_to_selected_equipment_hand(self):
        self.assertEqual(self.source.count('stack.hurtAndBreak(1, player, context.getHand().asEquipmentSlot())'), 2)
        self.assertNotIn('LivingEntity.getSlotForHand', self.source)

    def test_reviewed_upstream_survival_interaction_body_under_api_renames(self):
        # Restore the native API routes and a now-unused import.
        # This freezes inventory splitting, recipe action order, placement flags,
        # sounds and client/server action results, not just the method names.
        s = self.source
        for tag in TAGS:
            s = s.replace('KeyedIngredient.of(' + tag + ')', 'Ingredient.of(' + tag + ')')
        s = s.replace('.<InteractionResult>map(', '.map(')
        s = s.replace('buffer.writeIdentifier(', 'buffer.writeResourceLocation(')
        s = s.replace('context.getHand().asEquipmentSlot()', 'LivingEntity.getSlotForHand(context.getHand())')
        s = s.replace('import net.minecraft.world.entity.player.Player;\n',
                      'import net.minecraft.world.entity.LivingEntity;\nimport net.minecraft.world.entity.player.Player;\n')
        self.assertEqual(hashlib.sha256(s.encode()).hexdigest(), BEFORE_HASH)

    def test_apis_exist_in_resolved_minecraft_26_1_2(self):
        classpath = Path('port-diagnostics/classpath.txt')
        self.assertTrue(classpath.is_file(), 'Resolved Minecraft 26.1.2 classpath required')
        out = subprocess.check_output([
            'javap', '-classpath', classpath.read_text().strip(), '-public',
            'net.minecraft.world.InteractionHand', 'net.minecraft.network.FriendlyByteBuf',
        ], text=True, timeout=25)
        self.assertIn('net.minecraft.world.entity.EquipmentSlot asEquipmentSlot()', out)
        self.assertIn('writeIdentifier(net.minecraft.resources.Identifier)', out)


if __name__ == '__main__':
    unittest.main(verbosity=2)
