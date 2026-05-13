package com.nerosro.soulmark.skilltree;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The skill tree screen. Shows all registered trees as tabs/pages.
 * Player can swap between pages freely once the screen is open.
 * The starting page is determined by the held item when the key was pressed.
 * TODO: Full rendering (node shapes, connections, visibility states, scrolling/panning)
 * will be implemented incrementally.
 */
public class SkillTreeScreen extends Screen {

    private final List<Identifier> treeIds = new ArrayList<>();
    private int currentPageIndex;

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
        // TODO: Add tab buttons for each tree page
        // TODO: Add node rendering area with pan/zoom
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        // TODO: Render tree title, page indicator, nodes based on visibility and node type shapes
        // MC 26.1.2 rendering API uses GuiGraphicsExtractor — implement incrementally
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // Tab navigation between tree pages
        if (event.key() == 263) { // Left arrow
            currentPageIndex = (currentPageIndex - 1 + treeIds.size()) % treeIds.size();
            return true;
        }
        if (event.key() == 262) { // Right arrow
            currentPageIndex = (currentPageIndex + 1) % treeIds.size();
            return true;
        }

        // Allow the keybind to close the screen
        if (SkillTreeKeybinds.OPEN_SKILL_TREE.matches(event)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public Identifier getCurrentTreeId() {
        if (treeIds.isEmpty()) return null;
        return treeIds.get(currentPageIndex);
    }
}

