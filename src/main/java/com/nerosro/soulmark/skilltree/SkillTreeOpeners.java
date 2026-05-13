package com.nerosro.soulmark.skilltree;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry for tree opener mappings.
 * Job mods register which held item opens which tree page by default.
 */
public final class SkillTreeOpeners {

    private static final Map<Item, Identifier> OPENERS = new LinkedHashMap<>();

    private SkillTreeOpeners() {}

    /**
     * Registers a held item → tree mapping.
     * When the player presses the skill tree key while holding this item,
     * the screen opens on the associated tree page.
     */
    public static void register(Item item, Identifier treeId) {
        OPENERS.put(item, treeId);
    }

    /**
     * Returns the tree ID associated with the given item, or null if no mapping exists.
     */
    @Nullable
    public static Identifier getTreeForItem(Item item) {
        return OPENERS.get(item);
    }

    /**
     * Returns the first registered tree ID as a fallback, or null if nothing is registered.
     */
    @Nullable
    public static Identifier getDefaultTree() {
        if (OPENERS.isEmpty()) return null;
        return OPENERS.values().iterator().next();
    }
}
