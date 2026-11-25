package dev.parapet.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BirdFeedPileBlock extends Block {
    public static final int CAPACITY = 4;
    public static final IntegerProperty PORTIONS = IntegerProperty.create("portions", 1, CAPACITY);
    private static final VoxelShape[] SHAPES = boxes(CAPACITY - 1, i ->
        box(4 - i, 0, 4 - i, 12 + i, 1 + i * 0.5, 12 + i));

    public BirdFeedPileBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PORTIONS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PORTIONS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(PORTIONS) - 1];
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos support = pos.below();
        return level.getBlockState(support).isFaceSturdy(level, support, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbor, BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, level, ticks, pos, direction, neighbor, neighborState, random);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().is(asItem()) && state.getValue(PORTIONS) < CAPACITY;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (existing.is(this)) {
            int portions = existing.getValue(PORTIONS);
            return portions < CAPACITY ? existing.setValue(PORTIONS, portions + 1) : null;
        }
        if (!context.getLevel().getFluidState(context.getClickedPos()).isEmpty()) return null;
        return defaultBlockState();
    }

    /** Both the availability check and the decrement use the live state on the server thread. */
    public boolean eatOne(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(this)) return false;
        int portions = state.getValue(PORTIONS);
        return portions == 1 ? level.removeBlock(pos, false)
            : level.setBlock(pos, state.setValue(PORTIONS, portions - 1), UPDATE_ALL);
    }
}
