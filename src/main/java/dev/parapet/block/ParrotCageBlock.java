package dev.parapet.block;

import dev.parapet.ParapetFurniture;
import dev.parapet.cage.CageDecorations;
import dev.parapet.cage.CageFittingsBlockEntity;
import dev.parapet.cage.CageBirdCargo;
import dev.parapet.cage.CageRoost;
import dev.parapet.entity.ParrotEcology;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ParrotCageBlock extends Block implements EntityBlock {
    public enum Part implements StringRepresentable {
        BASE("base", 0, 0, 0), RIGHT("right", 1, 0, 0), FAR_RIGHT("far_right", 2, 0, 0),
        UPPER("upper", 0, 1, 0), UPPER_RIGHT("upper_right", 1, 1, 0), UPPER_FAR_RIGHT("upper_far_right", 2, 1, 0),
        TOP("top", 0, 2, 0), TOP_RIGHT("top_right", 1, 2, 0), TOP_FAR_RIGHT("top_far_right", 2, 2, 0),
        BACK("back", 0, 0, 1), BACK_RIGHT("back_right", 1, 0, 1), BACK_FAR_RIGHT("back_far_right", 2, 0, 1),
        UPPER_BACK("upper_back", 0, 1, 1), UPPER_BACK_RIGHT("upper_back_right", 1, 1, 1),
        UPPER_BACK_FAR_RIGHT("upper_back_far_right", 2, 1, 1), TOP_BACK("top_back", 0, 2, 1),
        TOP_BACK_RIGHT("top_back_right", 1, 2, 1), TOP_BACK_FAR_RIGHT("top_back_far_right", 2, 2, 1),
        REAR("rear", 0, 0, 2), REAR_RIGHT("rear_right", 1, 0, 2), REAR_FAR_RIGHT("rear_far_right", 2, 0, 2),
        UPPER_REAR("upper_rear", 0, 1, 2), UPPER_REAR_RIGHT("upper_rear_right", 1, 1, 2),
        UPPER_REAR_FAR_RIGHT("upper_rear_far_right", 2, 1, 2), TOP_REAR("top_rear", 0, 2, 2),
        TOP_REAR_RIGHT("top_rear_right", 1, 2, 2), TOP_REAR_FAR_RIGHT("top_rear_far_right", 2, 2, 2);

        final String name;
        final int x;
        final int y;
        final int z;

        Part(String name, int x, int y, int z) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override public String getSerializedName() {
            return name;
        }
    }

    public enum Size {
        STANDARD(1, 1, 1, 3, ParrotEcology.Size.SMALL),
        LARGE(2, 1, 2, 8, ParrotEcology.Size.MEDIUM),
        DELUXE(3, 2, 3, 12, ParrotEcology.Size.LARGE),
        SUPER_DELUXE(3, 3, 3, 12, ParrotEcology.Size.LARGE),
        GIANT(3, 3, 3, 12, ParrotEcology.Size.LARGE);

        public final int width;
        public final int depth;
        public final int height;
        public final double perchHeight;
        public final ParrotEcology.Size maxBirdSize;
        final List<Part> parts;

        Size(int width, int depth, int height, int perchPixels, ParrotEcology.Size maxBirdSize) {
            this.width = width;
            this.depth = depth;
            this.height = height;
            this.perchHeight = perchPixels / 16.0;
            this.maxBirdSize = maxBirdSize;
            this.parts = Arrays.stream(Part.values()).filter(part ->
                part.x < width && part.z < depth && part.y < height).toList();
        }
    }

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty GARLAND = BooleanProperty.create("garland");
    public static final BooleanProperty BELL = BooleanProperty.create("bell");
    private static final int QUIET = UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE | UPDATE_SUPPRESS_DROPS;

    private static final Map<Direction, VoxelShape> DOORS = Shapes.rotateHorizontal(box(0, 0, 0, 16, 16, 1));

    private record Geometry(Part part, boolean open, Direction facing) {}
    private final Map<Geometry, VoxelShape> walls = new HashMap<>();
    private final Size size;

    public ParrotCageBlock(Size size, BlockBehaviour.Properties properties) {
        super(properties.noOcclusion().pushReaction(PushReaction.IMMOVEABLE)
            .isSuffocating((state, level, pos) -> false));
        this.size = size;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(PART, Part.BASE)
            .setValue(OPEN, false).setValue(GARLAND, false).setValue(BELL, false));
        for (Part part : size.parts) {
            for (boolean open : new boolean[]{false, true}) {
                Shapes.rotateHorizontal(northShape(part, open)).forEach((facing, shape) ->
                    walls.put(new Geometry(part, open, facing), shape));
            }
        }
    }

    @Override
    public CageFittingsBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CageFittingsBlockEntity(pos, state);
    }

    public Size size() {
        return size;
    }

    private VoxelShape northShape(Part part, boolean open) {
        VoxelShape shell = part.z == size.depth - 1 ? box(0, 0, 15, 16, 16, 16) : Shapes.empty();
        if (part.z == 0 && !open) shell = Shapes.or(shell, box(0, 0, 0, 16, 16, 1));
        if (part.x == 0) shell = Shapes.or(shell, box(0, 0, 0, 1, 16, 16));
        if (part.x == size.width - 1) shell = Shapes.or(shell, box(15, 0, 0, 16, 16, 16));
        if (part.y == size.height - 1) shell = Shapes.or(shell, box(0, 15, 0, 16, 16, 16));
        if (part.y == 0) {
            double top = size.perchHeight * 16;
            double midZ = size.depth * 8 - part.z * 16;
            shell = Shapes.or(shell, box(0, 0, 0, 16, 1, 16));
            if (midZ + 1 > 0 && midZ - 1 < 16) {
                shell = Shapes.or(shell, box(0, top - 1, Math.max(0, midZ - 1), 16, top, Math.min(16, midZ + 1)));
            }
        }
        return shell.optimize();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, OPEN, GARLAND, BELL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return walls.getOrDefault(new Geometry(state.getValue(PART), state.getValue(OPEN), state.getValue(FACING)), Shapes.empty());
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        if (mirror == Mirror.NONE) return state;
        state = state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
        Part part = state.getValue(PART);
        for (Part reflected : size.parts) {
            if (reflected.x == size.width - 1 - part.x && reflected.y == part.y && reflected.z == part.z) {
                return state.setValue(PART, reflected);
            }
        }
        return state;
    }

    private BlockPos cell(BlockPos anchor, Direction facing, Part part) {
        return anchor.relative(facing.getClockWise(), part.x).relative(facing.getOpposite(), part.z).above(part.y);
    }

    private BlockPos anchor(BlockPos pos, BlockState state) {
        Part part = state.getValue(PART);
        Direction facing = state.getValue(FACING);
        return pos.relative(facing.getCounterClockWise(), part.x).relative(facing, part.z).below(part.y);
    }

    public Vec3 perch(BlockPos anchor, Direction facing) {
        return Vec3.atBottomCenterOf(anchor).relative(facing.getClockWise(), 0.64 * size.width - 0.5)
            .relative(facing.getOpposite(), size.depth * 0.5 - 0.5).add(0, size.perchHeight, 0);
    }

    public boolean loaded(Level level, BlockPos anchor, Direction facing) {
        BlockPos far = anchor.relative(facing.getClockWise(), size.width - 1).relative(facing.getOpposite(), size.depth - 1);
        return level.hasChunksAt(BlockPos.min(anchor, far), BlockPos.max(anchor, far));
    }

    public boolean complete(Level level, BlockPos anchor, Direction facing) {
        for (Part part : size.parts) {
            BlockState cell = level.getBlockState(cell(anchor, facing, part));
            if (!cell.is(this) || cell.getValue(PART) != part || cell.getValue(FACING) != facing) return false;
        }
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        List<ItemStack> items = CageDecorations.contents(context.getItemInHand());
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(GARLAND, items.stream().anyMatch(stack -> stack.is(ParapetFurniture.CAGE_GARLAND)))
            .setValue(BELL, items.stream().anyMatch(stack -> stack.is(ParapetFurniture.HANGING_BELL)));
    }

    public boolean placeCage(BlockPlaceContext context, BlockState base) {
        Level level = context.getLevel();
        Direction facing = base.getValue(FACING);
        Player player = context.getPlayer();
        var overrides = context.getItemInHand().get(DataComponents.BLOCK_STATE);
        if (overrides != null && overrides.apply(base) != base) return false;

        List<BlockPos> cells = size.parts.stream().map(p -> cell(context.getClickedPos(), facing, p)).toList();
        if (!loaded(level, context.getClickedPos(), facing)) return false;
        BlockPos far = cells.getLast();
        if (!level.isUnobstructed(null, Shapes.create(AABB.encapsulatingFullBlocks(context.getClickedPos(), far)))) return false;
        BlockState[] originals = new BlockState[cells.size()];
        for (int i = 0; i < cells.size(); i++) {
            BlockPos pos = cells.get(i);
            if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)) return false;
            if (player != null && (!level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()))) return false;
            originals[i] = level.getBlockState(pos);
            if (originals[i].hasBlockEntity() || !originals[i].canBeReplaced(BlockPlaceContext.at(context, pos, context.getClickedFace()))
                || !level.isUnobstructed(base.setValue(PART, size.parts.get(i)), pos, CollisionContext.placementContext(player))) return false;
        }
        for (int i = 0; i < cells.size(); i++) {
            if (!level.setBlock(cells.get(i), base.setValue(PART, size.parts.get(i)), QUIET)) {
                for (int j = i - 1; j >= 0; j--) level.setBlock(cells.get(j), originals[j], QUIET);
                return false;
            }
        }
        if (CageBirdCargo.isOccupied(context.getItemInHand()) && level instanceof ServerLevel server
            && !CageBirdCargo.restore(server, context.getItemInHand(), player, this, context.getClickedPos(), facing)) {
            for (int i = cells.size() - 1; i >= 0; i--) level.setBlock(cells.get(i), originals[i], QUIET);
            if (player != null) player.sendOverlayMessage(Component.translatable("message.parapet.cage.cargo_unavailable"));
            return false;
        }
        for (BlockPos pos : cells) {
            level.updateNeighborsAt(pos, this);
            level.getBlockState(pos).updateNeighbourShapes(level, pos, UPDATE_ALL);
        }
        return true;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        BlockPos root = anchor(pos, state);
        Direction facing = state.getValue(FACING);
        clearStructure(level, root, facing);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        // Commands may replace a member without calling affectNeighborsAfterRemoval.
        if (!neighborState.is(this)) {
            Part part = state.getValue(PART);
            Direction facing = state.getValue(FACING);
            int x = part.x + (direction == facing.getClockWise() ? 1 : direction == facing.getCounterClockWise() ? -1 : 0);
            int z = part.z + (direction == facing.getOpposite() ? 1 : direction == facing ? -1 : 0);
            int y = part.y + direction.getStepY();
            if (x >= 0 && x < size.width && z >= 0 && z < size.depth && y >= 0 && y < size.height) {
                ticks.scheduleTick(pos, this, 1);
            }
        }
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos root = anchor(pos, state);
        Direction facing = state.getValue(FACING);
        if (loaded(level, root, facing) && !complete(level, root, facing)) clearStructure(level, root, facing);
    }

    private void clearStructure(ServerLevel level, BlockPos root, Direction facing) {
        // Only the original destruction produces loot. Directly clearing siblings never enters their loot path.
        for (Part part : size.parts) {
            BlockPos other = cell(root, facing, part);
            BlockState sibling = level.getBlockState(other);
            if (sibling.is(this) && sibling.getValue(PART) == part && sibling.getValue(FACING) == facing) {
                level.setBlock(other, Blocks.AIR.defaultBlockState(), QUIET);
                level.updateNeighborsAt(other, this);
                Blocks.AIR.defaultBlockState().updateNeighbourShapes(level, other, UPDATE_ALL);
            }
        }
        CageRoost.onBroken(level, root);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos root = anchor(pos, state);
        if (level instanceof ServerLevel server && server.getGameRules().get(GameRules.BLOCK_DROPS)
            && complete(level, root, state.getValue(FACING))) {
            BlockEntity base = server.getBlockEntity(root);
            if (player.preventsBlockDrops()) {
                // Like a chest, creative mining spills the inventory even though the shell does not drop.
                for (ItemStack item : contents(state, base)) popResource(server, pos, item);
            } else if (player.hasCorrectToolForDrops(state)) {
                if (!root.equals(pos) && server.getBlockEntity(pos) instanceof CageFittingsBlockEntity member
                    && base instanceof CageFittingsBlockEntity storage) member.captureForDrops(storage);
                CageRoost.markForPacking(server, player, root, pos);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        var resident = CageRoost.takePackingCandidate(level, player, pos);
        if (resident != null && level.getGameRules().get(GameRules.BLOCK_DROPS)
            && CageBirdCargo.dropFromCage(level, pos, CageDecorations.pack(new ItemStack(this), contents(state, blockEntity)), resident, player)) {
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
            return;
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    private List<ItemStack> contents(BlockState state, BlockEntity member) {
        var stored = member instanceof CageFittingsBlockEntity cage ? cage.fittings() : ItemContainerContents.EMPTY;
        return CageDecorations.contents(stored, state.getValue(GARLAND), state.getValue(BELL));
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        Float radius = params.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
        if (radius != null && params.getLevel().getRandom().nextFloat() > 1F / radius) return List.of();
        var member = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (member != null && !member.isRemoved()) {
            member = params.getLevel().getBlockEntity(anchor(member.getBlockPos(), state));
        }
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this));
        drops.addAll(contents(state, member));
        return drops;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return CageDecorations.pack(new ItemStack(this), contents(state, level.getBlockEntity(anchor(pos, state))));
    }

    private void change(Level level, BlockPos root, Direction facing, BooleanProperty property, boolean value) {
        for (Part part : size.parts) {
            BlockPos pos = cell(root, facing, part);
            BlockState current = level.getBlockState(pos);
            level.setBlock(pos, current.setValue(property, value), UPDATE_CLIENTS);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (!CageDecorations.isFitting(stack)) {
            return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        BlockPos root = anchor(pos, state);
        Direction facing = state.getValue(FACING);
        if (!player.mayBuild() || !level.mayInteract(player, pos) || !level.mayInteract(player, root)
            || !loaded(level, root, facing) || !complete(level, root, facing)
            || !(level.getBlockEntity(root) instanceof CageFittingsBlockEntity base)) return InteractionResult.FAIL;
        List<ItemStack> fittings = contents(state, base);
        if (fittings.stream().anyMatch(item -> item.is(stack.getItem()))) {
            player.sendOverlayMessage(Component.translatable("message.parapet.cage.already_installed", stack.getHoverName()));
            return InteractionResult.SUCCESS;
        }
        fittings.add(stack.copyWithCount(1));
        base.setFittings(ItemContainerContents.fromItems(fittings));
        boolean bell = stack.is(ParapetFurniture.HANGING_BELL);
        if (bell || stack.is(ParapetFurniture.CAGE_GARLAND)) change(level, root, facing, bell ? BELL : GARLAND, true);
        player.sendOverlayMessage(Component.translatable("message.parapet.cage.installed", stack.getHoverName()));
        stack.consume(1, player);
        server.playSound(null, root, bell ? SoundEvents.BELL_BLOCK : SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.45F, 1.25F);
        server.gameEvent(player, GameEvent.BLOCK_CHANGE, root);
        return InteractionResult.SUCCESS;
    }

    public InteractionResult transferBird(BlockState state, Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        BlockPos root = anchor(pos, state);
        Direction facing = state.getValue(FACING);
        if (!level.mayInteract(player, pos) || !level.mayInteract(player, root)
            || !loaded(level, root, facing) || !complete(level, root, facing)) return InteractionResult.FAIL;
        CageRoost.transfer(server, this, root, facing, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isSecondaryUseActive()) return transferBird(state, level, pos, player);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockPos root = anchor(pos, state);
        Direction facing = state.getValue(FACING);
        if (!level.mayInteract(player, root) || !loaded(level, root, facing) || !complete(level, root, facing)) return InteractionResult.FAIL;
        boolean open = state.getValue(OPEN);
        if (open) {
            VoxelShape door = DOORS.get(facing);
            for (Part part : size.parts) {
                if (part.z != 0) continue;
                BlockPos member = cell(root, facing, part);
                if (!level.isUnobstructed(null, door.move(member))) {
                    player.sendOverlayMessage(Component.translatable("message.parapet.cage.door_blocked"));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        change(level, root, facing, OPEN, !open);
        level.playSound(null, root, open ? SoundEvents.IRON_DOOR_CLOSE : SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.65F, 1.12F);
        level.gameEvent(player, open ? GameEvent.BLOCK_CLOSE : GameEvent.BLOCK_OPEN, root);
        return InteractionResult.SUCCESS;
    }
}
