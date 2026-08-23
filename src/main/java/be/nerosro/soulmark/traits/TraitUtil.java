package be.nerosro.soulmark.traits;

import be.nerosro.soulmark.capability.SoulmarkAttachments;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Public utility API for the trait system.
 * Job mods should use this class to read player traits.
 */
public final class TraitUtil {

    private TraitUtil() {}

    /**
     * Rolls traits for a player and writes the result into the given TraitData.
     */
    public static void rollOrigin(TraitData data, RandomSource random) {
        TraitRoller.RollResult result = TraitRoller.roll(random);
        data.setTraits(result.boostIds(), result.neutralId(), result.penaltyIds());
    }

    /**
     * Returns the player's TraitData.
     */
    public static TraitData getTraitData(Player player) {
        return player.getData(SoulmarkAttachments.TRAITS.get());
    }

    /**
     * Returns the player's boost traits (may be more than one if Overmarked).
     */
    public static List<Trait> getBoostTraits(Player player) {
        return getTraitData(player).getBoostTraits();
    }

    /**
     * Returns the player's neutral trait, or null if not set or mod removed.
     */
    @Nullable
    public static Trait getNeutralTrait(Player player) {
        return getTraitData(player).getNeutralTrait();
    }

    /**
     * Returns the player's penalty traits (may be more than one if Overmarked).
     */
    public static List<Trait> getPenaltyTraits(Player player) {
        return getTraitData(player).getPenaltyTraits();
    }

    /**
     * Returns true if the player has the Markless trait (exclusive, no other traits).
     */
    public static boolean isMarkless(Player player) {
        Trait neutral = getNeutralTrait(player);
        return neutral != null && neutral.isMarkless();
    }

    /**
     * Returns true if the player has the Overmarked trait.
     */
    public static boolean isOvermarked(Player player) {
        Trait neutral = getNeutralTrait(player);
        return neutral != null && neutral.isOvermarked();
    }

    // ── Discovery state ─────────────────────────────────────────────────────

    private static final String REVEALED_KEY = "soulmark_traits_revealed";
    private static final String SCARS_REVEALED_KEY = "elemancy_scars_revealed";

    /**
     * Returns true if the player's traits have been revealed (e.g. via the Mirror).
     */
    public static boolean isTraitsRevealed(Player player) {
        return player.getPersistentData().getBoolean(REVEALED_KEY).orElse(false);
    }

    /**
     * Marks the player's traits as revealed. Call this after the Mirror reveal moment.
     */
    public static void revealTraits(Player player) {
        player.getPersistentData().putBoolean(REVEALED_KEY, true);
    }

    /**
     * Resets the traits discovery state. Dev/testing use only.
     */
    public static void resetTraitsReveal(Player player) {
        player.getPersistentData().remove(REVEALED_KEY);
    }

    /**
     * Returns true if the player's scars reference has been revealed (Tome + Mirror).
     */
    public static boolean isScarsRevealed(Player player) {
        return player.getPersistentData().getBoolean(SCARS_REVEALED_KEY).orElse(false);
    }

    /**
     * Marks the player's scars reference as revealed. Call when using Tome on Mirror.
     */
    public static void revealScars(Player player) {
        player.getPersistentData().putBoolean(SCARS_REVEALED_KEY, true);
    }

    /**
     * Resets the scars discovery state. Dev/testing use only.
     */
    public static void resetScarsReveal(Player player) {
        player.getPersistentData().remove(SCARS_REVEALED_KEY);
    }
}

