/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.items;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import net.dries007.tfc.common.LevelTier;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.component.glass.GlassOperation;
import net.dries007.tfc.common.component.glass.IGlassworkingTool;

public class GemSawItem extends ToolItem implements IGlassworkingTool
{
    public GemSawItem(LevelTier tier, Properties properties)
    {
        super(tier, TFCTags.Blocks.MINEABLE_WITH_GLASS_SAW, -.2f, -2.0f, properties);
    }

    @Override
    public GlassOperation getOperation()
    {
        return GlassOperation.SAW.value();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, display, tooltip, tooltipFlag);
        tooltip.accept(Component.translatable("tfc.tooltip.glass.tool_description", Component.translatable(getOperation().getTranslationId())));
    }
}
