/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client;

import java.awt.Color;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.effect.TFCEffects;
import net.dries007.tfc.common.entities.livestock.Age;
import net.dries007.tfc.common.entities.livestock.MammalProperties;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.entities.misc.TFCFishingHook;
import net.dries007.tfc.common.player.IPlayerInfo;
import net.dries007.tfc.common.player.PlayerInfo;
import net.dries007.tfc.config.DisabledExperienceBarStyle;
import net.dries007.tfc.config.HealthDisplayStyle;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.mixin.client.accessor.GuiAccessor;
import net.dries007.tfc.util.Helpers;

public enum IngameOverlays
{
    HEALTH(IngameOverlays::renderHealth),
    MOUNT_HEALTH(IngameOverlays::renderMountHealth),
    FOOD(IngameOverlays::renderFood),
    THIRST(IngameOverlays::renderThirst),
    INK(IngameOverlays::renderInk),
    CHISEL(IngameOverlays::renderChiselMode),
    EXPERIENCE(IngameOverlays::renderExperience),
    JUMP_BAR(IngameOverlays::renderJumpBar),
    HUD_MOVER(IngameOverlays::moveLeftAndRightHeights),
    FAMILIARITY(IngameOverlays::renderFamiliarity),
    ;

    private final Identifier id;
    final GuiLayer overlay;

    IngameOverlays(GuiLayer overlay)
    {
        this.id = Helpers.resourceLocation(name().toLowerCase(Locale.ROOT));
        this.overlay = overlay;
    }

    public static final Identifier TEXTURE = Helpers.identifier("textures/gui/icons/overlay.png");
    public static final Identifier INK_TEXTURE = Helpers.identifier("textures/misc/ink_splatter.png");
    public static final Identifier GLOW_INK_TEXTURE = Helpers.identifier("textures/misc/glow_ink_splatter.png");
    final static int HEALTH_TEXT_SHADOW_COLOR = new Color(104,0,0).getRGB();

    private static final Identifier VANILLA_HEALTH = VanillaGuiLayers.PLAYER_HEALTH;
    private static final Identifier VANILLA_MOUNT_HEALTH = VanillaGuiLayers.VEHICLE_HEALTH;
    private static final Identifier VANILLA_FOOD = VanillaGuiLayers.FOOD_LEVEL;
    private static final Identifier VANILLA_CONTEXT = VanillaGuiLayers.CONTEXTUAL_INFO_BAR;
    private static final Identifier VANILLA_CONTEXT_BACKGROUND = VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND;
    private static final Identifier VANILLA_EXP_LEVEL = VanillaGuiLayers.EXPERIENCE_LEVEL;

    public static void registerOverlays(RegisterGuiLayersEvent event)
    {
        above(event, VanillaGuiLayers.PLAYER_HEALTH, HEALTH);
        above(event, VanillaGuiLayers.VEHICLE_HEALTH, MOUNT_HEALTH);
        above(event, VanillaGuiLayers.FOOD_LEVEL, FOOD);
        above(event, VanillaGuiLayers.FOOD_LEVEL, THIRST);
        above(event, VanillaGuiLayers.CONTEXTUAL_INFO_BAR, EXPERIENCE);
        above(event, EXPERIENCE.id, JUMP_BAR);
        above(event, VanillaGuiLayers.PLAYER_HEALTH, HUD_MOVER);
        above(event, VanillaGuiLayers.CROSSHAIR, FAMILIARITY);

        top(event, INK);
        top(event, CHISEL);
    }

    private static void above(RegisterGuiLayersEvent event, Identifier vanilla, IngameOverlays overlay)
    {
        event.registerAbove(vanilla, overlay.id, overlay.overlay);
    }

    private static void top(RegisterGuiLayersEvent event, IngameOverlays overlay)
    {
        event.registerAboveAll(overlay.id, overlay.overlay);
    }

