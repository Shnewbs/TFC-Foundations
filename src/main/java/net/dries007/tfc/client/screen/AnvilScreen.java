/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.client.screen.button.AnvilPlanButton;
import net.dries007.tfc.client.screen.button.AnvilStepButton;
import net.dries007.tfc.client.screen.button.AnvilWeldButton;
import net.dries007.tfc.common.blockentities.AnvilBlockEntity;
import net.dries007.tfc.common.component.forge.ForgeRule;
import net.dries007.tfc.common.component.forge.ForgeStep;
import net.dries007.tfc.common.component.forge.Forging;
import net.dries007.tfc.common.container.AnvilContainer;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;

public class AnvilScreen extends BlockEntityScreen<AnvilBlockEntity, AnvilContainer>
{
    public static final Identifier BACKGROUND = Helpers.identifier("textures/gui/anvil.png");

    public AnvilScreen(AnvilContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, BACKGROUND, 176, 207);

        inventoryLabelY = 113;
    }

    @Override
    protected void init()
    {
        super.init();

        addRenderableWidget(new AnvilPlanButton(blockEntity, leftPos, topPos));
        addRenderableWidget(new AnvilWeldButton(blockEntity, leftPos, topPos));

        for (ForgeStep step : ForgeStep.VALUES)
        {
            addRenderableWidget(new AnvilStepButton(step, leftPos, topPos));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        final Level level = blockEntity.getLevel();
        final int guiLeft = leftPos, guiTop = topPos;

        if (TerraFirmaCraft.JEI)
        {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, guiLeft + 141, guiTop + 40, 0, 207, 9, 14, 256, 256);
        }

        assert level != null;


        final Forging forging = blockEntity.getMainInputForging();

        // Draw the progress indicators
        final int target = forging.target();
        final int range = TFCConfig.SERVER.anvilAcceptableWorkRange.get();
        final AnvilRecipe recipe = forging.getRecipe();
        if (recipe != null)
        {
            // progress indicator
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, guiLeft + 11 + forging.work(), guiTop + 104, 176, 0, 5, 5, 256, 256);

            // target indicator
            if (range < 2)
            {
                // render the pointer
                graphics.blit(RenderPipelines.GUI_TEXTURED, texture, guiLeft + 11 + target, guiTop + 98, 181, 0, 5, 5, 256, 256);
            }
            else
            {
                // render the bracket
                final int leftLimit = Math.max(0, target - range);
                final int rightLimit = Math.min(145, target + range);

                // left
                graphics.blit(RenderPipelines.GUI_TEXTURED, texture, guiLeft + 11 + leftLimit, guiTop + 96, 176, 7, 5, 7, 256, 256);
                // right
                graphics.blit(RenderPipelines.GUI_TEXTURED, texture, guiLeft + 11 + rightLimit, guiTop + 96, 186, 7, 5, 7, 256, 256);

                // bar
                for (int i = leftLimit + 2; i < rightLimit - 1; i++)
                {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, texture, guiLeft + 15 + i, guiTop + 94, 192, 5, 1, 5, 256, 256);
                }

                // center
                if (range > 2)
                {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, texture, guiLeft + 13 + (rightLimit + leftLimit) / 2, guiTop + 94, 181, 5, 5, 5, 256, 256);
                }

            }
        }

        // Draw rule icons
        if (recipe != null)
        {
            final List<ForgeRule> rules = recipe.getRules();
            for (int i = 0; i < rules.size(); i++)
            {
                final ForgeRule rule = rules.get(i);
                if (rule != null)
                {
                    final int xOffset = i * 19;

                    // The rule icon
                    graphics.blit(RenderPipelines.GUI_TEXTURED, texture, guiLeft + 61 + xOffset, guiTop + 13, rule.iconX(), rule.iconY() - 16, 16, 16, 16, 16, 256, 256);

                    // Extraction records each draw's tint rather than changing global shader state.
                    final int overlayColor = forging.matches(rule) ? 0xFF009933 : 0xFFFF6600;
                    graphics.blit(RenderPipelines.GUI_TEXTURED, texture, guiLeft + 59 + xOffset, guiTop + 13, 198, rule.overlayY(), 20, 22, 256, 256, overlayColor);
                }
            }
        }

        // Draw step icons
        int index = 0;
        for (ForgeStep step : forging.lastSteps())
        {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, guiLeft + 99 - (index * 19), guiTop + 34, step.iconX(), step.iconY() - 16, 16, 16, 16, 16, 256, 256);
            index++;
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        super.extractTooltip(graphics, mouseX, mouseY);

        final Level level = blockEntity.getLevel();
        final Forging forging = blockEntity.getMainInputForging();
        if (level != null)
        {
            final @Nullable AnvilRecipe recipe = forging.getRecipe();
            if (recipe != null)
            {
                final List<ForgeRule> rules = recipe.getRules();
                for (int i = 0; i < rules.size(); i++)
                {
                    final ForgeRule rule = rules.get(i);
                    if (rule != null)
                    {
                        final int xOffset = i * 19;
                        final int x = leftPos + 64 + xOffset;
                        final int y = topPos + 16;
                        if (mouseX > x && mouseX < x + 10 && mouseY > y && mouseY < y + 10)
                        {
                            graphics.setTooltipForNextFrame(font, rule.getDescriptionId(), mouseX, mouseY);
                        }
                    }
                }
            }
        }
    }
}
