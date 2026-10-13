/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin.accessor;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerExplosion.class)
public interface ServerExplosionAccessor
{
    @Invoker("calculateExplodedPositions")
    List<BlockPos> tfc$calculateExplodedPositions();

    @Invoker("hurtEntities")
    void tfc$hurtEntities(List<BlockPos> affectedBlocks);
}
