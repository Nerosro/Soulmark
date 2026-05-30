package be.nerosro.soulmark.skilltree;

import be.nerosro.soulmark.capability.SoulmarkAttachments;
import net.minecraft.world.entity.player.Player;

/**
 * Public utility API for the shared skill point wallet.
 * <p>
 * Flow for job mods:
 * 1. Job mod reaches a milestone
 * 2. Job mod calls getMilestoneCount() to check how many of that tier have been claimed
 * 3. Job mod decides payout based on its own table
 * 4. Job mod calls awardMilestone() with the tier and payout amount
 * <p>
 * For spending:
 * - Job mod calls trySpend() when the player wants to unlock a node
 */
public final class SkillPointUtil {

    private SkillPointUtil() {}

    /**
     * Returns the player's SkillPointData (the shared wallet).
     */
    public static SkillPointData getPointData(Player player) {
        return player.getData(SoulmarkAttachments.SKILL_POINTS.get());
    }

    /**
     * Returns how many milestones of the given tier this player has claimed.
     * Job mods use this to look up their payout table.
     */
    public static int getMilestoneCount(Player player, int tier) {
        return getPointData(player).getMilestoneCount(tier);
    }

    /**
     * Awards a milestone: records the tier claim and adds points to the wallet.
     * Called by job mods after they've determined the payout.
     */
    public static void awardMilestone(Player player, int tier, int points) {
        SkillPointData data = getPointData(player);
        data.recordMilestone(tier);
        data.awardPoints(points);
        player.setData(SoulmarkAttachments.SKILL_POINTS.get(), data);
    }

    /**
     * Awards a milestone with de-duplication. If the milestone ID has already been claimed,
     * does nothing and returns 0. Otherwise records the tier, claims the ID, awards points,
     * and returns the payout.
     */
    public static int awardMilestone(Player player, String milestoneId, int tier, int points) {
        SkillPointData data = getPointData(player);
        if (data.hasClaimed(milestoneId)) return 0;
        data.claim(milestoneId);
        data.recordMilestone(tier);
        data.awardPoints(points);
        player.setData(SoulmarkAttachments.SKILL_POINTS.get(), data);
        return points;
    }

    /**
     * Returns true if the given milestone ID has already been claimed by this player.
     */
    public static boolean hasClaimed(Player player, String milestoneId) {
        return getPointData(player).hasClaimed(milestoneId);
    }

    /**
     * Attempts to spend points from the shared wallet.
     * Returns true if the player had enough points and they were deducted.
     */
    public static boolean trySpend(Player player, int cost) {
        SkillPointData data = getPointData(player);
        boolean success = data.trySpend(cost);
        if (success) {
            player.setData(SoulmarkAttachments.SKILL_POINTS.get(), data);
        }
        return success;
    }

    /**
     * Returns the number of unspent points available to the player.
     */
    public static int getAvailablePoints(Player player) {
        return getPointData(player).getAvailablePoints();
    }

    /**
     * Returns total points ever earned by the player across all milestones.
     */
    public static int getTotalEarned(Player player) {
        return getPointData(player).getTotalEarned();
    }

    /**
     * Returns total points spent by the player across all trees.
     */
    public static int getTotalSpent(Player player) {
        return getPointData(player).getTotalSpent();
    }
}

