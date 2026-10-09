/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.placement;

import java.util.stream.Stream;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * Extension of the vanilla carving mask decorator which allows min and max y bounds.
 */
public class BoundedCarvingMaskPlacement extends PlacementModifier
{
    public static final MapCodec<BoundedCarvingMaskPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        VerticalAnchor.CODEC.optionalFieldOf("min_y", VerticalAnchor.bottom()).forGetter(c -> c.minY),
        VerticalAnchor.CODEC.optionalFieldOf("max_y", VerticalAnchor.top()).forGetter(c -> c.maxY),
        // Accept old TFC datapacks, but do not silently reinterpret a liquid mask as air.
        Codec.STRING.validate(step -> step.equals("air") ? DataResult.success(step) : DataResult.error(() -> "TFC carving masks only support the air step"))
            .optionalFieldOf("step", "air").forGetter(c -> "air")
    ).apply(instance, (minY, maxY, step) -> new BoundedCarvingMaskPlacement(minY, maxY)));

    private final VerticalAnchor minY;
    private final VerticalAnchor maxY;

    public BoundedCarvingMaskPlacement(VerticalAnchor minY, VerticalAnchor maxY)
    {
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public PlacementModifierType<?> type()
    {
        return TFCPlacements.CARVING_MASK.get();
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos)
    {
        final ChunkPos chunkPos = new ChunkPos(pos);
        final CarvingMask carvingMask = context.getCarvingMask(chunkPos);
        final int minY = this.minY.resolveY(context);
        final int maxY = this.maxY.resolveY(context);

        return carvingMask.stream(chunkPos).filter(p -> p.getY() >= minY && p.getY() <= maxY);
    }
}
