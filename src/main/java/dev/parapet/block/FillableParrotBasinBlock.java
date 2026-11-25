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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

abstract class FillableParrotBasinBlock extends Block {
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    FillableParrotBasinBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FILLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILLED);
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