    public static void checkGuiOverlays(RenderGuiLayerEvent.Pre event)
    {
        final Identifier id = event.getName();
        if (enableThisOrThat(id, TFCConfig.CLIENT.enableHungerBar.get(), FOOD.id, VANILLA_FOOD) || enableThisOrThat(id, TFCConfig.CLIENT.enableHealthBar.get(), HEALTH.id, VANILLA_HEALTH) || enableThisOrThat(id, TFCConfig.CLIENT.enableHealthBar.get(), MOUNT_HEALTH.id, VANILLA_MOUNT_HEALTH))
        {
            event.setCanceled(true);
        }
        else if (disableIfFalse(id, TFCConfig.CLIENT.enableThirstBar.get(), THIRST.id) || disableIfFalse(id, TFCConfig.CLIENT.enableInkSplatter.get(), INK.id))
        {
            event.setCanceled(true);
        }
        // Experience, locator and mount-jump rendering now share these two native layers.
        // Keep the locator unless a horizontal fishing meter occupies the same space.
        if ((id.equals(VANILLA_EXP_LEVEL) && !TFCConfig.CLIENT.enableExperienceBar.get())
            || ((id.equals(VANILLA_CONTEXT) || id.equals(VANILLA_CONTEXT_BACKGROUND))
                && (usesHorizontalFishingMeter()
                    || (!TFCConfig.CLIENT.enableExperienceBar.get() && !isLocatorBarActive()))))
        {
            event.setCanceled(true);
        }
    }

    private static boolean enableThisOrThat(Identifier id, boolean config, Identifier myOverlay, Identifier vanillaOverlay)
    {
        return (config && id.equals(vanillaOverlay)) || (!config && id.equals(myOverlay));
    }

    private static boolean disableIfFalse(Identifier id, boolean config, Identifier myOverlay)
    {
        return id.equals(myOverlay) && !config;
    }

