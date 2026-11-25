package dev.parapet.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ParrotBathBlock extends Block {
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");
    private static final VoxelShape BASIN = Shapes.or(
        box(1, 0, 1, 15, 3, 15),
        box(1, 3, 1, 15, 6, 3), box(1, 3, 13, 15, 6, 15),
        box(1, 3, 3, 3, 6, 13), box(13, 3, 3, 15, 6, 13));

    public ParrotBathBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FILLED, false));
    }

    public static double footHeight(float birdWidth) {
        // A wider bird straddles the two sides of the bowl.
        return birdWidth < 10.0 / 16.0 ? 3.0 / 16.0 : 6.0 / 16.0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILLED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASIN;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        boolean filling = stack.is(Items.WATER_BUCKET);
        if (!filling && !stack.is(Items.BUCKET)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (state.getValue(FILLED) != filling && !level.isClientSide()
            && level.setBlockAndUpdate(pos, state.setValue(FILLED, filling))) {
            ItemStack exchanged = new ItemStack(filling ? Items.BUCKET : Items.WATER_BUCKET);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, exchanged));
            level.playSound(null, pos, filling ? SoundEvents.BUCKET_EMPTY : SoundEvents.BUCKET_FILL,
                SoundSource.BLOCKS, 0.8F, 1.0F);
            level.gameEvent(player, filling ? GameEvent.FLUID_PLACE : GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
