/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes.outputs;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.jetbrains.annotations.Nullable;

/** Immutable recipe fluid prototype, safe during the native pre-component reload stage. */
public record FluidOutput(@Nullable FluidStackTemplate template)
{
    public static final FluidOutput EMPTY = new FluidOutput(null);
    public static final Codec<FluidOutput> CODEC = FluidStackTemplate.CODEC.xmap(FluidOutput::new, FluidOutput::requiredTemplate);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidOutput> STREAM_CODEC = FluidStack.STREAM_CODEC.map(FluidOutput::of, FluidOutput::copy);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidOutput> OPTIONAL_STREAM_CODEC = FluidStack.OPTIONAL_STREAM_CODEC.map(FluidOutput::of, FluidOutput::copy);

    public static FluidOutput of(FluidStack stack)
    {
        return stack.isEmpty() ? EMPTY : new FluidOutput(FluidStackTemplate.fromNonEmptyStack(stack));
    }

    private FluidStackTemplate requiredTemplate()
    {
        if (template == null) throw new IllegalStateException("Required recipe fluid output is empty");
        return template;
    }

    public FluidStack copy()
    {
        return template == null ? FluidStack.EMPTY : template.create();
    }

    public boolean isEmpty()
    {
        return template == null;
    }

    public int getAmount()
    {
        return template == null ? 0 : template.amount();
    }

    public Fluid getFluid()
    {
        return template == null ? Fluids.EMPTY : template.fluid().value();
    }
}
