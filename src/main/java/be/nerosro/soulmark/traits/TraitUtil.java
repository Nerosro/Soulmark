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
}

