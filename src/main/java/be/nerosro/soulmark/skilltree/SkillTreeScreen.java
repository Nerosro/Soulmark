package be.nerosro.soulmark.skilltree;

import be.nerosro.soulmark.network.ClientSkillTreeData;
import be.nerosro.soulmark.network.SkillNodeUnlockPayload;
import be.nerosro.soulmark.ui.UiRenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * The skill tree screen. Shows all registered trees as tabs/pages.
 * Player can swap between pages freely once the screen is open.
 * The starting page is determined by the held item when the key was pressed.
 */
public class SkillTreeScreen extends Screen {

    // ── Layout constants ────────────────────────────────────────────────────
    private static final int NODE_SIZE = 24;
    private static final int NODE_HALF = NODE_SIZE / 2;
    private static final int GRID_SPACING_X = 70;
    private static final int GRID_SPACING_Y = 70;
    private static final int HEADER_HEIGHT = 30;
    private static final int TAB_HEIGHT = 20;
    private static final int TAB_WIDTH = 80;
    private static final int TAB_GAP = 2;
    private static final int TOOLTIP_PADDING = 6;
    private static final int LINE_THICKNESS = 2;

    // ── Node type colors (frame tint) ───────────────────────────────────────
    private static final int COLOR_PASSIVE = 0xFF888888;       // gray
    private static final int COLOR_ABILITY = 0xFF4488FF;       // blue
    private static final int COLOR_UTILITY = 0xFF44BB44;       // green
    private static final int COLOR_SPECIALIZATION = 0xFFFFAA00; // gold
    private static final int COLOR_CAPSTONE = 0xFFFF4444;       // red

    // ── Visibility colors ───────────────────────────────────────────────────
    private static final int COLOR_UNLOCKED_BG = 0xFF2A2A3A;
    private static final int COLOR_SCRAMBLED_BG = 0xFF1A1A2A;
    private static final int COLOR_TEASED_BG = 0xFF101018;
    private static final int COLOR_EXCLUDED_BG = 0xFF3A1010;
    private static final int COLOR_LINE_UNLOCKED = 0xFFAAFFAA;
    private static final int COLOR_LINE_LOCKED = 0xFF444466;
    private static final int COLOR_TEXT_READABLE = 0xFFFFFFFF;
    private static final int COLOR_TEXT_SCRAMBLED = 0xFF888888;
    private static final int COLOR_POINTS_LABEL = 0xFFAAFFAA;

    // ── State ───────────────────────────────────────────────────────────────
    private final List<Identifier> treeIds = new ArrayList<>();
    private int currentPageIndex;

    // Scroll/pan offset for the tree view
    private double scrollX = 0;
    private double scrollY = 0;
    private boolean dragging = false;
    private double dragStartX, dragStartY;

    // Cache of nodes per tree, computed once per page switch
    private List<NodeEntry> currentNodes = new ArrayList<>();
    private Map<Identifier, NodeEntry> nodeMap = new HashMap<>();

    // Hovered node for tooltip
    private @Nullable Identifier hoveredNodeId;

    public SkillTreeScreen(Identifier startingTreeId) {
        super(Component.literal("Skill Tree"));

        // Gather all registered trees
        for (Map.Entry<ResourceKey<SkillTree>, SkillTree> entry : SkillTreeRegistries.TREE_REGISTRY.entrySet()) {
            treeIds.add(entry.getKey().identifier());
        }

        // Find starting page
        currentPageIndex = Math.max(0, treeIds.indexOf(startingTreeId));
    }

    @Override
    protected void init() {
        super.init();
        rebuildNodeCache();
        ClientSkillTreeData.setChangeListener(this::rebuildNodeCache);
    }

    @Override
    public void removed() {
        super.removed();
        ClientSkillTreeData.clearChangeListener();
    }

    // ── Page switching ──────────────────────────────────────────────────────

