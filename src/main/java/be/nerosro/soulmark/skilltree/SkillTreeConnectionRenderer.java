package be.nerosro.soulmark.skilltree;

import static be.nerosro.soulmark.skilltree.SkillTreeScreenConstants.Layout;
import static be.nerosro.soulmark.skilltree.SkillTreeScreenConstants.UI_Colors;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * Draws the parent→child connection lines between skill tree nodes.
 */
public final class SkillTreeConnectionRenderer {

    private SkillTreeConnectionRenderer() {
    }

    /**
     * Draws all connection lines for the given cached node entries.
     * Locked lines are drawn first, then unlocked lines on top, so unlocked (green)
     * lines aren't overwritten by overlapping locked horizontal/vertical segments.
     */
    public static void draw(GuiGraphicsExtractor graphics, SkillTreeNodeCache cache,
                            boolean horizontal, int offsetX, int offsetY) {
        List<SkillTreeNodeCache.NodeEntry> entries = cache.entries();

        for (int pass = 0; pass < 2; pass++) {
            for (SkillTreeNodeCache.NodeEntry entry : entries) {
                if (entry.node().isRoot()) continue;

                for (Identifier pid : entry.node().parentIds()) {
                    SkillTreeNodeCache.NodeEntry parent = cache.get(pid);
                    if (parent == null) continue;

                    boolean bothUnlocked = entry.visibility() == NodeVisibility.READABLE
                        && parent.visibility() == NodeVisibility.READABLE;

                    // Pass 0 = locked lines, pass 1 = unlocked lines
                    if ((pass == 0) == bothUnlocked) continue;

                    int lineColor = bothUnlocked
                        ? UI_Colors.LINE_UNLOCKED
                        : UI_Colors.LINE_LOCKED;

                    int x1 = offsetX + parent.pixelX();
                    int y1 = offsetY + parent.pixelY();
                    int x2 = offsetX + entry.pixelX();
                    int y2 = offsetY + entry.pixelY();
                    int thickness = Layout.LINE_THICKNESS;

                    if (horizontal) {
                        // L-shaped connection: horizontal then vertical then horizontal
                        int midX = (x1 + x2) / 2;
                        int topY = Math.min(y1, y2);
                        int bottomY = Math.max(y1, y2);

                        drawHorizontalSegment(graphics, x1, midX, y1, thickness, lineColor);
                        drawVerticalSegment(graphics, midX, topY, bottomY, thickness, lineColor);
                        drawHorizontalSegment(graphics, midX, x2, y2, thickness, lineColor);
                        drawArrow(graphics, x2, y2, x2 >= x1, true, lineColor);
                    } else {
                        // L-shaped connection: vertical then horizontal then vertical
                        int midY = (y1 + y2) / 2;
                        int leftX = Math.min(x1, x2);
                        int rightX = Math.max(x1, x2);

                        drawVerticalSegment(graphics, x1, y1, midY, thickness, lineColor);
                        drawHorizontalSegment(graphics, leftX, rightX, midY, thickness, lineColor);
                        drawVerticalSegment(graphics, x2, midY, y2, thickness, lineColor);
                        drawArrow(graphics, x2, y2, y2 >= y1, false, lineColor);
                    }
                }
            }
        }
    }

    private static void drawHorizontalSegment(GuiGraphicsExtractor graphics, int x1, int x2, int y, int thickness, int color) {
        int left = Math.min(x1, x2);
        int right = Math.max(x1, x2);
        graphics.fillGradient(left, y - thickness / 2 - 1, right, y + thickness / 2 + 1,
            UI_Colors.LINE_SHADOW, UI_Colors.LINE_SHADOW);
        graphics.fillGradient(left, y - thickness / 2, right, y + thickness / 2, color, color);
    }

    private static void drawVerticalSegment(GuiGraphicsExtractor graphics, int x, int y1, int y2, int thickness, int color) {
        int top = Math.min(y1, y2);
        int bottom = Math.max(y1, y2);
        graphics.fillGradient(x - thickness / 2 - 1, top, x + thickness / 2 + 1, bottom,
            UI_Colors.LINE_SHADOW, UI_Colors.LINE_SHADOW);
        graphics.fillGradient(x - thickness / 2, top, x + thickness / 2, bottom, color, color);
    }

    private static void drawArrow(GuiGraphicsExtractor graphics, int x, int y, boolean forward, boolean horizontal, int color) {
        int direction = forward ? 1 : -1;
        if (horizontal) {
            graphics.fillGradient(x - direction * 6, y - 4, x, y + 4, color, color);
            return;
        }
        graphics.fillGradient(x - 4, y - direction * 6, x + 4, y, color, color);
    }
}
