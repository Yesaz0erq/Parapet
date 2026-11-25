package dev.parapet.entity;

import java.util.List;
import net.minecraft.util.RandomSource;

/** Each bird can pass on either its visible colour or the colour carried beneath it. */
record PlumageGenes(String visible, String carried) {
    private String transmit(RandomSource random) {
        return random.nextBoolean() ? visible : carried;
    }

    private boolean contains(String color) {
        return visible.equals(color) || carried.equals(color);
    }

    static PlumageGenes breed(RandomSource random, PlumageGenes a, ParrotPersonality natureA,
                              PlumageGenes b, ParrotPersonality natureB, List<String> colors,
                              boolean mixedLovebirds) {
        String alleleA = a.transmit(random);
        String alleleB = b.transmit(random);
        boolean showA = random.nextInt(natureA.colorWeight() + natureB.colorWeight()) < natureA.colorWeight();
        String expressed = showA ? alleleA : alleleB;
        String latent = showA ? alleleB : alleleA;

        if (mixedLovebirds) {
            // The full lovebird catalogue is available even across its three plumage groups.
            return new PlumageGenes(colors.get(random.nextInt(colors.size())), expressed);
        }
        if (random.nextInt(16) == 0) {
            List<String> alternatives = colors.stream().filter(c -> !a.contains(c) && !b.contains(c)).toList();
            if (!alternatives.isEmpty()) {
                expressed = alternatives.get(random.nextInt(alternatives.size()));
            }
        }
        return new PlumageGenes(expressed, latent);
    }
}
