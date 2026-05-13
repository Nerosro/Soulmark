package com.nerosro.soulmark.skilltree;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * A single node in a skill tree.
 * Registered by job mods into the node registry.
 *
 * @param name        Display name of the node
 * @param description What unlocking this node does (shown when readable)
 * @param treeId      Which tree this node belongs to (e.g. "elemancy:main")
 * @param parentId    The parent node's registry ID, or null if this is a root node
 * @param nodeType    The category of this node (determines icon frame shape)
 */
public record SkillNode(
        String name,
        String description,
        Identifier treeId,
        @Nullable Identifier parentId,
        NodeType nodeType
) {
    /**
     * Returns true if this is a root node (no parent).
     */
    public boolean isRoot() {
        return parentId == null;
    }
}


