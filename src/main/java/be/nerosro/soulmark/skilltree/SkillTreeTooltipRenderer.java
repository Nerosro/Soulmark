package be.nerosro.soulmark.skilltree;

import be.nerosro.soulmark.network.ClientSkillTreeQuery;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the hover tooltip for a skill tree node, describing its unlock state.
 */
public final class SkillTreeTooltipRenderer {

    private SkillTreeTooltipRenderer() {}

    public static void draw(GuiGraphicsExtractor graphics, Font font, SkillTreeNodeCache cache,
                             int screenWidth, int screenHeight, int mouseX, int mouseY, Identifier nodeId) {
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        if (node == null) return;

        NodeVisibility visibility = SkillTreeUtil.getVisibility(ClientSkillTreeQuery.INSTANCE, nodeId);
        List<Component> lines = new ArrayList<>();

        if (visibility == NodeVisibility.READABLE) {
            lines.add(Component.literal(node.name()));
            if (!node.description().isEmpty()) {
                lines.add(Component.literal(node.description()).withStyle(ChatFormatting.GRAY));
            }
            lines.add(Component.literal("Type: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(node.nodeType().displayName()).withStyle(ChatFormatting.WHITE)));
            lines.add(Component.literal("Unlocked ✓").withStyle(ChatFormatting.GREEN));
        } else if (visibility == NodeVisibility.UNLOCKABLE) {
            SkillTreeNodeCache.NodeEntry entry = cache.get(nodeId);
            boolean excluded = entry != null && entry.excluded();
            if (excluded) {
                lines.add(Component.literal(node.name()).withStyle(ChatFormatting.RED));
                lines.add(Component.literal("Locked out (another specialization chosen)").withStyle(ChatFormatting.DARK_RED));
            } else {
                lines.add(Component.literal(node.name()).withStyle(ChatFormatting.YELLOW));
                if (!node.description().isEmpty()) {
                    lines.add(Component.literal(node.description()).withStyle(ChatFormatting.GRAY));
                }
                SkillTreePayment payment = SkillTreeUtil.getPayment(node);
                if (payment == null) {
                    lines.add(Component.literal("Payment unavailable").withStyle(ChatFormatting.RED));
                } else {
                    ChatFormatting costColor = node.soulGate() ? ChatFormatting.RED : ChatFormatting.YELLOW;
                    lines.add(Component.literal("Cost: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(node.cost() + " " + payment.displayName()).withStyle(costColor)));
                    if (payment.getClientAvailableBalance() >= node.cost()) {
                        lines.add(Component.literal("Click to unlock").withStyle(ChatFormatting.GREEN));
                    } else {
                        lines.add(Component.literal("Not enough " + payment.displayName()).withStyle(ChatFormatting.RED));
                    }
                }
                lines.add(Component.literal("Type: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(node.nodeType().displayName()).withStyle(ChatFormatting.WHITE)));
            }
        } else if (visibility == NodeVisibility.SCRAMBLED) {
            lines.add(Component.literal(SkillTreeNodeRenderer.scrambleText(node.name())).withStyle(ChatFormatting.DARK_GRAY));
            lines.add(Component.literal("???").withStyle(ChatFormatting.DARK_GRAY));
        } else if (visibility == NodeVisibility.TEASED) {
            lines.add(Component.literal("???").withStyle(ChatFormatting.DARK_GRAY));
            lines.add(Component.literal("Unlock nearby nodes first").withStyle(ChatFormatting.DARK_GRAY));
        }

        if (lines.isEmpty()) return;

        int maxWidth = 0;
        for (Component line : lines) {
            maxWidth = Math.max(maxWidth, font.width(line));
        }
        int padding = SkillTreeScreenConstants.Layout.TOOLTIP_PADDING;
        int lineHeight = SkillTreeScreenConstants.Layout.TOOLTIP_LINE_HEIGHT;
        int tooltipWidth = maxWidth + padding * 2;
        int tooltipHeight = lines.size() * lineHeight + padding * 2;

        int tx = mouseX + 12;
        int ty = mouseY - 12;
        if (tx + tooltipWidth > screenWidth) tx = mouseX - tooltipWidth - 4;
        if (ty + tooltipHeight > screenHeight) ty = screenHeight - tooltipHeight;
        if (ty < 0) ty = 0;

        int bg = SkillTreeScreenConstants.Colors.TOOLTIP_BG;
        int borderTop = SkillTreeScreenConstants.Colors.TOOLTIP_BORDER_TOP;
        int borderBottom = SkillTreeScreenConstants.Colors.TOOLTIP_BORDER_BOTTOM;

        graphics.fillGradient(tx, ty, tx + tooltipWidth, ty + tooltipHeight, bg, bg);
        graphics.fillGradient(tx, ty, tx + tooltipWidth, ty + 1, borderTop, borderTop);
        graphics.fillGradient(tx, ty + tooltipHeight - 1, tx + tooltipWidth, ty + tooltipHeight, borderBottom, borderBottom);
        graphics.fillGradient(tx, ty, tx + 1, ty + tooltipHeight, borderTop, borderBottom);
        graphics.fillGradient(tx + tooltipWidth - 1, ty, tx + tooltipWidth, ty + tooltipHeight, borderTop, borderBottom);

        for (int i = 0; i < lines.size(); i++) {
            graphics.text(font, lines.get(i),
                    tx + padding, ty + padding + i * lineHeight, SkillTreeScreenConstants.Colors.TEXT_READABLE);
        }
    }
}
