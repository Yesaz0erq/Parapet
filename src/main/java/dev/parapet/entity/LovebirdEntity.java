package dev.parapet.entity;

import dev.parapet.Parapet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class LovebirdEntity extends PetParrotEntity {
    public static final float MODEL_SCALE = 0.9305F;

    public LovebirdEntity(EntityType<? extends LovebirdEntity> type, Level level) {
        super(type, level, LovebirdPlumages.IDS);
    }

    @Override
    public LovebirdEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return createChick(Parapet.LOVEBIRD, level, mate);
    }
}
