package be.nerosro.soulmark.skilltree;

import static be.nerosro.soulmark.skilltree.NodeVisibility.READABLE;
import static be.nerosro.soulmark.skilltree.NodeVisibility.SCRAMBLED;
import static be.nerosro.soulmark.skilltree.NodeVisibility.TEASED;
import static be.nerosro.soulmark.skilltree.NodeVisibility.UNLOCKABLE;
import static be.nerosro.soulmark.skilltree.SkillTreeScreenConstants.Layout;
import static be.nerosro.soulmark.skilltree.SkillTreeScreenConstants.UI_Colors;

import org.jspecify.annotations.Nullable;

import be.nerosro.soulmark.ui.UiRenderUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/**
 * Draws individual skill tree nodes: background, frame, type indicator/icon, and label.
 */
public final class SkillTreeNodeRenderer {

    private static final Identifier SOUL_POINT_GATE_ICON =
        Identifier.fromNamespaceAndPath("soulmark", "textures/gui/skills/soul-gate.png");
    private static final FontDescription SCRAMBLED_FONT =
        new FontDescription.Resource(Identifier.withDefaultNamespace("alt"));

    private SkillTreeNodeRenderer() {
    }

    /**
     * Draws all nodes in the cache and returns the hovered node ID (if any).
     */
    public static @Nullable Identifier draw(GuiGraphicsExtractor graphics, Font font, SkillTreeNodeCache cache,
                                            int mouseX, int mouseY, int offsetX, int offsetY) {
        Identifier hovered = null;
        int half = Layout.NODE_HALF;

        for (SkillTreeNodeCache.NodeEntry entry : cache.entries()) {
            int cx = offsetX + entry.pixelX();
            int cy = offsetY + entry.pixelY();
            int left = cx - half;
            int top = cy - half;
            int right = cx + half;
            int bottom = cy + half;

            boolean isHovered = mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
            if (isHovered) hovered = entry.nodeId();

            BackgroundColors background = getBackgroundColors(entry);

            if (!entry.node().soulGate()) {
                graphics.fillGradient(left + 2, top + 2, right - 2, bottom - 2, background.top(), background.bottom());
            }

            if (!entry.node().soulGate()) {
                int frameColor = getNodeTypeColor(entry.node().nodeType());
                if (entry.visibility() == READABLE) {
                    drawNodeFrame(graphics, left, top, right, bottom, frameColor, isHovered);
                } else if (entry.visibility() == UNLOCKABLE) {
                    int dimmed = UiRenderUtil.dimColor(frameColor, entry.excluded() ? 0.3f : 0.7f);
                    drawNodeFrame(graphics, left, top, right, bottom, dimmed, isHovered && !entry.excluded());
                } else if (entry.visibility() == SCRAMBLED) {
                    drawNodeFrame(graphics, left, top, right, bottom, UiRenderUtil.dimColor(frameColor, 0.4f), false);
                } else {
                    drawNodeFrame(graphics, left, top, right, bottom, UiRenderUtil.dimColor(frameColor, 0.2f), false);
                }
            }

            if (entry.node().soulGate()
                && (entry.visibility() == READABLE || entry.visibility() == UNLOCKABLE)) {
                drawNodeIcon(graphics, cx, cy, SOUL_POINT_GATE_ICON, Layout.NODE_SIZE);
            } else if (!entry.node().soulGate() && entry.node().icon() != null
                && (entry.visibility() == READABLE || entry.visibility() == UNLOCKABLE)) {
                drawNodeIcon(graphics, cx, cy, entry.node().icon(), 20);
            } else if (!entry.node().soulGate()) {
                drawNodeTypeIndicator(graphics, cx, cy, entry.node().nodeType(), entry.visibility());
            }

            drawNodeLabel(graphics, font, cx, bottom + Layout.LABEL_OFFSET_Y, entry);
        }

        return hovered;
    }

    private record BackgroundColors(int top, int bottom) {
    }

    private static BackgroundColors getBackgroundColors(SkillTreeNodeCache.NodeEntry entry) {
        return switch (entry.visibility()) {
            case READABLE -> new BackgroundColors(UI_Colors.UNLOCKED_BG_TOP, UI_Colors.UNLOCKED_BG_BOTTOM);
            case UNLOCKABLE -> entry.excluded()
                ? new BackgroundColors(UI_Colors.EXCLUDED_BG_TOP, UI_Colors.EXCLUDED_BG_BOTTOM)
                : new BackgroundColors(UI_Colors.UNLOCKABLE_BG_TOP, UI_Colors.UNLOCKABLE_BG_BOTTOM);
            case SCRAMBLED -> new BackgroundColors(UI_Colors.SCRAMBLED_BG_TOP, UI_Colors.SCRAMBLED_BG_BOTTOM);
            case TEASED -> new BackgroundColors(UI_Colors.TEASED_BG_TOP, UI_Colors.TEASED_BG_BOTTOM);
            default -> new BackgroundColors(0xFF000000, 0xFF000000);
        };
    }

