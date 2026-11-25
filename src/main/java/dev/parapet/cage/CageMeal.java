package dev.parapet.cage;

/** The serving a perched bird turns toward during its feeding animation. */
public enum CageMeal {
    SEED, PREMIUM, WATER, HAND;

    private static final CageMeal[] ALL = values();

    public static CageMeal byIndex(int index) {
        return index >= 0 && index < ALL.length ? ALL[index] : HAND;
    }
}
