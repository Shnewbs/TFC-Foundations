/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

/** Registration for migrated world-model pipelines. Other model families migrate independently. */
public final class BlockModelRegistration
{
    private BlockModelRegistration() {}

    public static void registerLoaders(ModelEvent.RegisterLoaders event)
    {
        event.register(Identifier.fromNamespaceAndPath("tfc", "plant"), PlantBlockModel.Loader.INSTANCE);
        event.register(Identifier.fromNamespaceAndPath("tfc", "leaves"), LeavesBlockModel.Loader.INSTANCE);
        event.register(Identifier.fromNamespaceAndPath("tfc", "mold"), new MoldsModelLoader());
    }

    public static void registerBlockStateModels(RegisterBlockStateModels event)
    {
        event.registerModel(Identifier.fromNamespaceAndPath("tfc", "dynamic"), DynamicBlockModel.Unbaked.CODEC);
    }
}
