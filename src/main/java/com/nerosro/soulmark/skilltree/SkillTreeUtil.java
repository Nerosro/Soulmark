package com.nerosro.soulmark.skilltree;

import com.nerosro.soulmark.SoulmarkConfig;
import com.nerosro.soulmark.capability.SoulmarkAttachments;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Public utility API for the skill tree system.
 * Job mods should use this class to interact with player skill trees.
 */
public final class SkillTreeUtil {

    private SkillTreeUtil() {}

    // ── Player data access ───────────────────────────────────────────────────

    /**
     * Returns the player's SkillTreeData.
     */
    public static SkillTreeData getTreeData(Player player) {
        return player.getData(SoulmarkAttachments.SKILL_TREE.get());
    }

    // ── Unlock ───────────────────────────────────────────────────────────────

    /**
     * Attempts to unlock a node for the player.
     * Returns true if successful, false if:
     * - node doesn't exist in registry
     * - node is already unlocked
     * - parent is not unlocked (unless root)
     * - player has hit the point cap
     */
    public static boolean tryUnlock(Player player, Identifier nodeId) {
        SkillTreeData data = getTreeData(player);

        // Check point cap
        int cap = SoulmarkConfig.SKILL_POINT_CAP.get();
        if (cap > 0 && data.getPointsSpent() >= cap) return false;

        // Resolve node from registry
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        if (node == null) return false;

        // Already unlocked
        if (data.isUnlocked(nodeId)) return false;

        // Check parent is unlocked (root nodes have no parent requirement)
        if (!node.isRoot() && !data.isUnlocked(node.parentId())) return false;

        // Check visibility — must be SCRAMBLED (distance 1) to unlock
        if (getVisibility(data, nodeId) != NodeVisibility.SCRAMBLED) return false;

        return data.unlock(nodeId);
    }

    // ── Visibility ───────────────────────────────────────────────────────────

    /**
     * Computes the visibility state of a node for the given player.
     */
    public static NodeVisibility getVisibility(Player player, Identifier nodeId) {
        return getVisibility(getTreeData(player), nodeId);
    }

    /**
     * Computes the visibility state of a node from the given tree data.
     */
    public static NodeVisibility getVisibility(SkillTreeData data, Identifier nodeId) {
        if (data.isUnlocked(nodeId)) return NodeVisibility.READABLE;

        int distance = getDistanceToUnlocked(data, nodeId);
        return switch (distance) {
            case 1 -> NodeVisibility.SCRAMBLED;
            case 2 -> NodeVisibility.TEASED;
            default -> NodeVisibility.INVISIBLE;
        };
    }

    /**
     * Returns the distance (in hops toward root) to the nearest unlocked ancestor.
     * Returns Integer.MAX_VALUE if no unlocked ancestor is found.
     */
    private static int getDistanceToUnlocked(SkillTreeData data, Identifier nodeId) {
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        if (node == null) return Integer.MAX_VALUE;

        int distance = 0;
        Identifier current = nodeId;

        while (current != null) {
            if (data.isUnlocked(current)) return distance;
            SkillNode currentNode = SkillTreeRegistries.NODE_REGISTRY.getValue(current);
            if (currentNode == null) break;
            current = currentNode.parentId();
            distance++;
        }

        return Integer.MAX_VALUE;
    }

    // ── Query helpers ────────────────────────────────────────────────────────

    /**
     * Returns true if the player has the given node unlocked.
     */
    public static boolean hasNode(Player player, Identifier nodeId) {
        return getTreeData(player).isUnlocked(nodeId);
    }

    /**
     * Returns all children of a given node in the registry.
     */
    public static List<Identifier> getChildren(Identifier parentId) {
        List<Identifier> children = new ArrayList<>();
        for (Map.Entry<ResourceKey<SkillNode>, SkillNode> entry : SkillTreeRegistries.NODE_REGISTRY.entrySet()) {
            if (parentId.equals(entry.getValue().parentId())) {
                children.add(entry.getKey().identifier());
            }
        }
        return children;
    }

    /**
     * Returns all root nodes for a given tree.
     */
    public static List<Identifier> getRootNodes(Identifier treeId) {
        List<Identifier> roots = new ArrayList<>();
        for (Map.Entry<ResourceKey<SkillNode>, SkillNode> entry : SkillTreeRegistries.NODE_REGISTRY.entrySet()) {
            if (entry.getValue().treeId().equals(treeId) && entry.getValue().isRoot()) {
                roots.add(entry.getKey().identifier());
            }
        }
        return roots;
    }

    /**
     * Returns the remaining unlock points available for the player.
     * Returns -1 if no cap is configured (unlimited).
     */
    public static int getRemainingPoints(Player player) {
        int cap = SoulmarkConfig.SKILL_POINT_CAP.get();
        if (cap <= 0) return -1;
        return cap - getTreeData(player).getPointsSpent();
    }

    /**
     * Returns all unlocked node IDs of a specific type within a given tree.
     * Useful for job mods building equip menus (e.g. "show all unlocked abilities from my tree").
     */
    public static List<Identifier> getUnlockedNodesByType(Player player, Identifier treeId, NodeType type) {
        SkillTreeData data = getTreeData(player);
        List<Identifier> result = new ArrayList<>();
        for (Map.Entry<ResourceKey<SkillNode>, SkillNode> entry : SkillTreeRegistries.NODE_REGISTRY.entrySet()) {
            SkillNode node = entry.getValue();
            Identifier nodeId = entry.getKey().identifier();
            if (node.treeId().equals(treeId) && node.nodeType() == type && data.isUnlocked(nodeId)) {
                result.add(nodeId);
            }
        }
        return result;
    }
}
