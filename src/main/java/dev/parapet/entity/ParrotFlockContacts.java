package dev.parapet.entity;

import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

final class ParrotFlockContacts {
    private static final int RANGE = 12;
    private long nextCheck = -1;
    private long quietUntil;
    private long replyAt;
    private UUID pendingCaller;

    void tick(ServerLevel level, PetParrotEntity bird) {
        long now = level.getGameTime();
        if (pendingCaller != null) {
            if (!ready(bird)) {
                pendingCaller = null;
            } else if (now >= replyAt) {
                answer(level, bird, now);
            }
            return;
        }
        if (bird.isTame() || now < quietUntil || now < nextCheck) return;

        RandomSource random = bird.getRandom();
        boolean firstCheck = nextCheck < 0;
        nextCheck = now + 100 + random.nextInt(141);
        if (firstCheck || !inDaylight(level, bird)) return;

        long time = Math.floorMod(level.getDefaultClockTime(), 24_000L);
        boolean morning = time >= 23_000 || time < 2_500;
        if (random.nextInt(morning ? 3 : 10) != 0 || !ready(bird)) return;

        List<PetParrotEntity> flock = level.getEntitiesOfClass(PetParrotEntity.class,
            bird.getBoundingBox().inflate(RANGE), other -> other != bird && !other.isTame()
                && bird.distanceToSqr(other) <= RANGE * RANGE && sameFlockSpecies(bird, other));
        PetParrotEntity listener = null;
        int eligible = 0;
        for (PetParrotEntity other : flock) {
            if (now < other.flockContacts.quietUntil || other.flockContacts.pendingCaller != null
                    || !ready(other) || !inDaylight(level, other) || !bird.hasLineOfSight(other)) continue;
            if (random.nextInt(++eligible) == 0) listener = other;
        }
        if (listener == null) return;

        // A single nearby flock shares a pause, even if many birds were loaded together.
        long groupPause = now + (morning ? 400 : 700) + random.nextInt(201);
        for (PetParrotEntity other : flock) {
            other.flockContacts.quietUntil = Math.max(other.flockContacts.quietUntil, groupPause);
        }
        quietUntil = now + 1_200 + random.nextInt(601);
        listener.flockContacts.quietUntil = now + 1_200 + random.nextInt(601);
        listener.flockContacts.pendingCaller = bird.getUUID();
        listener.flockContacts.replyAt = now + 6 + random.nextInt(7);
        bird.getLookControl().setLookAt(listener, 40F, 30F);
        bird.setActivity(PetParrotActivity.CONTACT_CALL);
        bird.playSound(SoundEvents.PARROT_AMBIENT, 0.52F, 0.95F + random.nextFloat() * 0.16F);
    }

    private void answer(ServerLevel level, PetParrotEntity bird, long now) {
        UUID callerId = pendingCaller;
        pendingCaller = null;
        // A listener that unloaded or began another activity misses the call.
        if (now > replyAt + 4 || !inDaylight(level, bird)
                || !(level.getEntity(callerId) instanceof PetParrotEntity caller)
                || caller.getActivity() != PetParrotActivity.CONTACT_CALL || !healthyWildBird(caller)
                || bird.distanceToSqr(caller) > RANGE * RANGE || !bird.hasLineOfSight(caller)) return;
        bird.getLookControl().setLookAt(caller, 40F, 30F);
        bird.setActivity(PetParrotActivity.LISTEN);
        if (bird.getRandom().nextBoolean()) {
            bird.playSound(SoundEvents.PARROT_AMBIENT, 0.32F, 1.12F + bird.getRandom().nextFloat() * 0.12F);
        }
    }

    private static boolean healthyWildBird(PetParrotEntity bird) {
        return !bird.isTame() && bird.isAlive() && !bird.isNoAi() && !bird.isSilent()
            && bird.getHealth() > bird.getMaxHealth() * 0.5F && !bird.isFreezing()
            && !bird.isStartled() && bird.hurtTime == 0;
    }

    private static boolean ready(PetParrotEntity bird) {
        return healthyWildBird(bird) && bird.canStartNaturalDetail();
    }

    private static boolean inDaylight(ServerLevel level, PetParrotEntity bird) {
        return level.dimension().equals(Level.OVERWORLD) && !ParrotNightRoostGoal.isRoostTime(level)
            && level.isBrightOutside() && level.getEffectiveSkyBrightness(bird.blockPosition()) >= 6;
    }

    private static boolean sameFlockSpecies(PetParrotEntity bird, PetParrotEntity other) {
        if (!bird.isSameSpecies(other)) return false;
        return !(bird instanceof LovebirdEntity)
            || LovebirdPlumages.speciesOf(bird.getPlumageId()).equals(LovebirdPlumages.speciesOf(other.getPlumageId()));
    }
}