    public static void renderHealth(GuiGraphicsExtractor graphics, DeltaTracker delta)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        if (setupForSurvival(graphics, minecraft))
        {
            final Player player = (Player) minecraft.getCameraEntity();
            assert player != null;

            renderHealthBar(player, Minecraft.getInstance().gui, graphics, graphics.guiWidth(), graphics.guiHeight());
        }
    }

    public static void renderMountHealth(GuiGraphicsExtractor graphics, DeltaTracker delta)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        if (setupForSurvival(graphics, minecraft))
        {
            final Player player = (Player) minecraft.getCameraEntity();
            assert player != null;

            if (player.getVehicle() instanceof final LivingEntity entity)
            {
                renderHealthBar(entity, Minecraft.getInstance().gui, graphics, graphics.guiWidth(), graphics.guiHeight());
            }
        }
    }

    public static void renderFood(GuiGraphicsExtractor graphics, DeltaTracker delta)
    {
        final Gui gui = Minecraft.getInstance().gui;
        final Matrix3x2fStack stack = graphics.pose();
        final Minecraft minecraft = Minecraft.getInstance();
        if (setupForSurvival(graphics, minecraft))
        {
            Player player = (Player) minecraft.getCameraEntity();
            assert player != null;

            int x = graphics.guiWidth() / 2;
            int y = graphics.guiHeight() - gui.rightHeight;
            float percentFood = (float) player.getFoodData().getFoodLevel() / PlayerInfo.MAX_HUNGER;

            stack.pushMatrix();
            stack.translate(x + 10, y + 4);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 19, 81, 5, 256, 256);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 24, (int) (81 * percentFood), 5, 256, 256);
            stack.popMatrix();

            gui.rightHeight += 6;
        }
    }

    public static void renderThirst(GuiGraphicsExtractor graphics, DeltaTracker delta)
    {
        final Gui gui = Minecraft.getInstance().gui;
        final Matrix3x2fStack stack = graphics.pose();
        final Minecraft minecraft = Minecraft.getInstance();
        if (setupForSurvival(graphics, minecraft))
        {
            Player player = (Player) minecraft.getCameraEntity();
            assert player != null;

            final IPlayerInfo info = IPlayerInfo.get(player);

            int x = graphics.guiWidth() / 2;
            int y = graphics.guiHeight() - gui.rightHeight;
            float percentThirst = info.getThirst() / PlayerInfo.MAX_THIRST;
            float overheat = info.getThirstContributionFromTemperature();

            stack.pushMatrix();
            stack.translate(x + 10, y + 4);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 90, 19, 81, 5, 256, 256);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 90, 24, (int) (81 * percentThirst), 5, 256, 256);
            if (overheat > 0)
            {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 90, 29, 81, 5, 256, 256,
                    ARGB.color(Mth.clamp(overheat / PlayerInfo.MAX_TEMPERATURE_THIRST_DECAY, 0F, 1F), 0xFFFFFF));
            }
            stack.popMatrix();

            gui.rightHeight += 6;
        }
    }

    private static void renderChiselMode(GuiGraphicsExtractor graphics, DeltaTracker delta)
    {
        final Matrix3x2fStack stack = graphics.pose();
        final Minecraft mc = Minecraft.getInstance();
        if (setup(graphics, mc))
        {
            final Player player = ClientHelpers.getPlayer();
            if (player != null && Helpers.isItem(player.getItemInHand(InteractionHand.MAIN_HAND), TFCTags.Items.TOOLS_CHISEL))
            {
                stack.pushMatrix();
                if (!Helpers.isItem(player.getItemInHand(InteractionHand.OFF_HAND), TFCTags.Items.TOOLS_HAMMER))
                {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, graphics.guiWidth() / 2 + 100, graphics.guiHeight() - 21, 60, 58, 20, 20, 256, 256);
                }
                else
                {
                    // Delegate to the chisel mode
                    IPlayerInfo.get(player).chiselMode().createHotbarIcon((texture, u, v) -> graphics.blit(RenderPipelines.GUI_TEXTURED, texture, graphics.guiWidth() / 2 + 100, graphics.guiHeight() - 21, u, v, 20, 20, 256, 256));
                }
                stack.popMatrix();
            }
        }
    }

    private static void renderExperience(GuiGraphicsExtractor graphics, DeltaTracker delta)
    {
        final Minecraft mc = Minecraft.getInstance();
        final @Nullable LocalPlayer localPlayer = mc.player;
        final boolean isShowingExperience = TFCConfig.CLIENT.enableExperienceBar.get();
        final boolean isStyleLeftHotbar = (TFCConfig.CLIENT.disabledExperienceBarStyle.get() == DisabledExperienceBarStyle.LEFT_HOTBAR);
        if (localPlayer != null && localPlayer.fishing instanceof TFCFishingHook hook && setup(graphics, mc))
        {

            if (!isShowingExperience && isStyleLeftHotbar)
            {
                int barHeight;
                int uOffset;
                if (localPlayer.getVehicle() instanceof LivingEntity && TFCConfig.CLIENT.enableHealthBar.get()) // Increase the bar's height if a second health bar is present
                {
                    barHeight = 42;
                    uOffset = 164;
                }
                else
                {
                    barHeight = 32;
                    uOffset = 153;
                }
                final int x = graphics.guiWidth() / 2 - 97;
                final int y = graphics.guiHeight() - barHeight;
                final int texturePos = 36 + barHeight;
                final int amount = Mth.ceil(Mth.clampedMap(hook.pullExhaustion, 0, 100, 0, barHeight + 1));
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, uOffset, 36, 5, barHeight, 256, 256);
                if (amount > 0)
                {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, graphics.guiHeight() - amount, uOffset + 5, texturePos - amount, 5, amount, 256, 256);
                }
            }
            else
            {
                final int x = graphics.guiWidth() / 2 - 91;
                final int y = graphics.guiHeight() - 29;
                final int amount = Mth.ceil(Mth.clampedMap(hook.pullExhaustion, 0, 100, 0, 183));
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 111, 182, 5, 256, 256);
                if (amount > 0)
                {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 116, amount, 5, 256, 256);
                }
            }

        }
    }

    private static void renderJumpBar(GuiGraphicsExtractor graphics, DeltaTracker delta)
    {
        final Minecraft mc = Minecraft.getInstance();
        final LocalPlayer localPlayer = mc.player;
        final boolean isShowingExperience = TFCConfig.CLIENT.enableExperienceBar.get();
        final boolean isStyleLeftHotbar = (TFCConfig.CLIENT.disabledExperienceBarStyle.get() == DisabledExperienceBarStyle.LEFT_HOTBAR);
        if (isShowingExperience || !isStyleLeftHotbar)
            return;
        if (localPlayer != null && localPlayer.jumpableVehicle() != null && setup(graphics, mc))
        {

            int barHeight;
            int uOffset;
            if (TFCConfig.CLIENT.enableHealthBar.get()) // Use the taller bar if using TFC's health display
            {
                barHeight = 42;
                uOffset = 186;
            }
            else
            {
                barHeight = 32;
                uOffset = 175;
            }
            final int x = graphics.guiWidth() / 2 - 97;
            final int y = graphics.guiHeight() - barHeight;
            final int texturePos = 36 + barHeight;
            final int charge = (int) (localPlayer.getJumpRidingScale() * (float) (barHeight + 1));
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, uOffset, 36, 5, barHeight, 256, 256);
            if (charge > 0)
            {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, graphics.guiHeight() - charge, uOffset + 5, texturePos - charge, 5, charge, 256, 256);
            }

        }
    }

    private static void renderInk(GuiGraphicsExtractor graphics, DeltaTracker delta)
    {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson())
        {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null)
            {
                if (player.hasEffect(TFCEffects.INK.holder()))
                {
                    renderTextureOverlay(graphics, INK_TEXTURE, 1F);
                }
                else if (player.hasEffect(TFCEffects.GLOW_INK.holder()))
                {
                    renderTextureOverlay(graphics, GLOW_INK_TEXTURE, 1F);
                }
            }
        }
    }

    private static void renderHealthBar(LivingEntity entity, Gui gui, GuiGraphicsExtractor graphics, int width, int height)
    {
        final Matrix3x2fStack stack = graphics.pose();
        HealthDisplayStyle style = TFCConfig.CLIENT.healthDisplayStyle.get();
        float maxHealth = entity.getMaxHealth();

        int centerX = graphics.guiWidth() / 2;
        int y = height - gui.leftHeight;

        stack.pushMatrix();
        stack.translate(centerX - 91, y);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 0, 81, 9, 256, 256);

        float absorption = entity.getAbsorptionAmount();
        absorption = Float.isNaN(absorption) ? 0 : absorption;
        float percentHealth = (entity.getHealth() + absorption) / entity.getMaxHealth();
        float currentHealth = percentHealth * maxHealth;
        percentHealth = Mth.clamp(percentHealth, 0, 1);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 9, (int) (81 * percentHealth), 9, 256, 256);

        boolean isHurt = entity.getHealth() > 0.0F && entity.getHealth() < entity.getMaxHealth();
        boolean playerHasSaturation = entity instanceof Player player && player.getFoodData().getSaturationLevel() > 0;
        if ((playerHasSaturation && isHurt) || entity.hurtTime > 0 || entity.hasEffect(MobEffects.REGENERATION))
        {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 29, 81, 9, 256, 256);
        }

        float surplusPercent = Mth.clamp(percentHealth + (absorption / 20f) - 1, 0, 1);
        if (surplusPercent > 0)
        {
            // fill up the yellow bar until you get a second full bar, then just fill it up
            float percent = Math.min(surplusPercent, 1);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 90, 9, (int) (81 * percent), 9, 256, 256);
        }
        stack.popMatrix();

        // Health modifier affects both max and current health equally. All we do is draw different numbers as a result.
        final float healthModifier = entity instanceof Player player ? IPlayerInfo.get(player).getHealthModifier() : 1f;

        String text = style.format(currentHealth * healthModifier, maxHealth * healthModifier);
        stack.pushMatrix();
        stack.translate(centerX - 50, y);
        stack.scale(1f, 1f);

        graphics.text(gui.getFont(), text, (-1 * gui.getFont().width(text) / 2) + 1, 1, HEALTH_TEXT_SHADOW_COLOR, false);
        graphics.text(gui.getFont(), text, -1 * gui.getFont().width(text) / 2, 1, Color.WHITE.getRGB(), false);

        stack.popMatrix();

        gui.leftHeight += 10;
    }

    private static void renderFamiliarity(GuiGraphicsExtractor graphics, DeltaTracker delta)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        final Player player = mc.player;

        if (player.isShiftKeyDown() && IngameOverlays.setup(graphics, mc))
        {
            final Entity entity = mc.crosshairPickEntity;
            if (entity instanceof TFCAnimalProperties animal && animal.getAdultFamiliarityCap() > 0)
            {
                if (player.closerThan(entity, 5.0F))
                {
                    Matrix3x2fStack stack = graphics.pose();
                    stack.pushMatrix();

                    stack.translate(graphics.guiWidth() / 2f, graphics.guiHeight() / 2f - 45);
                    stack.scale(1.5f, 1.5f);

                    float familiarity = Math.max(0.0F, Math.min(1.0F, animal.getFamiliarity()));
                    int u;
                    int fontColor;
                    if (familiarity >= animal.getAdultFamiliarityCap() && animal.getAgeType() != Age.CHILD)
                    {
                        u = 132; // Render a red-ish outline for adults that cannot be familiarized more
                        fontColor = Color.RED.getRGB();
                    }
                    else if (familiarity >= 0.3F)
                    {
                        u = 112; // Render a white outline for the when the familiarity stopped decaying
                        fontColor = Color.WHITE.getRGB();
                    }
                    else
                    {
                        u = 92;
                        fontColor = Color.GRAY.getRGB();
                    }

                    if (TFCConfig.CLIENT.displayFamiliarityAsPercent.get())
                    {
                        String string = String.format("%.2f", familiarity * 100);

                        graphics.text(mc.font, string, -mc.font.width(string) / 2, 0, fontColor, false);

                    }
                    else
                    {
                        graphics.blit(RenderPipelines.GUI_TEXTURED, IngameOverlays.TEXTURE, -8, 0, u, 40, 16, 16, 256, 256);

                        graphics.blit(RenderPipelines.GUI_TEXTURED, IngameOverlays.TEXTURE, -6, 14 - (int) (12 * familiarity), familiarity == 1.0F ? 114 : 94, 74 - (int) (12 * familiarity), 12, (int) (12 * familiarity), 256, 256);
                    }
                    if (animal instanceof MammalProperties mammal && mammal.getPregnantTime() > 0 && mammal.isFertilized())
                    {
                        stack.translate(0, -15F);
                        String string = Component.translatable("tfc.tooltip.animal.pregnant", entity.getName().getString()).getString();
                        graphics.text(mc.font, string, -mc.font.width(string) / 2, 0, Color.WHITE.getRGB(), false);
                    }

                    stack.popMatrix();
                }
            }
        }
    }

    private static void renderTextureOverlay(GuiGraphicsExtractor graphics, Identifier location, float alpha)
    {
        final Minecraft mc = Minecraft.getInstance();
        final int screenWidth = mc.getWindow().getGuiScaledWidth();
        final int screenHeight = mc.getWindow().getGuiScaledHeight();

        graphics.blit(RenderPipelines.GUI_TEXTURED, location, 0, 0, 0.0F, 0.0F,
            screenWidth, screenHeight, screenWidth, screenHeight, ARGB.color(Mth.clamp(alpha, 0F, 1F), 0xFFFFFF));
    }

    private static void moveLeftAndRightHeights(GuiGraphicsExtractor graphics, DeltaTracker delta)
    {
        final Gui gui = Minecraft.getInstance().gui;
        final int offset = considerExperienceConfigs();
        gui.rightHeight -= offset;
        gui.leftHeight -= offset;
    }

    private static boolean setupForSurvival(GuiGraphicsExtractor gui, Minecraft minecraft)
    {
        final var gm = Minecraft.getInstance().gameMode;
        return gm != null && gm.canHurtPlayer() && setup(gui, minecraft);
    }

    public static boolean setup(GuiGraphicsExtractor gui, Minecraft minecraft)
    {
        if (!minecraft.options.hideGui && minecraft.getCameraEntity() instanceof Player)
        {
            return true;
        }
        return false;
    }

    private static boolean isLocatorBarActive()
    {
        return ((GuiAccessor) Minecraft.getInstance().gui).accessor$getContextualInfoBar().getRight() instanceof LocatorBarRenderer;
    }

    private static boolean usesHorizontalFishingMeter()
    {
        final Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && minecraft.player.fishing instanceof TFCFishingHook
            && !minecraft.options.hideGui && minecraft.getCameraEntity() instanceof Player
            && (TFCConfig.CLIENT.enableExperienceBar.get()
                || TFCConfig.CLIENT.disabledExperienceBarStyle.get() != DisabledExperienceBarStyle.LEFT_HOTBAR);
    }

    private static int considerExperienceConfigs()
    {
        if (TFCConfig.CLIENT.enableExperienceBar.get() || (isLocatorBarActive() && !usesHorizontalFishingMeter()))
        {
            return 0;
        }
        final LocalPlayer player = Minecraft.getInstance().player;
        return switch (TFCConfig.CLIENT.disabledExperienceBarStyle.get())
        {
            case LEFT_HOTBAR -> 6;
            case BUMP -> player != null && (player.fishing instanceof TFCFishingHook || player.jumpableVehicle() != null) ? 0 : 6;
            default -> 0;
        };
    }
}
