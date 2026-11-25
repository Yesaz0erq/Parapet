package dev.parapet.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.parapet.Parapet;
import dev.parapet.entity.ParrotEcology;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public final class ParrotFieldGuideScreen extends Screen {
    private static final int BOOK_W = 320, BOOK_H = 200, SPINE = 160, INDEX_ROWS = 8;
    private static final long TURN_NANOS = 500_000_000L;
    private static final int INK = 0xFF382F22, MUTED = 0xFF655642, GREEN = 0xFF33583F, RULE = 0xFFA99570;
    private static final Identifier BOOK = Parapet.id("textures/gui/field_guide.png");
    private static final Identifier LEAF = Parapet.id("textures/gui/page_leaf.png");
    private static final ParrotEcology[] SPECIES = ParrotEcology.values();

    private static final List<ParrotEcology> ALL_ENTRIES = List.of(SPECIES);

    private final Set<String> collected;
    private final List<ParrotEcology> missingEntries;
    private final Map<ParrotEcology, FieldGuideSpecimen> specimens = new EnumMap<>(ParrotEcology.class);
    private long opened;
    private boolean openingSkipped;
    private final int collectedCount;
    private float scale, originX, originY;
    private int page, target, indexFocus;
    private boolean indexOpen, notesOpen, missingOnly;
    private long turnStarted;
    private double pointerX, pointerY;

    public ParrotFieldGuideScreen(Set<String> collected) {
        super(label("title"));
        this.collected = Set.copyOf(collected);
        missingEntries = ALL_ENTRIES.stream().filter(entry -> !recorded(entry)).toList();
        collectedCount = ALL_ENTRIES.size() - missingEntries.size();
    }

    @Override
    protected void init() {
        scale = Math.min(1.65F, Math.min((width - 4F) / BOOK_W, (height - 4F) / BOOK_H));
        originX = (width - BOOK_W * scale) / 2F;
        originY = (height - BOOK_H * scale) / 2F;
    }

    private static Component label(String key, Object... args) {
        return Component.translatable("gui.parapet.guide." + key, args);
    }

    private static String path(ParrotEcology species) { return species.name().toLowerCase(Locale.ROOT); }
    private static Component name(ParrotEcology species) { return Component.translatable("entity.parapet." + path(species)); }
    private boolean recorded(ParrotEcology species) { return collected.contains(path(species)); }
    private int px(int x) { return Math.round(originX + x * scale); }
    private int py(int y) { return Math.round(originY + y * scale); }
    private FieldGuideSpecimen specimen(ParrotEcology species) {
        return specimens.computeIfAbsent(species, entry -> new FieldGuideSpecimen(minecraft, path(entry), recorded(entry)));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(gui, mouseX, mouseY, partialTick);
        pointerX = (mouseX - originX) / scale;
        pointerY = (mouseY - originY) / scale;
        float time = turnStarted == 0 ? 1 : (System.nanoTime() - turnStarted) / (float) TURN_NANOS;
        if (turnStarted != 0 && time >= 1F) {
            page = target;
            turnStarted = 0;
        }
        gui.pose().pushMatrix();
        gui.pose().translate(originX, originY).scale(scale);
        if (opened == 0) {
            opened = System.nanoTime();
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 0.86F, 0.42F));
        }
        float opening = openingProgress();
        if (opening < 1F) {
            ParrotGuideOpening.draw(gui, opening, () -> leftPage(gui, page), () -> rightPage(gui, page));
            gui.pose().popMatrix();
            return;
        }
        gui.blit(RenderPipelines.GUI_TEXTURED, BOOK, 0, 0, 0F, 0F, BOOK_W, BOOK_H, BOOK_W, BOOK_H);
        if (turnStarted != 0) {
            drawTurn(gui, time);
        } else {
            leftPage(gui, page);
            rightPage(gui, page);
        }
        gui.nextStratum();
        link(gui, Component.literal("×"), 299, 4, 316, 19, true);
        if (inside(299, 4, 316, 19)) {
            gui.setTooltipForNextFrame(font, label("close"), mouseX, mouseY);
        }
        gui.pose().popMatrix();
    }

    private float openingProgress() {
        if (openingSkipped) return 1F;
        if (opened == 0) return 0F;
        return Math.min(1F, (System.nanoTime() - opened) / (float) ParrotGuideOpening.DURATION_NANOS);
    }

    private void leftPage(GuiGraphicsExtractor gui, int number) {
        if (indexOpen) {
            indexPage(gui);
            return;
        }
        var species = SPECIES[number];
        fit(gui, label("progress", collectedCount, SPECIES.length), 20, 21, 82, MUTED);
        link(gui, label(notesOpen ? "details" : "notes"), 105, 16, 145, 32, true);
        gui.fill(20, 32, 101, 33, 0x77A99570);
        if (collectedCount > 0) gui.fill(20, 32, 20 + Math.round(81F * collectedCount / SPECIES.length), 33, GREEN);
        wrapped(gui, name(species), 20, 37, 125, 2, INK);
        gui.fill(20, 61, 144, 62, RULE);
        if (notesOpen) {
            wrapped(gui, observation(species), 20, 68, 124, 4, INK);
            wrapped(gui, label("note_roost"), 20, 113, 124, 4, GREEN);
        } else {
            habitatPage(gui, species);
        }
        link(gui, label("contents"), 40, 162, 119, 176, true);
        link(gui, Component.literal("<"), 19, 161, 37, 177, number > 0);
    }

    private static Component observation(ParrotEcology species) {
        return label(switch (species) {
            case BUDGERIGAR -> "note_seeds";
            case RAINBOW_LORIKEET, RED_LORY, DUSKY_LORY -> "note_nectar";
            case MONK_PARAKEET -> "note_chick";
            case SULFUR_CRESTED_COCKATOO -> "note_crest";
            default -> "note_flock";
        });
    }

    private void habitatPage(GuiGraphicsExtractor gui, ParrotEcology species) {
        var rarity = Component.translatable("rarity.parapet." + species.rarity().name().toLowerCase(Locale.ROOT));
        fit(gui, label("rarity", rarity), 20, 68, 124, GREEN);
        if (inside(19, 65, 145, 80) && turnStarted == 0) {
            var rates = label("encounter", species.spawnWeight(), species.chanceDenominator());
            gui.setTooltipForNextFrame(font, font.split(rates, Math.max(100, Math.min(220, width - 40))),
                px((int) pointerX), py((int) pointerY));
        }
        Component flock = species.minFlock() == species.maxFlock() ? Component.literal("" + species.minFlock())
            : label("range", species.minFlock(), species.maxFlock());
        fit(gui, label("flock", flock), 20, 81, 124, INK);
        fit(gui, label("habitat"), 20, 96, 124, MUTED);
        var biomes = Component.empty();
        for (var biome : species.biomes()) {
            if (!biomes.getSiblings().isEmpty()) biomes.append(label("separator"));
            biomes.append(Component.translatable(biome.identifier().toLanguageKey("biome")));
        }
        wrapped(gui, biomes, 20, 107, 125, 3, INK);
        if (inside(19, 106, 146, 136) && turnStarted == 0) {
            gui.setTooltipForNextFrame(font, font.split(biomes, Math.max(100, width - 40)), px((int) pointerX), py((int) pointerY));
        }
        fit(gui, label("lures"), 20, 140, 59, MUTED);
        int x = 82;
        for (var lure : species.tamingItems()) {
            var stack = lure.getDefaultInstance();
            gui.item(stack, x, 136);
            if (inside(x, 136, x + 16, 152) && turnStarted == 0) {
                gui.setTooltipForNextFrame(font, stack, px((int) pointerX), py((int) pointerY));
            }
            x += 19;
        }
    }

    private void rightPage(GuiGraphicsExtractor gui, int number) {
        var species = SPECIES[number];
        boolean known = recorded(species);
        int mark = known ? GREEN : RULE;
        gui.fill(178, 23, 183, 28, mark);
        if (!known) gui.fill(179, 24, 182, 27, 0xFFEBDBB8);
        fit(gui, label(known ? "recorded" : "unrecorded"), 187, 21, 53, known ? GREEN : MUTED);
        var length = adultLength(species);
        fit(gui, length, 245, 21, 54, INK);
        if (inside(241, 17, 301, 33) && turnStarted == 0) {
            var size = label("size_" + species.size().name().toLowerCase(Locale.ROOT));
            gui.setTooltipForNextFrame(font, label("measurement", size, length), px((int) pointerX), py((int) pointerY));
        }
        gui.fill(178, 34, 298, 35, RULE);
        if (minecraft.level != null) {
            var bird = specimen(species);
            float age = (System.nanoTime() - opened) / 50_000_000F;
            bird.draw(minecraft, gui, known, px(175), py(38), px(302), py(132), age);
            if (known && bird.count() > 1) {
                link(gui, Component.literal("<"), 174, 133, 191, 151, true);
                link(gui, Component.literal(">"), 285, 133, 302, 151, true);
                var color = Component.translatableWithFallback("gui.parapet.guide.plumage." + bird.color(),
                    bird.color().replace('_', ' '));
                centered(gui, color, 192, 132, 284, INK);
                centered(gui, label("counter", bird.index() + 1, bird.count()), 192, 145, 284, MUTED);
                if (inside(191, 130, 285, 156) && turnStarted == 0) {
                    gui.setTooltipForNextFrame(font, color, px((int) pointerX), py((int) pointerY));
                }
            } else {
                centered(gui, label(known ? "wild_type" : "unlock"), 175, 141, 302, MUTED);
            }
        }
        centered(gui, label("counter", number + 1, SPECIES.length), 198, 165, 278, MUTED);
        link(gui, Component.literal(">"), 284, 161, 302, 177, number + 1 < SPECIES.length);
    }

    private static Component adultLength(ParrotEcology species) {
        var ref = species.adultLengthCm();
        return ref.minCm() == ref.maxCm() ? label("length", ref.minCm())
            : label("length_range", ref.minCm(), ref.maxCm());
    }

    private List<ParrotEcology> indexEntries() {
        return missingOnly ? missingEntries : ALL_ENTRIES;
    }

    private void indexPage(GuiGraphicsExtractor gui) {
        var entries = indexEntries();
        int first = indexFocus / INDEX_ROWS * INDEX_ROWS;
        fit(gui, label("contents"), 20, 21, 56, GREEN);
        link(gui, label(missingOnly ? "filter_missing" : "filter_all"), 78, 16, 124, 32, true);
        if (inside(78, 16, 124, 32) && turnStarted == 0) {
            gui.setTooltipForNextFrame(font, label("filter_help"), px((int) pointerX), py((int) pointerY));
        }
        link(gui, Component.literal("×"), 127, 16, 144, 32, true);
        gui.fill(20, 34, 144, 35, RULE);
        if (entries.isEmpty()) wrapped(gui, label("all_recorded"), 20, 67, 124, 4, GREEN);
        for (int i = 0; i < INDEX_ROWS && first + i < entries.size(); i++) {
            var entry = entries.get(first + i);
            int y = 40 + i * 14;
            if (first + i == indexFocus || inside(18, y - 2, 146, y + 11)) {
                gui.fill(18, y - 2, 146, y + 10, 0x475A8058);
            }
            int mark = recorded(entry) ? GREEN : RULE;
            gui.fill(21, y + 3, 24, y + 6, mark);
            fit(gui, name(entry), 28, y, 115, INK);
        }
        link(gui, Component.literal("<"), 19, 161, 37, 177, first > 0);
        if (!entries.isEmpty()) {
            centered(gui, label("counter", first / INDEX_ROWS + 1, (entries.size() + INDEX_ROWS - 1) / INDEX_ROWS), 39, 165, 117, MUTED);
        }
        link(gui, Component.literal(">"), 124, 161, 143, 177, first + INDEX_ROWS < entries.size());
    }

    private void drawTurn(GuiGraphicsExtractor gui, float time) {
        boolean forward = target > page;
        leftPage(gui, forward ? page : target);
        rightPage(gui, forward ? target : page);
        gui.nextStratum();
        // Each band has its own projected width, edge lift and shade as the leaf rolls around the spine.
        // Both sides of the paper remain opaque, including when the outer edge points at the reader.
        double t = time * time * (3 - 2 * time);
        double angle = Math.PI * t;
        double cosine = Math.cos(angle), lift = Math.sin(angle);
        int direction = forward ? 1 : -1;
        int edge = SPINE + (int) Math.round(direction * 144 * cosine);
        int shadow = (int) Math.round(8 * lift);
        if (shadow > 0) {
            gui.fill(edge - shadow, 19, edge + shadow, 184, 0x30644934);
        }
        for (int u = 0; u < 144; u += 4) {
            double v = (u + 2) / 144.0;
            int a = leafX(u, direction, cosine, lift), b = leafX(u + 4, direction, cosine, lift);
            int x = Math.min(a, b), band = Math.max(1, Math.abs(b - a));
            int rise = (int) Math.round(12 * lift * v);
            int y = 16 - rise;
            int h = 168 + rise;
            int sourceX = (forward == (cosine >= 0)) ? u : 140 - u;
            int shade = 255 - (int) Math.round(58 * lift * (0.3 + 0.7 * v));
            int tint = 0xFF000000 | shade << 16 | shade << 8 | shade;
            gui.blit(RenderPipelines.GUI_TEXTURED, LEAF, x, y, sourceX, 0F, band, h, 4, 168, 144, 168, tint);
        }
        // The flat part still carries its original text or 3D plate. Only the curved part is foreshortened.
        int flat = (int) Math.round(143 * Math.pow(Math.abs(cosine), 2.4));
        if (flat > 1) {
            boolean right = forward == (cosine >= 0);
            gui.nextStratum();
            gui.enableScissor(right ? SPINE : SPINE - flat, 18, right ? SPINE + flat : SPINE, 182);
            if (right) rightPage(gui, forward ? page : target);
            else leftPage(gui, forward ? target : page);
            gui.disableScissor();
        }
    }

    private static int leafX(int u, int direction, double cosine, double lift) {
        double curve = 5.5 * lift * Math.sin(Math.PI * u / 144.0);
        return SPINE + (int) Math.round(direction * (u * cosine + curve));
    }

    private void wrapped(GuiGraphicsExtractor gui, Component message, int x, int y, int width, int rows, int color) {
        var lines = font.split(message, width);
        for (int i = 0; i < Math.min(rows, lines.size()); i++) {
            if (i == rows - 1 && lines.size() > rows) {
                StringBuilder last = new StringBuilder();
                lines.get(i).accept((index, style, codepoint) -> { last.appendCodePoint(codepoint); return true; });
                gui.text(font, font.plainSubstrByWidth(last.toString(), width - font.width("…")) + "…", x, y + i * 10, color, false);
            } else {
                gui.text(font, lines.get(i), x, y + i * 10, color, false);
            }
        }
    }

    private void fit(GuiGraphicsExtractor gui, Component message, int x, int y, int width, int color) {
        float factor = Math.min(1F, width / (float) Math.max(1, font.width(message)));
        if (factor < 0.78F) {
            factor = 0.78F;
            String shortText = font.plainSubstrByWidth(message.getString(), (int) (width / factor) - font.width("…"));
            message = Component.literal(shortText + "…");
        }
        gui.pose().pushMatrix();
        gui.pose().translate((float) x, (float) y).scale(factor);
        gui.text(font, message, 0, 0, color, false);
        gui.pose().popMatrix();
    }

    private void centered(GuiGraphicsExtractor gui, Component message, int left, int y, int right, int color) {
        int span = right - left;
        fit(gui, message, left + Math.max(0, (span - font.width(message)) / 2), y, span, color);
    }

    private void link(GuiGraphicsExtractor gui, Component message, int left, int top, int right, int bottom, boolean enabled) {
        if (enabled && turnStarted == 0 && inside(left, top, right, bottom)) {
            gui.fill(left, top, right, bottom, 0x455A8058);
        }
        centered(gui, message, left, (top + bottom - font.lineHeight) / 2, right, enabled ? GREEN : RULE);
    }

    private boolean inside(int left, int top, int right, int bottom) {
        return openingProgress() >= 1F && pointerX >= left && pointerX < right && pointerY >= top && pointerY < bottom;
    }

    private void turn(int to) {
        indexOpen = false;
        to = Mth.clamp(to, 0, SPECIES.length - 1);
        if (to == page || turnStarted != 0) return;
        target = to;
        turnStarted = System.nanoTime();
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1F));
    }

    private void index() {
        indexOpen = !indexOpen;
        if (indexOpen) focusOn(SPECIES[page]);
    }

    private void focusOn(ParrotEcology entry) {
        var entries = indexEntries();
        indexFocus = 0;
        while (indexFocus + 1 < entries.size() && entries.get(indexFocus).ordinal() < entry.ordinal()) indexFocus++;
    }

    private void toggleIndexFilter() {
        var entries = indexEntries();
        var selected = entries.isEmpty() ? SPECIES[page] : entries.get(indexFocus);
        missingOnly = !missingOnly;
        focusOn(selected);
    }

    private void moveIndex(int step) {
        var entries = indexEntries();
        if (!entries.isEmpty()) indexFocus = Mth.clamp(indexFocus + step, 0, entries.size() - 1);
    }

    private void openIndexEntry(int choice) {
        var entries = indexEntries();
        if (choice >= 0 && choice < entries.size()) turn(entries.get(choice).ordinal());
    }

    private void cycle(int direction) {
        if (minecraft.level != null && recorded(SPECIES[page])) specimen(SPECIES[page]).cycle(direction);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) return super.mouseClicked(event, doubleClick);
        if (openingProgress() < 1F) {
            openingSkipped = true;
            return true;
        }
        pointerX = (event.x() - originX) / scale;
        pointerY = (event.y() - originY) / scale;
        if (inside(299, 4, 316, 19)) { onClose(); return true; }
        if (turnStarted != 0) return true;
        if (indexOpen && inside(126, 16, 145, 33)) {
            indexOpen = false;
        } else if (indexOpen && inside(78, 16, 124, 32)) {
            toggleIndexFilter();
        } else if (indexOpen && inside(18, 38, 146, 150)) {
            int choice = indexFocus / INDEX_ROWS * INDEX_ROWS + ((int) pointerY - 38) / 14;
            openIndexEntry(choice);
        } else if (indexOpen && inside(19, 159, 38, 178)) {
            moveIndex(-INDEX_ROWS);
        } else if (indexOpen && inside(123, 159, 146, 178)) {
            moveIndex(INDEX_ROWS);
        } else if (!indexOpen && inside(105, 16, 145, 32)) {
            notesOpen = !notesOpen;
        } else if (!indexOpen && inside(39, 159, 122, 178)) {
            index();
        } else if (!indexOpen && inside(18, 159, 39, 178)) {
            turn(page - 1);
        } else if (inside(282, 159, 304, 178)) {
            turn(page + 1);
        } else if (inside(173, 131, 192, 153)) {
            cycle(-1);
        } else if (inside(283, 131, 304, 153)) {
            cycle(1);
        } else {
            return super.mouseClicked(event, doubleClick);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent key) {
        int code = key.key();
        if (openingProgress() < 1F) {
            openingSkipped = true;
            if (code == InputConstants.KEY_ESCAPE) { onClose(); return true; }
            if (code == InputConstants.KEY_SPACE || code == InputConstants.KEY_RETURN || code == InputConstants.KEY_NUMPADENTER) return true;
        }
        if (code == InputConstants.KEY_ESCAPE && indexOpen) { indexOpen = false; return true; }
        if (turnStarted != 0) return super.keyPressed(key);
        if (code == InputConstants.KEY_ESCAPE && notesOpen) { notesOpen = false; return true; }
        switch (code) {
            case InputConstants.KEY_HOME, InputConstants.KEY_I -> index();
            case InputConstants.KEY_F -> { if (indexOpen) toggleIndexFilter(); else return super.keyPressed(key); }
            case InputConstants.KEY_H -> { indexOpen = false; notesOpen = !notesOpen; }
            case InputConstants.KEY_LEFT, InputConstants.KEY_PAGEUP -> { if (indexOpen) moveIndex(-INDEX_ROWS); else turn(page - 1); }
            case InputConstants.KEY_RIGHT, InputConstants.KEY_PAGEDOWN -> { if (indexOpen) moveIndex(INDEX_ROWS); else turn(page + 1); }
            case InputConstants.KEY_UP -> { if (indexOpen) moveIndex(-1); else cycle(-1); }
            case InputConstants.KEY_DOWN -> { if (indexOpen) moveIndex(1); else cycle(1); }
            case InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER -> { if (indexOpen) openIndexEntry(indexFocus); else index(); }
            default -> { return super.keyPressed(key); }
        }
        return true;
    }

    @Override
    public Component getNarrationMessage() {
        if (indexOpen && indexEntries().isEmpty()) return label("all_recorded");
        var entry = indexOpen ? indexEntries().get(indexFocus) : SPECIES[page];
        if (notesOpen && !indexOpen) return label("notes_narration", name(entry), observation(entry), label("note_roost"));
        if (indexOpen) {
            return label("index_narration", label(missingOnly ? "filter_missing" : "filter_all"), name(entry), indexFocus + 1, indexEntries().size());
        }
        return label("narration", title, name(entry), label(recorded(entry) ? "recorded" : "unrecorded"),
            page + 1, SPECIES.length).copy().append(" ").append(label("adult_length", adultLength(entry)));
    }

    @Override public boolean isPauseScreen() { return false; }
}
