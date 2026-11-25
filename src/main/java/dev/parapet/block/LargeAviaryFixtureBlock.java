package dev.parapet.block;

import dev.parapet.block.ParrotCageBlock.Part;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import static dev.parapet.block.ParrotCageBlock.FACING;
import static dev.parapet.block.ParrotCageBlock.PART;
import static dev.parapet.block.ParrotBathBlock.FILLED;

public final class LargeAviaryFixtureBlock extends Block {
    public enum Kind { STAND, BATH }
    private static final int QUIET = UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE | UPDATE_SUPPRESS_DROPS;
    private final Kind kind;
    private final List<Part> parts;
    private final Map<Part, Map<Direction, VoxelShape>> shapes = new HashMap<>();

    public LargeAviaryFixtureBlock(Kind kind, Properties properties) {
        super(properties.pushReaction(PushReaction.IMMOVEABLE).isSuffocating((state, level, pos) -> false));
        this.kind = kind;
        parts = Arrays.stream(Part.values()).filter(p -> kind == Kind.BATH ? p.y == 0 : p.z == 0 && p.y < 2).toList();
        registerDefaultState(defaultBlockState().setValue(PART, Part.BASE).setValue(FACING, Direction.NORTH).setValue(FILLED, false));
        for (Part part : parts) shapes.put(part, Shapes.rotateHorizontal(shape(part)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING, FILLED);
    }

    private VoxelShape shape(Part p) {
        if (kind == Kind.STAND) {
            VoxelShape shape = p.y == 0 ? box(0, 0, 1, 16, 3, 15) : box(0, 9.8, 6.4, 16, 12, 9.6);
            if (p.x != 1) {
                double x = p.x == 0 ? 8.2 : 2.5;
                shape = Shapes.or(shape, box(x, p.y == 0 ? 3 : 0, 6.55, x + 3.3, p.y == 0 ? 16 : 10.8, 9.65));
            }
            return shape;
        }
        VoxelShape shape = box(0, 0, 0, 16, 3, 16);
        if (p.x == 0) shape = Shapes.or(shape, box(.5, 3, 0, 3.91, 7, 16));
        if (p.x == 2) shape = Shapes.or(shape, box(12.09, 3, 0, 15.5, 7, 16));
        if (p.z == 0) shape = Shapes.or(shape, box(0, 3, .5, 16, 7, 3.91));
        if (p.z == 2) shape = Shapes.or(shape, box(0, 3, 12.09, 16, 7, 15.5));
        return shape;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var rotated = shapes.get(state.getValue(PART));
        return rotated == null ? Shapes.empty() : rotated.get(state.getValue(FACING));
    }

    public boolean isLandingPart(BlockState state) {
        return kind == Kind.STAND ? state.getValue(PART) == Part.UPPER_RIGHT
            : state.getValue(PART) == Part.BACK_RIGHT && state.getValue(FILLED);
    }

    public boolean bath() { return kind == Kind.BATH; }

    public double landingHeight(float birdWidth) {
        if (kind == Kind.STAND) return 0.75;
        return birdWidth < 2.5F ? 3.0 / 16 : 7.0 / 16;
    }

    private static BlockPos cell(BlockPos root, Direction facing, Part part) {
        return root.relative(facing.getClockWise(), part.x).relative(facing.getOpposite(), part.z).above(part.y);
    }

    private static BlockPos root(BlockPos pos, BlockState state) {
        Part part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        return pos.relative(facing.getCounterClockWise(), part.x).relative(facing, part.z).below(part.y);
    }

    private boolean loaded(Level level, BlockPos root, Direction facing) {
        BlockPos end = cell(root, facing, parts.getLast());
        return level.hasChunksAt(BlockPos.min(root, end), BlockPos.max(root, end));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) return state;
        Part old = state.getValue(PART);
        Part reflected = parts.stream().filter(p -> p.x == 2 - old.x && p.y == old.y && p.z == old.z).findFirst().orElse(old);
        return state.rotate(mirror.getRotation(state.getValue(FACING))).setValue(PART, reflected);
    }

