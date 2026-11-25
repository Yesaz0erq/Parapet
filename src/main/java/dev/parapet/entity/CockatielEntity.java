package dev.parapet.entity;

import dev.parapet.Parapet;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class CockatielEntity extends PetParrotEntity {
    public static final float MODEL_SCALE = 0.842F;
    private static final List<String> COLORS = List.of("gray", "lutino", "pearl", "whiteface");

    public CockatielEntity(EntityType<? extends CockatielEntity> type, Level level) {
        super(type, level, COLORS);
    }

    @Override
    public CockatielEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return createChick(Parapet.COCKATIEL, level, mate);
    }
}
