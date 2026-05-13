package com.nerosro.soulmark.skilltree;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Client-side handler for the skill tree keybind.
 * Determines which tree page to open based on held item, then opens the screen.
 */
public class SkillTreeClientEvents {

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (!SkillTreeKeybinds.OPEN_SKILL_TREE.consumeClick()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // Determine starting page from held item
        Identifier startingTree = resolveStartingTree(player);

        // Open the skill tree screen
        if (startingTree != null) {
            mc.setScreen(new SkillTreeScreen(startingTree));
        }
    }

    /**
     * Checks main hand, then off hand for a registered tree opener item.
     * Falls back to the default (first registered) tree.
     */
    @Nullable
    private Identifier resolveStartingTree(Player player) {
        // Check main hand
        Item mainHand = player.getMainHandItem().getItem();
        Identifier tree = SkillTreeOpeners.getTreeForItem(mainHand);
        if (tree != null) return tree;

        // Check off hand
        Item offHand = player.getOffhandItem().getItem();
        tree = SkillTreeOpeners.getTreeForItem(offHand);
        if (tree != null) return tree;

        // Fallback to default
        return SkillTreeOpeners.getDefaultTree();
    }
}

