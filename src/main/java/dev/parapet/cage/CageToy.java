package dev.parapet.cage;

import dev.parapet.ParapetFurniture;
import dev.parapet.block.ParrotCageBlock.Size;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Installation limits apply to new toys; existing fittings remain real, recoverable item stacks. */
public enum CageToy {
    FORAGING_BALL(ParapetFurniture.FORAGING_BALL, Size.STANDARD),
    BEAK_RUBBING_PERCH(ParapetFurniture.BEAK_RUBBING_PERCH, Size.STANDARD),
    ROPE_SWING(ParapetFurniture.ROPE_SWING, Size.LARGE),
    CHEW_BLOCKS(ParapetFurniture.CHEW_BLOCKS, Size.LARGE),
    MINERAL_CHEW_CHARM(ParapetFurniture.MINERAL_CHEW_CHARM, Size.DELUXE),
    DOUBLE_RING_SWING(ParapetFurniture.DOUBLE_RING_SWING, Size.SUPER_DELUXE),
    LARGE_CHEW_MOBILE(ParapetFurniture.LARGE_CHEW_MOBILE, Size.GIANT);

    public enum Installation { ALLOWED, ALREADY_INSTALLED, UNSUPPORTED, FULL }

    private final Item item;
    private final Size minimumSize;
    private static final Map<Item, CageToy> BY_ITEM = Arrays.stream(values())
        .collect(Collectors.toUnmodifiableMap(CageToy::item, Function.identity()));

    CageToy(Item item, Size minimumSize) {
        this.item = item;
        this.minimumSize = minimumSize;
    }

    public Item item() { return item; }
    public Size minimumSize() { return minimumSize; }

    public static int capacity(Size size) {
        return switch (size) {
            case STANDARD -> 1;
            case LARGE -> 2;
            case DELUXE -> 3;
            case SUPER_DELUXE -> 4;
            case GIANT -> 5;
        };
    }

    public boolean fits(Size size) { return capacity(size) >= capacity(minimumSize); }

    public static Optional<CageToy> forItem(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.ofNullable(BY_ITEM.get(stack.getItem()));
    }

    public static List<CageToy> supported(Size size) {
        return Arrays.stream(values()).filter(toy -> toy.fits(size)).toList();
    }

    public static int installedCount(List<ItemStack> fittings) {
        return fittings.stream().filter(stack -> forItem(stack).isPresent()).mapToInt(ItemStack::getCount).sum();
    }

    public Installation installationIn(Size size, List<ItemStack> fittings) {
        if (fittings.stream().anyMatch(stack -> stack.is(item))) return Installation.ALREADY_INSTALLED;
        if (!fits(size)) return Installation.UNSUPPORTED;
        return installedCount(fittings) >= capacity(size) ? Installation.FULL : Installation.ALLOWED;
    }

    public Component minimumCageName() {
        return switch (minimumSize) {
            case STANDARD -> ParapetFurniture.PARROT_CAGE.getName();
            case LARGE -> ParapetFurniture.LARGE_PARROT_CAGE.getName();
            case DELUXE -> ParapetFurniture.DELUXE_PARROT_CAGE.getName();
            case SUPER_DELUXE -> ParapetFurniture.SUPER_DELUXE_PARROT_CAGE.getName();
            case GIANT -> ParapetFurniture.GIANT_PARROT_CAGE.getName();
        };
    }

    /** Remove one item, favouring legacy fittings that can no longer be installed in this cage. */
    public static ItemStack takeForRemoval(Size size, List<ItemStack> fittings) {
        int index = -1;
        for (int i = fittings.size() - 1; i >= 0; i--) {
            var toy = forItem(fittings.get(i));
            if (toy.isEmpty()) continue;
            if (index < 0) index = i;
            if (!toy.get().fits(size)) {
                index = i;
                break;
            }
        }
        if (index < 0) return ItemStack.EMPTY;
        ItemStack fitting = fittings.get(index);
        ItemStack removed = fitting.split(1);
        if (fitting.isEmpty()) fittings.remove(index);
        return removed;
    }
}
