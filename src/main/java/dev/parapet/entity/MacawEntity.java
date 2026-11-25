package dev.parapet.entity;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class MacawEntity extends PetParrotEntity {
    private static final List<String> COLORS = List.of("normal");
    private final MacawSpecies species;
    private final EntityType<MacawEntity> speciesType;

    public MacawEntity(EntityType<MacawEntity> type, Level level, MacawSpecies species) {
        super(type, level, COLORS);
        this.species = species;
        this.speciesType = type;
    }

    public MacawSpecies getSpecies() {
        return species;
    }

    @Override
    public String getVisualPlumageId() {
        return species.shortId();
    }

    @Override
    public MacawEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return createChick(speciesType, level, mate);
    }
}
