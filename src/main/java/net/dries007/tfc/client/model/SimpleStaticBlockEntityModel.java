/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;

import net.dries007.tfc.common.blockentities.BlockEntityModelData;

/** World-only snapshot models for piles and scraped items. Inventory items use their own static models. */
public final class SimpleStaticBlockEntityModel extends AbstractUnbakedModel implements DynamicBlockModel
{
    public enum Kind { INGOT, DOUBLE_INGOT, SCRAPING }
    private static final Identifier MISSING = Identifier.withDefaultNamespace("missingno");
    private final Kind kind;

    public SimpleStaticBlockEntityModel(StandardModelParameters parameters, Kind kind)
    {
        super(parameters);
        this.kind = kind;
    }

    @Override public UnbakedGeometry geometry() { return UnbakedGeometry.EMPTY; }

    @Override
    public BlockStateModel bakeBlock(ResolvedModel owner, ModelBaker baker, ModelState state)
    {
        final BlockStateModelPart empty = new SimpleModelWrapper(QuadCollection.EMPTY, owner.getTopAmbientOcclusion(), baker.missingBlockModelPart().particleMaterial());
        final var root = owner.getTopAdditionalProperties().getOptional(NeoForgeModelProperties.TRANSFORM);
        final ModelState transformed = root == null ? state : UnbakedElementsHelper.composeRootTransformIntoModelState(state, root);
        final StaticBlockMeshBaker meshBaker = new StaticBlockMeshBaker(baker.materials(), owner, transformed, owner.getTopAmbientOcclusion());
        if (kind == Kind.SCRAPING)
        {
            return new SnapshotBlockStateModel<>(
                (data, block) -> data.get(BlockEntityModelData.SCRAPING),
                value -> meshBaker.bake(StaticBlockMesh.scraping(value, MISSING)), empty);
        }
        final boolean doubled = kind == Kind.DOUBLE_INGOT;
        return new SnapshotBlockStateModel<PileKey>((data, block) -> {
            final var pile = data.get(BlockEntityModelData.PILE);
            if (pile == null) return null;
            // The geometry count comes from the current block state, not a potentially older inventory packet.
            int count = 0;
            for (var property : block.getProperties())
            {
                if (property.getName().equals("count") && block.getValue(property) instanceof Integer value) count = value;
            }
            return new PileKey(pile, count);
        }, value -> meshBaker.bake(StaticBlockMesh.pile(value.data(), value.count(), doubled)), empty);
    }

    private record PileKey(BlockEntityModelData.Pile data, int count) {}
}
