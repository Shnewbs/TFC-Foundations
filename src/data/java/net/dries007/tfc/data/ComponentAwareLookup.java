/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.data;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.block.entity.BannerPattern;

/** Native registry components for TFC's component-aware ItemStack outputs during datagen. */
public final class ComponentAwareLookup
{
    public static HolderLookup.Provider bindDefaultComponents(HolderLookup.Provider provider)
    {
        final var bound = withBannerPatternTags(provider);
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(bound).forEach(pending -> pending.apply());
        return bound;
    }

    public static HolderLookup.Provider withBannerPatternTags(HolderLookup.Provider provider)
    {
        // Native datagen supplies non-dereferenceable tag placeholders. Banner-pattern items store their
        // tag as a component, so materialize the real vanilla provider contents before component validation.
        final var registry = new MappedRegistry<BannerPattern>(Registries.BANNER_PATTERN, Lifecycle.stable());
        provider.lookupOrThrow(Registries.BANNER_PATTERN).listElements()
            .forEach(holder -> Registry.register(registry, holder.key(), holder.value()));
        registry.freeze();
        class Tags extends BannerPatternTagsProvider
        {
            Tags() { super(new PackOutput(Path.of(".")), CompletableFuture.completedFuture(provider), "minecraft"); }
            Map<Identifier, TagBuilder> collect()
            {
                addTags(provider);
                return Map.copyOf(builders);
            }
        }
        final var tags = new Tags().collect();
        registry.prepareTagReload(new TagLoader.LoadResult<>(Registries.BANNER_PATTERN, tags.keySet().stream()
            .collect(Collectors.toMap(id -> TagKey.create(Registries.BANNER_PATTERN, id), id -> resolve(tags, registry, id).toList())))).apply();
        return new HolderLookup.Provider() {
            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() { return provider.listRegistryKeys(); }

            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> key)
            {
                return key.equals(Registries.BANNER_PATTERN) ? (Optional) Optional.of(registry) : (Optional) provider.lookup(key);
            }

            @Override
            public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> parent)
            {
                // Preserve RegistrySetBuilder's owners for every registry we did not replace.
                final var original = provider.createSerializationContext(parent);
                return RegistryOps.create(parent, new RegistryOps.RegistryInfoLookup() {
                    @Override
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> key)
                    {
                        return key.equals(Registries.BANNER_PATTERN)
                            ? (Optional) Optional.of(RegistryOps.RegistryInfo.fromRegistryLookup(registry))
                            : original.lookupProvider.lookup(key);
                    }
                });
            }
        };
    }

    private static Stream<Holder<BannerPattern>> resolve(Map<Identifier, TagBuilder> tags, Registry<BannerPattern> registry, Identifier id)
    {
        return tags.get(id).build().stream().flatMap(entry -> {
            if (entry.isTag())
            {
                if (!tags.containsKey(entry.getId()))
                {
                    if (entry.isRequired()) throw new IllegalStateException("Missing banner pattern tag " + entry.getId());
                    return Stream.empty();
                }
                return resolve(tags, registry, entry.getId());
            }
            final Optional<Holder.Reference<BannerPattern>> holder = registry.get(ResourceKey.create(Registries.BANNER_PATTERN, entry.getId()));
            if (entry.isRequired() && holder.isEmpty()) throw new IllegalStateException("Missing banner pattern " + entry.getId());
            return holder.stream().map(h -> h);
        });
    }
}
