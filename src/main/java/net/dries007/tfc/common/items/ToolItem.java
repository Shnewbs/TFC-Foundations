/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.items;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.LevelTier;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.Helpers;

public class ToolItem extends Item
{
    public static float baseAttackDamage(LevelTier tier, float attackDamageFactor)
    {
        return (attackDamageFactor - 1f) * tier.getAttackDamageBonus();
    }

    public static ItemAttributeModifiers productAttributes(LevelTier tier, float attackDamageFactor, float attackSpeed)
    {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamageFactor * tier.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();
    }

    public static Properties toolProperties(LevelTier tier, TagKey<Block> mineableBlocks, float attackDamageFactor, float attackSpeed, Properties properties)
    {
        return properties
            .tool(tier.material(), mineableBlocks, baseAttackDamage(tier, attackDamageFactor), attackSpeed, 0f)
            .attributes(productAttributes(tier, attackDamageFactor, attackSpeed));
    }

    public static Properties swordProperties(LevelTier tier, float attackDamageFactor, float attackSpeed, Properties properties)
    {
        return properties
            .sword(tier.material(), baseAttackDamage(tier, attackDamageFactor), attackSpeed)
            .attributes(productAttributes(tier, attackDamageFactor, attackSpeed));
    }

    public static boolean willConsumeDurability(Level level, BlockPos pos, BlockState state)
    {
        return Helpers.isBlock(state.getBlock(), TFCTags.Blocks.CONSUMES_TOOL_DURABILITY) || state.getDestroySpeed(level, pos) != 0.0F;
    }

    public ToolItem(LevelTier tier, TagKey<Block> mineableBlocks, float attackDamageFactor, float attackSpeed, Properties properties)
    {
        super(toolProperties(tier, mineableBlocks, attackDamageFactor, attackSpeed, properties));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity)
    {
        if (willConsumeDurability(level, pos, state))
        {
            Helpers.damageItem(stack, entity, EquipmentSlot.MAINHAND);
        }
        return true;
    }
}
