package be.nerosro.soulmark.skilltree;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Stores the player's shared skill point wallet and milestone claim history.
 * <p>
 * Soulmark is the bookkeeper:
 * - Tracks available (earned - spent) points
 * - Tracks how many milestones per tier have been claimed
 * <p>
 * Job mods decide:
 * - When a milestone is reached
 * - How many points to award based on the tier count
 */
public class SkillPointData implements ValueIOSerializable {

    private int availablePoints;
    private int totalEarned;
    private int totalSpent;
    private final Map<Integer, Integer> milestoneCounts = new HashMap<>(); // tier -> times claimed
    private final Set<String> claimedMilestones = new HashSet<>(); // unique milestone IDs

    public SkillPointData() {}

    // ── Point wallet ─────────────────────────────────────────────────────────

    /**
     * Awards points to the player's shared wallet.
     * Called by job mods after they decide the payout for a milestone.
     */
    public void awardPoints(int amount) {
        if (amount <= 0) return;
        availablePoints += amount;
        totalEarned += amount;
    }

    /**
     * Attempts to spend points from the shared wallet.
     * Returns true if successful, false if insufficient points.
     */
    public boolean trySpend(int cost) {
        if (cost <= 0 || availablePoints < cost) return false;
        availablePoints -= cost;
        totalSpent += cost;
        return true;
    }

    /**
     * Returns the number of unspent points available.
     */
    public int getAvailablePoints() {
        return availablePoints;
    }

    /**
     * Returns total points ever earned across all milestones.
     */
    public int getTotalEarned() {
        return totalEarned;
    }

    /**
     * Returns total points spent across all trees.
     */
    public int getTotalSpent() {
        return totalSpent;
    }

    // ── Milestone tracking ───────────────────────────────────────────────────

    /**
     * Records that a milestone of the given tier was claimed.
     * Call this when awarding points for a milestone.
     */
    public void recordMilestone(int tier) {
        milestoneCounts.merge(tier, 1, Integer::sum);
    }

    /**
     * Returns how many milestones of the given tier have been claimed.
     * Job mods use this to determine their payout amount.
     */
    public int getMilestoneCount(int tier) {
        return milestoneCounts.getOrDefault(tier, 0);
    }

    /**
     * Returns true if the milestone with the given unique ID has already been claimed.
     */
    public boolean hasClaimed(String milestoneId) {
        return claimedMilestones.contains(milestoneId);
    }

    /**
     * Records the given milestone ID as claimed. Does not award points — call awardPoints separately.
     */
    public void claim(String milestoneId) {
        claimedMilestones.add(milestoneId);
    }

    // ── Serialization ────────────────────────────────────────────────────────

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("availablePoints", availablePoints);
        output.putInt("totalEarned", totalEarned);
        output.putInt("totalSpent", totalSpent);

        output.putInt("milestoneTierCount", milestoneCounts.size());
        int i = 0;
        for (Map.Entry<Integer, Integer> entry : milestoneCounts.entrySet()) {
            output.putInt("msTier_" + i, entry.getKey());
            output.putInt("msCount_" + i, entry.getValue());
            i++;
        }

        output.putInt("claimedCount", claimedMilestones.size());
        int j = 0;
        for (String id : claimedMilestones) {
            output.putString("claimed_" + j, id);
            j++;
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        availablePoints = input.getIntOr("availablePoints", 0);
        totalEarned = input.getIntOr("totalEarned", 0);
        totalSpent = input.getIntOr("totalSpent", 0);

        milestoneCounts.clear();
        int tierCount = input.getIntOr("milestoneTierCount", 0);
        for (int i = 0; i < tierCount; i++) {
            int tier = input.getIntOr("msTier_" + i, 0);
            int count = input.getIntOr("msCount_" + i, 0);
            if (count > 0) {
                milestoneCounts.put(tier, count);
            }
        }

        claimedMilestones.clear();
        int claimedCount = input.getIntOr("claimedCount", 0);
        for (int j = 0; j < claimedCount; j++) {
            String id = input.getStringOr("claimed_" + j, "");
            if (!id.isEmpty()) {
                claimedMilestones.add(id);
            }
        }
    }
}

