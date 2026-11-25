package dev.parapet.client;

import dev.parapet.cage.CageWorkbenchMenu;
import dev.parapet.cage.CageWorkbenchRecipe;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class CageWorkbenchScreen extends AbstractContainerScreen<CageWorkbenchMenu> {
    private static final int PAPER = 0xFFDED7C7;
    private static final int INK = 0xFF343930;
    private static final int GUIDE_INK = 0xFF43594A;
    private static final int GUIDE_Y = 20;
    private int guideIndex;

    public CageWorkbenchScreen(CageWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        titleLabelY = 5;
    }

    public static void register() {
        MenuScreens.register(CageWorkbenchMenu.TYPE, CageWorkbenchScreen::new);
    }

    @Override
    protected void init() {
        super.init();
        Button previous = addPageButton("<", "previous", 7, -1);
        Button next = addPageButton(">", "next", 153, 1);
        previous.active = next.active = menu.guideRecipes().size() > 1;
    }

    private Button addPageButton(String arrow, String direction, int x, int step) {
        Component name = Component.translatable("gui.parapet.workbench." + direction);
        return addRenderableWidget(Button.builder(Component.literal(arrow), button -> {
            guideIndex = Math.floorMod(guideIndex + step, menu.guideRecipes().size());
        }).bounds(leftPos + x, topPos + GUIDE_Y, 16, 16).tooltip(Tooltip.create(name))
            .createNarration(defaultText -> name.copy()).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(gui, mouseX, mouseY, partialTick);
        gui.pose().pushMatrix();
        gui.pose().translate(leftPos, topPos);
        gui.fill(0, 0, imageWidth, imageHeight, 0xFF3B392F);
        gui.fill(1, 1, imageWidth - 2, imageHeight - 2, 0xFFFAF2DD);
        gui.fill(3, 3, imageWidth - 1, imageHeight - 1, 0xFF8F8876);
        gui.fill(3, 3, imageWidth - 3, imageHeight - 3, PAPER);
        gui.fill(5, 16, 171, 39, 0xFFB9C6AC);
        gui.fill(5, 16, 171, 17, 0xFF899B82);
        gui.fill(5, 39, 171, 40, 0xFFF1EAD7);
        for (Slot slot : menu.slots) {
            int x = slot.x;
            int y = slot.y;
            gui.fill(x - 1, y - 1, x + 17, y + 17, 0xFFFFF6E0);
            gui.fill(x - 1, y - 1, x + 16, y + 16, 0xFF6F6D60);
            gui.fill(x, y, x + 16, y + 16, 0xFFABA89A);
        }
        connectors(gui, GUIDE_Y + 8, GUIDE_INK);
        connectors(gui, 58, 0xFF736D59);
        gui.pose().popMatrix();
    }

    private static void connectors(GuiGraphicsExtractor gui, int y, int color) {
        for (int x = 51; x <= 81; x += 30) {
            gui.fill(x, y - 3, x + 1, y + 4, color);
            gui.fill(x - 3, y, x + 4, y + 1, color);
        }
        gui.fill(110, y, 126, y + 1, color);
        for (int i = 0; i < 4; i++) {
            gui.fill(121 + i, y - 3 + i, 123 + i, y - 2 + i, color);
            gui.fill(121 + i, y + 3 - i, 123 + i, y + 4 - i, color);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        gui.text(font, title, titleLabelX, titleLabelY, INK, false);
        int pages = menu.guideRecipes().size();
        String page = (pages == 0 ? 0 : guideIndex + 1) + "/" + pages;
        Component guide = Component.translatable("gui.parapet.workbench.guide").append(" " + page);
        if (font.width(title) + font.width(guide) + 20 > imageWidth) {
            guide = Component.literal(page);
        }
        gui.text(font, guide, imageWidth - 8 - font.width(guide), titleLabelY, GUIDE_INK, false);
        gui.text(font, Component.translatable("gui.parapet.workbench.inputs"), 8, 40, INK, false);
        gui.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, INK, false);
        if (pages > 0) {
            for (int column = 0; column <= menu.getResultSlot(); column++) {
                drawGuideItem(gui, guideItem(column), menu.getSlot(column).x);
            }
        }
    }

    private ItemStack guideItem(int column) {
        CageWorkbenchRecipe recipe = menu.guideRecipes().get(guideIndex).value();
        return switch (column) {
            case 0 -> example(recipe.base(), recipe.baseCount());
            case 1 -> example(recipe.material(), recipe.materialCount());
            case 2 -> recipe.fitting().map(item -> example(item, recipe.fittingCount())).orElse(ItemStack.EMPTY);
            default -> new ItemStack(recipe.result());
        };
    }

    private static ItemStack example(Ingredient ingredient, int count) {
        return ingredient.items().findFirst().map(item -> new ItemStack(item, count)).orElse(ItemStack.EMPTY);
    }

    private void drawGuideItem(GuiGraphicsExtractor gui, ItemStack item, int x) {
        if (!item.isEmpty()) {
            gui.item(item, x, GUIDE_Y);
            gui.itemDecorations(font, item, x, GUIDE_Y);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        super.extractTooltip(gui, mouseX, mouseY);
        if (!menu.getCarried().isEmpty() || menu.guideRecipes().isEmpty()) {
            return;
        }
        for (int column = 0; column <= menu.getResultSlot(); column++) {
            if (isHovering(menu.getSlot(column).x, GUIDE_Y, 16, 16, mouseX, mouseY)) {
                ItemStack item = guideItem(column);
                if (!item.isEmpty()) {
                    gui.setTooltipForNextFrame(font, item, mouseX, mouseY);
                }
            }
        }
    }
}
