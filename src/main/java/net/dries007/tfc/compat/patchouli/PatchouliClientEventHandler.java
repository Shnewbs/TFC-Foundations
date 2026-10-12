/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat.patchouli;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2ic;
import vazkii.patchouli.client.base.ClientTicker;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.handler.TooltipHandler;
import vazkii.patchouli.common.base.PatchouliConfig;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;
import vazkii.patchouli.common.item.ItemModBook;
import vazkii.patchouli.common.util.ItemStackUtil;
import vazkii.patchouli.xplat.IClientXplatAbstractions;

import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.network.OpenFieldGuidePacket;

/**
 * This is modified from {@link TooltipHandler}, in order to render additional tooltips, as we don't have an explicit book item. We render a tooltip,
 * (1) when Patchouli otherwise wouldn't be rendering a tooltip, and (2) when the book in question is ours.
 */
public final class PatchouliClientEventHandler
{
    private static float lexiconLookupTime = 0;
    private static final int backgroundLength = 28;
    private static final int backgroundHeight = 34;

    public static void init()
    {
        final IEventBus bus = NeoForge.EVENT_BUS;

        bus.addListener(PatchouliClientEventHandler::renderBookTooltipWithoutBook);

        ConfigTextFunction.register();
    }

    public static void renderBookTooltipWithoutBook(RenderTooltipEvent.Pre event)
    {
        if (!TFCConfig.CLIENT.showGuideBookLinksAlways.get())
        {
            lexiconLookupTime = 0F;
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final GuiGraphicsExtractor graphics = event.getGraphics();
        final ItemStack stack = event.getItemStack();
        final int tooltipX = event.getX();
        final int tooltipY = event.getY();

        if (minecraft.player == null || minecraft.screen instanceof GuiBook || wouldPatchouliRenderATooltipHere(minecraft, stack))
        {
            lexiconLookupTime = 0F;
            return;
        }

        // We only are concerned with our own book
        final Book book = BookRegistry.INSTANCE.books.get(PatchouliIntegration.BOOK_ID);
        if (book == null)
        {
            lexiconLookupTime = 0F;
            return;
        }
        final Pair<BookEntry, Integer> entry = book.getContents().getEntryForStack(stack);

        if (entry != null && !entry.getFirst().isLocked())
        {
            final ItemStack bookStack = ItemModBook.forBook(book);

            int x = tooltipX - 34;
            int y = tooltipY - 4;

            // Estimate the item tooltip position and size
            // and move the book to a better position if needed
            int itemTooltipLength = 0;
            int itemTooltipHeight = 0;
            for (ClientTooltipComponent component : event.getComponents())
            {
                itemTooltipLength = Integer.max(itemTooltipLength, component.getWidth(event.getFont()));
                itemTooltipHeight += component.getHeight(event.getFont());
            }
            Vector2ic itemTooltipPos = event.getTooltipPositioner().positionTooltip(event.getScreenWidth(), event.getScreenHeight(), tooltipX, tooltipY, itemTooltipLength, itemTooltipHeight);
            int itemTooltipX = itemTooltipPos.x();
            int itemTooltipY = itemTooltipPos.y();
            if (itemTooltipX < tooltipX)
            {
                // Displayed tooltip is to the left of the cursor and would cover it, move the book to the right
                x = tooltipX + 18;
            }
            if (x + backgroundLength > event.getScreenWidth())
            {
                // Off the right side of the screen
                x = itemTooltipX + itemTooltipLength - backgroundLength;
                y = itemTooltipY - backgroundHeight - 8;
            }
            else if (x - 8 < 0)
            {
                // Off the left side of the screen
                x = itemTooltipX;
                y = itemTooltipY - backgroundHeight - 8;
            }
            if (y < 0)
            {
                // Off the top of the screen
                y = itemTooltipY + itemTooltipHeight + 20;
            }

            graphics.fill(x - 4, y - 4, x + 20, y + 26, 0x44000000);
            graphics.fill(x - (backgroundLength - 22), y - (backgroundHeight - 28), x + (backgroundLength - 6), y + (backgroundHeight - 6), 0x44000000);

            if (PatchouliConfig.get().useShiftForQuickLookup() ? minecraft.hasShiftDown() : minecraft.hasControlDown())
            {
                lexiconLookupTime += ClientTicker.delta;

                int cx = x + 8;
                int cy = y + 8;
                float r = 12;
                float time = 20F;
                float angles = lexiconLookupTime / time * 360F;

                final float alpha = 0.5F + 0.2F * ((float) Math.cos(ClientTicker.total / 10) * 0.5F + 0.5F);
                IClientXplatAbstractions.INSTANCE.submitGuiElement(graphics,
                    new LookupProgressState(new Matrix3x2f(graphics.pose()), cx, cy, Math.min(angles, 360F), r, alpha));

                if (lexiconLookupTime >= time)
                {
                    // Change: don't move the selected slot, because we're not opening from a stack
                    // minecraft.player.getInventory().selected = lexSlot;
                    final int spread = entry.getSecond();

                    // Change: don't open the book client side only
                    // See TerraFirmaCraft/2152
                    ClientPacketDistributor.sendToServer(new OpenFieldGuidePacket(entry.getFirst().getId(), spread * 2));
                }
            }
            else
            {
                lexiconLookupTime = 0F;
            }

            graphics.pose().pushMatrix();
            graphics.item(bookStack, x, y);
            graphics.itemDecorations(minecraft.font, bookStack, x, y);
            graphics.pose().popMatrix();

            graphics.pose().pushMatrix();
            graphics.text(minecraft.font, "?", x + 10, y + 8, 0xFFFFFFFF, true);

            graphics.pose().scale(0.5F, 0.5F);
            boolean mac = Util.getPlatform() == Util.OS.OSX;
            Component key = Component.literal(PatchouliConfig.get().useShiftForQuickLookup() ? "Shift" : mac ? "Cmd" : "Ctrl")
                .withStyle(ChatFormatting.BOLD);
            graphics.text(minecraft.font, key, (x + 10) * 2 - 16, (y + 8) * 2 + 20, 0xFFFFFFFF, true);
            graphics.pose().popMatrix();
        }
        else
        {
            lexiconLookupTime = 0F;
        }
    }

    /**
     * @return {@code true} if {@link TooltipHandler} would render a tooltip, so we don't try and render one on top of it
     */
    private static boolean wouldPatchouliRenderATooltipHere(Minecraft minecraft, ItemStack stack)
    {
        if (minecraft.player != null && !(minecraft.screen instanceof GuiBook))
        {
            for (int i = 0; i < Inventory.getSelectionSize(); i++)
            {
                final ItemStack stackAt = minecraft.player.getInventory().getItem(i);
                if (!stackAt.isEmpty())
                {
                    final Book book = ItemStackUtil.getBookFromStack(stackAt);
                    if (book != null)
                    {
                        final Pair<BookEntry, Integer> entry = book.getContents().getEntryForStack(stack);
                        if (entry != null && !entry.getFirst().isLocked())
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Captures the lookup fan before GUI submission; rendering does not read mutable client timing or poses.
     */
    private record LookupProgressState(Matrix3x2f pose, int cx, int cy, float angle, float radius, float alpha)
        implements GuiElementRenderState
    {
        @Override
        public RenderPipeline pipeline()
        {
            return RenderPipelines.SUNRISE_SUNSET;
        }

        @Override
        public TextureSetup textureSetup()
        {
            return TextureSetup.noTexture();
        }

        @Override
        public @Nullable ScreenRectangle scissorArea()
        {
            return null;
        }

        @Override
        public ScreenRectangle bounds()
        {
            final int extent = (int) Math.ceil(radius) + 2;
            return new ScreenRectangle(cx - extent, cy - extent, extent * 2, extent * 2).transformMaxBounds(pose);
        }

        @Override
        public void buildVertices(VertexConsumer buffer)
        {
            buffer.addVertexWith2DPose(pose, cx, cy).setColor(0F, 0.5F, 0F, alpha);
            for (float degrees = angle; degrees > 0; degrees--)
            {
                final float radians = (degrees - 90) / 180F * Mth.PI;
                buffer.addVertexWith2DPose(pose, cx + Mth.cos(radians) * radius, cy + Mth.sin(radians) * radius)
                    .setColor(0F, 1F, 0F, 1F);
            }
            buffer.addVertexWith2DPose(pose, cx, cy).setColor(0F, 1F, 0F, 0F);
        }
    }
}
