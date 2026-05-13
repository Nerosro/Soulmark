package com.nerosro.soulmark.affinity;

import com.nerosro.soulmark.capability.SoulmarkAttachments;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

/**
 * Public utility API for the affinity system.
 * Job mods should use this class to read player affinity.
 */
public final class AffinityUtil {

    private AffinityUtil() {}

    /**
     * Rolls a random affinity and writes it into the given AffinityData.
     */
    public static void rollOrigin(AffinityData data, RandomSource random) {
        Affinity[] values = Affinity.values();
        Affinity rolled = values[random.nextInt(values.length)];
        data.setOrigin(rolled);
    }

    /**
     * Returns the player's AffinityData.
     */
    public static AffinityData getAffinityData(Player player) {
        return player.getData(SoulmarkAttachments.AFFINITY.get());
    }

    /**
     * Returns the player's affinity, or null if not yet initialized.
     */
    public static Affinity getAffinity(Player player) {
        AffinityData data = getAffinityData(player);
        return data.isInitialized() ? data.getAffinity() : null;
    }
}

