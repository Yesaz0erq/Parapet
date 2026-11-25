package dev.parapet.entity;

import dev.parapet.Parapet;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class SunConureEntity extends PetParrotEntity {
    public static final float MODEL_SCALE = 0.8130F;
    private static final List<String> COLORS = List.of("normal");

    public SunConureEntity(EntityType<? extends SunConureEntity> type, Level level) {
        super(type, level, COLORS);
    }

    @Override
    public SunConureEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return createChick(Parapet.SUN_CONURE, level, mate);
    }
}
