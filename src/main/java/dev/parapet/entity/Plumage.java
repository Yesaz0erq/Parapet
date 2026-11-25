package dev.parapet.entity;

import java.util.Locale;
import net.minecraft.util.RandomSource;

public enum Plumage {
    GREEN, BLUE, YELLOW, WHITE, TURQUOISE, VIOLET;

    private static final Plumage[] COLORS = values();
    private final String id = name().toLowerCase(Locale.ROOT);

    public String getId() {
        return id;
    }

    public static Plumage random(RandomSource random) {
        return COLORS[random.nextInt(COLORS.length)];
    }

    public static Plumage byIndex(int index) {
        return index >= 0 && index < COLORS.length ? COLORS[index] : GREEN;
    }

    public static Plumage byId(String id) {
        for (Plumage color : COLORS) {
            if (color.id.equals(id)) {
                return color;
            }
        }
        return GREEN;
    }
}
