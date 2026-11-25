package dev.parapet.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;

/** Feather care after real exposure, and quiet-time behaviour on a pet's assigned perch. */
final class ParrotPerchRoutine {
    private int wetSamples;
    private int shakeUntil;
    private int nextCageDetail;

    void tick(ServerLevel level, PetParrotEntity bird, boolean freeToRest) {
        boolean caged = bird.getCageRoost().isAssigned();
        PetParrotActivity activity = bird.getActivity();
        boolean calm = bird.calmOnGround() && !bird.isFreezing() && !bird.isPanicking();
        boolean staying = bird.isTame() && (caged || bird.isOrderedToSit() && bird.isInSittingPose());
        if (bird.isTame() && activity == PetParrotActivity.NIGHT_ROOST
            && (!staying || !calm || !freeToRest || !ParrotNightRoostGoal.isRoostTime(level))) {
            bird.setActivity(PetParrotActivity.IDLE);
        } else if (caged && activity != PetParrotActivity.IDLE && activity != PetParrotActivity.NIGHT_ROOST
            && (!calm || !(activity == PetParrotActivity.RUFFLE || activity == PetParrotActivity.LISTEN
            || activity == PetParrotActivity.PREEN) || bird.getActivityTicks(0F) >= activity.durationTicks())) {
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
        if (!calm || !freeToRest || bird.getActivity() != PetParrotActivity.IDLE) return;
        if (!wet && shakeUntil > bird.tickCount && (caged || bird.canStartNaturalDetail())) {
            shakeUntil = 0;
            bird.setActivity(PetParrotActivity.RUFFLE);
            bird.playSound(SoundEvents.PARROT_FLY, 0.32F, 1.35F);
            level.sendParticles(ParticleTypes.SPLASH, bird.getX(), bird.getY(0.55), bird.getZ(),
                5, bird.getBbWidth() * 0.27, 0.05, bird.getBbWidth() * 0.27, 0.015);
        } else if (staying && ParrotNightRoostGoal.isRoostTime(level) && (caged || bird.canStartNaturalDetail())) {
            bird.setActivity(PetParrotActivity.NIGHT_ROOST);
        } else if (caged && bird.tickCount >= nextCageDetail) {
            if (nextCageDetail != 0) {
                bird.setActivity(bird.getRandom().nextBoolean() ? PetParrotActivity.PREEN : PetParrotActivity.LISTEN);
            }
            nextCageDetail = bird.tickCount + bird.getPersonality().preenDelay(bird.getRandom());
        }
    }
}
