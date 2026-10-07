package be.nerosro.soulmark.skilltree;

import static be.nerosro.soulmark.skilltree.SkillTreeScreenConstants.Layout;
import static be.nerosro.soulmark.skilltree.SkillTreeScreenConstants.UI_Colors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import be.nerosro.soulmark.network.ClientSkillTreeData;
import be.nerosro.soulmark.network.SkillNodeUnlockPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * The skill tree screen. Shows all registered trees as tabs/pages.
 * Player can swap between pages freely once the screen is open.
 * The starting page is determined by the held item when the key was pressed.
 * Rendering and node-cache logic are delegated to dedicated helper classes;
 * this class coordinates lifecycle, page state, and input handling.
 */
public class SkillTreeScreen extends Screen {

    // ── State ───────────────────────────────────────────────────────────────
    private final List<Identifier> treeIds = new ArrayList<>();
    private int currentPageIndex;

    // Scroll/pan offset for the tree view
    private double scrollX = 0;
    private double scrollY = 0;
    private boolean dragging = false;
    private double dragStartX, dragStartY;
    private float targetZoom = 1.0f;
    private float currentZoom = targetZoom;

    // Cache of nodes for the current page, rebuilt on page switch or unlock update
    private final SkillTreeNodeCache nodeCache = new SkillTreeNodeCache();

    // Hovered node for tooltip
    private @Nullable Identifier hoveredNodeId;

    public SkillTreeScreen(Identifier startingTreeId) {
        super(Component.literal("Skill Tree"));

        // Gather only discovered trees
        for (Map.Entry<ResourceKey<SkillTree>, SkillTree> entry : SkillTreeRegistries.TREE_REGISTRY.entrySet()) {
            Identifier id = entry.getKey().identifier();
            if (ClientSkillTreeData.isTreeDiscovered(id)) {
                treeIds.add(id);
            }
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
        targetZoom = 1.0f;
        currentZoom = targetZoom;
        rebuildNodeCache();
    }

    private void rebuildNodeCache() {
        nodeCache.rebuild(getCurrentTreeId());
    }

    // ── Rendering ───────────────────────────────────────────────────────────

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        currentZoom += (targetZoom - currentZoom) * Math.min(1.0f, partialTick * Layout.MAP_ZOOM_SMOOTHING);

        // Draw semi-transparent background
        graphics.fillGradient(0, 0, this.width, this.height, 0xC0101020, 0xC0101020);

        // Draw tabs
        drawTabs(graphics, mouseX, mouseY);

        // Draw tree title and points
        drawHeader(graphics);

        MapViewport viewport = mapViewport();
        drawMapViewport(graphics, viewport);

        // Keep graph content within its framed map surface.
        graphics.enableScissor(viewport.innerLeft(), viewport.innerTop(), viewport.innerRight(), viewport.innerBottom());

        int offsetX = (int) (viewport.centerX() / currentZoom + scrollX);
        int offsetY = (int) (viewport.centerY() / currentZoom + scrollY);
        int scaledMouseX = (int) (mouseX / currentZoom);
        int scaledMouseY = (int) (mouseY / currentZoom);

        SkillTree tree = !treeIds.isEmpty() ? SkillTreeRegistries.TREE_REGISTRY.getValue(treeIds.get(currentPageIndex)) : null;
        boolean horizontal = tree != null && tree.direction() == LayoutDirection.LEFT_RIGHT;

        graphics.pose().pushMatrix();
        graphics.pose().scale(currentZoom, currentZoom);
        SkillTreeConnectionRenderer.draw(graphics, nodeCache, horizontal, offsetX, offsetY);
        hoveredNodeId = SkillTreeNodeRenderer.draw(graphics, this.font, nodeCache, scaledMouseX, scaledMouseY, offsetX, offsetY);
        graphics.pose().popMatrix();
        graphics.disableScissor();

        // Draw tooltip for hovered node
        if (hoveredNodeId != null) {
            SkillTreeTooltipRenderer.draw(graphics, this.font, nodeCache, this.width, this.height, mouseX, mouseY, hoveredNodeId);
        }
    }

