package dev.parapet.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ParrotBathBlock extends FillableParrotBasinBlock {
    private static final VoxelShape BASIN = Shapes.or(
        box(1, 0, 1, 15, 3, 15),
        box(1, 3, 1, 15, 6, 3), box(1, 3, 13, 15, 6, 15),
        box(1, 3, 3, 3, 6, 13), box(13, 3, 3, 15, 6, 13));

    public ParrotBathBlock(Properties properties) {
        super(properties);
    }

    public static double footHeight(float birdWidth) {
        // A wider bird straddles the two sides of the bowl.
        return birdWidth < 10.0 / 16.0 ? 3.0 / 16.0 : 6.0 / 16.0;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASIN;
    }
}
