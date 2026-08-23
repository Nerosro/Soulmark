package be.nerosro.soulmark.skilltree;

import be.nerosro.soulmark.ui.UiRenderUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Draws individual skill tree nodes: background, frame, type indicator/icon, and label.
 */
public final class SkillTreeNodeRenderer {

    private static final Identifier SOUL_POINT_GATE_ICON =
            Identifier.fromNamespaceAndPath("soulmark", "textures/gui/skills/soul-gate.png");

    private SkillTreeNodeRenderer() {}

    /**
     * Draws all nodes in the cache and returns the hovered node ID (if any).
     */
    public static @Nullable Identifier draw(GuiGraphicsExtractor graphics, Font font, SkillTreeNodeCache cache,
                                             int mouseX, int mouseY, int offsetX, int offsetY) {
        Identifier hovered = null;
        int half = SkillTreeScreenConstants.Layout.NODE_HALF;

        for (SkillTreeNodeCache.NodeEntry entry : cache.entries()) {
            int cx = offsetX + entry.pixelX();
            int cy = offsetY + entry.pixelY();
            int left = cx - half;
            int top = cy - half;
            int right = cx + half;
            int bottom = cy + half;

            boolean isHovered = mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
            if (isHovered) hovered = entry.nodeId();

            int bgColor = switch (entry.visibility()) {
                case READABLE -> SkillTreeScreenConstants.Colors.UNLOCKED_BG;
                case UNLOCKABLE -> entry.excluded() ? SkillTreeScreenConstants.Colors.EXCLUDED_BG : SkillTreeScreenConstants.Colors.SCRAMBLED_BG;
                case SCRAMBLED -> SkillTreeScreenConstants.Colors.SCRAMBLED_BG;
                case TEASED -> SkillTreeScreenConstants.Colors.TEASED_BG;
                default -> 0xFF000000;
            };

            if (isHovered && entry.visibility() == NodeVisibility.UNLOCKABLE && !entry.excluded()) {
                bgColor = 0xFF2A2A44;
            }

            if (!entry.node().soulGate()) {
                graphics.fillGradient(left, top, right, bottom, bgColor, bgColor);
            }

            if (!entry.node().soulGate()) {
                int frameColor = getNodeTypeColor(entry.node().nodeType());
                if (entry.visibility() == NodeVisibility.READABLE) {
                    drawNodeFrame(graphics, left, top, right, bottom, frameColor);
                } else if (entry.visibility() == NodeVisibility.UNLOCKABLE) {
                    int dimmed = UiRenderUtil.dimColor(frameColor, entry.excluded() ? 0.3f : 0.7f);
                    drawNodeFrame(graphics, left, top, right, bottom, dimmed);
                } else if (entry.visibility() == NodeVisibility.SCRAMBLED) {
                    drawNodeFrame(graphics, left, top, right, bottom, UiRenderUtil.dimColor(frameColor, 0.4f));
                } else {
                    drawNodeFrame(graphics, left, top, right, bottom, UiRenderUtil.dimColor(frameColor, 0.2f));
                }
            }

            if (entry.node().soulGate()
                    && (entry.visibility() == NodeVisibility.READABLE || entry.visibility() == NodeVisibility.UNLOCKABLE)) {
                drawNodeIcon(graphics, cx, cy, SOUL_POINT_GATE_ICON, SkillTreeScreenConstants.Layout.NODE_SIZE);
            } else if (!entry.node().soulGate() && entry.node().icon() != null
                    && (entry.visibility() == NodeVisibility.READABLE || entry.visibility() == NodeVisibility.UNLOCKABLE)) {
                drawNodeIcon(graphics, cx, cy, entry.node().icon(), 16);
            } else if (!entry.node().soulGate()) {
                drawNodeTypeIndicator(graphics, cx, cy, entry.node().nodeType(), entry.visibility());
            }

            drawNodeLabel(graphics, font, cx, bottom + 2, entry);
        }

        return hovered;
    }

    private static void drawNodeFrame(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int color) {
        UiRenderUtil.drawBorder(graphics, left, top, right, bottom, color);
    }