    private static void drawNodeFrame(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int color, boolean highlighted) {
        graphics.fillGradient(left + 1, top + 1, right + 1, bottom + 1,
            UI_Colors.NODE_SHADOW, UI_Colors.NODE_SHADOW);
        UiRenderUtil.drawBorder(graphics, left, top, right, bottom, color);
        UiRenderUtil.drawBorder(graphics, left + 2, top + 2, right - 2, bottom - 2,
            highlighted ? UiRenderUtil.brightenColor(color, 0.35f) : UI_Colors.NODE_INNER_BORDER);
    }

    private static void drawNodeTypeIndicator(GuiGraphicsExtractor graphics, int cx, int cy,
                                              NodeType type, NodeVisibility visibility) {
        if (visibility == TEASED) return;

        int color = getNodeTypeColor(type);
        if (visibility == SCRAMBLED) {
            color = UiRenderUtil.dimColor(color, 0.4f);
        } else if (visibility == UNLOCKABLE) {
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
            case UTILITY -> drawNodeFrame(graphics, cx - size, cy - size, cx + size, cy + size, color, false);
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
        Component label;
        int color;
        if (entry.visibility() == READABLE) {
            label = Component.literal(entry.node().name());
            color = UI_Colors.TEXT_UNLOCKED;
        } else if (entry.visibility() == UNLOCKABLE) {
            // Show available nodes in white; excluded nodes remain red.
            label = Component.literal(entry.node().name());
            color = entry.excluded() ? 0xFF994444 : UI_Colors.TEXT_UNLOCKABLE;
        } else if (entry.visibility() == SCRAMBLED) {
            label = scrambledLabel(entry.node().name());
            color = UI_Colors.TEXT_SCRAMBLED;
        } else {
            return;
        }

        int paddingX = Layout.LABEL_NAMEPLATE_PADDING_X;
        int paddingY = Layout.LABEL_NAMEPLATE_PADDING_Y;
        int labelWidth = font.width(label);
        int left = cx - labelWidth / 2 - paddingX;
        int right = cx + (labelWidth + 1) / 2 + paddingX;
        int top = y - paddingY;
        int bottom = y + font.lineHeight + paddingY;
        graphics.fillGradient(left, top, right, bottom,
            UI_Colors.LABEL_NAMEPLATE_FILL_TOP,
            UI_Colors.LABEL_NAMEPLATE_FILL_BOTTOM);
        graphics.fillGradient(left, top, right, top + 1,
            UI_Colors.LABEL_NAMEPLATE_BORDER_TOP,
            UI_Colors.LABEL_NAMEPLATE_BORDER_TOP);
        graphics.fillGradient(left, bottom - 1, right, bottom,
            UI_Colors.LABEL_NAMEPLATE_BORDER_BOTTOM,
            UI_Colors.LABEL_NAMEPLATE_BORDER_BOTTOM);
        graphics.fillGradient(left, top + 1, left + 1, bottom - 1,
            UI_Colors.LABEL_NAMEPLATE_BORDER_TOP,
            UI_Colors.LABEL_NAMEPLATE_BORDER_BOTTOM);
        graphics.fillGradient(right - 1, top + 1, right, bottom - 1,
            UI_Colors.LABEL_NAMEPLATE_BORDER_TOP,
            UI_Colors.LABEL_NAMEPLATE_BORDER_BOTTOM);
        graphics.centeredText(font, label, cx, y, color);
    }

    static int getNodeTypeColor(NodeType type) {
        return switch (type) {
            case PASSIVE -> UI_Colors.PASSIVE;
            case ABILITY -> UI_Colors.ABILITY;
            case UTILITY -> UI_Colors.UTILITY;
            case RECIPE -> UI_Colors.RECIPE;
            case RITUAL -> UI_Colors.RITUAL;
            case SPECIALIZATION -> UI_Colors.SPECIALIZATION;
            case CAPSTONE -> UI_Colors.CAPSTONE;
            case DISCOVERY -> UI_Colors.UTILITY; // Never rendered, but required for exhaustive switch
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

    static Component scrambledLabel(String text) {
        return Component.literal(scrambleText(text)).withStyle(Style.EMPTY.withFont(SCRAMBLED_FONT));
    }

    static Component scrambledTooltipLabel(String text) {
        return Component.literal(scrambleText(text))
            .withStyle(Style.EMPTY.withFont(SCRAMBLED_FONT))
            .withStyle(net.minecraft.ChatFormatting.DARK_GRAY);
    }
}
