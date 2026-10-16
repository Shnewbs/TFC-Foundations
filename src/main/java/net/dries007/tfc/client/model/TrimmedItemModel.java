/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.neoforged.neoforge.client.model.ComposedModelState;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

/** Armor, optional overlay and material-colored trim, baked and cached within one native resource reload. */
public final class TrimmedItemModel implements ItemModel
{
    private final Unbaked definition;
    private final BakingContext context;
    private final Matrix4fc transformation;
    private final Map<String, ItemModel> cache = new HashMap<>();

    private TrimmedItemModel(Unbaked definition, BakingContext context, Matrix4fc transformation)
    {
        this.definition = definition;
        this.context = context;
        this.transformation = transformation;
    }

    public static String materialSuffix(@Nullable ArmorTrim trim)
    {
        return trim == null ? "" : trim.material().value().assets().base().suffix();
    }

    private ItemModel bakeTrim(String suffix)
    {
        final ModelBaker baker = context.blockModelBaker();
        final Textures textures = definition.textures();
        final Material.Baked armor = baker.materials().get(textures.armor(), () -> "tfc:trim");
        final ModelRenderProperties properties = new ModelRenderProperties(false, armor, baker.getModel(definition.parent()).getTopTransforms());
        final List<Material.Baked> materials = new ArrayList<>();
        materials.add(armor);
        textures.overlay().ifPresent(m -> materials.add(baker.materials().get(m, () -> "tfc:trim")));
        if (!suffix.isEmpty()) materials.add(baker.materials().get(new Material(textures.trim().sprite().withSuffix("_" + suffix)), () -> "tfc:trim"));
        final List<ItemModel> layers = new ArrayList<>();
        for (int index = 0; index < materials.size(); index++)
        {
            final boolean trim = !suffix.isEmpty() && index == materials.size() - 1;
            final var state = new ComposedModelState(BlockModelRotation.IDENTITY, trim ? ContainedFluidModel.COVER_TRANSFORM : ContainedFluidModel.FLUID_TRANSFORM);
            layers.add(new CuboidItemModelWrapper(index == 0 ? definition.tints() : List.of(), ItemLayerGeometry.translucent(baker.compute(new ItemModelGenerator.ItemLayerKey(materials.get(index), state, 0)), null, false), properties, transformation));
        }
        return new CompositeModel(layers);
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext,
        @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed)
    {
        cache.computeIfAbsent(materialSuffix(stack.get(DataComponents.TRIM)), this::bakeTrim)
            .update(output, stack, resolver, displayContext, level, owner, seed);
    }

    public record Textures(Material armor, Optional<Material> overlay, Material trim)
    {
        public static final Codec<Textures> CODEC = RecordCodecBuilder.create(i -> i.group(
            Material.CODEC.fieldOf("armor").forGetter(Textures::armor),
            Material.CODEC.optionalFieldOf("overlay").forGetter(Textures::overlay),
            Material.CODEC.fieldOf("trim").forGetter(Textures::trim)
        ).apply(i, Textures::new));
    }

    public record Unbaked(Textures textures, Identifier parent, List<ItemTintSource> tints) implements ItemModel.Unbaked
    {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Textures.CODEC.fieldOf("textures").forGetter(Unbaked::textures),
            Identifier.CODEC.optionalFieldOf("parent", ContainedFluidModel.DEFAULT_PARENT).forGetter(Unbaked::parent),
            ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(Unbaked::tints)
        ).apply(i, Unbaked::new));

        @Override public MapCodec<Unbaked> type() { return CODEC; }
        @Override public ItemModel bake(BakingContext context, Matrix4fc transformation) { return new TrimmedItemModel(this, context, transformation); }
        @Override public void resolveDependencies(Resolver resolver) { resolver.markDependency(parent); }
    }
}
