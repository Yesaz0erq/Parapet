package dev.parapet.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ParrotWaterBowlBlock extends FillableParrotBasinBlock {
    private static final VoxelShape BOWL = box(3, 0, 3, 13, 4, 13);

    public ParrotWaterBowlBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BOWL;
    }
}
