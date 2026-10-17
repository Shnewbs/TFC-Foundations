/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test;

import java.util.List;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.entities.TFCEntities;

import static org.junit.jupiter.api.Assertions.*;

public class TemptationAttributesTest implements TestSetup
{
    @Test
    public void registeredAnimalsUsingVanillaTemptGoalHaveTemptRange()
    {
        // This is the same registered supplier and value lookup used by LivingEntity/TemptGoal.
        // TFC's other wild animals use its own fixed-range DelegatingTemptingSensor.
        for (var entity : List.of(TFCEntities.RABBIT, TFCEntities.HORSE, TFCEntities.DONKEY,
            TFCEntities.MULE, TFCEntities.OCELOT))
        {
            final var attributes = new AttributeMap(DefaultAttributes.getSupplier(entity.get()));
            assertEquals(10.0, assertDoesNotThrow(() -> attributes.getValue(Attributes.TEMPT_RANGE), entity.getId().toString()));
        }
    }

    @Test
    public void rabbitRetainsTfcCombatAndMovementStats()
    {
        final var attributes = new AttributeMap(DefaultAttributes.getSupplier(TFCEntities.RABBIT.get()));
        assertEquals(12.0, attributes.getValue(Attributes.MAX_HEALTH));
        assertEquals(0.3F, attributes.getValue(Attributes.MOVEMENT_SPEED));
        assertEquals(3.0, attributes.getValue(Attributes.ATTACK_DAMAGE));
    }
}
