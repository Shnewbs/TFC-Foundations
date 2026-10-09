/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.button.BarrelSealButton;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blocks.devices.BarrelBlock;
import net.dries007.tfc.common.container.BarrelContainer;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.tooltip.Tooltips;

public class BarrelScreen extends BlockEntityScreen<BarrelBlockEntity, BarrelContainer>
{
    private static final Component SEAL = Component.translatable(TerraFirmaCraft.MOD_ID + ".tooltip.seal_barrel");
    private static final Component UNSEAL = Component.translatable(TerraFirmaCraft.MOD_ID + ".tooltip.unseal_barrel");
    private static final int MAX_RECIPE_NAME_LENGTH = 100;

    public static final Identifier BACKGROUND = Helpers.identifier("textures/gui/barrel.png");

    public BarrelScreen(BarrelContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, BACKGROUND, 176, 178);
        inventoryLabelY = 84;
    }

    @Override
    public void init()
    {
        super.init();
        addRenderableWidget(new BarrelSealButton(blockEntity, leftPos, topPos, isSealed() ? UNSEAL : SEAL));
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        super.extractLabels(graphics, mouseX, mouseY);
        if (isSealed())
        {
            highlightDisabledSlots(BarrelBlockEntity.SLOT_FLUID_CONTAINER_IN, BarrelBlockEntity.SLOT_ITEM);

            // Draw the text displaying both the seal date, and the recipe name
            final @Nullable Component recipe = blockEntity.getRecipeTooltip();
            if (recipe != null)
            {
                // todo 1.21: isn't there a method that draws a fixed-width string but moving back and forth for overlong strings?
                if (font.width(recipe) > MAX_RECIPE_NAME_LENGTH)
                {
                    int line = 0;
                    for (FormattedCharSequence text : font.split(recipe, MAX_RECIPE_NAME_LENGTH))
                    {
                        graphics.text(font, text, 70 + Math.floorDiv(MAX_RECIPE_NAME_LENGTH - font.width(text), 2), titleLabelY + (line * font.lineHeight), 0xFF404040, false);
                        line++;
                    }
                }
                else
                {
                    graphics.text(font, recipe.getString(), 70 + Math.floorDiv(MAX_RECIPE_NAME_LENGTH - font.width(recipe), 2), 61, 0xFF404040, false);
                }
            }
            final String date = Calendars.CLIENT.getExactTimeAndDate(blockEntity.getSealedTick()).getString();
            graphics.text(font, date, imageWidth / 2 - font.width(date) / 2, 74, 0xFF404040, false);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        if (TerraFirmaCraft.JEI)
        {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 92, topPos + 21, 227, 0, 9, 14, 256, 256);
        }

        final FluidStack fluidStack = blockEntity.getInventory().getFluidInTank(0);
        if (!fluidStack.isEmpty())
        {
            final TextureAtlasSprite sprite = RenderHelpers.getFluidSprite(fluidStack);
            final int fillHeight = (int) Math.ceil((float) 50 * fluidStack.getAmount() / (float) TFCConfig.SERVER.barrelCapacity.get());

            RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + 8, topPos + 70 - fillHeight, 16, fillHeight, 16, 16, RenderHelpers.getFluidColor(fluidStack));

        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + 7, topPos + 19, 176, 0, 18, 52, 256, 256);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        super.extractTooltip(graphics, mouseX, mouseY);
        final int relX = mouseX - leftPos;
        final int relY = mouseY - topPos;

        if (relX >= 7 && relY >= 19 && relX < 25 && relY < 71)
        {
            final FluidStack fluid = blockEntity.getInventory().getFluidInTank(0);
            if (!fluid.isEmpty())
            {
                graphics.setTooltipForNextFrame(font, Tooltips.fluidUnitsOf(fluid), mouseX, mouseY);
            }
        }
    }

    private boolean isSealed()
    {
        return blockEntity.getBlockState().getValue(BarrelBlock.SEALED);
    }

}
