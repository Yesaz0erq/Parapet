package dev.parapet.entity;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Wild birds fly to a treetop and settle there under normal gravity until dawn or a disturbance. */
final class ParrotNightRoostGoal extends Goal {
    private static final int RADIUS = 10;
    private static final int MAX_COLUMNS = 36;
    private static final int MAX_CANDIDATES = 5;

    private final PetParrotEntity bird;
    private final Set<BlockPos> candidates = new LinkedHashSet<>();
    private BlockPos perch;
    private int nextSearch;
    private int flightDeadline;
    private int nextSafetyCheck;
    private int groundedTicks;
    private boolean resting;

    ParrotNightRoostGoal(PetParrotEntity bird) {
        this.bird = bird;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    static boolean isRoostTime(ServerLevel level) {
        if (!level.dimensionType().hasSkyLight() || level.dimensionType().hasFixedTime()) return false;
        long time = Math.floorMod(level.getDefaultClockTime(), 24_000L);
        return time >= 12_500 && time < 23_000;
    }

    private boolean undisturbed(ServerLevel level) {
        return isRoostTime(level) && !bird.isTame() && !bird.getCageRoost().isAssigned()
            && bird.isAlive() && bird.getHealth() > bird.getMaxHealth() * 0.5F
            && !bird.isStartled() && !bird.isLeashed() && !bird.isPassenger() && !bird.isInLove()
            && !bird.isOnFire() && !bird.isFreezing() && !bird.isInWater() && !bird.isInLava()
            && bird.hurtTime == 0 && (bird.getLastHurtByMob() == null
                || bird.tickCount - bird.getLastHurtByMobTimestamp() >= 100);
    }

    private boolean dangerNearby(ServerLevel level, AABB area) {
        return !level.getEntitiesOfClass(Mob.class, area.inflate(5.0, 3.0, 5.0),
            mob -> mob != bird && mob.isAlive() && (mob instanceof Enemy || mob.getTarget() == bird)).isEmpty();
    }

    @Override
    public boolean canUse() {
        if (bird.tickCount < nextSearch || bird.isTame() || !(bird.level() instanceof ServerLevel level)) return false;
        nextSearch = bird.tickCount + 100 + bird.getRandom().nextInt(100);
        if (bird.getActivity() != PetParrotActivity.IDLE || !undisturbed(level) || dangerNearby(level, bird.getBoundingBox())) return false;

        candidates.clear();
        perch = null;
        BlockPos origin = bird.blockPosition();
        if (bird.onGround() && usablePerch(level, origin.below())) {
            perch = origin.below();
            return true;
        }
        for (int i = 0; i < MAX_COLUMNS && candidates.size() < MAX_CANDIDATES; i++) {
            int dx = i == 0 ? 0 : bird.getRandom().nextInt(2 * RADIUS + 1) - RADIUS;
            int dz = i == 0 ? 0 : bird.getRandom().nextInt(2 * RADIUS + 1) - RADIUS;
            if (dx * dx + dz * dz > RADIUS * RADIUS) continue;
            int x = origin.getX() + dx;
            int z = origin.getZ() + dz;
            var chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
            if (chunk == null) continue;
            int top = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            if (top < origin.getY() - 4 || top > origin.getY() + 24) continue;
            BlockPos support = new BlockPos(x, top, z);
            if (usablePerch(level, support)) candidates.add(support.above());
        }
        return !candidates.isEmpty();
    }

    private static boolean woodOrLeaves(BlockState state) {
        return state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS);
    }

