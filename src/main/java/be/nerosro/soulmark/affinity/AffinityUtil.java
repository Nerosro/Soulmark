package be.nerosro.soulmark.affinity;

import be.nerosro.soulmark.capability.SoulmarkAttachments;
import be.nerosro.soulmark.element.Element;
import be.nerosro.soulmark.element.SoulmarkElements;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Public utility API for the affinity system.
 * Job mods should use this class to read player affinity.
 */
public final class AffinityUtil {

    private AffinityUtil() {}

    /**
     * Rolls a random base element and writes it into the given AffinityData.
     */
    public static void rollOrigin(AffinityData data, RandomSource random) {
        List<Element> baseElements = SoulmarkElements.baseElements();
        Element rolled = baseElements.get(random.nextInt(baseElements.size()));
        data.setOrigin(rolled);
    }

    /**
     * Returns the player's AffinityData.
     */
    public static AffinityData getAffinityData(Player player) {
        return player.getData(SoulmarkAttachments.AFFINITY.get());
    }

    /**
     * Returns the player's affinity element, or null if not yet initialized.
     */
    public static @Nullable Element getAffinity(Player player) {
        AffinityData data = getAffinityData(player);
        return data.isInitialized() ? data.getAffinity() : null;
    }

    /**
     * Returns true when the player has the given affinity.
     */
    public static boolean hasAffinity(Player player, Element element) {
        return getAffinity(player) == element;
    }

    /**
     * Returns true when the player's affinity opposes the given element.
     */
    public static boolean hasOppositeAffinity(Player player, Element element) {
        Element affinity = getAffinity(player);
        return affinity != null && SoulmarkElements.getOpposite(affinity) == element;
    }

    // ── Discovery state ─────────────────────────────────────────────────────

    private static final String REVEALED_KEY = "soulmark_affinity_revealed";

    /**
     * Returns true if the player has already been told their affinity by any mod.
     */
    public static boolean isAffinityRevealed(Player player) {
        return player.getPersistentData().getBoolean(REVEALED_KEY).orElse(false);
    }

    /**
     * Marks the player's affinity as discovered. Call this after your mod's reveal moment.
     */
    public static void revealAffinity(Player player) {
        player.getPersistentData().putBoolean(REVEALED_KEY, true);
    }

    /**
     * Resets the discovery state. Dev/testing use only.
     */
    public static void resetAffinityReveal(Player player) {
        player.getPersistentData().remove(REVEALED_KEY);
    }
}

