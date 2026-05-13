package com.nerosro.soulmark.traits;

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
 * - At least 1 Rare+ among the three traits
 * - Markless cancels all other traits
 */
public final class TraitRoller {

    private TraitRoller() {
    }

    /**
     * Rolls a full set of traits for a player.
     * Returns a RollResult containing the three trait IDs and scale tier.
     */
    public static RollResult roll(RandomSource random) {
        // Gather all registered traits by type
        List<TraitEntry> boostPool = getPool(TraitType.BOOST);
        List<TraitEntry> neutralPool = getPool(TraitType.NEUTRAL);
        List<TraitEntry> penaltyPool = getPool(TraitType.PENALTY);

        // Roll all three, respecting max 1 legendary+ rule
        // Try neutral first (Markless lives here)
        TraitEntry neutral = rollFromPool(neutralPool, random, false);
        if (isExclusive(neutral)) {
            return new RollResult(null, neutral.id(), null);
        }
        boolean hasLegendaryPlus = neutral != null && isLegendaryPlus(neutral.trait());

        TraitEntry boost = rollFromPool(boostPool, random, hasLegendaryPlus);
        if (boost != null && isLegendaryPlus(boost.trait())) {
            hasLegendaryPlus = true;
        }

        TraitEntry penalty = rollFromPool(penaltyPool, random, hasLegendaryPlus);

        // Enforce: at least 1 Rare+ among the three
        if (!hasRarePlus(boost, neutral, penalty)) {
            // Randomly pick which pool gets rerolled targeting Rare+ only
            int slot = random.nextInt(3); // 0=boost, 1=neutral, 2=penalty
            List<TraitEntry> targetPool = switch (slot) {
                case 0 -> boostPool;
                case 1 -> neutralPool;
                default -> penaltyPool;
            };
            List<TraitEntry> rarePool = targetPool.stream()
                    .filter(traitEntry -> traitEntry
                            .trait()
                            .weight()
                            .ordinal() >= TraitWeight.RARE.ordinal())
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

        // Check if the reroll produced an exclusive trait (e.g. Markless via neutral reroll)
        if (isExclusive(neutral)) {
            return new RollResult(null, neutral.id(), null);
        }

        return new RollResult(
                boost != null ? boost.id() : null,
                neutral != null ? neutral.id() : null,
                penalty != null ? penalty.id() : null
        );
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private static List<TraitEntry> getPool(TraitType type) {
        List<TraitEntry> pool = new ArrayList<>();
        for (Map.Entry<ResourceKey<Trait>, Trait> entry : SoulmarkRegistries.TRAIT_REGISTRY.entrySet()) {
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
        return candidates.getLast();
    }

    private static boolean isLegendaryPlus(Trait trait) {
        return trait.weight().ordinal() >= TraitWeight.LEGENDARY.ordinal();
    }

    private static boolean isExclusive(TraitEntry entry) {
        return entry != null && entry.trait().isExclusive();
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
            Identifier boostId,
            Identifier neutralId,
            Identifier penaltyId
    ) {
        public static final RollResult EMPTY = new RollResult(null, null, null);

        public boolean isEmpty() {
            return boostId == null && neutralId == null && penaltyId == null;
        }
    }
}
