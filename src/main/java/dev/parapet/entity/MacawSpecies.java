package dev.parapet.entity;

import java.util.Locale;

public enum MacawSpecies {
    BLUE_AND_YELLOW(86, 1.32F, 2.23F, 1.82F),
    BLUE_THROATED(85, 1.30F, 2.21F, 1.80F),
    MILITARY(73, 1.12F, 1.90F, 1.55F),
    GREAT_GREEN(81, 1.24F, 2.10F, 1.72F),
    SCARLET(85, 1.30F, 2.21F, 1.80F),
    RED_AND_GREEN(90, 1.38F, 2.34F, 1.91F),
    RED_FRONTED(60, 1.15F, 1.89F, 1.54F),
    CHESTNUT_FRONTED(45, 0.86F, 1.42F, 1.15F),
    HYACINTH(95, 1.54F, 2.60F, 2.12F),
    LEAR(73, 1.18F, 2.00F, 1.63F),
    SPIX(56, 0.97F, 1.61F, 1.32F),
    RED_BELLIED(46, 0.80F, 1.32F, 1.09F),
    BLUE_HEADED(41, 0.72F, 1.36F, 1.12F),
    BLUE_WINGED(43, 0.76F, 1.43F, 1.17F),
    YELLOW_COLLARED(38, 0.67F, 1.26F, 1.03F),
    RED_SHOULDERED(30, 0.53F, 1.00F, 0.82F);

    private final String shortId = name().toLowerCase(Locale.ROOT);
    private final int lengthCm;
    private final float width;
    private final float height;
    private final float eyeHeight;

    MacawSpecies(int lengthCm, float width, float height, float eyeHeight) {
        this.lengthCm = lengthCm;
        this.width = width;
        this.height = height;
        this.eyeHeight = eyeHeight;
    }

    public String shortId() {
        return shortId;
    }

    public String entityId() {
        return shortId + "_macaw";
    }

    public int lengthCm() {
        return lengthCm;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float eyeHeight() {
        return eyeHeight;
    }
}
