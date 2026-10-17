/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.blockentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.client.model.StandaloneBlockModels;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.item.ItemComponent;
import net.dries007.tfc.common.items.PanItem;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.data.Deposit;

/** Stage selection happens during extraction; native layers retain only baked geometry. */
public final class PanItemRenderer implements ItemModel
{
    private static final Identifier BASE = Helpers.identifier("item/pan/filled");
    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;
    private final Map<Identifier, ItemModel> stages = new HashMap<>();

    private PanItemRenderer(ModelRenderProperties properties, Matrix4fc transformation)
    {
        this.properties = properties;
        this.transformation = transformation;
    }

    public static int stageIndex(int useTicks, int count)
    {
        return Mth.clamp(Math.round((float) useTicks / PanItem.USE_TIME * count), 0, count - 1);
    }

    private ItemModel bakeStage(Identifier id)
    {
        final var model = Minecraft.getInstance().getModelManager().getStandaloneModel(StandaloneBlockModels.key(id));
        final List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(42), parts);
        final var geometry = new QuadCollection.Builder();
        for (BlockStateModelPart part : parts)
        {
            part.getQuads(null).forEach(geometry::addUnculledFace);
            for (Direction side : Direction.values()) part.getQuads(side).forEach(geometry::addUnculledFace);
        }
        return new CuboidItemModelWrapper(List.of(), geometry.build(), properties, transformation);
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext,
        @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed)
    {
        if (level == null) return;
        final Deposit deposit = Deposit.get(stack.getOrDefault(TFCComponents.DEPOSIT, ItemComponent.EMPTY).stack());
        if (deposit == null || deposit.modelStages().isEmpty()) return;
        final var player = Minecraft.getInstance().player;
        final var models = deposit.modelStages();
        final Identifier id = models.get(player != null && displayContext.firstPerson() ? stageIndex(player.getTicksUsingItem(), models.size()) : 0);
        stages.computeIfAbsent(id, this::bakeStage).update(output, stack, resolver, displayContext, level, owner, seed);
    }

    public record Unbaked() implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());
        @Override public MapCodec<Unbaked> type() { return CODEC; }
        @Override public void resolveDependencies(Resolver resolver) { resolver.markDependency(BASE); }
        @Override public ItemModel bake(BakingContext context, Matrix4fc transformation)
        {
            final var baker = context.blockModelBaker();
            final var base = baker.getModel(BASE);
            return new PanItemRenderer(ModelRenderProperties.fromResolvedModel(baker, base, base.getTopTextureSlots()), transformation);
        }
    }
}