    private static void drawNodeTypeIndicator(GuiGraphicsExtractor graphics, int cx, int cy,
                                               NodeType type, NodeVisibility visibility) {
        if (visibility == NodeVisibility.TEASED) return;

        int color = getNodeTypeColor(type);
        if (visibility == NodeVisibility.SCRAMBLED) {
            color = UiRenderUtil.dimColor(color, 0.4f);
        } else if (visibility == NodeVisibility.UNLOCKABLE) {
            color = UiRenderUtil.dimColor(color, 0.7f);
        }

        int size = 4;
        switch (type) {
            // Small filled square (circle approximation)
            case PASSIVE -> graphics.fillGradient(cx - size, cy - size, cx + size, cy + size, color, color);
            case ABILITY -> {
                // Diamond shape
                graphics.fillGradient(cx - 1, cy - size, cx + 1, cy - size + 2, color, color);
                graphics.fillGradient(cx - size, cy - 1, cx + size, cy + 1, color, color);
                graphics.fillGradient(cx - 1, cy + size - 2, cx + 1, cy + size, color, color);
            }
            // Small square outline
            case UTILITY -> drawNodeFrame(graphics, cx - size, cy - size, cx + size, cy + size, color);
            case SPECIALIZATION -> {
                // Filled diamond
                for (int i = 0; i <= size; i++) {
                    graphics.fillGradient(cx - i, cy - (size - i), cx + i + 1, cy - (size - i) + 1, color, color);
                    graphics.fillGradient(cx - i, cy + (size - i) - 1, cx + i + 1, cy + (size - i), color, color);
                }
            }
            case CAPSTONE -> {
                // Star-like cross pattern
                graphics.fillGradient(cx - 1, cy - size, cx + 1, cy + size, color, color);
                graphics.fillGradient(cx - size, cy - 1, cx + size, cy + 1, color, color);
                for (int i = -size / 2; i <= size / 2; i++) {
                    graphics.fillGradient(cx + i, cy + i, cx + i + 1, cy + i + 1, color, color);
                    graphics.fillGradient(cx + i, cy - i, cx + i + 1, cy - i + 1, color, color);
                }
            }
            case RITUAL -> {
                // Circle approximation (octagon)
                graphics.fillGradient(cx - size + 1, cy - size, cx + size - 1, cy - size + 1, color, color);
                graphics.fillGradient(cx - size, cy - size + 1, cx + size, cy + size - 1, color, color);
                graphics.fillGradient(cx - size + 1, cy + size - 1, cx + size - 1, cy + size, color, color);
            }
            default -> {
                // RECIPE / DISCOVERY: no distinct indicator shape; RECIPE uses icon in practice,
                // DISCOVERY is never rendered (always INVISIBLE).
            }
        }
    }

    private static void drawNodeIcon(GuiGraphicsExtractor graphics, int cx, int cy, Identifier icon, int iconSize) {
        int halfIcon = iconSize / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon,
                cx - halfIcon, cy - halfIcon, 0, 0, iconSize, iconSize, iconSize, iconSize);
    }

    private static void drawNodeLabel(GuiGraphicsExtractor graphics, Font font, int cx, int y, SkillTreeNodeCache.NodeEntry entry) {
        if (entry.visibility() == NodeVisibility.READABLE) {
            graphics.centeredText(font, Component.literal(entry.node().name()),
                    cx, y, SkillTreeScreenConstants.Colors.TEXT_READABLE);
        } else if (entry.visibility() == NodeVisibility.UNLOCKABLE) {
            // Show real name but in a dimmer/different color to indicate not yet unlocked
            int color = entry.excluded() ? 0xFF994444 : 0xFFCCCC88;
            graphics.centeredText(font, Component.literal(entry.node().name()),
                    cx, y, color);
        } else if (entry.visibility() == NodeVisibility.SCRAMBLED) {
            String scrambled = scrambleText(entry.node().name());
            graphics.centeredText(font, Component.literal(scrambled),
                    cx, y, SkillTreeScreenConstants.Colors.TEXT_SCRAMBLED);
        }
        // TEASED: no label shown
    }

    static int getNodeTypeColor(NodeType type) {
        return switch (type) {
            case PASSIVE -> SkillTreeScreenConstants.Colors.PASSIVE;
            case ABILITY -> SkillTreeScreenConstants.Colors.ABILITY;
            case UTILITY -> SkillTreeScreenConstants.Colors.UTILITY;
            case RECIPE -> SkillTreeScreenConstants.Colors.RECIPE;
            case RITUAL -> SkillTreeScreenConstants.Colors.RITUAL;
            case SPECIALIZATION -> SkillTreeScreenConstants.Colors.SPECIALIZATION;
            case CAPSTONE -> SkillTreeScreenConstants.Colors.CAPSTONE;
            case DISCOVERY -> SkillTreeScreenConstants.Colors.UTILITY; // Never rendered, but required for exhaustive switch
        };
    }

    /**
     * Scrambles a name's letters into a deterministic pseudo-random string, used to render
     * SCRAMBLED-visibility node labels. Shared with {@link SkillTreeTooltipRenderer}.
     */
    static String scrambleText(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != ' ') {
                chars[i] = (char) ('a' + (chars[i] * 7 + i * 13) % 26);
            }
        }
        return new String(chars);
    }
}
