package be.nerosro.soulmark.traits;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles the trait rolling logic, enforcing all rules from the trait design doc.
 * <p>
 * Rules enforced:
 * - 1 trait from each pool (Boost, Neutral, Penalty)
 * - Weighted random selection using configured weight values
 * - Max 1 Legendary+ trait per player
 * - At least 1 Rare+ among the three base traits
 * - Markless cancels all other traits
 * - Overmarked adds an extra boost and penalty
 */
public final class TraitRoller {

    private TraitRoller() {
    }

    /**
     * Rolls a full set of traits for a player.
     * Returns a RollResult containing trait IDs.
     */
    public static RollResult roll(RandomSource random) {
        // Gather all registered traits by type
        List<TraitEntry> boostPool = getPool(TraitType.BOOST);
        List<TraitEntry> neutralPool = getPool(TraitType.NEUTRAL);
        List<TraitEntry> penaltyPool = getPool(TraitType.PENALTY);

        // Roll neutral first (Markless and Overmarked live here)
        TraitEntry neutral = rollFromPool(neutralPool, random, false);
        if (isMarkless(neutral)) {
            return RollResult.exclusiveNeutral(neutral.id());
        }
        boolean hasLegendaryPlus = neutral != null && isLegendaryPlus(neutral.trait());

        TraitEntry boost = rollFromPool(boostPool, random, hasLegendaryPlus);
        if (boost != null && isLegendaryPlus(boost.trait())) {
            hasLegendaryPlus = true;
        }

        TraitEntry penalty = rollFromPool(penaltyPool, random, hasLegendaryPlus);

        // Enforce: at least 1 Rare+ among the three base traits
        if (!hasRarePlus(boost, neutral, penalty)) {
            int slot = random.nextInt(3);
            List<TraitEntry> targetPool = switch (slot) {
                case 0 -> boostPool;
                case 1 -> neutralPool;
                default -> penaltyPool;
            };
            List<TraitEntry> rarePool = targetPool.stream()
                    .filter(e -> e.trait().weight().ordinal() >= TraitWeight.RARE.ordinal())
                    .toList();
            if (!rarePool.isEmpty()) {
                TraitEntry rerolled = rollFromPool(rarePool, random, hasLegendaryPlus);
                switch (slot) {
                    case 0 -> boost = rerolled;
                    case 1 -> neutral = rerolled;
                    default -> penalty = rerolled;
                }
            }
        }

        // Post-reroll Markless check
        if (isMarkless(neutral)) {
            return RollResult.exclusiveNeutral(neutral.id());
        }

        // Build boost and penalty lists
        List<Identifier> boosts = new ArrayList<>();
        if (boost != null) boosts.add(boost.id());
        List<Identifier> penalties = new ArrayList<>();
        if (penalty != null) penalties.add(penalty.id());

        // Overmarked: roll an extra boost and penalty (any rarity, but no duplicate trait names)
        if (neutral != null && neutral.trait().isOvermarked()) {
            TraitEntry extraBoost = rollExcluding(boostPool, boosts, random);
            if (extraBoost != null) boosts.add(extraBoost.id());

            TraitEntry extraPenalty = rollExcluding(penaltyPool, penalties, random);
            if (extraPenalty != null) penalties.add(extraPenalty.id());
        }

        return new RollResult(
                boosts,
                neutral != null ? neutral.id() : null,
                penalties
        );
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private static List<TraitEntry> getPool(TraitType type) {
        List<TraitEntry> pool = new ArrayList<>();
        for (Map.Entry<ResourceKey<Trait>, Trait> entry : TraitRegistries.TRAIT_REGISTRY.entrySet()) {
            if (entry.getValue().type() == type) {
                pool.add(new TraitEntry(entry.getKey().identifier(), entry.getValue()));
            }
        }
        return pool;
    }

    private static TraitEntry rollFromPool(List<TraitEntry> pool, RandomSource random, boolean blockLegendaryPlus) {
        // Build weighted list
        List<TraitEntry> candidates = pool;
        if (blockLegendaryPlus) {
            candidates = pool.stream()
                    .filter(e -> !isLegendaryPlus(e.trait()))
                    .toList();
        }
        if (candidates.isEmpty()) return null;

        int totalWeight = candidates.stream()
                .mapToInt(traitEntry -> traitEntry.trait().weight().getConfiguredWeight())
                .sum();

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (TraitEntry entry : candidates) {
            cumulative += entry.trait().weight().getConfiguredWeight();
            if (roll < cumulative) {
                return entry;
            }
        }
        throw new IllegalStateException("Weighted selection failed — cumulative weight did not cover roll");
    }

    private static boolean isLegendaryPlus(Trait trait) {
        return trait.weight().ordinal() >= TraitWeight.LEGENDARY.ordinal();
    }

    private static boolean isMarkless(TraitEntry entry) {
        return entry != null && entry.trait().isMarkless();
    }

    /**
     * Rolls from a pool excluding traits already rolled (by registry ID).
     * Used by Overmarked to prevent duplicate traits.
     */
    private static TraitEntry rollExcluding(List<TraitEntry> pool, List<Identifier> alreadyRolled, RandomSource random) {
        List<TraitEntry> filtered = pool.stream()
                .filter(e -> !alreadyRolled.contains(e.id()))
                .toList();
        return rollFromPool(filtered, random, false);
    }

    private static boolean hasRarePlus(TraitEntry boost, TraitEntry neutral, TraitEntry penalty) {
        return (boost != null && boost.trait().weight().ordinal() >= TraitWeight.RARE.ordinal())
                || (neutral != null && neutral.trait().weight().ordinal() >= TraitWeight.RARE.ordinal())
                || (penalty != null && penalty.trait().weight().ordinal() >= TraitWeight.RARE.ordinal());
    }

    // ── Data classes ─────────────────────────────────────────────────────────

    private record TraitEntry(Identifier id, Trait trait) {
    }

    public record RollResult(
            List<Identifier> boostIds,
            Identifier neutralId,
            List<Identifier> penaltyIds
    ) {
        public static final RollResult EMPTY = new RollResult(List.of(), null, List.of());

        public static RollResult exclusiveNeutral(Identifier neutralId) {
            return new RollResult(List.of(), neutralId, List.of());
        }

        public boolean isEmpty() {
            return boostIds.isEmpty() && neutralId == null && penaltyIds.isEmpty();
        }
    }
}