    private boolean place(BlockPlaceContext context, BlockState base) {
        Level level = context.getLevel();
        BlockPos root = context.getClickedPos();
        Direction facing = base.getValue(FACING);
        Player player = context.getPlayer();
        var overrides = context.getItemInHand().get(DataComponents.BLOCK_STATE);
        if (!loaded(level, root, facing) || overrides != null && overrides.apply(base) != base) return false;
        BlockState[] replaced = new BlockState[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);
            BlockPos pos = cell(root, facing, part);
            BlockState old = level.getBlockState(pos);
            if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos) || old.hasBlockEntity()
                || !old.canBeReplaced(BlockPlaceContext.at(context, pos, context.getClickedFace()))
                || player != null && (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()))
                || !level.isUnobstructed(base.setValue(PART, part), pos, CollisionContext.placementContext(player))) return false;
            replaced[i] = old;
        }
        for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);
            if (!level.setBlock(cell(root, facing, part), base.setValue(PART, part), QUIET)) {
                for (int j = i - 1; j >= 0; j--) level.setBlock(cell(root, facing, parts.get(j)), replaced[j], QUIET);
                return false;
            }
        }
        for (Part part : parts) {
            BlockPos pos = cell(root, facing, part);
            level.updateNeighborsAt(pos, this);
            level.getBlockState(pos).updateNeighbourShapes(level, pos, UPDATE_ALL);
        }
        return true;
    }

    private boolean matches(BlockState state, Direction facing, Part part) {
        return state.is(this) && state.getValue(FACING) == facing && state.getValue(PART) == part;
    }

    private void clear(ServerLevel level, BlockPos root, Direction facing) {
        for (Part part : parts) {
            BlockPos where = cell(root, facing, part);
            if (level.hasChunkAt(where) && matches(level.getBlockState(where), facing, part)) {
                level.setBlock(where, Blocks.AIR.defaultBlockState(), QUIET);
                level.updateNeighborsAt(where, this);
                Blocks.AIR.defaultBlockState().updateNeighbourShapes(level, where, UPDATE_ALL);
            }
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
        clear(level, root(pos, state), state.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbor, BlockState neighborState, RandomSource random) {
        if (!neighborState.is(this)) ticks.scheduleTick(pos, this, 1);
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos root = root(pos, state);
        Direction facing = state.getValue(FACING);
        if (loaded(level, root, facing) && parts.stream().anyMatch(p -> !matches(level.getBlockState(cell(root, facing, p)), facing, p))) {
            clear(level, root, facing);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        if (!bath() || !(stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET))) return InteractionResult.PASS;
        boolean filling = stack.is(Items.WATER_BUCKET);
        Direction facing = state.getValue(FACING);
        BlockPos root = root(pos, state);
        if (filling != state.getValue(FILLED) && !level.isClientSide() && loaded(level, root, facing)
            && parts.stream().allMatch(p -> matches(level.getBlockState(cell(root, facing, p)), facing, p))) {
            for (Part part : parts) level.setBlock(cell(root, facing, part), state.setValue(PART, part).setValue(FILLED, filling), UPDATE_CLIENTS);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(filling ? Items.BUCKET : Items.WATER_BUCKET)));
            level.playSound(null, pos, filling ? SoundEvents.BUCKET_EMPTY : SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.8F, 1.0F);
            level.gameEvent(player, filling ? GameEvent.FLUID_PLACE : GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.SUCCESS;
    }

    public static final class PlacementItem extends BlockItem {
        public PlacementItem(LargeAviaryFixtureBlock block, net.minecraft.world.item.Item.Properties properties) { super(block, properties); }
        @Override
        protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
            return ((LargeAviaryFixtureBlock) getBlock()).place(context, state);
        }
    }
}
