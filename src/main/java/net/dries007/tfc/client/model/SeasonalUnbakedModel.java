/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;

/** Retains the existing inline seasonal model JSONs and resolves every child's dependencies. */
public final class SeasonalUnbakedModel extends AbstractUnbakedModel implements DynamicBlockModel
{
    private final List<UnbakedModel> alternatives;
    private final List<String> names;
    private final int fallback;
    private final Function<List<BlockStateModelPart>, BlockStateModel> factory;

    public SeasonalUnbakedModel(JsonObject json, JsonDeserializationContext context, List<String> names,
        int fallback, Function<List<BlockStateModelPart>, BlockStateModel> factory)
    {
        super(StandardModelParameters.parse(json, context));
        this.names = List.copyOf(names);
        final List<UnbakedModel> alternatives = new ArrayList<>(names.size());
        for (String name : names)
        {
            alternatives.add(context.deserialize(GsonHelper.getAsJsonObject(json, name), UnbakedModel.class));
        }
        this.alternatives = List.copyOf(alternatives);
        this.fallback = fallback;
        this.factory = factory;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver)
    {
        if (parent() != null) resolver.markDependency(parent());
        alternatives.forEach(model -> model.resolveDependencies(resolver));
    }

    @Override
    public BlockStateModel bakeBlock(ResolvedModel owner, ModelBaker baker, ModelState state)
    {
        final List<BlockStateModelPart> baked = new ArrayList<>(alternatives.size());
        for (int i = 0; i < alternatives.size(); i++)
        {
            final String name = owner.debugName() + "/" + names.get(i);
            baked.add(SimpleModelWrapper.bake(baker, baker.resolveInlineModel(alternatives.get(i), () -> name), state));
        }
        return factory.apply(List.copyOf(baked));
    }

    @Override
    public UnbakedGeometry geometry()
    {
        // Ordinary consumers get a default; world variants use bakeBlock instead.
        // TFC inventory assets use separate static models, not these world definitions.
        return (slots, baker, state, debug) -> {
            final ResolvedModel child = baker.resolveInlineModel(alternatives.get(fallback), debug);
            return child.bakeTopGeometry(child.getTopTextureSlots(), baker, state);
        };
    }
}