    private void drawTabs(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int tabY = Layout.HEADER_HEIGHT;
        int tabHeight = Layout.TAB_HEIGHT;
        int tabWidth = Layout.TAB_WIDTH;
        int tabGap = Layout.TAB_GAP;
        int startX = 10;

        for (int i = 0; i < treeIds.size(); i++) {
            int tabX = startX + i * (tabWidth + tabGap);
            boolean isSelected = i == currentPageIndex;
            boolean isHovered = mouseX >= tabX && mouseX < tabX + tabWidth
                && mouseY >= tabY && mouseY < tabY + tabHeight;

            int bgColor = isSelected ? UI_Colors.TAB_SELECTED_BG
                : (isHovered ? UI_Colors.TAB_HOVERED_BG : UI_Colors.TAB_NORMAL_BG);
            graphics.fillGradient(tabX, tabY, tabX + tabWidth, tabY + tabHeight, bgColor, bgColor);

            // Border
            int borderColor = isSelected ? UI_Colors.TAB_SELECTED_BORDER : UI_Colors.TAB_NORMAL_BORDER;
            graphics.fillGradient(tabX, tabY, tabX + tabWidth, tabY + 1, borderColor, borderColor);
            graphics.fillGradient(tabX, tabY + tabHeight - 1, tabX + tabWidth, tabY + tabHeight, borderColor, borderColor);
            graphics.fillGradient(tabX, tabY, tabX + 1, tabY + tabHeight, borderColor, borderColor);
            graphics.fillGradient(tabX + tabWidth - 1, tabY, tabX + tabWidth, tabY + tabHeight, borderColor, borderColor);

            SkillTree tree = SkillTreeRegistries.TREE_REGISTRY.getValue(treeIds.get(i));
            String label = tree != null ? tree.name() : treeIds.get(i).getPath();
            int textColor = isSelected ? UI_Colors.TEXT_READABLE : 0xFFAAAAAA;
            graphics.centeredText(this.font, Component.literal(label), tabX + tabWidth / 2, tabY + (tabHeight - 8) / 2, textColor);
        }
    }

