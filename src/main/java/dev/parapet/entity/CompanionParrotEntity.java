package dev.parapet.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class CompanionParrotEntity extends PetParrotEntity {
    private static final EntityDataAccessor<Boolean> FEMALE =
        SynchedEntityData.defineId(CompanionParrotEntity.class, EntityDataSerializers.BOOLEAN);
    private final CompanionParrotSpecies species;
    private final EntityType<CompanionParrotEntity> speciesType;

    public CompanionParrotEntity(EntityType<CompanionParrotEntity> type, Level level, CompanionParrotSpecies species) {
        super(type, level, species.variants());
        this.species = species;
        this.speciesType = type;
        if (species.hasSexSpecificPlumage() && !level.isClientSide()) {
            entityData.set(FEMALE, random.nextBoolean());
        }
    }

    public CompanionParrotSpecies getSpecies() {
        return species;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FEMALE, false);
    }

    public boolean isFemale() {
        return entityData.get(FEMALE);
    }

    @Override
    public String getVisualPlumageId() {
        if (!species.hasSexSpecificPlumage()) {
            return getPlumageId();
        }
        return getPlumageId() + (isFemale() ? "_female" : "_male");
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (species.hasSexSpecificPlumage()) {
            output.putString("Sex", isFemale() ? "female" : "male");
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        if (species.hasSexSpecificPlumage()) {
            input.getString("Sex").ifPresent(sex -> {
                if ("female".equals(sex) || "male".equals(sex)) {
                    entityData.set(FEMALE, "female".equals(sex));
                }
            });
        }
    }

    @Override
    protected boolean acceptsFood(ItemStack stack) {
        if (species.eatsFruit()) {
            return stack.is(Items.MELON_SLICE) || stack.is(Items.SWEET_BERRIES);
        }
        return super.acceptsFood(stack);
    }

    private boolean compatibleSex(AgeableMob mate) {
        return !species.hasSexSpecificPlumage()
            || mate instanceof CompanionParrotEntity partner && isFemale() != partner.isFemale();
    }

    @Override
    public boolean canMate(Animal other) {
        return compatibleSex(other) && super.canMate(other);
    }

    @Override
    public CompanionParrotEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        // Spawn eggs on adults pass that same adult as the mate.
        if (mate != this && !compatibleSex(mate)) {
            return null;
        }
        return createChick(speciesType, level, mate);
    }
}
