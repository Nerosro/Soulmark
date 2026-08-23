package be.nerosro.soulmark.ui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

/**
 * Overlay-based radial selection menu that allows player movement.
 * Renders as a GUI layer (no Screen) so WASD/Jump/Sneak work naturally.
 * Mouse is ungrabbed for cursor-based selection; camera stays locked.
 * <p>
 * Usage:
 * <pre>
 *   RadialMenuOverlay.open(entries, selected -> { ... }, equippedId);
 * </pre>
 */
public class RadialMenuOverlay implements GuiLayer {

    // ── Layout constants ────────────────────────────────────────────────────
    private static final int RING_RADIUS = 80;
    private static final int ENTRY_SIZE = 28;
    private static final int ENTRY_HALF = ENTRY_SIZE / 2;
    private static final int LABEL_OFFSET = 18;
    private static final int CENTER_DEAD_ZONE = 20;

    // ── Colors ──────────────────────────────────────────────────────────────
    private static final int RING_BG_COLOR = 0xAA000000;
    private static final int CENTER_DOT_COLOR = 0xFF666666;
    private static final int CENTER_DOT_BORDER = 0xFF888888;
    private static final int SELECTED_BORDER = 0xFFFFDD44;
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final int DESC_COLOR = 0xFFAAAAAA;
    private static final int LINE_COLOR_NORMAL = 0xFF333344;
    private static final int LINE_COLOR_HOVERED = 0xFF888888;
    private static final int DETAIL_BAR_BG = 0xC0000000;

    // ── Singleton state ─────────────────────────────────────────────────────
    private static boolean active;
    private static List<RadialMenuEntry> entries = List.of();
    private static @Nullable Consumer<RadialMenuEntry> onSelect;
    private static @Nullable Identifier currentlyEquipped;
    private static int hoveredIndex = -1;

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Opens the radial menu overlay.
     *
     * @param menuEntries       The entries to display
     * @param selectCallback    Called when the player selects an entry
     * @param equipped          The node ID of the currently equipped ability, or null
     */
    public static void open(List<RadialMenuEntry> menuEntries, Consumer<RadialMenuEntry> selectCallback,
                            @Nullable Identifier equipped) {
        Minecraft mc = Minecraft.getInstance();
        if (menuEntries == null || menuEntries.isEmpty()) return;

        entries = menuEntries;
        onSelect = selectCallback;
        currentlyEquipped = equipped;
        hoveredIndex = -1;
        active = true;

        // Release mouse so cursor is visible for selection
        mc.mouseHandler.releaseMouse();
    }

    /**
     * Opens the radial menu without a currently-equipped indicator.
     */
    public static void open(List<RadialMenuEntry> menuEntries, Consumer<RadialMenuEntry> selectCallback) {
        open(menuEntries, selectCallback, null);
    }

    /**
     * Closes the radial menu without selecting anything.
     */
    public static void close() {
        if (!active) return;
        active = false;
        entries = List.of();
        onSelect = null;
        currentlyEquipped = null;
        hoveredIndex = -1;

        Minecraft mc = Minecraft.getInstance();
        mc.mouseHandler.grabMouse();
    }

    /**
     * Closes the radial menu, selecting the currently hovered entry if any.
     * Returns true if a selection was made.
     */
    public static boolean confirmAndClose() {
        if (!active) return false;
        if (hoveredIndex >= 0 && hoveredIndex < entries.size() && onSelect != null) {
            Consumer<RadialMenuEntry> callback = onSelect;
            RadialMenuEntry selected = entries.get(hoveredIndex);
            close();
            callback.accept(selected);
            return true;
        }
        close();
        return false;
    }

    public static boolean isActive() {
        return active;
    }

    public static int getHoveredIndex() {
        return hoveredIndex;
    }

    public static @Nullable RadialMenuEntry getHoveredEntry() {
        if (!active || hoveredIndex < 0 || hoveredIndex >= entries.size()) return null;
        return entries.get(hoveredIndex);
    }

