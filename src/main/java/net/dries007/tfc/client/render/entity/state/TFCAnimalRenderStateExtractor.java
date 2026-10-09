/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity.state;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.GenderedRenderAnimal;
import net.dries007.tfc.common.entities.aquatic.AmphibiousAnimal;
import net.dries007.tfc.common.entities.aquatic.AquaticCritter;
import net.dries007.tfc.common.entities.livestock.Age;
import net.dries007.tfc.common.entities.livestock.OviparousAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.common.entities.livestock.WoolyAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.entities.livestock.camel.AbstractCamel;
import net.dries007.tfc.common.entities.livestock.camel.BactrianCamel;
import net.dries007.tfc.common.entities.livestock.pet.Dog;
import net.dries007.tfc.common.entities.predator.Predator;
import net.dries007.tfc.common.entities.prey.Pest;
import net.dries007.tfc.common.entities.prey.RammingPrey;
import net.dries007.tfc.common.entities.prey.WildAnimal;
import net.dries007.tfc.common.entities.prey.WingedPrey;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Season;

/** The only live-world boundary for hierarchical animal animation data. */
public final class TFCAnimalRenderStateExtractor
{
    private TFCAnimalRenderStateExtractor() {}

    public static void extract(Mob entity, TFCAnimalRenderState state, float partialTick)
    {
        state.resetCustomState();
        final var position = entity.position();
        state.atGuideOrigin = Math.abs(position.x) < 0.01f && Math.abs(position.y) < 0.01f && Math.abs(position.z) < 0.01f;
        state.onGround = entity.onGround();
        state.movementLengthSqr = entity.getDeltaMovement().lengthSqr();
        // 26.1 uses the native water tracker instead of the removed bubble flag.
        state.inWaterOrBubble = entity.isInWater();
        state.movingOnLand = state.onGround && state.movementLengthSqr > 1.0E-6D && !state.inWaterOrBubble;
        state.aggressive = entity.isAggressive();
        if (entity instanceof GenderedRenderAnimal animal)
        {
            state.maleCharacteristics = animal.displayMaleCharacteristics();
            state.femaleCharacteristics = animal.displayFemaleCharacteristics();
        }
        if (entity instanceof WildAnimal animal)
        {
            state.isMale = animal.isMale();
        }
        if (entity instanceof TFCAnimalProperties animal)
        {
            state.isMale = animal.isMale();
            state.isOld = animal.getAgeType() == Age.OLD;
        }
        if (entity instanceof TFCAnimal animal)
        {
            state.geneticSizeScale = LivestockRenderStateMath.geneticScale(animal.getGeneticSize());
        }
        if (entity instanceof WoolyAnimal animal)
        {
            state.hasProduct = animal.hasProduct();
        }
        if (entity instanceof OviparousAnimal bird)
        {
            state.wingFlap = LivestockRenderStateMath.wingFlap(bird.oFlap, bird.flap, bird.oFlapSpeed, bird.flapSpeed, partialTick);
        }
        if (entity instanceof RammingPrey prey)
        {
            state.telegraphingAttack = prey.isTelegraphingAttack();
            state.telegraphAttackTick = prey.getTelegraphAttackTick();
            state.attackingAnimation.copyFrom(prey.attackingAnimation);
        }
        if (entity instanceof Predator predator)
        {
            state.sleeping = predator.isSleeping();
            state.sleepingAnimation.copyFrom(predator.sleepingAnimation);
            state.attackingAnimation.copyFrom(predator.attackingAnimation);
            state.hurtByEntity = predator.getBrain().checkMemory(MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_PRESENT);
            state.bearCrawlsOn = predator.getBlockStateOn().is(TFCTags.Blocks.BEAR_CRAWLS_ON);
        }
        if (entity instanceof Pest pest)
        {
            state.climbing = pest.isClimbing();
            state.walkingAnimation.copyFrom(pest.walkingAnimation);
            state.eatingAnimation.copyFrom(pest.eatingAnimation);
            state.searchingAnimation.copyFrom(pest.searchingAnimation);
            state.sniffingAnimation.copyFrom(pest.sniffingAnimation);
            state.draggingAnimation.copyFrom(pest.draggingAnimation);
        }
        if (entity instanceof WingedPrey)
        {
            // Keep the prior client-calendar/hemisphere choice, but capture it
            // before model submission rather than querying it during drawing.
            state.fallSeason = Calendars.CLIENT.getHemispheralCalendarMonthOfYear(ClientHelpers.inNorthernHemisphere()).getSeason() == Season.FALL;
        }
        if (entity instanceof AmphibiousAnimal animal)
        {
            state.playingDead = animal.isPlayingDead();
        }
        if (entity instanceof AquaticCritter critter)
        {
            state.idleAnimation.copyFrom(critter.idleAnimation);
            state.hurtAnimation.copyFrom(critter.hurtAnimation);
        }
        if (entity instanceof Dog dog)
        {
            state.sleeping = dog.isSleeping();
            state.sitting = dog.isSitting();
            state.headRollAngle = dog.getHeadRollAngle(partialTick);
            state.hasOwner = dog.getOwnerUUID() != null;
            state.collarColor = dog.getCollarColor().getTextureDiffuseColor();
        }
        if (entity instanceof AbstractCamel camel)
        {
            state.isSaddled = !camel.getItemBySlot(EquipmentSlot.SADDLE).isEmpty();
            state.sitAnimationState.copyFrom(camel.sitAnimationState);
            state.sitPoseAnimationState.copyFrom(camel.sitPoseAnimationState);
            state.sitUpAnimationState.copyFrom(camel.sitUpAnimationState);
            state.idleAnimationState.copyFrom(camel.idleAnimationState);
            state.dashAnimationState.copyFrom(camel.dashAnimationState);
        }
        if (entity instanceof BactrianCamel camel)
        {
            state.hasProduct = camel.hasProduct();
        }
    }
}
