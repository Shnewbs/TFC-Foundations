/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.server.packs.resources.ResourceManager;

/** Keeps TFC's item and block materials together without collecting other mods' items. */
public final class TFCItemSpriteSource implements SpriteSource
{
    public static final TFCItemSpriteSource INSTANCE = new TFCItemSpriteSource();
    public static final MapCodec<TFCItemSpriteSource> CODEC = MapCodec.unit(INSTANCE);

    private TFCItemSpriteSource() {}

    @Override
    public void run(ResourceManager manager, SpriteSource.Output output)
    {
        manager.listResources("textures/item", id -> id.getNamespace().equals("tfc") && id.getPath().endsWith(".png"))
            .forEach((id, resource) -> output.add(TEXTURE_ID_CONVERTER.fileToId(id), resource));
    }

    @Override
    public MapCodec<? extends SpriteSource> codec()
    {
        return CODEC;
    }
}
