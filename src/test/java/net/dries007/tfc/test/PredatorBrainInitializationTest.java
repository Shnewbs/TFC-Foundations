/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.test;

import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryMap;
import org.junit.jupiter.api.Test;

import net.dries007.tfc.common.entities.ai.TFCBrain;
import net.dries007.tfc.common.entities.ai.predator.PredatorAi;
import net.dries007.tfc.common.entities.ai.predator.PackPredatorAi;
import net.dries007.tfc.common.entities.ai.predator.AmphibiousPredatorAi;
import net.dries007.tfc.common.entities.predator.Predator;

import static org.junit.jupiter.api.Assertions.*;

public class PredatorBrainInitializationTest implements TestSetup
{
    private static final class NativeBrain extends Brain<Predator>
    {
        NativeBrain(List<net.minecraft.world.entity.ai.ActivityData<Predator>> activities)
        {
            super(PredatorAi.MEMORY_TYPES, List.of(), activities, MemoryMap.EMPTY, RandomSource.create(42));
        }
    }

    @Test
    public void solitaryPredatorBrainCanInitializeBeforeItsOwnerReceivesIt()
    {
        final var brain = new NativeBrain(PredatorAi.createActivities(null));
        assertSame(brain, assertDoesNotThrow(() -> PredatorAi.makeBrain(brain, null)));
        assertEquals(TFCBrain.HUNT.get(), brain.getActiveNonCoreActivity().orElseThrow());
    }

    @Test
    public void packPredatorBrainCanInitializeBeforeItsOwnerReceivesIt()
    {
        final var brain = new NativeBrain(PackPredatorAi.createActivities(null));
        assertSame(brain, assertDoesNotThrow(() -> PackPredatorAi.makeBrain(brain, null)));
        assertEquals(TFCBrain.HUNT.get(), brain.getActiveNonCoreActivity().orElseThrow());
    }

    @Test
    public void amphibiousPredatorBrainCanInitializeBeforeItsOwnerReceivesIt()
    {
        final var brain = new NativeBrain(AmphibiousPredatorAi.createActivities(null));
        assertSame(brain, assertDoesNotThrow(() -> AmphibiousPredatorAi.makeBrain(brain, null)));
        assertEquals(TFCBrain.HUNT.get(), brain.getActiveNonCoreActivity().orElseThrow());
    }
}
