package com.nerosro.soulmark.mana;

import com.nerosro.soulmark.SoulmarkConfig;
import com.nerosro.soulmark.capability.SoulmarkAttachments;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

/**
 * Public utility API for the mana system.
 * Job mods should use this class to interact with player mana.
 */
public final class ManaUtil {

    private ManaUtil() {} // no instantiation

    // ── Origin roll ──────────────────────────────────────────────────────────

    /**
     * Rolls origin mana values with an inverse pool/regen relationship
     * and writes them into the given ManaData.
     * A small random deviation is applied to both values so that even extreme rolls
     * don't lock the player into the absolute min/max of the opposite stat.
     */
    public static void rollOrigin(ManaData mana, RandomSource random) {
        float spectrum = random.nextFloat(); // 0.0 = low pool/high regen, 1.0 = high pool/low regen

        float minPool = SoulmarkConfig.MANA_MIN_POOL.get().floatValue();
        float maxPool = SoulmarkConfig.MANA_MAX_POOL.get().floatValue();
        float minRegen = SoulmarkConfig.MANA_MIN_REGEN.get().floatValue();
        float maxRegen = SoulmarkConfig.MANA_MAX_REGEN.get().floatValue();
        int baseDelay = SoulmarkConfig.MANA_BASE_DELAY_TICKS.get();

        float poolRange = maxPool - minPool;
        float regenRange = maxRegen - minRegen;

        // Base values from inverse relationship
        float basePool = Mth.lerp(spectrum, minPool, maxPool);
        float baseRegen = Mth.lerp(1.0f - spectrum, minRegen, maxRegen);

        // Apply a small random deviation (±10% of the total range)
        float deviation = 0.10f;
        float poolOffset = (random.nextFloat() * 2f - 1f) * deviation * poolRange;
        float regenOffset = (random.nextFloat() * 2f - 1f) * deviation * regenRange;

        float pool = Mth.clamp(basePool + poolOffset, minPool, maxPool);
        float regen = Mth.clamp(baseRegen + regenOffset, minRegen, maxRegen);

        mana.setOrigin(pool, regen, baseDelay);
    }

    // ── Public API for job mods ──────────────────────────────────────────────

    /**
     * Returns the player's ManaData. Never null (returns uninitialized data if not yet rolled).
     */
    public static ManaData getMana(Player player) {
        return player.getData(SoulmarkAttachments.MANA.get());
    }

    /**
     * Attempts to spend mana. Returns true if the player had enough and mana was deducted.
     * Resets the regen delay on success.
     */
    public static boolean trySpend(Player player, float cost) {
        ManaData mana = getMana(player);
        return mana.spend(cost);
    }

    /**
     * Returns true if the player currently has at least the given amount of mana.
     */
    public static boolean hasEnough(Player player, float cost) {
        return getMana(player).hasEnough(cost);
    }

    /**
     * Returns the player's current mana as a fraction of their max pool (0.0 to 1.0).
     */
    public static float getManaFraction(Player player) {
        ManaData mana = getMana(player);
        if (mana.getMaxPool() <= 0) return 0f;
        return mana.getCurrentMana() / mana.getMaxPool();
    }
}
