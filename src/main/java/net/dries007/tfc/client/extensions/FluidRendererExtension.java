/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.extensions;

import java.util.function.ToIntBiFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.Nullable;

public record FluidRendererExtension(
    int tintColor,
    ToIntBiFunction<BlockAndTintGetter, BlockPos> tintColorFunction,
    Identifier stillTexture,
    Identifier flowingTexture,
    @Nullable Identifier overlayTexture,
    @Nullable Identifier renderOverlayTexture
) implements IClientFluidTypeExtensions, FluidTintSource
{
    public FluidRendererExtension(int tintColor, Identifier stillTexture, Identifier flowingTexture, @Nullable Identifier overlayTexture, @Nullable Identifier renderOverlayTexture)
    {
        this(tintColor, (level, pos) -> tintColor, stillTexture, flowingTexture, overlayTexture, renderOverlayTexture);
    }

    @Override
    public int color(FluidState state)
    {
        return tintColor;
    }

    @Override
    public int colorInWorld(FluidState state, BlockState blockState, BlockAndTintGetter getter, BlockPos pos)
    {
        return tintColorFunction.applyAsInt(getter, pos);
    }

    public FluidModel.Unbaked model()
    {
        return new FluidModel.Unbaked(new Material(stillTexture), new Material(flowingTexture),
            overlayTexture == null ? null : new Material(overlayTexture), this);
    }

    @Override
    @Nullable
    public Identifier getRenderOverlayTexture(Minecraft minecraft)
    {
        return renderOverlayTexture;
    }
}
