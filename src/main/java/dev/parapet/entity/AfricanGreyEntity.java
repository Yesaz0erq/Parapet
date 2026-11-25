package dev.parapet.entity;

import dev.parapet.Parapet;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class AfricanGreyEntity extends PetParrotEntity {
    public static final float MODEL_SCALE = 1.189408F;
    private static final List<String> SPECIES = List.of("congo", "timneh");

    public AfricanGreyEntity(EntityType<? extends AfricanGreyEntity> type, Level level) {
        super(type, level, SPECIES);
    }

    @Override
    protected boolean hasPlumageGenes() {
        return false;
    }

    @Override
    protected float sanitizeScale(float scale) {
        float speciesScale = entityData.get(PLUMAGE) == SPECIES.indexOf("timneh") ? 30.0F / 33.0F : 1.0F;
        return super.sanitizeScale(scale) * speciesScale;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (PLUMAGE.equals(accessor)) {
            refreshDimensions();
        }
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable("entity.parapet.african_grey." + getPlumageId());
    }

    @Override
    protected boolean isSameSpecies(PetParrotEntity other) {
        return super.isSameSpecies(other) && getPlumageId().equals(other.getPlumageId());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Species", getPlumageId());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getString("Species").filter(SPECIES::contains).ifPresent(this::setPlumageId);
    }

    @Override
    public AfricanGreyEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return createChick(Parapet.AFRICAN_GREY, level, mate);
    }
}
