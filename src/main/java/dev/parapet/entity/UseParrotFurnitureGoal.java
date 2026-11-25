package dev.parapet.entity;

import dev.parapet.block.LargeAviaryFixtureBlock;
import dev.parapet.block.ParrotBathBlock;
import dev.parapet.block.ParrotPlayStandBlock;
import dev.parapet.block.ParrotToyBlock;
import java.util.EnumMap;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** A free-roaming pet picks a reachable perch, pool or plaything and lands before using it. */
final class UseParrotFurnitureGoal extends Goal {
    private enum Use {
        PERCH, BATHE, BALL, SWING, CHEW;
        PetParrotActivity activity() {
            return switch (this) {
                case BATHE -> PetParrotActivity.BATHE;
                case BALL, CHEW -> PetParrotActivity.FORAGE;
                default -> PetParrotActivity.IDLE;
            };
        }
    }
    private record Site(BlockPos pos, Use use, Vec3 feet, boolean preferred) {}
    private final PetParrotEntity bird;
    private Site target;
    private boolean settled;
    private int until;
    private int nextSearch;

    UseParrotFurnitureGoal(PetParrotEntity bird) {
        this.bird = bird;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private Site site(BlockPos pos, BlockState state, boolean small) {
        Use use;
        double height;
        boolean preferred = false;
        if (state.getBlock() instanceof LargeAviaryFixtureBlock fixture && fixture.isLandingPart(state)) {
            use = fixture.bath() ? Use.BATHE : Use.PERCH;
            height = fixture.landingHeight(bird.getBbWidth());
            preferred = !small;
        } else if (state.getBlock() instanceof ParrotBathBlock && state.getValue(ParrotBathBlock.FILLED)) {
            use = Use.BATHE;
            height = ParrotBathBlock.footHeight(bird.getBbWidth());
            preferred = small;
        } else if (state.getBlock() instanceof ParrotPlayStandBlock) {
            use = Use.PERCH;
            height = 1;
            preferred = small;
        } else if (state.getBlock() instanceof ParrotToyBlock toy) {
            use = switch (toy.kind()) { case SWING -> Use.SWING; case BALL -> Use.BALL; case CHEW -> Use.CHEW; };
            if (use != Use.SWING || !small) {
                Vec3 center = Vec3.atBottomCenterOf(pos);
                Vec3 near = bird.position().subtract(center).multiply(1.0, 0.0, 1.0);
                if (near.lengthSqr() < 0.01) near = new Vec3(0, 0, -1);
                Vec3 feet = center.add(near.normalize().scale(0.64 + bird.getBbWidth() / 2.0));
                BlockPos floor = BlockPos.containing(feet).below();
                if (!bird.level().getBlockState(floor).isFaceSturdy(bird.level(), floor, Direction.UP)) return null;
                return new Site(pos.immutable(), use, feet, false);
            }
            height = 5.75 / 16;
        } else {
            return null;
        }
        return new Site(pos.immutable(), use, Vec3.atBottomCenterOf(pos).add(0, height, 0), preferred);
    }

    @Override
    public boolean canUse() {
        if (bird.tickCount < nextSearch || bird.getActivity() != PetParrotActivity.IDLE || !bird.canUseFurniture()) return false;
        nextSearch = bird.tickCount + 100 + bird.getRandom().nextInt(100);
        boolean small = ParrotEcology.forBird(bird).size() == ParrotEcology.Size.SMALL;
        var choices = new EnumMap<Use, Site>(Use.class);
        for (BlockPos pos : BlockPos.withinManhattan(bird.blockPosition(), 6)) {
            if (!bird.level().hasChunkAt(pos)) continue;
            Site candidate = site(pos, bird.level().getBlockState(pos), small);
            if (candidate == null) continue;
            Site previous = choices.get(candidate.use());
            if (previous != null && (previous.preferred() || !candidate.preferred())) continue;
            if (bird.getOwner() != null && bird.getOwner().distanceToSqr(candidate.feet()) > 25.0 || !hasSpace(candidate.feet())) continue;
            choices.put(candidate.use(), candidate);
        }
        if (choices.isEmpty()) return false;
        target = choices.values().stream().skip(bird.getRandom().nextInt(choices.size())).findFirst().orElseThrow();
        return true;
    }

    private boolean hasSpace(Vec3 at) {
        AABB box = bird.getBoundingBox().move(at.subtract(bird.position())).move(0, 0.001, 0);
        return bird.level().noCollision(bird, box)
            && bird.level().getEntitiesOfClass(PetParrotEntity.class, box, other -> other != bird).isEmpty();
    }

    @Override
    public void start() {
        settled = false;
        until = bird.tickCount + 180;
        Vec3 feet = target.feet();
        bird.getNavigation().moveTo(feet.x, feet.y + 0.9, feet.z, 1.0);
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || bird.tickCount >= until || !bird.canUseFurniture() || !bird.level().hasChunkAt(target.pos())) return false;
        BlockState state = bird.level().getBlockState(target.pos());
        Site current = site(target.pos(), state, ParrotEcology.forBird(bird).size() == ParrotEcology.Size.SMALL);
        return current != null && current.use() == target.use()
            && bird.getActivity() == (settled ? target.use().activity() : PetParrotActivity.IDLE)
            && (!settled || bird.distanceToSqr(target.feet()) < 0.20);
    }

