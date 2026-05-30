package be.nerosro.soulmark.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Shared UI rendering utilities used by both SkillTreeScreen and RadialMenuScreen.
 */
public final class UiRenderUtil {

    private UiRenderUtil() {}

    /**
     * Dims a color by multiplying RGB channels by the given factor. Alpha is preserved.
     */
    public static int dimColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Brightens a color by multiplying RGB channels by (1 + factor), clamped to 255.
     */
    public static int brightenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * (1 + factor)));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * (1 + factor)));
        int b = Math.min(255, (int) ((color & 0xFF) * (1 + factor)));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Draws a 1-pixel border rectangle (not filled).
     */
    public static void drawBorder(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int color) {
        graphics.fillGradient(left, top, right, top + 1, color, color);
        graphics.fillGradient(left, bottom - 1, right, bottom, color, color);
        graphics.fillGradient(left, top, left + 1, bottom, color, color);
        graphics.fillGradient(right - 1, top, right, bottom, color, color);
    }
}
