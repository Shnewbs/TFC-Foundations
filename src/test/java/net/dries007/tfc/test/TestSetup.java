/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.data.tags.VanillaItemTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.data.internal.NeoForgeBlockTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeFluidTagsProvider;
import net.neoforged.neoforge.common.data.internal.NeoForgeItemTagsProvider;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;

import net.dries007.tfc.data.providers.BuiltinBlockTags;
import net.dries007.tfc.data.providers.BuiltinDrinkables;
import net.dries007.tfc.data.providers.BuiltinFluidHeat;
import net.dries007.tfc.data.providers.BuiltinFluidTags;
import net.dries007.tfc.data.providers.BuiltinFoods;
import net.dries007.tfc.data.providers.BuiltinItemHeat;
import net.dries007.tfc.data.providers.BuiltinItemSizes;
import net.dries007.tfc.data.providers.BuiltinItemTags;
import net.dries007.tfc.data.providers.BuiltinKnappingTypes;
import net.dries007.tfc.data.providers.BuiltinRecipes;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.collections.IndirectHashCollection;
import net.dries007.tfc.util.data.FluidHeat;

/**
 * Bootstraps a number of useful pieces of data for unit tests. This is done using a hybrid of TFC and vanilla data generation,
 * plus a number of terrible hacks to get this to work... but... it does work. And it allows fast testing of complex mechanics
 * (molds, item/fluid heat components, heating, etc.)
 */
public interface TestSetup
{
    AtomicBoolean LOADED = new AtomicBoolean(false);
    java.util.concurrent.atomic.AtomicReference<HolderLookup.Provider> REGISTRIES = new java.util.concurrent.atomic.AtomicReference<>();
    Object LOCK = new Object();

    @BeforeAll
    @SuppressWarnings({"deprecation", "UnstableApiUsage", "DataFlowIssue"})
    static void beforeAll()
    {
        synchronized (LOCK)
        {
            if (LOADED.get()) return;

            final HolderLookup.Provider lookup = createLookup();
            final CompletableFuture<HolderLookup.Provider> provider = CompletableFuture.completedFuture(lookup);
            final Path path = Path.of(".");
            final PackOutput output = new PackOutput(path);
            final CompletableFuture<?> now = CompletableFuture.completedFuture(null);

            final TagMap itemTagMap = new TagMap();
            final TagMap blockTagMap = new TagMap();
            final TagMap fluidTagMap = new TagMap();

            new BuiltinDrinkables(output, provider).run(lookup); // Must run before fluid tags

            final CompletableFuture<TagsProvider.TagLookup<Block>> blockTagLookup = CompletableFuture.completedFuture(key -> Optional.ofNullable(blockTagMap.get(key.location())));

            add(blockTagMap, new VanillaBlockTagsProvider(output, provider));
            add(itemTagMap, new VanillaItemTagsProvider(output, provider));
            add(fluidTagMap, new FluidTagsProvider(output, provider));

            add(blockTagMap, new NeoForgeBlockTagsProvider(output, provider));
            add(itemTagMap, new NeoForgeItemTagsProvider(output, provider, blockTagLookup));
            add(fluidTagMap, new NeoForgeFluidTagsProvider(output, provider));

            add(blockTagMap, new BuiltinBlockTags(output, provider));
            add(itemTagMap, new BuiltinItemTags(output, provider, blockTagLookup));
            add(fluidTagMap, new BuiltinFluidTags(output, provider, now));

            resolve(blockTagMap, BuiltInRegistries.BLOCK);
            resolve(itemTagMap, BuiltInRegistries.ITEM);
            resolve(fluidTagMap, BuiltInRegistries.FLUID);
            // Recreate the pre-tag drinkable definitions against the now-bound tags before runtime cache enumeration.
            new BuiltinDrinkables(output, provider).run(lookup);

            // The native loader binds these after world data loads, which this registry/data fixture supplies.
            BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup).forEach(pending -> pending.apply());

            new BuiltinFoods(output, provider).run(lookup);
            new BuiltinFluidHeat(output, provider).run(lookup);
            final var itemHeat = new BuiltinItemHeat(output, provider, now);
            itemHeat.run(lookup);
            new BuiltinItemSizes(output, provider).run(lookup);
            new BuiltinKnappingTypes(output, provider).run(lookup); // Must run before recipes

            final List<RecipeHolder<?>> holders = new ArrayList<>();
            new BuiltinRecipes(output, provider, now, itemHeat).buildRecipes(lookup, new RecipeOutput() {
                @Override
                public Advancement.Builder advancement()
                {
                    return Advancement.Builder.recipeAdvancement();
                }

                @Override
                public void includeRootAdvancement() {} // Collect recipes without writing advancements.

                @Override
                public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions)
                {
                    holders.add(new RecipeHolder<>(id, recipe));
                }
            });
            final RecipeMap recipeManager = RecipeMap.create(holders);

            Helpers.setCachedRecipeMap(recipeManager);
            IndirectHashCollection.reloadAllCaches(recipeManager);
            FluidHeat.updateCache();
            net.dries007.tfc.common.component.TFCComponents.onModifyDefaultComponentsAfterResourceReload();

            REGISTRIES.set(lookup);
            LOADED.set(true);
        }
    }


    private static HolderLookup.Provider createLookup()
    {
        return net.dries007.tfc.data.ComponentAwareLookup.withBannerPatternTags(VanillaRegistries.createLookup());
    }

    Field TAG_BUILDERS = Helpers.uncheck(() -> {
        final var field = TagsProvider.class.getDeclaredField("builders");
        field.setAccessible(true);
        return field;
    });
    Method TAG_CONTENTS_PROVIDER = Helpers.uncheck(() -> {
        final var method = TagsProvider.class.getDeclaredMethod("createContentsProvider");
        method.setAccessible(true);
        return method;
    });

    private static <T extends TagsProvider<?>> void add(TagMap map, T provider)
    {
        Helpers.uncheck(() -> {
            TAG_BUILDERS.set(provider, map);
            ((CompletableFuture<?>) TAG_CONTENTS_PROVIDER.invoke(provider)).get();
        });
    }

    private static <T> void resolve(TagMap map, Registry<T> registry)
    {
        final TagResolver<T> resolver = new TagResolver<>(map, registry);
        registry.prepareTagReload(new TagLoader.LoadResult<>(registry.key(), map.keySet()
            .stream()
            .collect(Collectors.toMap(
                e -> TagKey.create(registry.key(), e),
                e -> resolver.resolve(e).toList()
            )))).apply();
    }

    record TagResolver<T>(Map<Identifier, TagBuilder> builder, Registry<T> registry)
    {
        Stream<Holder<T>> resolve(Identifier id)
        {
            return Objects.requireNonNull(builder.get(id), () -> "No tag for " + id + " in registry " + registry.key().identifier())
                .build()
                .stream()
                .flatMap(e -> e.isTag()
                    ? e.isRequired() || builder.containsKey(e.getId())
                        ? resolve(e.getId())
                        : Stream.empty()
                    : Stream.of(registry.getOrThrow(ResourceKey.create(registry.key(), e.getId()))));
        }
    }

    class TagMap extends LinkedHashMap<Identifier, TagBuilder>
    {
        @Override
        public void clear() {} // No-op
    }

    default long seed()
    {
        final long seed = RandomSupport.generateUniqueSeed();
        System.out.printf("Seed: %d\n", seed);
        return seed;
    }
}
