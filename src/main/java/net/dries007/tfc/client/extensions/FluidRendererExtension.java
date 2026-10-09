/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.extensions;

import java.util.function.ToIntBiFunction;
import net.minecraft.client.Minecraft;
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
) implements IClientFluidTypeExtensions
{
    public FluidRendererExtension(int tintColor, Identifier stillTexture, Identifier flowingTexture, @Nullable Identifier overlayTexture, @Nullable Identifier renderOverlayTexture)
    {
        this(tintColor, (level, pos) -> tintColor, stillTexture, flowingTexture, overlayTexture, renderOverlayTexture);
    }

    @Override
    public int getTintColor()
    {
        return tintColor;
    }

    @Override
    public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos)
    {
        return tintColorFunction.applyAsInt(getter, pos);
    }

    @Override
    public Identifier getStillTexture()
    {
        return stillTexture;
    }

    @Override
    public Identifier getFlowingTexture()
    {
        return flowingTexture;
    }

    @Override
    @Nullable
    public Identifier getOverlayTexture()
    {
        return overlayTexture;
    }

    @Override
    @Nullable
    public Identifier getRenderOverlayTexture(Minecraft minecraft)
    {
        return renderOverlayTexture;
    }
}
