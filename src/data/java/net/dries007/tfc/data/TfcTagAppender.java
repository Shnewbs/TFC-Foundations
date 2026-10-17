/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.data;

import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

/** Fluent TFC additions delegate to the native key-based tag builder, including removals and optional references. */
public class TfcTagAppender<T> implements TagAppender<ResourceKey<T>, T>
{
    private final TagAppender<ResourceKey<T>, T> delegate;
    public TfcTagAppender(TagBuilder builder) { delegate = TagAppender.forBuilder(builder); }
    @Override public TfcTagAppender<T> add(ResourceKey<T> key) { delegate.add(key); return this; }
    @Override public TfcTagAppender<T> addOptional(ResourceKey<T> key) { delegate.addOptional(key); return this; }
    @Override public TfcTagAppender<T> addTag(TagKey<T> tag) { delegate.addTag(tag); return this; }
    @Override public TfcTagAppender<T> addOptionalTag(TagKey<T> tag) { delegate.addOptionalTag(tag); return this; }
    @Override public TfcTagAppender<T> add(TagEntry entry) { delegate.add(entry); return this; }
    @Override public TfcTagAppender<T> remove(ResourceKey<T> key) { delegate.remove(key); return this; }
    @Override public TfcTagAppender<T> remove(TagKey<T> tag) { delegate.remove(tag); return this; }
    @Override public TfcTagAppender<T> replace(boolean value) { delegate.replace(value); return this; }
    public TfcTagAppender<T> addOptionalTag(net.minecraft.resources.Identifier id) { delegate.add(TagEntry.optionalTag(id)); return this; }
    public TfcTagAppender<T> addTags(TagKey<T>... tags) { for (TagKey<T> tag : tags) addTag(tag); return this; }
}
