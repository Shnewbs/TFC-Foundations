/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public enum IngotPileBlockModel implements UnbakedModelLoader<SimpleStaticBlockEntityModel>
{
    INSTANCE;

    @Override
    public SimpleStaticBlockEntityModel read(JsonObject json, JsonDeserializationContext context)
    {
        return new SimpleStaticBlockEntityModel(StandardModelParameters.parse(json, context), SimpleStaticBlockEntityModel.Kind.INGOT);
    }
}
