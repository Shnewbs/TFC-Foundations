/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

/** Stable lookup keys across reloads; baked models remain owned by each native ModelManager reload. */
public final class StandaloneBlockModels
{
    private static final Map<Identifier, StandaloneModelKey<BlockStateModel>> KEYS = new ConcurrentHashMap<>();

    private StandaloneBlockModels() {}

    public static StandaloneModelKey<BlockStateModel> key(Identifier id)
    {
        return KEYS.computeIfAbsent(id, value -> new StandaloneModelKey<>(value::toString));
    }

    public static void register(ModelEvent.RegisterStandalone event, Identifier id)
    {
        event.register(key(id), SimpleUnbakedStandaloneModel.blockStateModel(id));
    }
}
