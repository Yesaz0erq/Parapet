package dev.parapet.entity;

import dev.parapet.Parapet;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class CaiqueEntity extends PetParrotEntity {
    public static final float MODEL_SCALE = 0.9896F;
    private static final List<String> COLORS = List.of("black_headed", "white_bellied");

    public CaiqueEntity(EntityType<? extends CaiqueEntity> type, Level level) {
        super(type, level, COLORS);
    }

    @Override
    protected boolean hasPlumageGenes() {
        return false;
    }

    @Override
    protected boolean isSameSpecies(PetParrotEntity other) {
        return super.isSameSpecies(other) && getPlumageId().equals(other.getPlumageId());
    }

    @Override
    public CaiqueEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return createChick(Parapet.CAIQUE, level, mate);
    }
}
