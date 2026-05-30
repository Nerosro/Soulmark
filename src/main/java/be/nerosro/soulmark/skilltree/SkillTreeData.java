package be.nerosro.soulmark.skilltree;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.*;

/**
 * Stores the player's skill tree progress.
 * Tracks which nodes are unlocked, per-tree spend totals, and active exclusion groups.
 * Point spending is handled by SkillPointData.
 */
public class SkillTreeData implements ValueIOSerializable {

    private final Set<Identifier> unlockedNodes = new HashSet<>();
    private final Map<Identifier, Integer> spentPerTree = new HashMap<>();  // treeId -> total spent in that tree
    private final Set<String> activeExclusions = new HashSet<>();           // exclusion groups that have been committed

    public SkillTreeData() {}

    // ── Unlock logic ─────────────────────────────────────────────────────────

    /**
     * Unlocks a node. Returns true if the node was newly unlocked.
     */
    public boolean unlock(Identifier nodeId) {
        return unlockedNodes.add(nodeId);
    }

    /**
     * Returns true if the given node is unlocked.
     */
    public boolean isUnlocked(Identifier nodeId) {
        return unlockedNodes.contains(nodeId);
    }

    /**
     * Returns an unmodifiable view of all unlocked node IDs.
     */
    public Set<Identifier> getUnlockedNodes() {
        return Collections.unmodifiableSet(unlockedNodes);
    }

    // ── Per-tree spend tracking ──────────────────────────────────────────────

    /**
     * Records that points were spent in a specific tree.
     * Used by job mods to determine internal overreach (e.g. Elemancy recovery case).
     */
    public void recordTreeSpend(Identifier treeId, int amount) {
        spentPerTree.merge(treeId, amount, Integer::sum);
    }

    /**
     * Returns the total number of points spent in a specific tree.
     */
    public int getSpentInTree(Identifier treeId) {
        return spentPerTree.getOrDefault(treeId, 0);
    }

    // ── Exclusion groups ─────────────────────────────────────────────────────

    /**
     * Marks an exclusion group as active (committed).
     * Other nodes in the same group become permanently locked out.
     */
    public void activateExclusionGroup(String group) {
        activeExclusions.add(group);
    }

    /**
     * Returns true if the given exclusion group has been committed.
     */
    public boolean isExclusionActive(String group) {
        return activeExclusions.contains(group);
    }

    /**
     * Returns all active exclusion group names.
     */
    public Set<String> getActiveExclusions() {
        return Collections.unmodifiableSet(activeExclusions);
    }

    // ── Serialization ────────────────────────────────────────────────────────

    // Format: indexed keys ("node_0", "node_1", "treeSpendId_0", etc.) with count prefixes.
    // This is simple and correct. Do not change without a migration path for existing saves.

    @Override
    public void serialize(ValueOutput output) {
        // Unlocked nodes
        output.putInt("nodeCount", unlockedNodes.size());
        int i = 0;
        for (Identifier id : unlockedNodes) {
            output.putString("node_" + i, id.toString());
            i++;
        }

        // Per-tree spend
        output.putInt("treeSpendCount", spentPerTree.size());
        i = 0;
        for (Map.Entry<Identifier, Integer> entry : spentPerTree.entrySet()) {
            output.putString("treeSpendId_" + i, entry.getKey().toString());
            output.putInt("treeSpendAmt_" + i, entry.getValue());
            i++;
        }

        // Exclusion groups
        output.putInt("exclusionCount", activeExclusions.size());
        i = 0;
        for (String group : activeExclusions) {
            output.putString("exclusion_" + i, group);
            i++;
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        // Unlocked nodes
        int count = input.getIntOr("nodeCount", 0);
        unlockedNodes.clear();
        for (int i = 0; i < count; i++) {
            input.getString("node_" + i).map(Identifier::parse).ifPresent(unlockedNodes::add);
        }

        // Per-tree spend
        int treeSpendCount = input.getIntOr("treeSpendCount", 0);
        spentPerTree.clear();
        for (int i = 0; i < treeSpendCount; i++) {
            var id = input.getString("treeSpendId_" + i).map(Identifier::parse);
            int amt = input.getIntOr("treeSpendAmt_" + i, 0);
            id.ifPresent(identifier -> {
                if (amt > 0) spentPerTree.put(identifier, amt);
            });
        }

        // Exclusion groups
        int exclusionCount = input.getIntOr("exclusionCount", 0);
        activeExclusions.clear();
        for (int i = 0; i < exclusionCount; i++) {
            input.getString("exclusion_" + i).ifPresent(activeExclusions::add);
        }
    }
}