    private void switchToPage(int index) {
        if (index < 0 || index >= treeIds.size()) return;
        currentPageIndex = index;
        scrollX = 0;
        scrollY = 0;
        rebuildNodeCache();
    }

    private void rebuildNodeCache() {
        currentNodes.clear();
        nodeMap.clear();
        if (treeIds.isEmpty()) return;

        Identifier treeId = treeIds.get(currentPageIndex);
        SkillTree tree = SkillTreeRegistries.TREE_REGISTRY.getValue(treeId);
        boolean horizontal = tree != null && tree.direction() == LayoutDirection.LEFT_RIGHT;

        List<Identifier> allNodes = SkillTreeUtil.getAllNodesInTree(treeId);

        for (Identifier nodeId : allNodes) {
            SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
            if (node == null) continue;

            // Compute visibility from client cache
            NodeVisibility visibility = computeClientVisibility(nodeId, node);
            if (visibility == NodeVisibility.INVISIBLE) continue;

            // Compute pixel position from grid coordinates, swapping axes for LEFT_RIGHT
            int pixelX;
            int pixelY;
            if (horizontal) {
                pixelX = node.gridY() * GRID_SPACING_X;
                pixelY = node.gridX() * GRID_SPACING_Y;
            } else {
                pixelX = node.gridX() * GRID_SPACING_X;
                pixelY = node.gridY() * GRID_SPACING_Y;
            }

            boolean excluded = isClientExcluded(nodeId);
            NodeEntry nodeEntry = new NodeEntry(nodeId, node, pixelX, pixelY, visibility, excluded);
            currentNodes.add(nodeEntry);
            nodeMap.put(nodeId, nodeEntry);
        }
    }

    /**
     * Computes visibility using client-side cache (no player reference needed).
     */
    private NodeVisibility computeClientVisibility(Identifier nodeId, SkillNode node) {
        if (ClientSkillTreeData.isUnlocked(nodeId)) return NodeVisibility.READABLE;

        // Check distance to nearest unlocked ancestor
        int distance = getClientDistanceToUnlocked(nodeId);
        return switch (distance) {
            case 1 -> NodeVisibility.UNLOCKABLE;
            case 2 -> NodeVisibility.SCRAMBLED;
            case 3 -> NodeVisibility.TEASED;
            default -> {
                // Root nodes with no parent are always at least UNLOCKABLE
                if (node.isRoot()) yield NodeVisibility.UNLOCKABLE;
                yield NodeVisibility.INVISIBLE;
            }
        };
    }

    private int getClientDistanceToUnlocked(Identifier nodeId) {
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        if (node == null) return Integer.MAX_VALUE;

        if (node.parentIds().isEmpty()) {
            return ClientSkillTreeData.isUnlocked(nodeId) ? 0 : Integer.MAX_VALUE;
        }

        int minDistance = Integer.MAX_VALUE;
        for (Identifier parentId : node.parentIds()) {
            int dist = getClientDistanceSingle(parentId, 1);
            minDistance = Math.min(minDistance, dist);
        }
        return minDistance;
    }

    private int getClientDistanceSingle(Identifier nodeId, int startDistance) {
        int distance = startDistance;
        Identifier current = nodeId;

        while (current != null) {
            if (ClientSkillTreeData.isUnlocked(current)) return distance;
            SkillNode currentNode = SkillTreeRegistries.NODE_REGISTRY.getValue(current);
            if (currentNode == null) break;
            current = currentNode.parentId();
            distance++;
        }

        return Integer.MAX_VALUE;
    }

