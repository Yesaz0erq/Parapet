package dev.parapet.client;

import dev.parapet.Parapet;
import dev.parapet.entity.PetParrotEntity;
import dev.parapet.interaction.ParrotInteractionMenu;
import dev.parapet.interaction.ParrotTrick;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class ParrotInteractionScreen extends AbstractContainerScreen<ParrotInteractionMenu> {
    private static final Identifier PANEL = Parapet.id("textures/gui/interaction_panel.png");
    private static final Identifier BUTTONS = Parapet.id("textures/gui/interaction_buttons.png");
    private static final Identifier ICONS = Parapet.id("textures/gui/interaction_icons.png");
    private static final int IVORY = 0xFFFFF5DC;
    private static final int PALE = 0xFFE1E8DA;
    private final List<TrickButton> choices = new ArrayList<>();
    private float scale;

    private ParrotInteractionScreen(ParrotInteractionMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 256, 192);
    }

    public static void register() {
        MenuScreens.register(ParrotInteractionMenu.TYPE, ParrotInteractionScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(ParrotInteractionMenu.Profile.TYPE, (profile, context) -> {
            if (context.player().containerMenu instanceof ParrotInteractionMenu menu) menu.acceptProfile(profile);
        });
    }

    @Override
    protected void init() {
        super.init();
        choices.clear();
        scale = Math.max(0.05F, Math.min(1F, Math.min((width - 4F) / imageWidth, (height - 4F) / imageHeight)));
        leftPos = (width - scaled(imageWidth)) / 2;
        topPos = (height - scaled(imageHeight)) / 2;
        for (ParrotTrick trick : ParrotTrick.values()) {
            int i = trick.ordinal();
            choices.add(addRenderableWidget(new TrickButton(trick, i % 2 == 0 ? 16 : 131, 88 + i / 2 * 23)));
        }
        Component close = Component.translatable("gui.close");
        addRenderableWidget(Button.builder(Component.literal("×"), button -> onClose())
            .bounds(leftPos + scaled(222), topPos + scaled(10), scaled(18), scaled(18))
            .tooltip(Tooltip.create(close)).createNarration(fallback -> close.copy()).build());
        updateButtons();
    }

    private int scaled(int value) { return Math.max(1, Math.round(value * scale)); }

    private void updateButtons() {
        boolean ready = menu.profile() != null && menu.availability() == ParrotInteractionMenu.Availability.READY;
        for (TrickButton button : choices) button.active = ready && menu.affinity() >= button.trick.affinity;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(gui, mouseX, mouseY, partialTick);
        gui.pose().pushMatrix();
        gui.pose().translate(leftPos, topPos);
        gui.pose().scale(scale, scale);
        gui.blit(RenderPipelines.GUI_TEXTURED, PANEL, 0, 0, 0F, 0F, 256, 192, 256, 192);
        gui.pose().popMatrix();
    }

    private Component plumage(PetParrotEntity bird) {
        var plumage = text("plumage", plumageName(bird.getVisualPlumageId()));
        if (!menu.profile().carried().isEmpty()) {
            plumage.append(" · ").append(Component.translatable("message.parapet.carried_plumage", plumageName(menu.profile().carried())));
        }
        return plumage;
    }

    private static Component plumageName(String id) {
        return Component.translatableWithFallback("gui.parapet.guide.plumage." + id, id.replace('_', ' '));
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
        gui.pose().pushMatrix();
        gui.pose().scale(scale, scale);
        drawLine(gui, title, 13, 154, IVORY);
        if (menu.profile() != null && minecraft.level != null && minecraft.level.getEntity(menu.profile().birdId()) instanceof PetParrotEntity bird) {
            drawLine(gui, text("nature", Component.translatable("personality.parapet." + bird.getPersonality().id())), 29, 160, PALE);
            Component full = plumage(bird);
            drawLine(gui, full, 42, 158, PALE);
            if (mouseX >= leftPos + scaled(16) && mouseX <= leftPos + scaled(176)
                && mouseY >= topPos + scaled(40) && mouseY <= topPos + scaled(53)) {
                String ids = bird.getVisualPlumageId();
                if (!menu.profile().carried().isEmpty()) ids += " / " + menu.profile().carried();
                gui.setTooltipForNextFrame(font, java.util.List.of(full.getVisualOrderText(),
                    Component.literal(ids).getVisualOrderText()), mouseX, mouseY);
            }
        }
        int bond = menu.affinity();
        drawLine(gui, text("bond", bond, PetParrotEntity.MAX_AFFINITY), 57, 224, IVORY);
        gui.fill(16, 70, 240, 75, 0xFF182B2C);
        if (bond > 0) gui.fill(17, 71, 17 + Math.round(222F * bond / PetParrotEntity.MAX_AFFINITY), 74, 0xFFE6BC68);
        Component status = menu.profile() == null ? text("loading") :
            text(menu.availability().name().toLowerCase(Locale.ROOT), menu.cooldownSeconds());
        drawLine(gui, status, 163, 224, IVORY);
        gui.pose().popMatrix();
    }

    private static net.minecraft.network.chat.MutableComponent text(String suffix, Object... args) {
        return Component.translatable("gui.parapet.interaction." + suffix, args);
    }

    private void drawLine(GuiGraphicsExtractor gui, Component value, int y, int max, int color) {
        gui.text(font, fit(value.getString(), max), 16, y, color, false);
    }

    private String fit(String value, int max) {
        if (font.width(value) <= max) return value;
        return font.plainSubstrByWidth(value, Math.max(0, max - font.width("…"))) + "…";
    }

    @Override
    public Component getNarrationMessage() {
        var message = title.copy().append(". ").append(text("bond", menu.affinity(), PetParrotEntity.MAX_AFFINITY));
        if (menu.profile() != null && minecraft.level != null && minecraft.level.getEntity(menu.profile().birdId()) instanceof PetParrotEntity bird) {
            message.append(". ").append(text("nature", Component.translatable("personality.parapet." + bird.getPersonality().id())))
                .append(". ").append(plumage(bird));
        }
        return message;
    }

    private final class TrickButton extends Button {
        private final ParrotTrick trick;

        private TrickButton(ParrotTrick trick, int x, int y) {
            super(leftPos + scaled(x), topPos + scaled(y), scaled(109), scaled(19), trick.label(), ignored -> {
                var profile = menu.profile();
                if (profile != null) ClientPlayNetworking.send(new ParrotInteractionMenu.Choose(menu.containerId, profile.session(), trick.ordinal()));
            }, DEFAULT_NARRATION);
            this.trick = trick;
            Component help = trick.affinity == 0 ? trick.description()
                : trick.description().copy().append(" ").append(text("requires", trick.affinity));
            setTooltip(Tooltip.create(help));
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
            boolean locked = menu.affinity() < trick.affinity;
            gui.pose().pushMatrix();
            gui.pose().translate(getX(), getY());
            gui.pose().scale(getWidth() / 109F, getHeight() / 19F);
            int spriteY = !active ? 38 : isHoveredOrFocused() ? 19 : 0;
            gui.blit(RenderPipelines.GUI_TEXTURED, BUTTONS, 0, 0, 0F, spriteY, 109, 19, 109, 57);
            int icon = locked ? 6 : trick.ordinal();
            gui.blit(RenderPipelines.GUI_TEXTURED, ICONS, 5, 3, icon * 12F, 0F, 12, 12, 84, 12);
            String label = trick.label().getString();
            String threshold = locked ? Integer.toString(trick.affinity) : "";
            gui.text(font, fit(label, locked ? 84 - font.width(threshold) - 4 : 84), 21, 5, active ? IVORY : PALE, false);
            if (locked) gui.text(font, threshold, 103 - font.width(threshold), 5, PALE, false);
            gui.pose().popMatrix();
        }
    }
}
