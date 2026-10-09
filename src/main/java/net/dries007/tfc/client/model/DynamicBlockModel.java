/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

/** A model JSON whose world geometry is selected through the block-state pipeline. */
public interface DynamicBlockModel extends UnbakedModel
{
    BlockStateModel bakeBlock(ResolvedModel owner, ModelBaker baker, ModelState state);

    /** Retains native variant rotations and UV lock, including within weighted/multipart models. */
    record Unbaked(Variant variant) implements CustomUnbakedBlockStateModel
    {
        public static final MapCodec<Unbaked> CODEC = Variant.MAP_CODEC.xmap(Unbaked::new, Unbaked::variant);

        @Override
        public MapCodec<Unbaked> codec()
        {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver)
        {
            variant.resolveDependencies(resolver);
        }

        @Override
        public BlockStateModel bake(ModelBaker baker)
        {
            final ResolvedModel owner = baker.getModel(variant.modelLocation());
            for (ResolvedModel model = owner; model != null; model = model.parent())
            {
                if (model.wrapped() instanceof DynamicBlockModel dynamic)
                {
                    return dynamic.bakeBlock(owner, baker, variant.modelState().asModelState());
                }
            }
            // A resource pack may replace a dynamic model with ordinary cuboid geometry.
            return new SingleVariant(variant.bake(baker));
        }
    }
}
