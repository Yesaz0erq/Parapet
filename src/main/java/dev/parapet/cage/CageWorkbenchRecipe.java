package dev.parapet.cage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.parapet.ParapetFurniture;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.level.Level;

public record CageWorkbenchRecipe(Ingredient base, int baseCount, Ingredient material, int materialCount,
                                  Optional<Ingredient> fitting, int fittingCount, Item result, int order)
        implements Recipe<CageWorkbenchRecipe.Input> {
    private static final Codec<Integer> COUNT = Codec.intRange(1, 64);
    private static final MapCodec<CageWorkbenchRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Ingredient.CODEC.fieldOf("base").forGetter(CageWorkbenchRecipe::base),
        COUNT.optionalFieldOf("base_count", 1).forGetter(CageWorkbenchRecipe::baseCount),
        Ingredient.CODEC.fieldOf("material").forGetter(CageWorkbenchRecipe::material),
        COUNT.optionalFieldOf("material_count", 1).forGetter(CageWorkbenchRecipe::materialCount),
        Ingredient.CODEC.optionalFieldOf("fitting").forGetter(CageWorkbenchRecipe::fitting),
        COUNT.optionalFieldOf("fitting_count", 1).forGetter(CageWorkbenchRecipe::fittingCount),
        BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(CageWorkbenchRecipe::result),
        Codec.INT.optionalFieldOf("order", 0).forGetter(CageWorkbenchRecipe::order)
    ).apply(i, CageWorkbenchRecipe::new));
    public static final RecipeType<CageWorkbenchRecipe> TYPE = new RecipeType<>() {};
    static final RecipeBookCategory CATEGORY = new RecipeBookCategory();
    static final RecipeSerializer<CageWorkbenchRecipe> SERIALIZER =
        new RecipeSerializer<>(CODEC, ByteBufCodecs.fromCodecWithRegistries(CODEC.codec()));

    public static List<RecipeHolder<CageWorkbenchRecipe>> sortedRecipes(Level level) {
        return level.recipeAccess().getSynchronizedRecipes().getAllOfType(TYPE).stream()
            .sorted(Comparator.comparingInt((RecipeHolder<CageWorkbenchRecipe> holder) -> holder.value().order())
                .thenComparing(holder -> holder.id().identifier()))
            .toList();
    }

    public record Input(ItemStack base, ItemStack material, ItemStack fitting) implements RecipeInput {
        @Override public ItemStack getItem(int index) {
            return switch (index) {
                case 0 -> base;
                case 1 -> material;
                case 2 -> fitting;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override public int size() { return 3; }
    }

    boolean accepts(int slot, ItemStack stack) {
        if (stack.getItem() instanceof ParrotCageItem && (CageBirdCargo.isOccupied(stack)
            || CageDecorations.hasContents(stack))) return false;
        return switch (slot) {
            case 0 -> base.test(stack);
            case 1 -> material.test(stack);
            case 2 -> fitting.isPresent() && fitting.get().test(stack);
            default -> false;
        };
    }

    @Override
    public boolean matches(Input input, Level level) {
        return accepts(0, input.base()) && input.base().getCount() >= baseCount
            && accepts(1, input.material()) && input.material().getCount() >= materialCount
            && (fitting.isEmpty() ? input.fitting().isEmpty()
                : accepts(2, input.fitting()) && input.fitting().getCount() >= fittingCount);
    }

    @Override
    public ItemStack assemble(Input input) {
        return new ItemStack(result);
    }

    @Override
    public List<RecipeDisplay> display() {
        var station = new SlotDisplay.ItemSlotDisplay(ParapetFurniture.CAGE_WORKBENCH.asItem());
        return List.of(new SmithingRecipeDisplay(base.display(), material.display(),
            fitting.map(Ingredient::display).orElse(SlotDisplay.Empty.INSTANCE),
            new SlotDisplay.ItemSlotDisplay(result), station));
    }

    @Override public PlacementInfo placementInfo() {
        return PlacementInfo.createFromOptionals(List.of(Optional.of(base), Optional.of(material), fitting));
    }
    @Override public boolean showNotification() { return true; }
    @Override public String group() { return ""; }
    @Override public RecipeType<CageWorkbenchRecipe> getType() { return TYPE; }
    @Override public RecipeSerializer<CageWorkbenchRecipe> getSerializer() { return SERIALIZER; }
    @Override public RecipeBookCategory recipeBookCategory() { return CATEGORY; }
}
