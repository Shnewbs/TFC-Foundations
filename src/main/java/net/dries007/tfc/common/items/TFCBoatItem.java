/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.items;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.dries007.tfc.common.entities.misc.TFCBoat;

public class TFCBoatItem extends BoatItem
{
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

    private final Supplier<? extends EntityType<TFCBoat>> boat;

    public TFCBoatItem(Supplier<? extends EntityType<TFCBoat>> boat, Properties properties)
    {
        super(false, Boat.Type.OAK, properties);
        this.boat = boat;
    }

    /**
     * Copy of BoatItem#use (superclass)
     */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        ItemStack itemstack = player.getItemInHand(hand);
        HitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitresult.getType() == HitResult.Type.MISS)
        {
            return InteractionResult.PASS;
        }
        else
        {
            Vec3 vec3 = player.getViewVector(1.0F);
            List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(vec3.scale(5.0D)).inflate(1.0D), ENTITY_PREDICATE);
            if (!list.isEmpty())
            {
                Vec3 vec31 = player.getEyePosition();

                for (Entity entity : list)
                {
                    AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
                    if (aabb.contains(vec31))
                    {
                        return InteractionResult.PASS;
                    }
                }
            }

            if (hitresult.getType() == HitResult.Type.BLOCK)
            {
                // tfc start
                TFCBoat boat = this.boat.get().create(level);
                if (boat != null)
                {
                    boat.setPos(hitresult.getLocation().x, hitresult.getLocation().y, hitresult.getLocation().z);
                }
                else
                {
                    return InteractionResult.FAIL;
                }
                // tfc end
                boat.setYRot(player.getYRot());
                if (!level.noCollision(boat, boat.getBoundingBox().inflate(-0.1D)))
                {
                    return InteractionResult.FAIL;
                }
                else
                {
                    if (!level.isClientSide)
                    {
                        level.addFreshEntity(boat);
                        level.gameEvent(player, GameEvent.ENTITY_PLACE, hitresult.getLocation());
                        if (!player.getAbilities().instabuild)
                        {
                            itemstack.shrink(1);
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME).heldItemTransformedTo(itemstack);
                }
            }
            else
            {
                return InteractionResult.PASS;
            }
        }
    }
}
