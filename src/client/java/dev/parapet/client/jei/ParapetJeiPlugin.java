package dev.parapet.client.jei;

import dev.parapet.Parapet;
import dev.parapet.ParapetFurniture;
import dev.parapet.cage.CageWorkbenchRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class ParapetJeiPlugin implements IModPlugin {
    private static final IRecipeHolderType<CageWorkbenchRecipe> WORKBENCH = IRecipeHolderType.create(CageWorkbenchRecipe.TYPE);

    @Override
    public Identifier getPluginUid() {
        return Parapet.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new WorkbenchCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(WORKBENCH, CageWorkbenchRecipe.sortedRecipes(Minecraft.getInstance().level));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(WORKBENCH, ParapetFurniture.CAGE_WORKBENCH);
    }

    private static final class WorkbenchCategory extends AbstractRecipeCategory<RecipeHolder<CageWorkbenchRecipe>> {
        WorkbenchCategory(IGuiHelper gui) {
            super(WORKBENCH, ParapetFurniture.CAGE_WORKBENCH.getName(),
                gui.createDrawableItemLike(ParapetFurniture.CAGE_WORKBENCH), 146, 32);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder layout, RecipeHolder<CageWorkbenchRecipe> holder, IFocusGroup focuses) {
            CageWorkbenchRecipe recipe = holder.value();
            boolean threeInputs = recipe.fitting().isPresent();
            input(layout, threeInputs ? 2 : 36, recipe.base(), recipe.baseCount());
            input(layout, threeInputs ? 36 : 70, recipe.material(), recipe.materialCount());
            recipe.fitting().ifPresent(fitting -> input(layout, 70, fitting, recipe.fittingCount()));
            layout.addOutputSlot(125, 8).setOutputSlotBackground().add(recipe.result());
        }

        private static void input(IRecipeLayoutBuilder layout, int x, Ingredient ingredient, int count) {
            var slot = layout.addInputSlot(x, 8).setStandardSlotBackground();
            slot.addItemStacks(ingredient.display().resolveForStacks(slot.getContextMap()).stream()
                .map(stack -> stack.copyWithCount(count)).toList());
        }

        @Override
        public void createRecipeExtras(IRecipeExtrasBuilder extras, RecipeHolder<CageWorkbenchRecipe> holder, IFocusGroup focuses) {
            if (holder.value().fitting().isPresent()) {
                extras.addRecipePlusSignWidget().setPosition(22, 10);
            }
            extras.addRecipePlusSignWidget().setPosition(56, 10);
            extras.addRecipeArrowWidget().setPosition(93, 7);
        }
    }
}
