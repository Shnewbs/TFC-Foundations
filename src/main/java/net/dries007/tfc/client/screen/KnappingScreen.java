/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.client.screen.button.KnappingButton;
import net.dries007.tfc.common.container.KnappingContainer;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.data.KnappingPattern;
import net.dries007.tfc.util.data.KnappingType;

public class KnappingScreen extends TFCContainerScreen<KnappingContainer>
{
    public static final Identifier BACKGROUND = Helpers.identifier("textures/gui/knapping.png");

    private final Identifier buttonLocation;
    @Nullable private final Identifier buttonDisabledLocation;
    private final List<ScreenParticle> particles = new ArrayList<>();
    private final RandomSource random = RandomSource.create();

    public static Identifier getHighTexture(ItemStack stack)
    {
        return getButtonLocation(stack.getItem(), false);
    }

    @Nullable
    public static Identifier getLowTexture(KnappingType type, ItemStack stack)
    {
        return type.hasOffTexture() ? getButtonLocation(stack.getItem(), true) : null;
    }

    public static Identifier getButtonLocation(Item item, boolean disabled)
    {
        return Helpers.identifier("textures/gui/knapping/" + BuiltInRegistries.ITEM.getKey(item).getPath() + (disabled ? "_disabled" : "") + ".png");
    }

    public KnappingScreen(KnappingContainer container, Inventory inv, Component name)
    {
        super(container, inv, name, BACKGROUND, 176, 186);
        inventoryLabelY = 94;
        titleLabelY -= 2;

        final ItemStack stack = container.getOriginalStack();

        buttonLocation = getHighTexture(stack);
        buttonDisabledLocation = getLowTexture(container.getKnappingType(), stack);
    }

    @Override
    protected void init()
    {
        super.init();
        for (int x = 0; x < KnappingPattern.MAX_WIDTH; x++)
        {
            for (int y = 0; y < KnappingPattern.MAX_HEIGHT; y++)
            {
                int bx = (width - imageWidth) / 2 + 12 + 16 * x;
                int by = (height - imageHeight) / 2 + 12 + 16 * y;
                addRenderableWidget(new KnappingButton(x + 5 * y, bx, by, 16, 16, buttonLocation, menu.getKnappingType().clickSound(), this::spawnParticles));
            }
        }
        menu.setRequiresReset(true);
    }

    private void spawnParticles(Button button)
    {
        if (button instanceof KnappingButton knappingButton && menu.getKnappingType().spawnsParticles() && TFCConfig.CLIENT.enableScreenParticles.get() && ClientHelpers.useFancyGraphics())
        {
            final int amount = Mth.nextInt(random, 0, 3);
            for (int i = 0; i < amount; i++)
            {
                final var particle = new ScreenParticle(knappingButton.getTexture(), button.getX(), button.getY(), Mth.nextFloat(random, -0.1f, 0.1f), Mth.nextFloat(random, 1.2f, 1.5f), 16, 16, random);
                particles.add(particle);
            }
        }
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();
        for (ScreenParticle particle : particles)
        {
            particle.tick();
        }
        particles.removeIf(ScreenParticle::shouldBeRemoved);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        // Check if the container has been updated
        if (menu.requiresReset())
        {
            for (var widget : children())
            {
                if (widget instanceof KnappingButton button)
                {
                    button.visible = menu.getPattern().get(button.id);
                }
            }
            menu.setRequiresReset(false);
        }

        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        for (var widget : children())
        {
            if (widget instanceof KnappingButton button)
            {
                if (button.visible) // Active button
                {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, buttonLocation, button.getX(), button.getY(), 0, 0, 16, 16, 16, 16);
                }
                else if (buttonDisabledLocation != null) // Disabled / background texture
                {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, buttonDisabledLocation, button.getX(), button.getY(), 0, 0, 16, 16, 16, 16);
                }
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        for (ScreenParticle particle : particles)
        {
            particle.render(graphics);
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY)
    {
        final double x = event.x();
        final double y = event.y();
        final int clickType = event.button();
        if (clickType == 0)
        {
            mouseClicked(event, false);
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        final double x = event.x();
        final double y = event.y();
        final int clickType = event.button();
        if (clickType == 0)
        {
            undoAccidentalButtonPress(x, y);
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void undoAccidentalButtonPress(double x, double y)
    {
        for (var widget : children())
        {
            if (widget instanceof KnappingButton button && button.isMouseOver(x, y))
            {
                menu.getPattern().set(button.id, false);
            }
        }
    }
}