    private boolean usablePerch(ServerLevel level, BlockPos support) {
        if (support.getY() < level.getMinY() + 2 || !level.hasChunkAt(support)) return false;
        BlockState state = level.getBlockState(support);
        if (!woodOrLeaves(state) || !state.isCollisionShapeFullBlock(level, support) || !state.getFluidState().isEmpty()) return false;
        // A log lying on the ground is not an elevated branch.
        for (int depth = 1; depth <= 2; depth++) {
            BlockState below = level.getBlockState(support.below(depth));
            if (!woodOrLeaves(below) && !below.isAir()) return false;
        }
        Vec3 feet = Vec3.atBottomCenterOf(support.above());
        AABB body = bird.getBoundingBox().move(feet.subtract(bird.position())).move(0, 0.001, 0);
        AABB margins = body.inflate(1.0, 0.5, 1.0);
        if (body.maxY >= level.getMaxY() + 1 || !level.getWorldBorder().isWithinBounds(body)
            || !level.hasChunksAt(BlockPos.containing(margins.getMinPosition()), BlockPos.containing(margins.getMaxPosition()))
            || !level.noCollision(bird, body)) return false;
        for (BlockPos pos : BlockPos.betweenClosed(margins)) {
            BlockState nearby = level.getBlockState(pos);
            if (nearby.is(BlockTags.FIRE) || nearby.getFluidState().is(FluidTags.LAVA) || CampfireBlock.isLitCampfire(nearby)
                || nearby.is(Blocks.CACTUS) || nearby.is(Blocks.SWEET_BERRY_BUSH) || nearby.is(Blocks.WITHER_ROSE)
                || nearby.is(Blocks.MAGMA_BLOCK) || nearby.is(Blocks.POWDER_SNOW)) return false;
        }
        for (BlockPos pos : BlockPos.betweenClosed(body)) {
            if (!level.getFluidState(pos).isEmpty()) return false;
        }
        return level.getEntitiesOfClass(PetParrotEntity.class, body, other -> other != bird && other.isAlive()).isEmpty();
    }

    @Override
    public void start() {
        resting = false;
        groundedTicks = 0;
        flightDeadline = bird.tickCount + 200;
        nextSafetyCheck = bird.tickCount;
        if (perch != null) {
            bird.getNavigation().stop();
            return;
        }
        // Minecraft's pathfinding region reads currently loaded chunks with getChunkNow.
        var route = bird.getNavigation().createPath(candidates, 0);
        if (route != null && route.canReach() && candidates.contains(route.getTarget())
            && bird.getNavigation().moveTo(route, 1.05)) {
            perch = route.getTarget().below();
        }
        candidates.clear();
    }

    @Override
    public boolean canContinueToUse() {
        if (perch == null || !(bird.level() instanceof ServerLevel level) || !undisturbed(level)
            || bird.getActivity() != (resting ? PetParrotActivity.NIGHT_ROOST : PetParrotActivity.IDLE)) return false;
        if (resting && !onPerch() || !resting && bird.tickCount >= flightDeadline) return false;
        if (bird.tickCount >= nextSafetyCheck) {
            nextSafetyCheck = bird.tickCount + 20;
            return usablePerch(level, perch) && !dangerNearby(level, bird.getBoundingBox())
                && (resting || !dangerNearby(level, new AABB(perch.above())));
        }
        return true;
    }

    private boolean onPerch() {
        return bird.onGround() && bird.blockPosition().below().equals(perch)
            && Math.abs(bird.getY() - (perch.getY() + 1.0)) < 0.08;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (perch == null || resting) return;
        Vec3 feet = Vec3.atBottomCenterOf(perch.above());
        double dx = feet.x - bird.getX();
        double dz = feet.z - bird.getZ();
        double horizontal = dx * dx + dz * dz;
        double height = bird.getY() - feet.y;
        if (onPerch() && horizontal <= 0.24) {
            bird.getNavigation().stop();
            bird.getMoveControl().setWait();
            if (bird.getDeltaMovement().horizontalDistanceSqr() < 0.0025 && ++groundedTicks >= 6) {
                resting = true;
                bird.setActivity(PetParrotActivity.NIGHT_ROOST);
            }
            return;
        }
        groundedTicks = 0;
        if (horizontal < 2.25 && height >= -0.1 && height < 1.8) {
            bird.getNavigation().stop();
            if (horizontal <= 0.13) {
                bird.getMoveControl().setWait();
            } else {
                bird.getMoveControl().setWantedPosition(feet.x, feet.y + 0.65, feet.z, 0.58);
            }
        } else if (bird.getNavigation().isDone()) {
            perch = null;
        }
    }

    @Override
    public void stop() {
        bird.getNavigation().stop();
        bird.getMoveControl().setWait();
        if (resting && bird.getActivity() == PetParrotActivity.NIGHT_ROOST) bird.setActivity(PetParrotActivity.IDLE);
        resting = false;
        perch = null;
        candidates.clear();
        nextSearch = bird.tickCount + 100 + bird.getRandom().nextInt(100);
    }
}
