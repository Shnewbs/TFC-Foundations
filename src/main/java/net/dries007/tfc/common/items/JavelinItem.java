/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.items;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

import net.dries007.tfc.common.LevelTier;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.entities.misc.ThrownJavelin;
import net.dries007.tfc.util.Helpers;

/**
 * Implementation based on {@link TridentItem}
 */
public class JavelinItem extends Item
{
    private final LevelTier tier;

    public JavelinItem(LevelTier tier, float attackSpeed, Properties properties)
    {
        super(ToolItem.swordProperties(tier, 0.7f, attackSpeed, properties));
        this.tier = tier;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack)
    {
        return ItemUseAnimation.TRIDENT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity)
    {
        return 72000;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int ticksLeft)
    {
        if (entity instanceof Player player)
        {
            int i = this.getUseDuration(stack, entity) - ticksLeft;
            if (i < 10)
            {
                return false;
            }
            {
                if (!level.isClientSide())
                {
                    Helpers.damageItem(stack, player, entity.getUsedItemHand());

                    ThrownJavelin javelin = new ThrownJavelin(level, player, stack);
                    javelin.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                    if (player.getAbilities().instabuild)
                    {
                        javelin.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    }

                    level.addFreshEntity(javelin);
                    level.playSound(null, javelin, TFCSounds.JAVELIN_THROWN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (!player.getAbilities().instabuild)
                    {
                        player.getInventory().removeItem(stack);
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
        return entity instanceof Player;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        ItemStack held = player.getItemInHand(hand);
        if (held.getDamageValue() >= held.getMaxDamage() - 1)
        {
            return InteractionResult.FAIL;
        }
        else
        {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME.heldItemTransformedTo(held);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag)
    {
        tooltip.accept(Component.translatable("tfc.tooltip.javelin.thrown_damage", String.format("%.0f", getThrownDamage())).withStyle(ChatFormatting.DARK_GREEN));
    }

    public float getThrownDamage()
    {
        return 1.5f * tier.getAttackDamageBonus();
    }
}