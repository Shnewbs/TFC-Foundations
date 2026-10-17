/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.data;

import java.util.Locale;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.fluids.SimpleFluid;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.data.FluidHeat;

public interface Accessors
{
    static Ingredient ingredient(TagKey<Item> tag)
    {
        // Datagen serializes the named holder reference; it must not flatten an unbound tag to an empty item list.
        return Ingredient.of(BuiltInRegistries.ITEM.get(tag).orElseGet(() -> net.minecraft.core.HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag)));
    }

    static net.neoforged.neoforge.fluids.crafting.FluidIngredient fluidIngredient(TagKey<Fluid> tag)
    {
        return net.neoforged.neoforge.fluids.crafting.FluidIngredient.of(BuiltInRegistries.FLUID.get(tag)
            .orElseGet(() -> net.minecraft.core.HolderSet.emptyNamed(BuiltInRegistries.FLUID, tag)));
    }

    static Ingredient ingredient(ItemLike... items) { return Ingredient.of(items); }
    static Ingredient ingredient(java.util.stream.Stream<? extends ItemLike> items) { return Ingredient.of(items); }
    static Ingredient ingredient(net.minecraft.core.HolderSet<Item> items) { return Ingredient.of(items); }

    static Ingredient ingredient(ItemStack stack) { return Ingredient.of(stack.getItem()); }

    static ItemStack resultOf(net.minecraft.world.item.crafting.Recipe<?> recipe, net.minecraft.core.HolderLookup.Provider registries)
    {
        if (recipe instanceof net.dries007.tfc.common.recipes.ISimpleRecipe<?> simple) return simple.getResultItem(registries);
        if (recipe instanceof net.dries007.tfc.common.recipes.AdvancedShapedRecipe shaped) return shaped.getResultItem(registries);
        if (recipe instanceof net.dries007.tfc.common.recipes.AdvancedShapelessRecipe shapeless) return shapeless.getResultItem(registries);
        // Naming an output must not resolve unrelated ingredient previews while datagen tags are placeholders.
        if (recipe instanceof net.minecraft.world.item.crafting.ShapedRecipe shaped)
            return ((net.dries007.tfc.mixin.accessor.ShapedRecipeAccessor) shaped).accessor$getResult().create();
        if (recipe instanceof net.minecraft.world.item.crafting.ShapelessRecipe shapeless)
            return shapeless.result() == null ? ItemStack.EMPTY : shapeless.result().create();
        final var context = new net.minecraft.util.context.ContextMap.Builder()
            .withParameter(net.minecraft.world.item.crafting.display.SlotDisplayContext.REGISTRIES, registries)
            .create(net.minecraft.world.item.crafting.display.SlotDisplayContext.CONTEXT);
        return recipe.display().stream().map(display -> display.result().resolveForFirstStack(context))
            .filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
    }

    default Ingredient ingredientOf(Metal metal, Metal.ItemType type)
    {
        return type.isCommonTagPart()
            ? Accessors.ingredient(commonTagOf(metal, type))
            : Accessors.ingredient(TFCItems.METAL_ITEMS.get(metal).get(type).get());
    }

    default Ingredient ingredientOf(Metal metal, Metal.BlockType type)
    {
        return type == Metal.BlockType.BLOCK
            ? Accessors.ingredient(storageBlockTagOf(Registries.ITEM, metal))
            : Accessors.ingredient(TFCBlocks.METALS.get(metal).get(type).get());
    }

    default Ingredient ingredientOf(Ingredient... values)
    {
        return CompoundIngredient.of(values);
    }

    default <T> TagKey<T> logsTagOf(ResourceKey<Registry<T>> registry, Wood wood)
    {
        return TagKey.create(registry, Helpers.identifier(wood.getSerializedName() + "_logs"));
    }

    default TagKey<Item> commonTagOf(Metal metal, Metal.ItemType type)
    {
        assert type.isCommonTagPart() : "Non-typical use of tag for " + metal.getSerializedName() + " / " + type.name();
        assert type.has(metal) : "Non-typical use of " + metal.getSerializedName() + " / " + type.name();
        return commonTagOf(Registries.ITEM, type.name() + "s/" + metal.name());
    }

    default <T> TagKey<T> storageBlockTagOf(ResourceKey<Registry<T>> key, Metal metal)
    {
        assert metal.defaultParts() : "Non-typical use of a non-default metal " + metal.getSerializedName();
        return commonTagOf(key, "storage_blocks/" + metal.getSerializedName());
    }

    default TagKey<Block> oreBlockTagOf(Ore ore, @Nullable Ore.Grade grade)
    {
        return commonTagOf(Registries.BLOCK, "ores/" + (ore.isGraded() ? ore.metal().name() : ore.name()) + (grade == null ? "" : "/" + grade.name()));
    }

    default <T> TagKey<T> commonTagOf(ResourceKey<Registry<T>> key, String name)
    {
        return TagKey.create(key, Identifier.fromNamespaceAndPath("c", name.toLowerCase(Locale.ROOT)));
    }

    default <T> TagKey<T> tfcTagOf(ResourceKey<Registry<T>> key, String name)
    {
        return TagKey.create(key, Identifier.fromNamespaceAndPath("tfc", name.toLowerCase(Locale.ROOT)));
    }

    default Item dyeOf(DyeColor color)
    {
        return itemOf(Identifier.withDefaultNamespace(color.getSerializedName() + "_dye"));
    }

    default Item dyedOf(DyeColor color, String suffix)
    {
        return itemOf(Identifier.withDefaultNamespace(color.getSerializedName() + "_" + suffix));
    }

    default Item itemOf(Identifier name)
    {
        assert BuiltInRegistries.ITEM.containsKey(name) : "No item '" + name + "'";
        return BuiltInRegistries.ITEM.getOptional(name).orElseThrow();
    }

    default Fluid fluidOf(DyeColor color)
    {
        return TFCFluids.COLORED_FLUIDS.get(color).getSource();
    }

    default Fluid fluidOf(SimpleFluid fluid)
    {
        return TFCFluids.SIMPLE_FLUIDS.get(fluid).getSource();
    }

    default Fluid fluidOf(Metal metal)
    {
        return TFCFluids.METALS.get(metal).getSource();
    }

    default String nameOf(Ingredient ingredient)
    {
        if (ingredient.getCustomIngredient() instanceof CompoundIngredient ing) return nameOf(ing.children().get(0));
        return ingredient.getValues().unwrap().map(tag -> tag.location().getPath(), values -> nameOf(values.getFirst().value()));
    }

    default String nameOf(Fluid fluid)
    {
        assert fluid != Fluids.EMPTY : "Should never get name of Items.AIR";
        return BuiltInRegistries.FLUID.getKey(fluid).getPath();
    }

    default String nameOf(ItemLike item)
    {
        assert item.asItem() != Items.AIR : "Should never get name of Items.AIR";
        assert item.asItem() != Items.BARRIER : "Should never get name of Items.BARRIER";
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }

    default int units(Metal.ItemType type)
    {
        return switch (type)
        {
            case ROD -> 50;
            default -> 100;
            case DOUBLE_INGOT, SHEET, FISH_HOOK, FISHING_ROD, SWORD, SWORD_BLADE, MACE, MACE_HEAD, SHEARS, UNFINISHED_BOOTS -> 200;
            case DOUBLE_SHEET, TUYERE, UNFINISHED_HELMET, UNFINISHED_CHESTPLATE, UNFINISHED_GREAVES, SHIELD, BOOTS -> 400;
            case HELMET, GREAVES -> 600;
            case CHESTPLATE -> 800;
            case HORSE_ARMOR -> 1200;
        };
    }

    default int units(Metal.BlockType type)
    {
        return switch (type)
        {
            case ANVIL -> 1400;
            case BLOCK, EXPOSED_BLOCK, WEATHERED_BLOCK, OXIDIZED_BLOCK, LAMP, GRATE, EXPOSED_GRATE, WEATHERED_GRATE, OXIDIZED_GRATE -> 100;
            case BLOCK_SLAB, EXPOSED_BLOCK_SLAB, WEATHERED_BLOCK_SLAB, OXIDIZED_BLOCK_SLAB -> 50;
            case BLOCK_STAIRS, EXPOSED_BLOCK_STAIRS, WEATHERED_BLOCK_STAIRS, OXIDIZED_BLOCK_STAIRS -> 75;
            case BARS -> 25;
            case CHAIN -> 6;
            case TRAPDOOR -> 200;
        };
    }

    default float temperatureOf(Metal metal)
    {
        return FluidHeat.MANAGER.getOrThrow(Helpers.identifier(metal.getSerializedName())).meltTemperature();
    }

    /**
     * Converts a number of hours into player ticks, since recipe durations are defined in player ticks.
     * Recipes using this will take the same amount of real-world time to complete regardless of the
     * server config's specified day length.
     * @param hours The number of in-game hours
     * @return The equivalent number of player ticks
     */
    default int hours(int hours)
    {
        return hours * ICalendar.PLAYER_TICKS_IN_DEFAULT_HOUR;
    }

    /**
     * Given a {@code Map<T1, Map<T2, V1>>}, and a key {@code T2}, constructs a map of all the mappings of {@code T1} to maps which contain
     * an entry for the given key {@code T2}
     * @return An immutable map, with iteration order given by iteration order of the input map
     */
    default <T1, T2, V> Map<T1, V> pivot(Map<T1, Map<T2, V>> map, T2 key)
    {
        // This method must maintain a consistent, deterministic ordering, so we can't collect into a typical
        // hash map - we must use an order-preserving map here - immutable map is the easiest way to do that
        final ImmutableMap.Builder<T1, V> builder = new ImmutableMap.Builder<>();
        for (Map.Entry<T1, Map<T2, V>> entry : map.entrySet())
            if (entry.getValue().containsKey(key))
                builder.put(entry.getKey(), entry.getValue().get(key));
        return builder.build();
    }

    default BlockGetter empty()
    {
        return new BlockGetter() {
            @Nullable
            @Override
            public BlockEntity getBlockEntity(BlockPos pos)
            {
                return null;
            }

            @Override
            public BlockState getBlockState(BlockPos pos)
            {
                return Blocks.AIR.defaultBlockState();
            }

            @Override
            public FluidState getFluidState(BlockPos pos)
            {
                return Fluids.EMPTY.defaultFluidState();
            }

            @Override
            public int getHeight()
            {
                return 0;
            }

            @Override
            public int getMinY()
            {
                return 0;
            }
        };
    }
}