    // ── GuiLayer rendering ──────────────────────────────────────────────────

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!active || entries.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        // Get mouse position in GUI coordinates
        double mouseX = mc.mouseHandler.xpos() * screenWidth / mc.getWindow().getWidth();
        double mouseY = mc.mouseHandler.ypos() * screenHeight / mc.getWindow().getHeight();

        // Update hovered index
        hoveredIndex = computeHoveredIndex((int) mouseX, (int) mouseY, centerX, centerY);

        // Dim circle behind the ring for readability
        int dimRadius = RING_RADIUS + ENTRY_SIZE + LABEL_OFFSET + 10;
        fillCircle(graphics, centerX, centerY, dimRadius, RING_BG_COLOR);

        // Draw connecting lines
        drawConnectionLines(graphics, centerX, centerY);

        // Draw entries
        for (int i = 0; i < entries.size(); i++) {
            drawEntry(graphics, mc, i, centerX, centerY);
        }

        // Draw center dot
        drawCenter(graphics, centerX, centerY);

        // Draw hovered entry details
        if (hoveredIndex >= 0) {
            drawHoveredDetails(graphics, mc, entries.get(hoveredIndex), centerX, centerY);
        }
    }

    // ── Event handlers ──────────────────────────────────────────────────────

    @SubscribeEvent
    public void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (!active) return;

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS) {
            if (hoveredIndex >= 0) {
                confirmAndClose();
            }
            event.setCanceled(true);
        } else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && event.getAction() == GLFW.GLFW_PRESS) {
            close();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onKey(InputEvent.Key event) {
        if (!active) return;

        if (event.getKey() == GLFW.GLFW_KEY_ESCAPE && event.getAction() == GLFW.GLFW_PRESS) {
            close();
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (!active) return;

        // Close if player becomes null (disconnect)
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            close();
        }
    }

    // ── Drawing helpers ─────────────────────────────────────────────────────

    private void drawCenter(GuiGraphicsExtractor graphics, int cx, int cy) {
        int size = 4;
        graphics.fillGradient(cx - size, cy - size, cx + size, cy + size, CENTER_DOT_COLOR, CENTER_DOT_COLOR);
        graphics.fillGradient(cx - size, cy - size, cx + size, cy - size + 1, CENTER_DOT_BORDER, CENTER_DOT_BORDER);
        graphics.fillGradient(cx - size, cy + size - 1, cx + size, cy + size, CENTER_DOT_BORDER, CENTER_DOT_BORDER);
        graphics.fillGradient(cx - size, cy - size, cx - size + 1, cy + size, CENTER_DOT_BORDER, CENTER_DOT_BORDER);
        graphics.fillGradient(cx + size - 1, cy - size, cx + size, cy + size, CENTER_DOT_BORDER, CENTER_DOT_BORDER);
    }

    private void drawConnectionLines(GuiGraphicsExtractor graphics, int cx, int cy) {
        for (int i = 0; i < entries.size(); i++) {
            double angle = getAngleForIndex(i);
            int ex = cx + (int) (Math.cos(angle) * RING_RADIUS);
            int ey = cy + (int) (Math.sin(angle) * RING_RADIUS);

            int lineColor = (i == hoveredIndex) ? LINE_COLOR_HOVERED : LINE_COLOR_NORMAL;
            drawLine(graphics, cx, cy, ex, ey, lineColor);
        }
    }

    private void drawEntry(GuiGraphicsExtractor graphics, Minecraft mc, int index, int cx, int cy) {
        RadialMenuEntry entry = entries.get(index);
        double angle = getAngleForIndex(index);

        int ex = cx + (int) (Math.cos(angle) * RING_RADIUS);
        int ey = cy + (int) (Math.sin(angle) * RING_RADIUS);

        int left = ex - ENTRY_HALF;
        int top = ey - ENTRY_HALF;
        int right = ex + ENTRY_HALF;
        int bottom = ey + ENTRY_HALF;

        boolean isHovered = index == hoveredIndex;
        boolean isEquipped = currentlyEquipped != null && currentlyEquipped.equals(entry.nodeId());

        // Background
        int bgColor = UiRenderUtil.dimColor(entry.color(), 0.2f);
        if (isHovered) bgColor = UiRenderUtil.dimColor(entry.color(), 0.4f);
        graphics.fillGradient(left, top, right, bottom, bgColor, bgColor);

        // Border
        int borderColor = isHovered ? UiRenderUtil.brightenColor(entry.color(), 1.0f) : UiRenderUtil.dimColor(entry.color(), 0.6f);
        if (isEquipped) borderColor = SELECTED_BORDER;
        UiRenderUtil.drawBorder(graphics, left, top, right, bottom, borderColor);

        // Equipped indicator (double border)
        if (isEquipped) {
            UiRenderUtil.drawBorder(graphics, left - 1, top - 1, right + 1, bottom + 1, UiRenderUtil.dimColor(SELECTED_BORDER, 0.5f));
        }

        // Icon: texture if available, otherwise character
        if (entry.icon() != null) {
            int iconSize = 16;
            int halfIcon = iconSize / 2;
            graphics.blit(RenderPipelines.GUI_TEXTURED, entry.icon(),
                    ex - halfIcon, ey - halfIcon, 0, 0, iconSize, iconSize, iconSize, iconSize);
        } else {
            int iconColor = isHovered ? LABEL_COLOR : UiRenderUtil.dimColor(entry.color(), 0.9f);
            graphics.centeredText(mc.font, Component.literal(entry.iconChar()), ex, ey - 4, iconColor);
        }

        // Label below the entry box
        int labelY = bottom + 3;
        int labelColor = isHovered ? LABEL_COLOR : DESC_COLOR;
        graphics.centeredText(mc.font, Component.literal(entry.name()), ex, labelY, labelColor);
    }

    private void drawHoveredDetails(GuiGraphicsExtractor graphics, Minecraft mc, RadialMenuEntry entry, int cx, int cy) {
        String text = entry.name();
        int textWidth = mc.font.width(text);
        int y = cy + RING_RADIUS + 50;

        // Background bar
        int barLeft = cx - textWidth / 2 - 8;
        int barRight = cx + textWidth / 2 + 8;
        int barTop = y - 4;
        int barBottom = y + 12;
        graphics.fillGradient(barLeft, barTop, barRight, barBottom, DETAIL_BAR_BG, DETAIL_BAR_BG);

        // Border in entry color
        int borderColor = UiRenderUtil.dimColor(entry.color(), 0.7f);
        graphics.fillGradient(barLeft, barTop, barRight, barTop + 1, borderColor, borderColor);
        graphics.fillGradient(barLeft, barBottom - 1, barRight, barBottom, borderColor, borderColor);

        // Text
        graphics.centeredText(mc.font, Component.literal(text), cx, y, entry.color());
    }

    // ── Geometry helpers ────────────────────────────────────────────────────

    private double getAngleForIndex(int index) {
        double step = (2 * Math.PI) / entries.size();
        return -Math.PI / 2 + step * index;
    }

    private int computeHoveredIndex(int mouseX, int mouseY, int cx, int cy) {
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < CENTER_DEAD_ZONE) return -1;

        double angle = Math.atan2(dy, dx);
        double step = (2 * Math.PI) / entries.size();
        double adjusted = angle + Math.PI / 2 + step / 2;
        if (adjusted < 0) adjusted += 2 * Math.PI;
        return (int) (adjusted / step) % entries.size();
    }

    private void drawLine(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) return;

        for (int i = 0; i <= steps; i += 2) { // Step by 2 for a dotted-line aesthetic
            int x = x1 + dx * i / steps;
            int y = y1 + dy * i / steps;
            graphics.fillGradient(x, y, x + 1, y + 1, color, color);
        }
    }

    private void fillCircle(GuiGraphicsExtractor graphics, int cx, int cy, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.sqrt((long) radius * radius - (long) y * y);
            graphics.fillGradient(cx - halfWidth, cy + y, cx + halfWidth, cy + y + 1, color, color);
        }
    }
}
