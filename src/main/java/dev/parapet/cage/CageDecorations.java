package dev.parapet.cage;

import dev.parapet.ParapetFurniture;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;

public final class CageDecorations {
    private static final String GARLAND = "parapet:cage_garland";
    private static final String BELL = "parapet:hanging_bell";

    private CageDecorations() {}

    public static boolean isFitting(ItemStack stack) {
        return CageToy.forItem(stack).isPresent() || stack.is(ParapetFurniture.CAGE_GARLAND)
            || stack.is(ParapetFurniture.HANGING_BELL);
    }

    public static boolean hasContents(ItemStack cage) {
        return !contents(cage).isEmpty();
    }

    public static List<ItemStack> contents(ItemStack cage) {
        var flags = cage.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return contents(cage.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY),
            flags.getBooleanOr(GARLAND, false), flags.getBooleanOr(BELL, false));
    }

    public static List<ItemStack> contents(ItemContainerContents stored, boolean garland, boolean bell) {
        List<ItemStack> result = new ArrayList<>();
        stored.nonEmptyItemCopyStream().forEach(result::add);
        // Cages placed or packed before 0.10 stored these two fittings as flags.
        if (garland) addLegacyFitting(result, ParapetFurniture.CAGE_GARLAND);
        if (bell) addLegacyFitting(result, ParapetFurniture.HANGING_BELL);
        return result;
    }

    private static void addLegacyFitting(List<ItemStack> items, Item fitting) {
        if (items.stream().noneMatch(stack -> stack.is(fitting))) items.add(new ItemStack(fitting));
    }

    public static ItemStack pack(ItemStack cage, List<ItemStack> items) {
        if (!items.isEmpty()) cage.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        // The inventory models use these flags for the garland and bell overlays.
        boolean garland = items.stream().anyMatch(stack -> stack.is(ParapetFurniture.CAGE_GARLAND));
        boolean bell = items.stream().anyMatch(stack -> stack.is(ParapetFurniture.HANGING_BELL));
        if (garland || bell) CustomData.update(DataComponents.CUSTOM_DATA, cage, tag -> {
            if (garland) tag.putBoolean(GARLAND, true);
            if (bell) tag.putBoolean(BELL, true);
        });
        return cage;
    }
}
