/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client;

import java.util.function.BiConsumer;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;

import net.dries007.tfc.client.extensions.FluidRendererExtension;
import net.dries007.tfc.common.fluids.FluidHolder;
import net.dries007.tfc.common.fluids.TFCFluids;

import static net.dries007.tfc.client.ClientEventHandler.*;

/** Shares fluid appearance definitions between model reloads and underwater overlays. */
public final class FluidModelRegistration
{
    public static void registerExtensions(RegisterClientExtensionsEvent event)
    {
        forEach((holder, extension) -> event.registerFluidType(extension, holder.getType()));
    }

    public static void registerModels(RegisterFluidModelsEvent event)
    {
        forEach((holder, extension) -> event.register(extension.model(), holder.source(), holder.flowing()));
        // River water shares the vanilla water fluid type, but has its own fluid registry entry.
        event.register(new FluidModel.Unbaked(new Material(WATER_STILL), new Material(WATER_FLOW),
            new Material(WATER_OVERLAY), FluidTintSources.water()), TFCFluids.RIVER_WATER);
    }

    private static void forEach(BiConsumer<FluidHolder<?>, FluidRendererExtension> register)
    {
        TFCFluids.METALS.forEach((metal, holder) -> register.accept(holder,
            new FluidRendererExtension(TFCFluids.ALPHA_MASK | metal.getColor(), MOLTEN_STILL, MOLTEN_FLOW, null, null)));
        TFCFluids.SIMPLE_FLUIDS.forEach((fluid, holder) -> register.accept(holder,
            new FluidRendererExtension(fluid.isTransparent() ? TFCFluids.ALPHA_MASK | fluid.getColor() : fluid.getColor(),
                WATER_STILL, WATER_FLOW, WATER_OVERLAY, UNDERWATER_LOCATION)));
        TFCFluids.COLORED_FLUIDS.forEach((color, holder) -> register.accept(holder,
            new FluidRendererExtension(TFCFluids.ALPHA_MASK | color.getTextureDiffuseColor(),
                WATER_STILL, WATER_FLOW, WATER_OVERLAY, null)));
        register.accept(TFCFluids.SALT_WATER,
            new FluidRendererExtension(TFCFluids.ALPHA_MASK | 0x3F76E4,
                (level, pos) -> level.getBlockTint(pos, TFCColors.SALT_WATER) | TFCFluids.ALPHA_MASK,
                WATER_STILL, WATER_FLOW, WATER_OVERLAY, UNDERWATER_LOCATION));
        register.accept(TFCFluids.SPRING_WATER,
            new FluidRendererExtension(TFCFluids.ALPHA_MASK | 0x4ECBD7,
                WATER_STILL, WATER_FLOW, WATER_OVERLAY, UNDERWATER_LOCATION));
    }

    private FluidModelRegistration() {}
}
