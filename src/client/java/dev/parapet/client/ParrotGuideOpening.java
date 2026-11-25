package dev.parapet.client;

import dev.parapet.Parapet;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Closed cover, hinged front board and following flyleaf in the guide's native 320 × 200 coordinates. */
public final class ParrotGuideOpening {
    public static final long DURATION_NANOS = 580_000_000L;
    private static final int SPINE = 160, HEIGHT = 200;
    private static final int PAPER_WIDTH = 152, PAPER_TOP = 8, PAPER_HEIGHT = 174;
    private static final int BAND = 4;
    private static final Identifier BOOK = Parapet.id("textures/gui/field_guide.png");
    private static final Identifier COVER = Parapet.id("textures/gui/field_guide_cover.png");
    private static final Identifier LEAF = Parapet.id("textures/gui/page_leaf.png");

    private ParrotGuideOpening() {}

    /**
     * The caller has already positioned the book. Each callback draws the corresponding page's
     * untransformed contents, including the normal inventory-entity preview on the right.
     */
    public static void draw(GuiGraphicsExtractor gui, float progress, Runnable drawLeftPage, Runnable drawRightPage) {
        float t = Mth.clamp(progress, 0F, 1F);
        if (t >= 1F) {
            gui.blit(RenderPipelines.GUI_TEXTURED, BOOK, 0, 0, 0F, 0F, 320, HEIGHT, 320, HEIGHT);
            drawLeftPage.run();
            drawRightPage.run();
            return;
        }

        // The shut volume starts centred. Its stationary back board reaches its reading position
        // while the right-hand sheet is still flat, before any live entity preview is visible.
        int hinge = 80 + Math.round(80 * ease(t, 0.055F, 0.40F));
        float boardAngle = Mth.PI * ease(t, 0.035F, 0.76F);
        float leafAngle = Mth.PI * ease(t, 0.41F, 1F);
        gui.blit(RenderPipelines.GUI_TEXTURED, BOOK, hinge, 0, SPINE, 0F, SPINE, HEIGHT, 320, HEIGHT);

        if (hinge == SPINE && t > 0.41F) {
            drawRightPage.run();
        }
        gui.nextStratum();
        if (boardAngle < Mth.HALF_PI) {
            drawLeaf(gui, hinge, leafAngle, drawLeftPage);
            gui.nextStratum();
            drawBoard(gui, hinge, boardAngle);
        } else {
            drawBoard(gui, hinge, boardAngle);
            gui.nextStratum();
            drawLeaf(gui, hinge, leafAngle, drawLeftPage);
        }
    }

    private static void drawBoard(GuiGraphicsExtractor gui, int hinge, float angle) {
        float width = (float) Math.cos(angle);
        float lift = (float) Math.sin(angle);
        int edge = hinge + Math.round(SPINE * width);
        if (width > 0F) {
            shadow(gui, edge, 5, 189, lift, hinge + SPINE - 3);
        }
        if (Math.abs(width) < 0.009F) {
            gui.fill(hinge - 1, 5, hinge + 1, 191, 0xFF8B714E);
            return;
        }

        for (int u = 0; u < SPINE; u += BAND) {
            int a = hinge + Math.round(u * width), b = hinge + Math.round((u + BAND) * width);
            if (a == b) continue;
            float radius = (u + 0.5F * BAND) / SPINE;
            int tilt = Math.round(7F * lift * radius);
            int shade = 255 - Math.round((23F + 40F * radius) * lift);
            int sourceX = width > 0F ? u : SPINE - u - BAND;
            // The reverse side uses the exact printed left page and spine of the fully opened book.
            gui.blit(RenderPipelines.GUI_TEXTURED, width > 0F ? COVER : BOOK,
                Math.min(a, b), tilt, sourceX, 0F, Math.abs(b - a), HEIGHT - 2 * tilt,
                BAND, HEIGHT, width > 0F ? SPINE : 320, HEIGHT, grey(shade));
        }
    }

    private static void drawLeaf(GuiGraphicsExtractor gui, int hinge, float angle, Runnable drawLeftPage) {
        float width = (float) Math.cos(angle);
        float lift = (float) Math.sin(angle);
        int edge = hinge + Math.round(PAPER_WIDTH * width);
        shadow(gui, edge, PAPER_TOP + 3, PAPER_TOP + PAPER_HEIGHT - 2, lift, hinge + PAPER_WIDTH);
        for (int u = 0; u < PAPER_WIDTH; u += BAND) {
            int a = leafX(hinge, u, width, lift);
            int b = leafX(hinge, u + BAND, width, lift);
            if (a == b) continue;
            float radius = (u + BAND * 0.5F) / PAPER_WIDTH;
            int rise = Math.round(lift * (5F * radius + 4F * (float) Math.sin(Math.PI * radius)));
            int shade = 255 - Math.round(lift * (12F + 38F * radius));
            int x = Math.min(a, b);
            if (width >= 0F) {
                // Scale the existing 144 × 168 flyleaf to the board's paper bounds.
                int s0 = Math.round(144F * u / PAPER_WIDTH);
                int s1 = Math.round(144F * (u + BAND) / PAPER_WIDTH);
                gui.blit(RenderPipelines.GUI_TEXTURED, LEAF, x, PAPER_TOP - rise, s0, 0F,
                    Math.abs(a - b), PAPER_HEIGHT, s1 - s0, 168, 144, 168, grey(shade));
            } else {
                gui.blit(RenderPipelines.GUI_TEXTURED, BOOK, x, PAPER_TOP - rise, SPINE - u - BAND, (float) PAPER_TOP,
                    Math.abs(a - b), PAPER_HEIGHT, BAND, PAPER_HEIGHT, 320, HEIGHT, grey(shade));
            }
        }
        // The inner edge settles first. Printed text remains at its native proportions and is revealed
        // from the spine toward the edge; the emerging right-page model never needs a second transform.
        if (width < 0F) {
            int flat = Math.round(PAPER_WIDTH * width * width);
            if (flat > 0) {
                gui.nextStratum();
                gui.enableScissor(SPINE - flat, PAPER_TOP, SPINE, PAPER_TOP + PAPER_HEIGHT);
                drawLeftPage.run();
                gui.disableScissor();
            }
        }
    }

    private static int leafX(int hinge, int u, float width, float lift) {
        double bend = 6F * lift * Math.sin(Math.PI * u / PAPER_WIDTH);
        return hinge + (int) Math.round(width * u + bend);
    }

    private static void shadow(GuiGraphicsExtractor gui, int edge, int top, int bottom, float lift, int limit) {
        int depth = Math.round(lift * 10F);
        if (depth == 0) return;
        for (int offset = 0; offset < depth && edge + offset < limit; offset++) {
            int alpha = Math.round(42F * lift * (1F - (float) offset / depth));
            gui.fill(edge + offset, top, edge + offset + 1, bottom, alpha << 24 | 0x493520);
        }
    }

    private static float ease(float progress, float start, float end) {
        float t = Mth.clamp((progress - start) / (end - start), 0F, 1F);
        return t * t * (3F - 2F * t);
    }

    private static int grey(int value) {
        return 0xFF000000 | value << 16 | value << 8 | value;
    }
}
