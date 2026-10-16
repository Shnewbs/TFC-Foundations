/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.color.item.FluidContentsTint;
import net.neoforged.neoforge.client.model.ComposedModelState;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/** Native item layers. Contents are inspected, never drained, so solid metal remains visible in molds. */
public final class ContainedFluidModel implements ItemModel
{
    public static final Transformation FLUID_TRANSFORM = new Transformation(new Vector3f(0.001f, 0.001f, 0), new Quaternionf(), new Vector3f(1, 1, 1.002f), new Quaternionf());
    public static final Transformation COVER_TRANSFORM = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 1.004f), new Quaternionf());
    public static final Identifier DEFAULT_PARENT = Identifier.fromNamespaceAndPath("neoforge", "item/default");

    private final Unbaked definition;
    private final BakingContext context;
    private final Matrix4fc transformation;
    // Each baked ItemModel belongs to one resource reload, including this cache and its baker.
    private final Map<Fluid, ItemModel> cache = new IdentityHashMap<>();

    private ContainedFluidModel(Unbaked definition, BakingContext context, Matrix4fc transformation)
    {
        this.definition = definition;
        this.context = context;
        this.transformation = transformation;
    }

    private ItemModel bakeFluid(Fluid fluid)
    {
        final ModelBaker baker = context.blockModelBaker();
        final var textures = definition.textures();
        final Material.Baked base = textures.base().map(m -> baker.materials().get(m, () -> "tfc:fluid_container")).orElse(null);
        final Material.Baked fluidMaterial = fluid == Fluids.EMPTY ? null : Minecraft.getInstance().getModelManager()
            .getFluidStateModelSet().get(fluid.defaultFluidState()).stillMaterial();
        final Material.Baked cover = base == null ? null : textures.cover().map(m -> baker.materials().get(m, () -> "tfc:fluid_container")).orElse(null);
        final Material.Baked particle = textures.particle().map(m -> baker.materials().get(m, () -> "tfc:fluid_container"))
            .orElseGet(() -> fluidMaterial == null ? baker.materials().get(new Material(Identifier.fromNamespaceAndPath("tfc", "block/empty")), () -> "tfc:fluid_container") : fluidMaterial);
        final ModelRenderProperties properties = new ModelRenderProperties(false, particle, baker.getModel(definition.parent()).getTopTransforms());
        final List<ItemModel> layers = new ArrayList<>();
        if (base != null)
        {
            layers.add(new CuboidItemModelWrapper(List.of(), ItemLayerGeometry.translucent(baker.compute(new ItemModelGenerator.ItemLayerKey(base, BlockModelRotation.IDENTITY, 0)), null, false), properties, transformation));
        }
        if (fluidMaterial != null && textures.fluid().isPresent())
        {
            final Material.Baked mask = baker.materials().get(textures.fluid().get(), () -> "tfc:fluid_container");
            final boolean emissive = fluid.getFluidType().getLightLevel() > 0;
            final QuadCollection quads = UnbakedElementsHelper.bakeItemMaskQuads(baker, 0, mask, fluidMaterial,
                new ComposedModelState(BlockModelRotation.IDENTITY, FLUID_TRANSFORM), new ExtraFaceData(-1, emissive ? 15 : 0, !emissive));
            layers.add(new CuboidItemModelWrapper(List.of(FluidContentsTint.INSTANCE), ItemLayerGeometry.translucent(quads, fluidMaterial.sprite(), emissive), properties, transformation));
        }
        if (cover != null)
        {
            final QuadCollection quads = UnbakedElementsHelper.bakeItemMaskQuads(baker, 0, cover, base,
                new ComposedModelState(BlockModelRotation.IDENTITY, COVER_TRANSFORM));
            layers.add(new CuboidItemModelWrapper(List.of(), ItemLayerGeometry.translucent(quads, base.sprite(), false), properties, transformation));
        }
        return new CompositeModel(layers);
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext,
        @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed)
    {
        // Native FluidUtil now reads ResourceHandler contents without simulating extraction.
        final var contents = FluidUtil.getFirstStackContained(stack);
        cache.computeIfAbsent(contents.getFluid(), this::bakeFluid).update(output, stack, resolver, displayContext, level, owner, seed);
    }

    public record Unbaked(DynamicFluidContainerModel.Textures textures, Identifier parent) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            DynamicFluidContainerModel.Textures.CODEC.fieldOf("textures").forGetter(Unbaked::textures),
            Identifier.CODEC.optionalFieldOf("parent", DEFAULT_PARENT).forGetter(Unbaked::parent)
        ).apply(i, Unbaked::new));

        @Override public MapCodec<Unbaked> type() { return CODEC; }
        @Override public ItemModel bake(BakingContext context, Matrix4fc transformation) { return new ContainedFluidModel(this, context, transformation); }
        @Override public void resolveDependencies(Resolver resolver) { resolver.markDependency(parent); }
    }
}
