/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks;

import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.dries007.tfc.util.Helpers;

public class CharcoalPileBlock extends Block
{
    public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
    public static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[] {
        Shapes.empty(),
        box(0, 0, 0, 16, 2, 16),
        box(0, 0, 0, 16, 4, 16),
        box(0, 0, 0, 16, 6, 16),
        box(0, 0, 0, 16, 8, 16),
        box(0, 0, 0, 16, 10, 16),
        box(0, 0, 0, 16, 12, 16),
        box(0, 0, 0, 16, 14, 16),
        Shapes.block()
    };

    public CharcoalPileBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack tool, boolean willHarvest, FluidState fluid)
    {
        final int prevLayers = state.getValue(LAYERS);
        if (prevLayers > 1 && !player.isCreative())
        {
            return level.setBlock(pos, state.setValue(LAYERS, prevLayers - 1), level.isClientSide() ? 11 : 3);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, tool, willHarvest, fluid);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player)
    {
        return new ItemStack(Items.CHARCOAL);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type)
    {
        return type == PathComputationType.LAND && state.getValue(LAYERS) < 5;
    }

    @Override
    protected BlockState updateShape(BlockState stateIn, LevelReader level, ScheduledTickAccess tickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random)
    {
        // Merging two piles mutates both positions. The new read-only shape callback
        // must defer this to the server tick instead of writing through LevelReader.
        if (facing == Direction.DOWN && Helpers.isBlock(facingState, this) && facingState.getValue(LAYERS) < 8)
        {
            tickAccess.scheduleTick(currentPos, this, 1);
            return stateIn;
        }
        return canSurvive(stateIn, level, currentPos) ? stateIn : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        final BlockPos below = pos.below();
        final BlockState belowState = level.getBlockState(below);
        if (belowState.is(this) && belowState.getValue(LAYERS) < 8)
        {
            final int original = state.getValue(LAYERS);
            final int space = 8 - belowState.getValue(LAYERS);
            final int amount = Math.min(space, original);
            level.setBlock(below, belowState.setValue(LAYERS, belowState.getValue(LAYERS) + amount), Block.UPDATE_ALL);
            if (amount == original)
            {
                // Prevent a second charcoal drop, as in the original shape callback.
                level.destroyBlock(pos, false);
            }
            else
            {
                level.setBlock(pos, state.setValue(LAYERS, original - amount), Block.UPDATE_ALL);
            }
        }
        else if (!state.canSurvive(level, pos))
        {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state)
    {
        return true;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos)
    {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        BlockState blockstate = level.getBlockState(pos.below());
        return Block.isFaceFull(blockstate.getCollisionShape(level, pos.below()), Direction.UP) || (blockstate.getBlock() == this && blockstate.getValue(LAYERS) == 8);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPE_BY_LAYER[state.getValue(LAYERS) - 1];
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context)
    {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder.add(LAYERS));
    }
}
