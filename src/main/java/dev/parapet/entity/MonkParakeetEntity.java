package dev.parapet.entity;

import dev.parapet.Parapet;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class MonkParakeetEntity extends PetParrotEntity {
    public static final float MODEL_SCALE = 0.8077F;
    private static final List<String> COLORS = List.of(
        "green", "blue", "turquoise", "lutino", "albino", "cinnamon", "pallid", "grey_green", "cobalt", "violet"
    );

    public MonkParakeetEntity(EntityType<? extends MonkParakeetEntity> type, Level level) {
        super(type, level, COLORS);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if ((tickCount + getId()) % 10 != 0 || !isBaby() || !isTame()) {
            return;
        }
        if (getOwner() instanceof Player owner && !owner.isSpectator() && owner.isAlive() && isDisplayAudienceInRange(owner)) {
            tryStartDisplay(PetParrotActivity.BEG, owner, 180);
        }
    }

    @Override
    protected boolean isDisplayAudienceInRange(Player player) {
        return isBaby() && isTame() && player.level() == level() && isOwnedBy(player)
            && distanceToSqr(player) <= 9.0 && player.isHolding(this::acceptsFood);
    }

    @Override
    public MonkParakeetEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return createChick(Parapet.MONK_PARAKEET, level, mate);
    }
}
