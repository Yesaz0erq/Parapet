package dev.parapet.entity;

import net.minecraft.world.entity.ai.goal.BreedGoal;

/** Keeps vanilla partner selection, parental credit, experience and the single offspring spawn. */
final class ParrotBreedGoal extends BreedGoal {
    private static final int COURTSHIP_TICKS = 300;
    private long breedAfter;

    ParrotBreedGoal(PetParrotEntity bird) {
        super(bird, 1.0);
    }

    @Override
    public void start() {
        breedAfter = level.getGameTime() + COURTSHIP_TICKS;
    }

    @Override
    public boolean canContinueToUse() {
        return partner != null && partner.isAlive() && !animal.isPanicking() && !partner.isPanicking()
            && animal.canMate(partner);
    }

    @Override
    public void tick() {
        // Either partner's goal can finish first. Rechecking love prevents a second birth in the same tick.
        if (!canContinueToUse()) {
            return;
        }
        animal.getLookControl().setLookAt(partner, 10.0F, animal.getMaxHeadXRot());
        animal.getNavigation().moveTo(partner, 1.0);
        if (level.getGameTime() >= breedAfter && animal.distanceToSqr(partner) < 9.0 && animal.hasLineOfSight(partner)) {
            breed();
        }
    }
}
