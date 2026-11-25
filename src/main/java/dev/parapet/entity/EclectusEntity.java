package dev.parapet.entity;

import dev.parapet.Parapet;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class EclectusEntity extends PetParrotEntity {
    public static final float MODEL_SCALE = 0.9579F;
    private static final List<String> SEXES = List.of("male", "female");

    public EclectusEntity(EntityType<? extends EclectusEntity> type, Level level) {
        super(type, level, SEXES);
    }

    public boolean isFemale() {
        return getPlumageId().equals("female");
    }

    @Override
    protected boolean hasPlumageGenes() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Sex", getPlumageId());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        // Sex and the legacy Plumage key feed the same synchronized value.
        input.getString("Sex").filter(SEXES::contains).ifPresent(this::setPlumageId);
    }

    @Override
    public boolean canMate(Animal other) {
        return other instanceof EclectusEntity partner && isFemale() != partner.isFemale() && super.canMate(partner);
    }

    @Override
    public EclectusEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        // Spawn eggs on adults use the same individual as both arguments.
        if (mate != this && mate instanceof EclectusEntity partner && isFemale() == partner.isFemale()) {
            return null;
        }
        EclectusEntity chick = createChick(Parapet.ECLECTUS, level, mate);
        if (chick != null) {
            chick.randomizePlumage();
        }
        return chick;
    }
}
