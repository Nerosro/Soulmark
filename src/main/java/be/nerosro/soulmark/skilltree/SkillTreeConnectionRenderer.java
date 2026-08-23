package be.nerosro.soulmark.skilltree;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Draws the parent→child connection lines between skill tree nodes.
 */
public final class SkillTreeConnectionRenderer {

    private SkillTreeConnectionRenderer() {}

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
                            ? SkillTreeScreenConstants.Colors.LINE_UNLOCKED
                            : SkillTreeScreenConstants.Colors.LINE_LOCKED;

                    int x1 = offsetX + parent.pixelX();
                    int y1 = offsetY + parent.pixelY();
                    int x2 = offsetX + entry.pixelX();
                    int y2 = offsetY + entry.pixelY();
                    int thickness = SkillTreeScreenConstants.Layout.LINE_THICKNESS;

                    if (horizontal) {
                        // L-shaped connection: horizontal then vertical then horizontal
                        int midX = (x1 + x2) / 2;
                        int topY = Math.min(y1, y2);
                        int bottomY = Math.max(y1, y2);

                        graphics.fillGradient(x1, y1 - thickness / 2, midX, y1 + thickness / 2, lineColor, lineColor);
                        graphics.fillGradient(midX - thickness / 2, topY, midX + thickness / 2, bottomY, lineColor, lineColor);
                        graphics.fillGradient(midX, y2 - thickness / 2, x2, y2 + thickness / 2, lineColor, lineColor);
                    } else {
                        // L-shaped connection: vertical then horizontal then vertical
                        int midY = (y1 + y2) / 2;
                        int leftX = Math.min(x1, x2);
                        int rightX = Math.max(x1, x2);

                        graphics.fillGradient(x1 - thickness / 2, y1, x1 + thickness / 2, midY, lineColor, lineColor);
                        graphics.fillGradient(leftX, midY - thickness / 2, rightX, midY + thickness / 2, lineColor, lineColor);
                        graphics.fillGradient(x2 - thickness / 2, midY, x2 + thickness / 2, y2, lineColor, lineColor);
                    }
                }
            }
        }
    }
}
