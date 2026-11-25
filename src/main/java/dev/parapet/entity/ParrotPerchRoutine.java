package dev.parapet.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;

/** Feather care and nighttime rest outside an aviary. */
final class ParrotPerchRoutine {
    private int wetSamples;
    private int shakeUntil;

    void tick(ServerLevel level, PetParrotEntity bird, boolean freeToRest) {
        PetParrotActivity activity = bird.getActivity();
        boolean calm = bird.calmOnGround() && !bird.isFreezing() && !bird.isPanicking();
        boolean staying = bird.isTame() && bird.isOrderedToSit() && bird.isInSittingPose();
        if (bird.isTame() && activity == PetParrotActivity.NIGHT_ROOST
            && (!staying || !calm || !freeToRest || !ParrotNightRoostGoal.isRoostTime(level))) {
            bird.setActivity(PetParrotActivity.IDLE);
        }

        // Sample the weather at the bird, so a roof, a dry biome or a frozen biome keeps it dry.
        if ((bird.tickCount + bird.getId()) % 10 != 0) return;
        boolean wet = bird.isInWater() || level.isRainingAt(bird.blockPosition()) || activity == PetParrotActivity.BATHE;
        if (wet) {
            wetSamples = Math.min(3, wetSamples + 1);
            if (wetSamples == 3) shakeUntil = bird.tickCount + 240;
        } else {
            wetSamples = 0;
        }
        if (!calm || !freeToRest || !bird.canStartNaturalDetail()) return;
        if (!wet && shakeUntil > bird.tickCount) {
            shakeUntil = 0;
            bird.setActivity(PetParrotActivity.RUFFLE);
            bird.playSound(SoundEvents.PARROT_FLY, 0.32F, 1.35F);
            level.sendParticles(ParticleTypes.SPLASH, bird.getX(), bird.getY(0.55), bird.getZ(),
                5, bird.getBbWidth() * 0.27, 0.05, bird.getBbWidth() * 0.27, 0.015);
        } else if (staying && ParrotNightRoostGoal.isRoostTime(level)) {
            bird.setActivity(PetParrotActivity.NIGHT_ROOST);
        }
    }
}
