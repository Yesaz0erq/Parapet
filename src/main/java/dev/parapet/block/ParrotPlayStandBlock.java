package dev.parapet.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ParrotPlayStandBlock extends Block {
    private static final VoxelShape STAND = Shapes.or(
        box(1, 0, 2, 15, 2, 14),
        box(7, 2, 7, 9, 15, 9),
        box(1, 14, 7, 15, 16, 9));

    public ParrotPlayStandBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return STAND;
    }
}
