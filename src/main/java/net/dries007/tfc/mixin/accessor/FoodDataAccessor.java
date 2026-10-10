/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin.accessor;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Access the same exhaustion value that vanilla consumes in FoodData#tick. */
@Mixin(FoodData.class)
public interface FoodDataAccessor
{
    @Accessor("exhaustionLevel")
    float tfc$getExhaustionLevel();

    @Accessor("exhaustionLevel")
    void tfc$setExhaustionLevel(float value);
}
