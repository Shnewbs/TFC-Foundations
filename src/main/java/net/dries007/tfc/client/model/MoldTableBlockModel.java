/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

import net.dries007.tfc.common.blockentities.BlockEntityModelData;

/** The base table and its mold are baked together per reload; block entities retain only item identifiers. */
public final class MoldTableBlockModel implements DynamicBlockStateModel
{
    private final BlockStateModelPart base;
    private final Map<Identifier, BlockStateModelPart> molds;
    private final int flags;

    // Discovery completes before ModelManager resolves dependencies. Each Unbaked captures its own immutable copy.
    // Already-baked tables never read this catalog, so a failed/new reload cannot change their geometry.
    private static volatile Map<Identifier, Identifier> discovered = Map.of();

    public MoldTableBlockModel(BlockStateModelPart base, Map<Identifier, BlockStateModelPart> molds)
    {
        this.base = base;
        this.molds = Map.copyOf(molds);
        this.flags = molds.values().stream().mapToInt(BlockStateModelPart::materialFlags).reduce(base.materialFlags(), (a, b) -> a | b);
    }

    public static @Nullable Identifier itemForModelResource(Identifier resource)
    {
        final String path = resource.getPath();
        final String prefix = "models/block/mold/";
        if (!path.startsWith(prefix) || !path.endsWith(".json") || path.length() <= prefix.length() + 5) return null;
        return Identifier.fromNamespaceAndPath(resource.getNamespace(), path.substring(prefix.length(), path.length() - 5));
    }

    public static void registerStandaloneModels(ModelEvent.RegisterStandalone event)
    {
        registerDiscoveredModels(event, Minecraft.getInstance().getResourceManager()
            .listResources("models/block/mold", resource -> resource.getPath().endsWith(".json")).keySet());
    }

    /** The resource collection is passed explicitly so discovery can be tested without a running client. */
    public static void registerDiscoveredModels(ModelEvent.RegisterStandalone event, Collection<Identifier> resources)
    {
        final Map<Identifier, Identifier> catalog = new HashMap<>();
        resources.stream().sorted().forEach(resource -> {
                final Identifier item = itemForModelResource(resource);
                if (item == null) return;
                final Identifier model = Identifier.fromNamespaceAndPath(item.getNamespace(), "block/mold/" + item.getPath());
                catalog.put(item, model);
                event.register(new StandaloneModelKey<>(model::toString), SimpleUnbakedStandaloneModel.simpleModelWrapper(model));
            });
        discovered = Map.copyOf(catalog);
    }

    public @Nullable BlockStateModelPart mold(@Nullable Identifier item)
    {
        return item == null ? null : molds.get(item);
    }

    public Object geometryKey(@Nullable Identifier item)
    {
        return new GeometryKey(this, mold(item));
    }

    private @Nullable Identifier item(BlockAndTintGetter level, BlockPos pos)
    {
        return level.getModelData(pos).get(BlockEntityModelData.MOLD);
    }

    public void collect(@Nullable Identifier item, List<BlockStateModelPart> output)
    {
        output.add(base);
        final BlockStateModelPart mold = mold(item);
        if (mold != null) output.add(mold);
    }

    @Override public void collectParts(RandomSource random, List<BlockStateModelPart> output) { output.add(base); }
    @Override public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> output) { collect(item(level, pos), output); }
    @Override public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) { return geometryKey(item(level, pos)); }
    @Override public Material.Baked particleMaterial() { return base.particleMaterial(); }
    @Override public int materialFlags() { return flags; }
    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state)
    {
        final BlockStateModelPart mold = mold(item(level, pos));
        return base.materialFlags() | (mold == null ? 0 : mold.materialFlags());
    }

    private record GeometryKey(MoldTableBlockModel model, @Nullable BlockStateModelPart mold) {}

    public static final class Unbaked extends AbstractUnbakedModel implements DynamicBlockModel
    {
        private final UnbakedGeometry geometry;
        private Map<Identifier, Identifier> catalog = Map.of();

        private Unbaked(StandardModelParameters parameters, UnbakedGeometry geometry)
        {
            super(parameters);
            this.geometry = geometry;
        }

        @Override public UnbakedGeometry geometry() { return geometry; }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver)
        {
            if (parent() != null) resolver.markDependency(parent());
            // ModelManager joins the standalone-loading future before discovering model dependencies.
            catalog = discovered;
            catalog.values().forEach(resolver::markDependency);
        }

        @Override
        public BlockStateModel bakeBlock(ResolvedModel owner, ModelBaker baker, ModelState state)
        {
            final Map<Identifier, BlockStateModelPart> parts = new HashMap<>();
            final var root = owner.getTopAdditionalProperties().getOptional(NeoForgeModelProperties.TRANSFORM);
            final ModelState attached = root == null ? state : UnbakedElementsHelper.composeRootTransformIntoModelState(state, root);
            catalog.forEach((item, model) -> parts.put(item, SimpleModelWrapper.bake(baker, model, attached)));
            return new MoldTableBlockModel(SimpleModelWrapper.bake(baker, owner, state), parts);
        }
    }

    public enum Loader implements UnbakedModelLoader<Unbaked>
    {
        INSTANCE;

        @Override
        public Unbaked read(JsonObject json, JsonDeserializationContext context)
        {
            // Never remove fields from the caller's JSON; other consumers may share it.
            final JsonObject vanilla = json.deepCopy();
            vanilla.remove("loader");
            final CuboidModel model = context.deserialize(vanilla, CuboidModel.class);
            return new Unbaked(StandardModelParameters.parse(vanilla, context), model.geometry());
        }
    }
}
