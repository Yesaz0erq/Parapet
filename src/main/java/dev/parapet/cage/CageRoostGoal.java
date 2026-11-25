package dev.parapet.cage;

import dev.parapet.entity.PetParrotEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

/** Reserves locomotion while the server holds this existing bird on its perch. */
public final class CageRoostGoal extends Goal {
    private final PetParrotEntity bird;

    public CageRoostGoal(PetParrotEntity bird) {
        this.bird = bird;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return bird.getCageRoost().isAssigned();
    }

    @Override
    public void start() {
        bird.getNavigation().stop();
        bird.getMoveControl().setWait();
    }
}