    // Note: iterates full node registry per call. Acceptable for current tree sizes (<50 nodes).
    // If multi-mod setups grow to 100+ nodes, consider caching exclusion results in rebuildNodeCache.
    private boolean isClientExcluded(Identifier nodeId) {
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        if (node == null || node.exclusionGroup() == null) return false;

        // Check if any other node in the same exclusion group is already unlocked
        for (Map.Entry<ResourceKey<SkillNode>, SkillNode> entry : SkillTreeRegistries.NODE_REGISTRY.entrySet()) {
            SkillNode other = entry.getValue();
            Identifier otherId = entry.getKey().identifier();
            if (otherId.equals(nodeId)) continue;
            if (node.exclusionGroup().equals(other.exclusionGroup())
                    && other.nodeType() == NodeType.SPECIALIZATION
                    && ClientSkillTreeData.isUnlocked(otherId)) {
                return true;
            }
        }

        return false;
    }

    // ── Rendering ───────────────────────────────────────────────────────────

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        // Draw semi-transparent background
        graphics.fillGradient(0, 0, this.width, this.height, 0xC0101020, 0xC0101020);

        // Draw tabs
        drawTabs(graphics, mouseX, mouseY);

        // Draw tree title and points
        drawHeader(graphics);

        // Draw connections and nodes (offset by scroll)
        int offsetX = this.width / 2 + (int) scrollX;
        int offsetY = HEADER_HEIGHT + TAB_HEIGHT + 20 + (int) scrollY;

        drawConnections(graphics, offsetX, offsetY);
        hoveredNodeId = drawNodes(graphics, mouseX, mouseY, offsetX, offsetY);

