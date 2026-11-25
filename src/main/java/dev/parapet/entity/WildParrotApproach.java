package dev.parapet.entity;

import dev.parapet.ParapetFurniture;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** A wild bird watches nearby players, approaches a lure, or takes flight when rushed. */
final class WildParrotApproach extends Goal {
    enum Lure {
        NONE(0, 0, 1), SPECIES_ITEM(6, 1, 3), FEED(10, 2, 3), PREMIUM(14, 9, 10);

        final int range;
        final int successes;
        final int attempts;

        Lure(int range, int successes, int attempts) {
            this.range = range;
            this.successes = successes;
            this.attempts = attempts;
        }

        static Lure of(PetParrotEntity bird, ItemStack stack) {
            if (ParapetFurniture.isPremiumFeed(stack)) {
                return PREMIUM;
            }
            if (ParapetFurniture.isBirdFeed(stack)) {
                return FEED;
            }
            return !stack.isEmpty() && ParrotEcology.forBird(bird).acceptsTamingItem(stack) ? SPECIES_ITEM : NONE;
        }
    }

    private static final double TRUST_DISTANCE = 3.25;
    private static final int PATIENCE_TICKS = 40;
    private static final float[] FLIGHT_ANGLES = {0F, 0.62F, -0.62F, 1.05F, -1.05F};
    private final PetParrotEntity bird;
    private Player visitor;
    private boolean visitorStill;
    private Vec3 threat;
    private Map<UUID, Vec3> lastPlayerPositions = Map.of();
    private int lastObservation;
    private boolean justStartled;
    private int nextScan;
    private int frightenedUntil;
    private int flightUntil;
    private int patience;

    WildParrotApproach(PetParrotEntity bird) {
        this.bird = bird;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    boolean frightened() {
        return bird.tickCount < frightenedUntil;
    }

    boolean acceptsFrom(Player player) {
        return !frightened() && visitor == player && patience >= PATIENCE_TICKS
            && visitorStill && bird.distanceToSqr(player) <= TRUST_DISTANCE * TRUST_DISTANCE && standingStill(player)
            && bird.hasLineOfSight(player);
    }

    @Override
    public boolean canUse() {
        if (bird.isTame() || bird.getCageRoost().isAssigned() || !(bird.level() instanceof ServerLevel level)) {
            return false;
        }
        if (bird.tickCount < flightUntil) {
            return true;
        }
        if (frightened() || bird.tickCount < nextScan) {
            return false;
        }
        scan(level);
        return visitor != null || bird.tickCount < flightUntil;
    }

    @Override
    public boolean canContinueToUse() {
        return !bird.isTame() && !bird.getCageRoost().isAssigned()
            && (bird.tickCount < flightUntil || !frightened() && visitor != null);
    }

    private void scan(ServerLevel level) {
        nextScan = bird.tickCount + 5;
        Player nearest = null;
        boolean nearestStill = false;
        double nearestDistance = Double.MAX_VALUE;
        List<Player> nearby = level.getEntitiesOfClass(Player.class, bird.getBoundingBox().inflate(Lure.PREMIUM.range),
            candidate -> candidate.isAlive() && !candidate.isSpectator());
        Map<UUID, Vec3> previous = lastPlayerPositions;
        int elapsed = bird.tickCount - lastObservation;
        lastPlayerPositions = nearby.isEmpty() ? Map.of() : new HashMap<>();
        lastObservation = bird.tickCount;
        for (Player player : nearby) {
            Vec3 position = player.position();
            Vec3 prior = previous.get(player.getUUID());
            lastPlayerPositions.put(player.getUUID(), position);
            Vec3 motion = player.getKnownMovement();
            if (prior != null && elapsed > 0 && elapsed <= 10) {
                Vec3 measured = position.subtract(prior).scale(1.0 / elapsed);
                if (measured.lengthSqr() > motion.lengthSqr()) {
                    motion = measured;
                }
            }
            double distance = bird.distanceToSqr(player);
            boolean mightStartle = distance < 36.0 && rushesTowardBird(player, motion, distance);
            int range = Math.max(Lure.of(bird, player.getMainHandItem()).range, Lure.of(bird, player.getOffhandItem()).range);
            if ((!mightStartle && (range == 0 || distance > range * range)) || !bird.hasLineOfSight(player)) {
                continue;
            }
            if (mightStartle) {
                threat = player.position();
                frightenedUntil = bird.tickCount + 100;
                flightUntil = bird.tickCount + 48;
                visitor = null;
                patience = 0;
                justStartled = true;
                return;
            }
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
                nearestStill = !player.isSprinting() && motion.horizontalDistanceSqr() < 0.035 * 0.035;
            }
        }
        visitorStill = nearestStill;
        if (nearest != visitor) {
            visitor = nearest;
            patience = 0;
        }
    }

