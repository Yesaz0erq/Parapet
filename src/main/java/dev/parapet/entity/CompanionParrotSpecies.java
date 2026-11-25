package dev.parapet.entity;

import java.util.List;
import java.util.Locale;

public enum CompanionParrotSpecies {
    JARDINES_PARROT(28, 0.983210F, 0.90F, 1.37F, 1.12F, "gulielmi", "fantiensis", "massaicus"),
    BOURKES_PARROT(19, 0.755930F, 0.41F, 0.70F, 0.56F, "wild", "opaline", "lutino", "rubino"),
    RAINBOW_LORIKEET(26, 0.815569F, 0.49F, 0.97F, 0.78F, "normal"),
    RED_LORY(31, 1.059604F, 0.71F, 1.26F, 1.02F, "normal"),
    DUSKY_LORY(25, 0.975798F, 0.84F, 1.23F, 1.01F, "orange_band", "yellow_band"),
    BLUE_FRONTED_AMAZON(36, 1.096542F, 1.08F, 1.65F, 1.34F, "aestiva", "xanthopteryx"),
    YELLOW_HEADED_AMAZON(37, 1.086365F, 1.13F, 1.75F, 1.42F, "normal"),
    ORANGE_WINGED_AMAZON(31, 1.012205F, 0.93F, 1.46F, 1.20F, "normal"),
    ROSE_RINGED_PARAKEET(40, 0.925629F, 0.56F, 1.10F, 0.89F, "green", "blue", "lutino", "albino", "grey", "turquoise", "violet"),
    EASTERN_ROSELLA(30, 0.928744F, 0.62F, 1.10F, 0.89F, "normal", "golden_mantled"),
    CRIMSON_ROSELLA(36, 1.052985F, 0.90F, 1.33F, 1.09F, "crimson", "yellow", "adelaide"),
    SENEGAL_PARROT(23, 0.903802F, 0.60F, 1.07F, 0.87F, "senegalus", "mesotypus", "versteri");

    private final String id = name().toLowerCase(Locale.ROOT);
    private final int lengthCm;
    private final float modelScale;
    private final float width;
    private final float height;
    private final float eyeHeight;
    private final List<String> variants;

    CompanionParrotSpecies(int lengthCm, float modelScale, float width, float height, float eyeHeight, String... variants) {
        this.lengthCm = lengthCm;
        this.modelScale = modelScale;
        this.width = width;
        this.height = height;
        this.eyeHeight = eyeHeight;
        this.variants = List.of(variants);
    }

    public String id() {
        return id;
    }

    public int lengthCm() {
        return lengthCm;
    }

    public float modelScale() {
        return modelScale;
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

    public List<String> variants() {
        return variants;
    }

    public boolean hasSexSpecificPlumage() {
        return this == BOURKES_PARROT || this == ROSE_RINGED_PARAKEET;
    }

    public boolean eatsFruit() {
        return this == RAINBOW_LORIKEET || this == RED_LORY || this == DUSKY_LORY;
    }
}
