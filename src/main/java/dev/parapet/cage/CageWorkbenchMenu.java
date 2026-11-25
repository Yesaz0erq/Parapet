package dev.parapet.cage;

import dev.parapet.Parapet;
import dev.parapet.ParapetFurniture;
import java.util.List;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;

public final class CageWorkbenchMenu extends ItemCombinerMenu {
    public static final MenuType<CageWorkbenchMenu> TYPE = new MenuType<>(CageWorkbenchMenu::new, FeatureFlags.VANILLA_SET);

    private final List<RecipeHolder<CageWorkbenchRecipe>> guideRecipes;
    private RecipeHolder<CageWorkbenchRecipe> currentRecipe;

    public static void register() {
        var key = Parapet.id("cage_workbench");
        Registry.register(BuiltInRegistries.MENU, key, TYPE);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, key, CageWorkbenchRecipe.TYPE);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, key, CageWorkbenchRecipe.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, key, CageWorkbenchRecipe.CATEGORY);
        RecipeSynchronization.synchronizeRecipeSerializer(CageWorkbenchRecipe.SERIALIZER);
    }

    private CageWorkbenchMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    CageWorkbenchMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        this(id, inventory, access, CageWorkbenchRecipe.sortedRecipes(inventory.player.level()));
    }

    private CageWorkbenchMenu(int id, Inventory inventory, ContainerLevelAccess access, List<RecipeHolder<CageWorkbenchRecipe>> recipes) {
        super(TYPE, id, inventory, access, slots(recipes));
        guideRecipes = recipes;
    }

    public List<RecipeHolder<CageWorkbenchRecipe>> guideRecipes() {
        return guideRecipes;
    }

    private static ItemCombinerMenuSlotDefinition slots(List<RecipeHolder<CageWorkbenchRecipe>> recipes) {
        var slots = ItemCombinerMenuSlotDefinition.create();
        for (int i = 0; i < 3; i++) {
            int slot = i;
            slots.withSlot(slot, 28 + 30 * slot, 50, stack -> recipes.stream().anyMatch(holder -> holder.value().accepts(slot, stack)));
        }
        return slots.withResultSlot(3, 132, 50).build();
    }

    @Override
    protected boolean canMoveIntoInputSlots(ItemStack stack) {
        return getSlot(0).mayPlace(stack) || getSlot(1).mayPlace(stack) || getSlot(2).mayPlace(stack);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.getBlock() == ParapetFurniture.CAGE_WORKBENCH;
    }

    private CageWorkbenchRecipe.Input input() {
        return new CageWorkbenchRecipe.Input(inputSlots.getItem(0), inputSlots.getItem(1), inputSlots.getItem(2));
    }

    @Override
    public void createResult() {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        var input = input();
        var level = serverPlayer.level();
        currentRecipe = level.recipeAccess().getRecipeFor(CageWorkbenchRecipe.TYPE, input, level).orElse(null);
        if (currentRecipe != null && resultSlots.setRecipeUsed(serverPlayer, currentRecipe)) {
            resultSlots.setItem(0, currentRecipe.value().assemble(input));
        } else {
            currentRecipe = null;
            resultSlots.setRecipeUsed(null);
            resultSlots.setItem(0, ItemStack.EMPTY);
        }
    }

    @Override
    protected boolean mayPickup(Player who, boolean hasOutput) {
        return hasOutput && (who.level().isClientSide()
            || currentRecipe != null && currentRecipe.value().matches(input(), who.level()));
    }

    @Override
    public ItemStack quickMoveStack(Player who, int slot) {
        if (slot == getResultSlot() && !mayPickup(who, getSlot(slot).hasItem())) {
            return ItemStack.EMPTY;
        }
        return super.quickMoveStack(who, slot);
    }

    @Override
    protected void onTake(Player who, ItemStack result) {
        if (currentRecipe == null || !(who instanceof ServerPlayer)) return;
        CageWorkbenchRecipe recipe = currentRecipe.value();
        var input = input();
        recipe.assemble(input).onCraftedBy(who, 1);
        resultSlots.awardUsedRecipes(who, List.of(input.base().copy(), input.material().copy(), input.fitting().copy()));
        input.base().shrink(recipe.baseCount());
        input.material().shrink(recipe.materialCount());
        if (recipe.fitting().isPresent()) input.fitting().shrink(recipe.fittingCount());
        inputSlots.setChanged();
        access.execute((level, pos) -> level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE,
            SoundSource.BLOCKS, .6F, 1.15F));
    }
}