    private boolean rushesTowardBird(Player player, Vec3 motion, double distanceSquared) {
        if (player.isShiftKeyDown() || player.isCrouching()) {
            return false;
        }
        Vec3 toward = bird.position().subtract(player.position());
        double speed = motion.length();
        // A quiet player should never frighten a bird that flies up to them.
        if (speed < 0.09 || toward.lengthSqr() < 0.0001 || motion.dot(toward.normalize()) < 0.065) {
            return false;
        }
        return player.isSprinting() || motion.horizontalDistance() >= 0.24 || distanceSquared < 2.1 * 2.1;
    }

    private static boolean standingStill(Player player) {
        return !player.isSprinting() && player.getKnownMovement().horizontalDistanceSqr() < 0.035 * 0.035;
    }

    private Vec3 awayFromThreat() {
        Vec3 away = bird.position().subtract(threat).horizontal();
        return away.lengthSqr() < 0.001 ? Vec3.directionFromRotation(0, bird.getYRot()) : away.normalize();
    }

    private void launch() {
        justStartled = false;
        bird.setActivity(PetParrotActivity.IDLE);
        bird.getNavigation().stop();
        Vec3 away = awayFromThreat();
        double lift = bird.level().noCollision(bird, bird.getBoundingBox().move(0, 0.36, 0)) ? 0.36 : 0.06;
        bird.addDeltaMovement(away.scale(0.32).add(0, lift, 0));
        bird.playSound(SoundEvents.PARROT_FLY, 0.7F, 1.2F);
        chooseEscapePath(away);
    }

    private void chooseEscapePath(Vec3 away) {
        for (float angle : FLIGHT_ANGLES) {
            Vec3 destination = bird.position().add(away.yRot(angle).scale(8)).add(0, 2.3, 0);
            BlockPos end = BlockPos.containing(destination);
            if (!bird.level().hasChunkAt(end)
                || !bird.level().noCollision(bird, bird.getBoundingBox().move(destination.subtract(bird.position())))) {
                continue;
            }
            var path = bird.getNavigation().createPath(end, 0);
            if (path != null && path.canReach() && bird.getNavigation().moveTo(path, 1.45)) {
                return;
            }
        }
        Vec3 nearby = bird.position().add(away.scale(2)).add(0, 1, 0);
        bird.getMoveControl().setWantedPosition(nearby.x, nearby.y, nearby.z, 1.35);
    }

    @Override
    public void start() {
        bird.setActivity(PetParrotActivity.IDLE);
        if (justStartled) {
            launch();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (frightened()) {
            if (bird.tickCount < flightUntil && bird.getNavigation().isDone() && bird.tickCount % 8 == 0) {
                chooseEscapePath(awayFromThreat());
            }
            return;
        }
        if (!(bird.level() instanceof ServerLevel level)) {
            return;
        }
        if (bird.tickCount >= nextScan) {
            scan(level);
            if (justStartled) {
                launch();
            }
        }
        if (visitor == null) {
            return;
        }
        bird.getLookControl().setLookAt(visitor, 40.0F, 35.0F);
        if (bird.distanceToSqr(visitor) > TRUST_DISTANCE * TRUST_DISTANCE) {
            patience = 0;
            if (bird.getNavigation().isDone() || bird.tickCount % 10 == 0) {
                bird.getNavigation().moveTo(visitor, 0.95);
            }
            return;
        }
        bird.getNavigation().stop();
        if (!visitorStill || !standingStill(visitor)) {
            patience = 0;
        } else if (patience < PATIENCE_TICKS && ++patience == PATIENCE_TICKS) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, bird.getX(), bird.getEyeY() + 0.12, bird.getZ(),
                4, 0.16, 0.12, 0.16, 0.01);
            bird.playSound(SoundEvents.PARROT_AMBIENT, 0.45F, 1.25F);
        }
    }

    @Override
    public void stop() {
        visitor = null;
        patience = 0;
        bird.getNavigation().stop();
    }
}
