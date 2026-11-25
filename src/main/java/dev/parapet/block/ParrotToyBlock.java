package dev.parapet.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ParrotToyBlock extends Block {
    public enum Kind { BALL, SWING, CHEW }
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private final Kind kind;
    public ParrotToyBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }
    public Kind kind() { return kind; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(ACTIVE); }

    public void play(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.getValue(ACTIVE)) {
            level.setBlock(pos, state.setValue(ACTIVE, true), UPDATE_CLIENTS);
            level.scheduleTick(pos, this, 6);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE)) level.setBlock(pos, state.setValue(ACTIVE, false), UPDATE_CLIENTS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (kind) {
            case BALL -> box(3, 0, 3, 13, 9.5, 13);
            case SWING -> Shapes.or(box(.5, 0, 3, 15.5, 1.5, 13), box(2.5, 4.25, 6.9, 13.25, 5.75, 8.42),
                box(1, 1.5, 7, 2.1, 15.3, 8.5), box(14, 1.5, 7, 15.1, 15.3, 8.5));
            case CHEW -> Shapes.or(box(2, 0, 2, 14, 1.3, 14), box(4.35, 2.1, 7.6, 8.4, 14.25, 11),
                box(10, 1.3, 8.8, 11.55, 15.7, 10.25));
        };
    }
}
