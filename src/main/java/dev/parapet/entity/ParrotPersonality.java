package dev.parapet.entity;

import java.util.Locale;
import java.util.UUID;
import net.minecraft.util.RandomSource;

public enum ParrotPersonality {
    CALM(3, 180, 180),
    CURIOUS(2, 240, 240),
    BOLD(4, 300, 240),
    SHY(1, 360, 240);

    private static final ParrotPersonality[] ALL = values();
    private final String id = name().toLowerCase(Locale.ROOT);
    private final int colorWeight;
    private final int minimumPreenDelay;
    private final int preenDelaySpread;

    ParrotPersonality(int colorWeight, int minimumPreenDelay, int preenDelaySpread) {
        this.colorWeight = colorWeight;
        this.minimumPreenDelay = minimumPreenDelay;
        this.preenDelaySpread = preenDelaySpread;
    }

    public String id() {
        return id;
    }

    int colorWeight() {
        return colorWeight;
    }

    int preenDelay(RandomSource random) {
        return minimumPreenDelay + random.nextInt(preenDelaySpread + 1);
    }

    static ParrotPersonality fromIndex(int index) {
        return ALL[index];
    }

    static ParrotPersonality forUuid(UUID uuid) {
        return ALL[Math.floorMod(uuid.hashCode(), ALL.length)];
    }

    static ParrotPersonality fromId(String id, UUID fallback) {
        for (ParrotPersonality personality : ALL) {
            if (personality.id.equals(id)) {
                return personality;
            }
        }
        return forUuid(fallback);
    }
}