        // Draw tooltip for hovered node
        if (hoveredNodeId != null) {
            drawNodeTooltip(graphics, mouseX, mouseY, hoveredNodeId);
        }
    }

    private void drawTabs(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int tabY = HEADER_HEIGHT;
        int startX = 10;

        for (int i = 0; i < treeIds.size(); i++) {
            int tabX = startX + i * (TAB_WIDTH + TAB_GAP);
            boolean isSelected = i == currentPageIndex;
            boolean isHovered = mouseX >= tabX && mouseX < tabX + TAB_WIDTH
                    && mouseY >= tabY && mouseY < tabY + TAB_HEIGHT;

            int bgColor = isSelected ? 0xFF3344AA : (isHovered ? 0xFF2A2A4A : 0xFF1A1A2A);
            graphics.fillGradient(tabX, tabY, tabX + TAB_WIDTH, tabY + TAB_HEIGHT, bgColor, bgColor);

            // Border
            int borderColor = isSelected ? 0xFF5566CC : 0xFF333344;
            graphics.fillGradient(tabX, tabY, tabX + TAB_WIDTH, tabY + 1, borderColor, borderColor);
            graphics.fillGradient(tabX, tabY + TAB_HEIGHT - 1, tabX + TAB_WIDTH, tabY + TAB_HEIGHT, borderColor, borderColor);
            graphics.fillGradient(tabX, tabY, tabX + 1, tabY + TAB_HEIGHT, borderColor, borderColor);
            graphics.fillGradient(tabX + TAB_WIDTH - 1, tabY, tabX + TAB_WIDTH, tabY + TAB_HEIGHT, borderColor, borderColor);

            SkillTree tree = SkillTreeRegistries.TREE_REGISTRY.getValue(treeIds.get(i));
            String label = tree != null ? tree.name() : treeIds.get(i).getPath();
            int textColor = isSelected ? 0xFFFFFFFF : 0xFFAAAAAA;
            graphics.centeredText(this.font, Component.literal(label), tabX + TAB_WIDTH / 2, tabY + (TAB_HEIGHT - 8) / 2, textColor);
        }
    }

    private void drawHeader(GuiGraphicsExtractor graphics) {
        // Tree title
        if (!treeIds.isEmpty()) {
            SkillTree tree = SkillTreeRegistries.TREE_REGISTRY.getValue(treeIds.get(currentPageIndex));
            String title = tree != null ? tree.name() : "Unknown Tree";
            graphics.centeredText(this.font, Component.literal(title),
                    this.width / 2, 8, 0xFFFFFFFF);
        }

        // Available points
        int points = ClientSkillTreeData.getAvailablePoints();
        String pointsText = "Skill Points: " + points;
        graphics.text(this.font, Component.literal(pointsText),
                this.width - this.font.width(pointsText) - 10, 8, COLOR_POINTS_LABEL);
    }

    private void drawConnections(GuiGraphicsExtractor graphics, int offsetX, int offsetY) {
        SkillTree tree = !treeIds.isEmpty()
                ? SkillTreeRegistries.TREE_REGISTRY.getValue(treeIds.get(currentPageIndex)) : null;
        boolean horizontal = tree != null && tree.direction() == LayoutDirection.LEFT_RIGHT;

        // Two passes: locked (grey) first, then unlocked (green) on top,
        // so green lines aren't overwritten by overlapping grey horizontal segments.
        for (int pass = 0; pass < 2; pass++) {
            for (NodeEntry entry : currentNodes) {
                if (entry.node.isRoot()) continue;

                for (Identifier pid : entry.node.parentIds()) {
                    NodeEntry parent = findEntry(pid);
                    if (parent == null) continue;

                    boolean bothUnlocked = entry.visibility == NodeVisibility.READABLE
                            && parent.visibility == NodeVisibility.READABLE;

                    // Pass 0 = locked lines, pass 1 = unlocked lines
                    if ((pass == 0) == bothUnlocked) continue;

                    int lineColor = bothUnlocked ? COLOR_LINE_UNLOCKED : COLOR_LINE_LOCKED;

                    int x1 = offsetX + parent.pixelX;
                    int y1 = offsetY + parent.pixelY;
                    int x2 = offsetX + entry.pixelX;
                    int y2 = offsetY + entry.pixelY;

                    if (horizontal) {
                        // L-shaped connection: horizontal then vertical then horizontal
                        int midX = (x1 + x2) / 2;

                        // Horizontal segment from parent right to midpoint
                        int topY = Math.min(y1, y2);
                        int bottomY = Math.max(y1, y2);
                        graphics.fillGradient(x1, y1 - LINE_THICKNESS / 2, midX, y1 + LINE_THICKNESS / 2, lineColor, lineColor);

                        // Vertical segment
                        graphics.fillGradient(midX - LINE_THICKNESS / 2, topY, midX + LINE_THICKNESS / 2, bottomY, lineColor, lineColor);

                        // Horizontal segment from midpoint to child
                        graphics.fillGradient(midX, y2 - LINE_THICKNESS / 2, x2, y2 + LINE_THICKNESS / 2, lineColor, lineColor);
                    } else {
                        // L-shaped connection: vertical then horizontal then vertical
                        int midY = (y1 + y2) / 2;

                        // Vertical segment from parent down to midpoint
                        graphics.fillGradient(x1 - LINE_THICKNESS / 2, y1, x1 + LINE_THICKNESS / 2, midY, lineColor, lineColor);

                        // Horizontal segment
                        int leftX = Math.min(x1, x2);
                        int rightX = Math.max(x1, x2);
                        graphics.fillGradient(leftX, midY - LINE_THICKNESS / 2, rightX, midY + LINE_THICKNESS / 2, lineColor, lineColor);

                        // Vertical segment from midpoint down to child
                        graphics.fillGradient(x2 - LINE_THICKNESS / 2, midY, x2 + LINE_THICKNESS / 2, y2, lineColor, lineColor);
                    }
                }
            }
        }
    }

    /**
     * Draws all nodes and returns the hovered node ID (if any).
     */
    private @Nullable Identifier drawNodes(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                            int offsetX, int offsetY) {
        Identifier hovered = null;

        for (NodeEntry entry : currentNodes) {
            int cx = offsetX + entry.pixelX;
            int cy = offsetY + entry.pixelY;
            int left = cx - NODE_HALF;
            int top = cy - NODE_HALF;
            int right = cx + NODE_HALF;
            int bottom = cy + NODE_HALF;

            boolean isHovered = mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
            if (isHovered) hovered = entry.nodeId;

            // Background color based on visibility
            int bgColor = switch (entry.visibility) {
                case READABLE -> COLOR_UNLOCKED_BG;
                case UNLOCKABLE -> entry.excluded ? COLOR_EXCLUDED_BG : COLOR_SCRAMBLED_BG;
                case SCRAMBLED -> COLOR_SCRAMBLED_BG;
                case TEASED -> COLOR_TEASED_BG;
                default -> 0xFF000000;
            };

            // Brighten on hover
            if (isHovered && entry.visibility == NodeVisibility.UNLOCKABLE && !entry.excluded) {
                bgColor = 0xFF2A2A44;
            }

            // Draw node background
            graphics.fillGradient(left, top, right, bottom, bgColor, bgColor);

            // Draw frame border based on node type
            int frameColor = getNodeTypeColor(entry.node.nodeType());
            if (entry.visibility == NodeVisibility.READABLE) {
                // Bright frame for unlocked
                drawNodeFrame(graphics, left, top, right, bottom, frameColor);
            } else if (entry.visibility == NodeVisibility.UNLOCKABLE) {
                // Slightly dimmed frame for unlockable
                int dimmed = dimColor(frameColor, entry.excluded ? 0.3f : 0.7f);
                drawNodeFrame(graphics, left, top, right, bottom, dimmed);
            } else if (entry.visibility == NodeVisibility.SCRAMBLED) {
                // Dimmed frame for scrambled
                int dimmed = dimColor(frameColor, 0.4f);
                drawNodeFrame(graphics, left, top, right, bottom, dimmed);
            } else {
                // Very dim frame for teased
                drawNodeFrame(graphics, left, top, right, bottom, dimColor(frameColor, 0.2f));
            }

            // Draw node type indicator shape in center (or icon if available)
            if (entry.node.icon() != null
                    && (entry.visibility == NodeVisibility.READABLE || entry.visibility == NodeVisibility.UNLOCKABLE)) {
                drawNodeIcon(graphics, cx, cy, entry.node.icon());
            } else {
                drawNodeTypeIndicator(graphics, cx, cy, entry.node.nodeType(), entry.visibility);
            }

            // Draw text below node
            drawNodeLabel(graphics, cx, bottom + 2, entry);
        }

        return hovered;
    }

    private void drawNodeFrame(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int color) {
        UiRenderUtil.drawBorder(graphics, left, top, right, bottom, color);
    }

    private void drawNodeTypeIndicator(GuiGraphicsExtractor graphics, int cx, int cy,
                                        NodeType type, NodeVisibility visibility) {
        if (visibility == NodeVisibility.TEASED) return;

        int color = getNodeTypeColor(type);
        if (visibility == NodeVisibility.SCRAMBLED) {
            color = dimColor(color, 0.4f);
        } else if (visibility == NodeVisibility.UNLOCKABLE) {
            color = dimColor(color, 0.7f);
        }

        int size = 4;
        switch (type) {
            case PASSIVE -> {
                // Small filled square (circle approximation)
                graphics.fillGradient(cx - size, cy - size, cx + size, cy + size, color, color);
            }
            case ABILITY -> {
                // Diamond shape
                graphics.fillGradient(cx - 1, cy - size, cx + 1, cy - size + 2, color, color);
                graphics.fillGradient(cx - size, cy - 1, cx + size, cy + 1, color, color);
                graphics.fillGradient(cx - 1, cy + size - 2, cx + 1, cy + size, color, color);
            }
            case UTILITY -> {
                // Small square outline
                drawNodeFrame(graphics, cx - size, cy - size, cx + size, cy + size, color);
            }
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
        }
    }

    private void drawNodeIcon(GuiGraphicsExtractor graphics, int cx, int cy, Identifier icon) {
        int iconSize = 16;
        int halfIcon = iconSize / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, icon,
                cx - halfIcon, cy - halfIcon, 0, 0, iconSize, iconSize, iconSize, iconSize);
    }

    private void drawNodeLabel(GuiGraphicsExtractor graphics, int cx, int y, NodeEntry entry) {
        if (entry.visibility == NodeVisibility.READABLE) {
            graphics.centeredText(this.font, Component.literal(entry.node.name()),
                    cx, y, COLOR_TEXT_READABLE);
        } else if (entry.visibility == NodeVisibility.UNLOCKABLE) {
            // Show real name but in a dimmer/different color to indicate not yet unlocked
            int color = entry.excluded ? 0xFF994444 : 0xFFCCCC88;
            graphics.centeredText(this.font, Component.literal(entry.node.name()),
                    cx, y, color);
        } else if (entry.visibility == NodeVisibility.SCRAMBLED) {
            String scrambled = scrambleText(entry.node.name());
            graphics.centeredText(this.font, Component.literal(scrambled),
                    cx, y, COLOR_TEXT_SCRAMBLED);
        }
        // TEASED: no label shown
    }

    private void drawNodeTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, Identifier nodeId) {
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        if (node == null) return;

        NodeVisibility visibility = computeClientVisibility(nodeId, node);
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
            boolean excluded = isClientExcluded(nodeId);
            if (excluded) {
                lines.add(Component.literal(node.name()).withStyle(ChatFormatting.RED));
                lines.add(Component.literal("Locked out (another specialization chosen)").withStyle(ChatFormatting.DARK_RED));
            } else {
                lines.add(Component.literal(node.name()).withStyle(ChatFormatting.YELLOW));
                if (!node.description().isEmpty()) {
                    lines.add(Component.literal(node.description()).withStyle(ChatFormatting.GRAY));
                }
                lines.add(Component.literal("Cost: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(node.cost() + " point" + (node.cost() != 1 ? "s" : "")).withStyle(ChatFormatting.YELLOW)));
                lines.add(Component.literal("Type: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(node.nodeType().displayName()).withStyle(ChatFormatting.WHITE)));
                if (ClientSkillTreeData.getAvailablePoints() >= node.cost()) {
                    lines.add(Component.literal("Click to unlock").withStyle(ChatFormatting.GREEN));
                } else {
                    lines.add(Component.literal("Not enough skill points").withStyle(ChatFormatting.RED));
                }
            }
        } else if (visibility == NodeVisibility.SCRAMBLED) {
            lines.add(Component.literal(scrambleText(node.name())).withStyle(ChatFormatting.DARK_GRAY));
            lines.add(Component.literal("???").withStyle(ChatFormatting.DARK_GRAY));
        } else if (visibility == NodeVisibility.TEASED) {
            lines.add(Component.literal("???").withStyle(ChatFormatting.DARK_GRAY));
            lines.add(Component.literal("Unlock nearby nodes first").withStyle(ChatFormatting.DARK_GRAY));
        }

        if (lines.isEmpty()) return;

        // Calculate tooltip dimensions
        int maxWidth = 0;
        for (Component line : lines) {
            maxWidth = Math.max(maxWidth, this.font.width(line));
        }
        int tooltipWidth = maxWidth + TOOLTIP_PADDING * 2;
        int tooltipHeight = lines.size() * 10 + TOOLTIP_PADDING * 2;

        // Position tooltip (avoid going off-screen)
        int tx = mouseX + 12;
        int ty = mouseY - 12;
        if (tx + tooltipWidth > this.width) tx = mouseX - tooltipWidth - 4;
        if (ty + tooltipHeight > this.height) ty = this.height - tooltipHeight;
        if (ty < 0) ty = 0;

        // Draw tooltip background and border
        graphics.fillGradient(tx, ty, tx + tooltipWidth, ty + tooltipHeight, 0xF0100010, 0xF0100010);
        graphics.fillGradient(tx, ty, tx + tooltipWidth, ty + 1, 0xFF5000FF, 0xFF5000FF);
        graphics.fillGradient(tx, ty + tooltipHeight - 1, tx + tooltipWidth, ty + tooltipHeight, 0xFF28007F, 0xFF28007F);
        graphics.fillGradient(tx, ty, tx + 1, ty + tooltipHeight, 0xFF5000FF, 0xFF28007F);
        graphics.fillGradient(tx + tooltipWidth - 1, ty, tx + tooltipWidth, ty + tooltipHeight, 0xFF5000FF, 0xFF28007F);

        // Draw text lines
        for (int i = 0; i < lines.size(); i++) {
            graphics.text(this.font, lines.get(i),
                    tx + TOOLTIP_PADDING, ty + TOOLTIP_PADDING + i * 10, 0xFFFFFFFF);
        }
    }

    // ── Input handling ──────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT) {
            switchToPage((currentPageIndex - 1 + treeIds.size()) % treeIds.size());
            return true;
        }
        if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT) {
            switchToPage((currentPageIndex + 1) % treeIds.size());
            return true;
        }

        if (SkillTreeKeybinds.OPEN_SKILL_TREE.matches(event)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        // Check tab clicks
        int tabY = HEADER_HEIGHT;
        int startX = 10;
        for (int i = 0; i < treeIds.size(); i++) {
            int tabX = startX + i * (TAB_WIDTH + TAB_GAP);
            if (mouseX >= tabX && mouseX < tabX + TAB_WIDTH
                    && mouseY >= tabY && mouseY < tabY + TAB_HEIGHT) {
                switchToPage(i);
                return true;
            }
        }

        // Check node clicks (left button = unlock)
        if (button == 0 && hoveredNodeId != null) {
            SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(hoveredNodeId);
            if (node != null) {
                NodeVisibility vis = computeClientVisibility(hoveredNodeId, node);
                if (vis == NodeVisibility.UNLOCKABLE && !isClientExcluded(hoveredNodeId)) {
                    ClientPacketDistributor.sendToServer(new SkillNodeUnlockPayload(hoveredNodeId));
                    return true;
                }
            }
        }

        // Start pan drag with right or middle mouse button
        if (button == 1 || button == 2) {
            dragging = true;
            dragStartX = mouseX - scrollX;
            dragStartY = mouseY - scrollY;
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int button = event.button();
        if ((button == 1 || button == 2) && dragging) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging) {
            scrollX = event.x() - dragStartX;
            scrollY = event.y() - dragStartY;
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollXAmount, double scrollYAmount) {
        this.scrollY += scrollYAmount * 20;
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private @Nullable NodeEntry findEntry(Identifier nodeId) {
        return nodeMap.get(nodeId);
    }

    private static int getNodeTypeColor(NodeType type) {
        return switch (type) {
            case PASSIVE -> COLOR_PASSIVE;
            case ABILITY -> COLOR_ABILITY;
            case UTILITY -> COLOR_UTILITY;
            case SPECIALIZATION -> COLOR_SPECIALIZATION;
            case CAPSTONE -> COLOR_CAPSTONE;
        };
    }

    private static int dimColor(int color, float factor) {
        return UiRenderUtil.dimColor(color, factor);
    }

    private static String scrambleText(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != ' ') {
                chars[i] = (char) ('a' + (chars[i] * 7 + i * 13) % 26);
            }
        }
        return new String(chars);
    }

    public Identifier getCurrentTreeId() {
        if (treeIds.isEmpty()) return null;
        return treeIds.get(currentPageIndex);
    }

    // ── Inner record for cached node data ───────────────────────────────────

    private record NodeEntry(
            Identifier nodeId,
            SkillNode node,
            int pixelX,
            int pixelY,
            NodeVisibility visibility,
            boolean excluded
    ) {}
}

