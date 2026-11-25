package dev.parapet.client;

import dev.parapet.entity.ParrotEcology;
import java.util.EnumMap;

/** Timing and posture within the authored size clips. Shared rigs can still have very different occupants. */
enum CageBirdMotion {
    BUDGERIGAR(Style.SEED, 1.20F, 0.94F, 0.84F),
    COCKATIEL(Style.CREST, 0.91F, 0.90F, 1.13F),
    GREEN_CHEEK_CONURE(Style.CONURE, 1.09F, 1.04F, 1.06F),
    LOVEBIRD(Style.SEED, 1.11F, 1.16F, 0.54F),
    MONK_PARAKEET(Style.CONURE, 1.01F, 1.01F, 0.94F),
    CAIQUE(Style.CONURE, 1.18F, 1.19F, 0.49F),
    ECLECTUS(Style.BROAD_BILL, 0.82F, 0.96F, 0.77F),
    SUN_CONURE(Style.CONURE, 1.13F, 1.07F, 1.12F),
    SULFUR_CRESTED_COCKATOO(Style.CREST, 0.80F, 1.06F, 0.83F),
    AFRICAN_GREY(Style.BROAD_BILL, 0.89F, 1.03F, 0.61F),
    JARDINES_PARROT(Style.BROAD_BILL, 0.96F, 1.09F, 0.65F),
    BOURKES_PARROT(Style.SEED, 1.05F, 0.87F, 0.97F),
    RAINBOW_LORIKEET(Style.LORY, 1.19F, 1.08F, 0.94F),
    RED_LORY(Style.LORY, 1.12F, 1.03F, 0.87F),
    DUSKY_LORY(Style.LORY, 1.08F, 1.12F, 0.79F),
    BLUE_FRONTED_AMAZON(Style.BROAD_BILL, 0.87F, 1.10F, 0.74F),
    YELLOW_HEADED_AMAZON(Style.BROAD_BILL, 0.83F, 1.15F, 0.69F),
    ORANGE_WINGED_AMAZON(Style.BROAD_BILL, 0.93F, 1.01F, 0.81F),
    ROSE_RINGED_PARAKEET(Style.LONG_TAIL, 1.03F, 0.96F, 1.32F),
    EASTERN_ROSELLA(Style.LONG_TAIL, 1.07F, 1.02F, 1.07F),
    CRIMSON_ROSELLA(Style.LONG_TAIL, 0.98F, 1.10F, 1.15F),
    SENEGAL_PARROT(Style.BROAD_BILL, 1.02F, 1.12F, 0.60F),
    BLUE_AND_YELLOW_MACAW(Style.MACAW, 0.84F, 1.08F, 1.29F),
    BLUE_THROATED_MACAW(Style.MACAW, 0.90F, 0.99F, 1.31F),
    MILITARY_MACAW(Style.MACAW, 0.88F, 1.04F, 1.18F),
    GREAT_GREEN_MACAW(Style.MACAW, 0.77F, 1.12F, 1.33F),
    SCARLET_MACAW(Style.MACAW, 0.93F, 1.06F, 1.37F),
    RED_AND_GREEN_MACAW(Style.MACAW, 0.79F, 1.14F, 1.39F),
    RED_FRONTED_MACAW(Style.MACAW, 0.98F, 1.01F, 1.12F),
    CHESTNUT_FRONTED_MACAW(Style.MACAW, 1.01F, 1.06F, 0.91F),
    HYACINTH_MACAW(Style.MACAW, 0.72F, 1.20F, 1.42F),
    LEAR_MACAW(Style.MACAW, 0.85F, 1.16F, 1.24F),
    SPIX_MACAW(Style.MACAW, 0.94F, 0.95F, 1.17F),
    RED_BELLIED_MACAW(Style.MACAW, 1.03F, 0.97F, 1.09F),
    BLUE_HEADED_MACAW(Style.MACAW, 0.97F, 1.00F, 1.05F),
    BLUE_WINGED_MACAW(Style.MACAW, 1.07F, 0.98F, 1.13F),
    YELLOW_COLLARED_MACAW(Style.MACAW, 1.04F, 1.06F, 0.97F),
    RED_SHOULDERED_MACAW(Style.MACAW, 1.10F, 0.94F, 0.90F);

    enum Style { SEED, CREST, CONURE, BROAD_BILL, LORY, LONG_TAIL, MACAW }

    private static final EnumMap<ParrotEcology, CageBirdMotion> BY_SPECIES = new EnumMap<>(ParrotEcology.class);
    static {
        for (ParrotEcology species : ParrotEcology.values()) BY_SPECIES.put(species, valueOf(species.name()));
    }

    final Style style;
    final float idleRate, peckDepth, tailBalance;

    CageBirdMotion(Style style, float idleRate, float peckDepth, float tailBalance) {
        this.style = style;
        this.idleRate = idleRate;
        this.peckDepth = peckDepth;
        this.tailBalance = tailBalance;
    }

    static CageBirdMotion forSpecies(ParrotEcology species) { return BY_SPECIES.get(species); }
}
