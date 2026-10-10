"""Exact-target source/API contracts for the ToolMaterial transition.

These checks are deliberately NOT game bootstrap, data-pack loading, or tool-use tests.
"""
import json
import os
from pathlib import Path
import re
import subprocess
import unittest

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java/net/dries007/tfc'
REF = json.loads((ROOT / 'tools/porting/tool-material-contracts.json').read_text())


def read(name):
    return (SRC / name).read_text()


class ToolMaterialContracts(unittest.TestCase):
    def test_target_signature_is_native_record(self):
        cp = (ROOT / 'port-diagnostics/classpath.txt').read_text().strip()
        java = Path(os.environ.get('JAVA_HOME', '')) / 'bin/javap' if os.environ.get('JAVA_HOME') else 'javap'
        r = subprocess.run([str(java), '-classpath', cp, 'net.minecraft.world.item.ToolMaterial',
                            'net.minecraft.world.item.Item$Properties',
                            'net.minecraft.world.item.crafting.Ingredient'],
                           text=True, capture_output=True, timeout=45, check=True).stdout
        self.assertIn('final class net.minecraft.world.item.ToolMaterial extends java.lang.Record', r)
        self.assertIn('net.minecraft.world.item.Item$Properties tool(net.minecraft.world.item.ToolMaterial', r)
        self.assertIn('net.minecraft.world.item.Item$Properties sword(net.minecraft.world.item.ToolMaterial', r)
        self.assertIn('net.minecraft.world.item.crafting.Ingredient of(net.minecraft.core.HolderSet<', r)

    def test_all_14_legacy_tier_statistics_are_preserved(self):
        s = read('common/TFCTiers.java')
        pattern = r'public static final LevelTier (\w+) = create\("([a-z_]+)", BlockTags\.(\w+), (\d+), (\d+), ([\d.]+f), ([\d.]+f), (\d+)\);'
        found = [
            {'field':m[0],'name':m[1],'block_tag':m[2],'level':int(m[3]),'durability':int(m[4]),
             'speed':float(m[5][:-1]),'attack_bonus':float(m[6][:-1]),'enchantment':int(m[7])}
            for m in re.findall(pattern, s)
        ]
        self.assertEqual(found, REF['tiers'])
        self.assertIn('new ToolMaterial(incorrect, uses, speed, damage, enchantmentValue, noRepairs)', s)
        self.assertIn('TagKey.create(Registries.ITEM, Helpers.identifier("repairs/" + name + "_tools"))', s)
        self.assertNotIn('SimpleTier', s)

    def test_level_accessors_keep_tfc_progression(self):
        s = read('common/LevelTier.java')
        for key in ('ToolMaterial material()', 'int level()', 'material().durability()',
                    'material().speed()', 'material().attackDamageBonus()',
                    'material().enchantmentValue()', 'material().repairItems()'):
            self.assertIn(key, s)
        self.assertNotIn('extends Tier', s)

    def test_tool_attack_product_and_native_material_properties(self):
        s = read('common/items/ToolItem.java')
        for key in ('(attackDamageFactor - 1f) * tier.getAttackDamageBonus()',
                    'attackDamageFactor * tier.getAttackDamageBonus()',
                    '.tool(tier.material(), mineableBlocks, baseAttackDamage(tier, attackDamageFactor)',
                    '.sword(tier.material(), baseAttackDamage(tier, attackDamageFactor)',
                    'public class ToolItem extends Item', 'return true;'):
            self.assertIn(key, s)
        self.assertNotIn('DiggerItem', s)

    def test_original_metal_attack_factors_retained(self):
        old = REF['metal_attack_factors']
        source = read('util/Metal.java')
        chunk = source[source.index('public enum ItemType'):]
        for name, (factor, speed) in old.items():
            # Specialized factories now encode the factor in their item's constructor.
            # Preserve each declared enum name and verify direct attribute factories where applicable.
            self.assertRegex(chunk, rf'(?m)^\s*{name}\(PartType\.ALL, metal -> new ')
            if name in ('PICKAXE','AXE','SHOVEL','SAW','SWORD','MACE'):
                self.assertIn(str(factor).rstrip('0').rstrip('.') if not float(factor).is_integer() else str(int(factor)), chunk)
        self.assertRegex(chunk, r'MACE\(PartType.ALL, metal -> new TFCMaceItem\(base\(metal\)\.attributes\(ToolItem\.productAttributes\(metal\.toolTier\(\), 1\.3f, -3\.4f\)\)')
        self.assertRegex(chunk, r'SHIELD\(PartType.ALL, metal -> new TFCShieldItem\(metal\.toolTier\(\), base\(metal\)\)\)')
        self.assertNotIn('new TieredItem(', chunk)
        self.assertNotIn('new SwordItem(', chunk)

    def test_weapon_items_and_rock_tools_use_material_components(self):
        s = read('common/items/TFCItems.java')
        for nm in ('OBSIDIAN_AXE','OBSIDIAN_HOE','OBSIDIAN_JAVELIN','OBSIDIAN_SHOVEL', 'OBSIDIAN_KNIFE'):
            self.assertIn(nm,s)
        rock = read('common/blocks/rock/RockCategory.java')
        for nm in ('AXE','HAMMER','HOE','JAVELIN','KNIFE','SHOVEL'):
            self.assertRegex(rock, rf'(?m)^\s*{nm}\(rock ->')
        self.assertIn('rock.tier().material()',rock)

    def test_shield_components_and_size_classification(self):
        shield=read('common/items/TFCShieldItem.java')
        for text in ('.durability(tier.getUses())','.enchantable(tier.getEnchantmentValue())',
                     '.repairable(tier.material().repairItems())','getDamageBlocked()'):
            self.assertIn(text,shield)
        sizes=read('common/component/size/ItemSizeManager.java')
        self.assertIn('stack.has(DataComponents.TOOL)',sizes)
        self.assertIn('DataComponents.EQUIPPABLE',sizes)
        self.assertIn('item instanceof BucketItem',sizes)
        self.assertIn('HEAD, CHEST, LEGS, FEET, BODY -> true',sizes)
        self.assertNotIn('item instanceof TieredItem',sizes)

    def test_tooltips_and_prospecting_remain_present(self):
        p=read('common/items/PropickItem.java')
        g=read('common/items/GemSawItem.java')
        for source in (p,g):
            self.assertIn('TooltipDisplay display, Consumer<Component> tooltip',source)
        self.assertIn('tfc.tooltip.propick.accuracy',p)
        self.assertIn('falseNegativeChance',p)
        self.assertIn('tfc.tooltip.glass.tool_description',g)
        self.assertIn('getOperation().getTranslationId()',g)

    def test_holder_tag_conversion_for_recipe_items(self):
        h=read('common/recipes/RecipeHelpers.java')
        self.assertIn('ingredient.items().map(Holder::value)',h)
        self.assertIn('Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(tag))',h)
        self.assertIn('new ItemStack(item, count)',h)


if __name__ == '__main__':
    unittest.main(verbosity=2)
