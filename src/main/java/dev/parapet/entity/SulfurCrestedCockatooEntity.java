package dev.parapet.entity;

import dev.parapet.Parapet;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class SulfurCrestedCockatooEntity extends PetParrotEntity {
    public static final float MODEL_SCALE = 1.273327F;
    private static final List<String> COLORS = List.of("normal");
    private UUID lastObserver;
    private boolean observerWasClose;
    private boolean observerHadFood;

    public SulfurCrestedCockatooEntity(EntityType<? extends SulfurCrestedCockatooEntity> type, Level level) {
        super(type, level, COLORS);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if ((tickCount + getId()) % 10 != 0) {
            return;
        }
        Player observer = level.getNearestPlayer(getX(), getY(), getZ(), 5.0, player -> player.isAlive() && !player.isSpectator());
        if (observer == null || !hasLineOfSight(observer)) {
            lastObserver = null;
            return;
        }
        boolean sameObserver = observer.getUUID().equals(lastObserver);
        double distance = distanceToSqr(observer);
        // Enter at 3 blocks; crossing back and forth over that distance alone does not retrigger the crest.
        boolean close = distance <= 9.0 || sameObserver && observerWasClose && distance <= 16.0;
        boolean hasFood = observer.isHolding(this::acceptsFood);
        if ((close && (!sameObserver || !observerWasClose)) || (hasFood && (!sameObserver || !observerHadFood))) {
            tryStartDisplay(PetParrotActivity.RAISE_CREST, observer, 240);
        }
        lastObserver = observer.getUUID();
        observerWasClose = close;
        observerHadFood = hasFood;
    }

    @Override
    public SulfurCrestedCockatooEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return createChick(Parapet.SULFUR_CRESTED_COCKATOO, level, mate);
    }
}
