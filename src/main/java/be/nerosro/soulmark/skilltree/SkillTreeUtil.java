package be.nerosro.soulmark.skilltree;

import be.nerosro.soulmark.capability.SoulmarkAttachments;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

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
     * Result of an attempted unlock. SUCCESS means the node was unlocked.
     * Other values describe why the unlock failed.
     */
    public enum UnlockResult {
        SUCCESS,
        NODE_NOT_FOUND,
        ALREADY_UNLOCKED,
        PARENT_NOT_UNLOCKED,
        PREREQUISITE_NOT_MET,
        EXCLUDED,
        HIDDEN,
        NOT_VISIBLE,
        INSUFFICIENT_POINTS
    }

    /**
     * Attempts to unlock a node for the player.
     * Returns a result enum indicating success or the specific failure reason.
     */
    public static UnlockResult tryUnlockDetailed(Player player, Identifier nodeId) {
        SkillTreeData data = getTreeData(player);

        // Resolve node from registry
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        if (node == null) return UnlockResult.NODE_NOT_FOUND;

        // Already unlocked
        if (data.isUnlocked(nodeId)) return UnlockResult.ALREADY_UNLOCKED;

        // Check hidden condition first — hidden nodes can't even be considered
        if (!isRevealed(player, data, node)) return UnlockResult.HIDDEN;

        // Check parent is unlocked (root nodes have no parent requirement)
        if (!node.isRoot()) {
            if (node.parentMode() == ParentMode.ALL) {
                for (Identifier pid : node.parentIds()) {
                    if (!data.isUnlocked(pid)) return UnlockResult.PARENT_NOT_UNLOCKED;
                }
            } else {
                boolean anyUnlocked = node.parentIds().stream().anyMatch(data::isUnlocked);
                if (!anyUnlocked) return UnlockResult.PARENT_NOT_UNLOCKED;
            }
        }

        // Check extra prerequisites (cross-tree and same-tree)
        for (Identifier prereq : node.prerequisites()) {
            if (!data.isUnlocked(prereq)) return UnlockResult.PREREQUISITE_NOT_MET;
        }

        // Check exclusion groups — if this node's group is already committed by another node
        if (node.exclusionGroup() != null && data.isExclusionActive(node.exclusionGroup())
                && !isNodeInActiveExclusionPath(data, nodeId, node.exclusionGroup())) {
            return UnlockResult.EXCLUDED;
        }

        // Check visibility — must be UNLOCKABLE (distance 1) to unlock
        if (getVisibility(data, nodeId) != NodeVisibility.UNLOCKABLE) return UnlockResult.NOT_VISIBLE;

        // Spend from shared wallet (use node's cost)
        if (!SkillPointUtil.trySpend(player, node.cost())) return UnlockResult.INSUFFICIENT_POINTS;

        // Perform the unlock
        data.unlock(nodeId);

        // Track per-tree spend
        data.recordTreeSpend(node.treeId(), node.cost());

        // Activate exclusion group if this is a specialization node with one
        if (node.exclusionGroup() != null && node.nodeType() == NodeType.SPECIALIZATION) {
            data.activateExclusionGroup(node.exclusionGroup());
        }

        // Persist
        player.setData(SoulmarkAttachments.SKILL_TREE.get(), data);

        return UnlockResult.SUCCESS;
    }

    /**
     * Attempts to unlock a node for the player.
     * Returns true if successful, false otherwise.
     * Use {@link #tryUnlockDetailed} for specific failure reasons.
     */
    public static boolean tryUnlock(Player player, Identifier nodeId) {
        return tryUnlockDetailed(player, nodeId) == UnlockResult.SUCCESS;
    }

    // ── Hidden condition evaluation ──────────────────────────────────────────

    /**
     * Returns true if a node with a hidden condition should be revealed (visible).
     * Nodes without a hidden condition are always revealed.
     */
    public static boolean isRevealed(Player player, SkillTreeData data, SkillNode node) {
        HiddenCondition condition = node.hiddenCondition();
        if (condition == null) return true;

        return switch (condition.type()) {
            case NODE_UNLOCKED -> {
                if (condition.detail() == null) yield false;
                yield data.isUnlocked(Identifier.parse(condition.detail()));
            }
            case MOD_LOADED -> {
                if (condition.detail() == null) yield false;
                yield ModList.get().isLoaded(condition.detail());
            }
            case CUSTOM -> {
                if (condition.detail() == null) yield false;
                HiddenConditionRegistry.Evaluator evaluator = HiddenConditionRegistry.get(condition.detail());
                yield evaluator != null && evaluator.test(player, condition.detail());
            }
        };
    }

    /**
     * Returns true if a node is revealed for the given player.
     */
    public static boolean isRevealed(Player player, Identifier nodeId) {
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        if (node == null) return false;
        return isRevealed(player, getTreeData(player), node);
    }

    // ── Exclusion helpers ────────────────────────────────────────────────────

    /**
     * Returns true if the given node is part of the path that activated its exclusion group.
     * This prevents the exclusion from blocking nodes that are in the same group but on the
     * already-chosen specialization branch.
     */
    private static boolean isNodeInActiveExclusionPath(SkillTreeData data, Identifier nodeId, String exclusionGroup) {
        // Walk up from this node; if any ancestor activated this group, allow it
        Identifier current = nodeId;
        while (current != null) {
            SkillNode n = SkillTreeRegistries.NODE_REGISTRY.getValue(current);
            if (n == null) break;
            if (data.isUnlocked(current) && exclusionGroup.equals(n.exclusionGroup())
                    && n.nodeType() == NodeType.SPECIALIZATION) {
                return true;
            }
            current = n.parentId();
        }
        return false;
    }

    /**
     * Returns true if the node is locked out by an active exclusion group.
     */
    public static boolean isExcluded(Player player, Identifier nodeId) {
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        if (node == null || node.exclusionGroup() == null) return false;
        SkillTreeData data = getTreeData(player);
        return data.isExclusionActive(node.exclusionGroup())
                && !isNodeInActiveExclusionPath(data, nodeId, node.exclusionGroup());
    }

    // ── Visibility ───────────────────────────────────────────────────────────

    /**
     * Computes the visibility state of a node for the given player.
     * Hidden nodes (unrevealed) are always INVISIBLE regardless of distance.
     */
    public static NodeVisibility getVisibility(Player player, Identifier nodeId) {
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        if (node == null) return NodeVisibility.INVISIBLE;
        SkillTreeData data = getTreeData(player);
        if (!isRevealed(player, data, node)) return NodeVisibility.INVISIBLE;
        return getVisibility(data, nodeId);
    }

    /**
     * Computes the visibility state of a node from the given tree data.
     * Does not check hidden conditions — use the Player overload for full checks.
     */
    public static NodeVisibility getVisibility(SkillTreeData data, Identifier nodeId) {
        if (data.isUnlocked(nodeId)) return NodeVisibility.READABLE;

        int distance = getDistanceToUnlocked(data, nodeId);
        return switch (distance) {
            case 1 -> NodeVisibility.UNLOCKABLE;
            case 2 -> NodeVisibility.SCRAMBLED;
            case 3 -> NodeVisibility.TEASED;
            default -> NodeVisibility.INVISIBLE;
        };
    }

    /**
     * Returns the distance (in hops toward root) to the nearest unlocked ancestor.
     * For multi-parent nodes, returns the minimum distance across all parent paths.
     * Returns Integer.MAX_VALUE if no unlocked ancestor is found.
     */
    private static int getDistanceToUnlocked(SkillTreeData data, Identifier nodeId) {
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        if (node == null) return Integer.MAX_VALUE;

        if (node.parentIds().isEmpty()) {
            // Root node — distance is 0 if unlocked, MAX otherwise
            return data.isUnlocked(nodeId) ? 0 : Integer.MAX_VALUE;
        }

        int minDistance = Integer.MAX_VALUE;
        for (Identifier parentId : node.parentIds()) {
            int dist = getDistanceToUnlockedSingle(data, parentId, 1);
            minDistance = Math.min(minDistance, dist);
        }
        return minDistance;
    }

    private static int getDistanceToUnlockedSingle(SkillTreeData data, Identifier nodeId, int startDistance) {
        int distance = startDistance;
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
            if (entry.getValue().parentIds().contains(parentId)) {
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
     * Returns total points spent within a specific tree by this player.
     * Useful for job-specific checks like the Elemancy recovery case.
     */
    public static int getSpentInTree(Player player, Identifier treeId) {
        return getTreeData(player).getSpentInTree(treeId);
    }

    /**
     * Returns all unlocked node IDs of a specific type within a given tree.
     * Useful for job mods building equip menus (e.g. "show all unlocked abilities from my tree").
     */
    public static List<Identifier> getUnlockedNodesByType(Player player, Identifier treeId, NodeType type) {
        return getUnlockedNodesFiltered(player, treeId, node -> node.nodeType() == type);
    }

    /**
     * Returns all unlocked node IDs that have a specific tag within a given tree.
     */
    public static List<Identifier> getUnlockedNodesByTag(Player player, Identifier treeId, String tag) {
        return getUnlockedNodesFiltered(player, treeId, node -> node.tags().contains(tag));
    }

    private static List<Identifier> getUnlockedNodesFiltered(Player player, Identifier treeId, java.util.function.Predicate<SkillNode> predicate) {
        SkillTreeData data = getTreeData(player);
        List<Identifier> result = new ArrayList<>();
        for (Map.Entry<ResourceKey<SkillNode>, SkillNode> entry : SkillTreeRegistries.NODE_REGISTRY.entrySet()) {
            SkillNode node = entry.getValue();
            Identifier id = entry.getKey().identifier();
            if (node.treeId().equals(treeId) && predicate.test(node) && data.isUnlocked(id)) {
                result.add(id);
            }
        }
        return result;
    }

    /**
     * Returns all node IDs in a given tree, regardless of unlock state.
     * Useful for UI rendering and debug tools.
     */
    public static List<Identifier> getAllNodesInTree(Identifier treeId) {
        List<Identifier> result = new ArrayList<>();
        for (Map.Entry<ResourceKey<SkillNode>, SkillNode> entry : SkillTreeRegistries.NODE_REGISTRY.entrySet()) {
            if (entry.getValue().treeId().equals(treeId)) {
                result.add(entry.getKey().identifier());
            }
        }
        return result;
    }
}