    @Override
    public boolean requiresUpdateEveryTick() { return true; }

    private void play(ServerLevel level) {
        BlockState state = level.getBlockState(target.pos());
        if (state.getBlock() instanceof ParrotToyBlock toy) {
            toy.play(level, target.pos(), state);
            bird.getLookControl().setLookAt(Vec3.atCenterOf(target.pos()));
            bird.playSound(switch (target.use()) {
                case BALL -> SoundEvents.GRASS_HIT;
                case CHEW -> SoundEvents.WOOD_HIT;
                default -> SoundEvents.BAMBOO_HIT;
            }, 0.3F, 1.2F + bird.getRandom().nextFloat() * 0.4F);
        }
    }

    @Override
    public void tick() {
        if (target == null) return;
        Vec3 landing = target.feet();
        if (settled) {
            if (bird.level() instanceof ServerLevel level) {
                if (target.use() == Use.BATHE && bird.tickCount % 7 == 0)
                    level.sendParticles(ParticleTypes.SPLASH, landing.x, landing.y + 0.12, landing.z, 7, 0.25, 0.07, 0.25, 0.025);
                if (bird.tickCount % 13 == 0) play(level);
            }
            return;
        }
        double dx = bird.getX() - landing.x, dz = bird.getZ() - landing.z;
        double horizontal = dx * dx + dz * dz;
        double height = bird.getY() - landing.y;
        if (horizontal < 0.065 && height >= -0.04 && height < 0.32) {
            if (!hasSpace(landing)) { target = null; return; }
            bird.getNavigation().stop();
            bird.getMoveControl().setWait();
            bird.setDeltaMovement(Vec3.ZERO);
            bird.setPos(landing);
            bird.setOnGround(true);
            settled = true;
            PetParrotActivity activity = target.use().activity();
            bird.setActivity(activity);
            if (target.use() == Use.BATHE) bird.playSound(SoundEvents.GENERIC_SPLASH, 0.45F, 1.35F);
            if (bird.level() instanceof ServerLevel level) play(level);
            until = bird.tickCount + (activity == PetParrotActivity.IDLE ? 180 + bird.getRandom().nextInt(100) : activity.durationTicks());
        } else if (horizontal < 2.25 && height > -2.0 && height < 2.3) {
            bird.getNavigation().stop();
            double approachY = horizontal > 0.065 ? landing.y + 0.9 : landing.y + 0.025;
            bird.getMoveControl().setWantedPosition(landing.x, approachY, landing.z, 0.65);
        } else if (bird.getNavigation().isDone()) {
            target = null;
        }
    }

    @Override
    public void stop() {
        bird.getNavigation().stop();
        if (settled && target != null && bird.getActivity() == target.use().activity()) bird.setActivity(PetParrotActivity.IDLE);
        nextSearch = bird.tickCount + (settled ? 320 : 100) + bird.getRandom().nextInt(160);
        target = null;
        settled = false;
    }
}
