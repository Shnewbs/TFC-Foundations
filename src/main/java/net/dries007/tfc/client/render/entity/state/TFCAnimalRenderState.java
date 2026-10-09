/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.AnimationState;
import org.jspecify.annotations.Nullable;

/**
 * Values captured during extraction for TFC's hierarchical animal models.
 * No live entity, level, inventory or brain is retained by the submitted state.
 * Animation timelines are copied into independently owned, reusable instances.
 */
public class TFCAnimalRenderState extends LivingEntityRenderState
{
    public double movementLengthSqr;
    public int telegraphAttackTick;
    public boolean telegraphingAttack;
    public float headRollAngle;
    public int collarColor = -1;
    public boolean isOld;
    public boolean isMale;
    public boolean isSaddled;
    @Nullable public Identifier texture;
    public boolean atGuideOrigin;
    public boolean onGround;
    public boolean inWaterOrBubble;
    public boolean movingOnLand;
    public boolean aggressive;
    public boolean hurtByEntity;
    public boolean bearCrawlsOn;
    public boolean maleCharacteristics;
    public boolean hasProduct;
    public boolean playingDead;
    public boolean sleeping;
    public boolean sitting;
    public boolean climbing;
    public boolean fallSeason;
    public boolean hasOwner;

    public final AnimationState sleepingAnimation = new AnimationState();
    public final AnimationState attackingAnimation = new AnimationState();
    public final AnimationState walkingAnimation = new AnimationState();
    public final AnimationState eatingAnimation = new AnimationState();
    public final AnimationState searchingAnimation = new AnimationState();
    public final AnimationState sniffingAnimation = new AnimationState();
    public final AnimationState draggingAnimation = new AnimationState();
    public final AnimationState idleAnimation = new AnimationState();
    public final AnimationState hurtAnimation = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState sitPoseAnimationState = new AnimationState();
    public final AnimationState sitUpAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState dashAnimationState = new AnimationState();

    /** Clear custom data before reusing this state for another extraction. */
    public void resetCustomState()
    {
        movementLengthSqr = 0;
        telegraphAttackTick = 0;
        telegraphingAttack = false;
        headRollAngle = 0;
        collarColor = -1;
        isOld = false;
        isMale = false;
        isSaddled = false;
        texture = null;
        atGuideOrigin = false;
        onGround = false;
        inWaterOrBubble = false;
        movingOnLand = false;
        aggressive = false;
        hurtByEntity = false;
        bearCrawlsOn = false;
        maleCharacteristics = false;
        hasProduct = false;
        playingDead = false;
        sleeping = false;
        sitting = false;
        climbing = false;
        fallSeason = false;
        hasOwner = false;
        sleepingAnimation.stop();
        attackingAnimation.stop();
        walkingAnimation.stop();
        eatingAnimation.stop();
        searchingAnimation.stop();
        sniffingAnimation.stop();
        draggingAnimation.stop();
        idleAnimation.stop();
        hurtAnimation.stop();
        sitAnimationState.stop();
        sitPoseAnimationState.stop();
        sitUpAnimationState.stop();
        idleAnimationState.stop();
        dashAnimationState.stop();
    }
}
