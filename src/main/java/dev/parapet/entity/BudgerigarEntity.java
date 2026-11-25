package dev.parapet.entity;

import dev.parapet.Parapet;
import java.util.Arrays;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class BudgerigarEntity extends PetParrotEntity {
    private static final List<String> COLORS = Arrays.stream(Plumage.values()).map(Plumage::getId).toList();

    public BudgerigarEntity(EntityType<? extends BudgerigarEntity> type, Level level) {
        super(type, level, COLORS);
    }

    public Plumage getPlumage() {
        return Plumage.byId(getPlumageId());
    }

    @Override
    public BudgerigarEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return createChick(Parapet.BUDGERIGAR, level, mate);
    }
}