    private void drawHeader(GuiGraphicsExtractor graphics) {
        // Tree title
        if (!treeIds.isEmpty()) {
            SkillTree tree = SkillTreeRegistries.TREE_REGISTRY.getValue(treeIds.get(currentPageIndex));
            String title = tree != null ? tree.name() : "Unknown Tree";
            graphics.centeredText(this.font, Component.literal(title),
                this.width / 2, 8, UI_Colors.TEXT_READABLE);
        }

        SkillTree tree = treeIds.isEmpty() ? null : SkillTreeRegistries.TREE_REGISTRY.getValue(treeIds.get(currentPageIndex));
        // Soul Points are dormant until multiple job mods need shared Soul Point gates.
        // int soulPoints = ClientSkillTreeData.getAvailableSoulPoints();
        // String soulPointsText = "Soul Points: " + soulPoints;
        // graphics.text(this.font, Component.literal(soulPointsText),
        //     this.width - this.font.width(soulPointsText) - 10, 8, SkillTreeScreenConstants.Colors.SOUL_POINT_LABEL);

        if (tree != null && tree.defaultPayment() != null) {
            String jobPointsText = tree.defaultPayment().displayName() + ": " + tree.defaultPayment().getClientAvailableBalance();
            graphics.text(this.font, Component.literal(jobPointsText),
                this.width - this.font.width(jobPointsText) - 10, 8, UI_Colors.JOB_POINT_LABEL);
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

        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        // Check tab clicks
        int tabY = Layout.HEADER_HEIGHT;
        int tabWidth = Layout.TAB_WIDTH;
        int tabHeight = Layout.TAB_HEIGHT;
        int tabGap = Layout.TAB_GAP;
        int startX = 10;
        for (int i = 0; i < treeIds.size(); i++) {
            int tabX = startX + i * (tabWidth + tabGap);
            if (mouseX >= tabX && mouseX < tabX + tabWidth
                && mouseY >= tabY && mouseY < tabY + tabHeight) {
                switchToPage(i);
                return true;
            }
        }

        // Check node clicks (left button = unlock)
        if (button == 0 && hoveredNodeId != null) {
            SkillTreeNodeCache.NodeEntry entry = nodeCache.get(hoveredNodeId);
            if (entry != null) {
                if (entry.visibility() == NodeVisibility.UNLOCKABLE && !entry.excluded()) {
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
        targetZoom = Math.clamp(targetZoom + (float) scrollYAmount * Layout.MAP_ZOOM_STEP,
            Layout.MAP_MIN_ZOOM, Layout.MAP_MAX_ZOOM);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private MapViewport mapViewport() {
        int top = Layout.HEADER_HEIGHT + Layout.TAB_HEIGHT
            + Layout.MAP_TOP_MARGIN;
        int left = Layout.MAP_MARGIN;
        int right = this.width - Layout.MAP_MARGIN;
        int bottom = this.height - Layout.MAP_BOTTOM_MARGIN;
        return new MapViewport(left, top, right, bottom);
    }

    private void drawMapViewport(GuiGraphicsExtractor graphics, MapViewport viewport) {
        graphics.fillGradient(viewport.left(), viewport.top(), viewport.right(), viewport.bottom(),
            UI_Colors.MAP_BACKGROUND, UI_Colors.MAP_BACKGROUND);

        int gridSize = Layout.MAP_GRID_SIZE;
        for (int x = viewport.left(); x < viewport.right(); x += gridSize) {
            graphics.fillGradient(x, viewport.top(), x + 1, viewport.bottom(),
                UI_Colors.MAP_GRID, UI_Colors.MAP_GRID);
        }
        for (int y = viewport.top(); y < viewport.bottom(); y += gridSize) {
            graphics.fillGradient(viewport.left(), y, viewport.right(), y + 1,
                UI_Colors.MAP_GRID, UI_Colors.MAP_GRID);
        }

        int frame = Layout.MAP_FRAME_THICKNESS;
        graphics.fillGradient(viewport.left(), viewport.top(), viewport.right(), viewport.top() + frame,
            UI_Colors.MAP_FRAME_HIGHLIGHT, UI_Colors.MAP_FRAME_HIGHLIGHT);
        graphics.fillGradient(viewport.left(), viewport.bottom() - frame, viewport.right(), viewport.bottom(),
            UI_Colors.MAP_FRAME_OUTER, UI_Colors.MAP_FRAME_OUTER);
        graphics.fillGradient(viewport.left(), viewport.top(), viewport.left() + frame, viewport.bottom(),
            UI_Colors.MAP_FRAME_INNER, UI_Colors.MAP_FRAME_INNER);
        graphics.fillGradient(viewport.right() - frame, viewport.top(), viewport.right(), viewport.bottom(),
            UI_Colors.MAP_FRAME_OUTER, UI_Colors.MAP_FRAME_OUTER);
    }

    private record MapViewport(int left, int top, int right, int bottom) {
        private int innerLeft() {
            return left + Layout.MAP_FRAME_THICKNESS;
        }

        private int innerTop() {
            return top + Layout.MAP_FRAME_THICKNESS;
        }

        private int innerRight() {
            return right - Layout.MAP_FRAME_THICKNESS;
        }

        private int innerBottom() {
            return bottom - Layout.MAP_FRAME_THICKNESS;
        }

        private int centerX() {
            return (innerLeft() + innerRight()) / 2;
        }

        private int centerY() {
            return (innerTop() + innerBottom()) / 2;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public @Nullable Identifier getCurrentTreeId() {
        if (treeIds.isEmpty()) return null;
        return treeIds.get(currentPageIndex);
    }
}


