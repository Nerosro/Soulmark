package com.nerosro.soulmark.skilltree;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores the player's skill tree progress.
 * Tracks which nodes are unlocked and how many points have been spent.
 */
public class SkillTreeData implements ValueIOSerializable {

    private final Set<Identifier> unlockedNodes = new HashSet<>();
    private int pointsSpent;

    public SkillTreeData() {}

    // ── Unlock logic ─────────────────────────────────────────────────────────

    /**
     * Unlocks a node. Returns true if the node was newly unlocked.
     */
    public boolean unlock(Identifier nodeId) {
        boolean added = unlockedNodes.add(nodeId);
        if (added) pointsSpent++;
        return added;
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

    public int getPointsSpent() {
        return pointsSpent;
    }

    // ── Serialization ────────────────────────────────────────────────────────

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("pointsSpent", pointsSpent);
        output.putInt("nodeCount", unlockedNodes.size());
        int i = 0;
        for (Identifier id : unlockedNodes) {
            output.putString("node_" + i, id.toString());
            i++;
        }
    }

    @Override
    public void deserialize(ValueInput input) {

        pointsSpent = input.getIntOr("pointsSpent", 0);
        int count = input.getIntOr("nodeCount", 0);
        unlockedNodes.clear();
        for (int i = 0; i < count; i++) {
            input.getString("node_" + i).map(Identifier::parse).ifPresent(unlockedNodes::add);
        }
    }
}
