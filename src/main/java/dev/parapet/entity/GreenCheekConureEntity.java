package dev.parapet.entity;

import dev.parapet.Parapet;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class GreenCheekConureEntity extends PetParrotEntity {
    public static final float MODEL_SCALE = 0.815F;
    private static final List<String> COMMON_COLORS = List.of("normal", "pineapple", "turquoise", "cinnamon");
    private static final String RARE_COLOR = "suncheek";
    private static final List<String> COLORS = Stream.concat(COMMON_COLORS.stream(),
        Stream.of(RARE_COLOR)).toList();

    public GreenCheekConureEntity(EntityType<? extends GreenCheekConureEntity> type, Level level) {
        super(type, level, COLORS);
    }

    @Override
    protected void randomizePlumage() {
        setPlumageId(random.nextInt(64) == 0 ? RARE_COLOR : COMMON_COLORS.get(random.nextInt(COMMON_COLORS.size())));
    }

    @Override
    public GreenCheekConureEntity getBreedOffspring(ServerLevel level, AgeableMob mate) {
        GreenCheekConureEntity chick = createChick(Parapet.GREEN_CHEEK_CONURE, level, mate);
        // A spawn egg used on an adult passes that same adult as both parents.
        if (chick != null && mate == this) {
            chick.randomizePlumage();
        }
        return chick;
    }
}
