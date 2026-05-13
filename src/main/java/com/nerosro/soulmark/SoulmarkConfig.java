package com.nerosro.soulmark;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Central config for all Soulmark systems.
 * Produces a single soulmark-common.toml file.
 */
public class SoulmarkConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ── Mana ─────────────────────────────────────────────────────────────────

    public static final ModConfigSpec.DoubleValue MANA_MIN_POOL = BUILDER
            .comment("Minimum mana pool a player can roll at origin")
            .defineInRange("mana.minPool", 85.0, 1.0, 10000.0);

    public static final ModConfigSpec.DoubleValue MANA_MAX_POOL = BUILDER
            .comment("Maximum mana pool a player can roll at origin")
            .defineInRange("mana.maxPool", 115.0, 1.0, 10000.0);

    public static final ModConfigSpec.DoubleValue MANA_MIN_REGEN = BUILDER
            .comment("Minimum mana regen (per second) a player can roll at origin")
            .defineInRange("mana.minRegen", 1.0, 0.1, 1000.0);

    public static final ModConfigSpec.DoubleValue MANA_MAX_REGEN = BUILDER
            .comment("Maximum mana regen (per second) a player can roll at origin")
            .defineInRange("mana.maxRegen", 5.0, 0.1, 1000.0);

    public static final ModConfigSpec.IntValue MANA_BASE_DELAY_TICKS = BUILDER
            .comment("Base delay (in ticks) before mana regen begins after casting. 20 ticks = 1 second")
            .defineInRange("mana.baseDelayTicks", 40, 1, 6000);

    // ── Traits ───────────────────────────────────────────────────────────────

    public static final ModConfigSpec.IntValue TRAIT_WEIGHT_COMMON = BUILDER
            .comment("Relative weight for Common traits in the roll pool")
            .defineInRange("traits.weights.common", 60, 1, 1000);

    public static final ModConfigSpec.IntValue TRAIT_WEIGHT_UNCOMMON = BUILDER
            .comment("Relative weight for Uncommon traits in the roll pool")
            .defineInRange("traits.weights.uncommon", 30, 1, 1000);

    public static final ModConfigSpec.IntValue TRAIT_WEIGHT_RARE = BUILDER
            .comment("Relative weight for Rare traits in the roll pool")
            .defineInRange("traits.weights.rare", 15, 1, 1000);

    public static final ModConfigSpec.IntValue TRAIT_WEIGHT_LEGENDARY = BUILDER
            .comment("Relative weight for Legendary traits in the roll pool")
            .defineInRange("traits.weights.legendary", 5, 1, 1000);

    public static final ModConfigSpec.IntValue TRAIT_WEIGHT_EXOTIC = BUILDER
            .comment("Relative weight for Exotic traits in the roll pool")
            .defineInRange("traits.weights.exotic", 1, 1, 1000);

    // ── Skill Tree ───────────────────────────────────────────────────────────

    public static final ModConfigSpec.IntValue SKILL_POINT_CAP = BUILDER
            .comment("Maximum number of skill points a player can spend total. Set to 0 for unlimited.")
            .defineInRange("skilltree.pointCap", 30, 0, 10000);

    public static final ModConfigSpec SPEC = BUILDER.build();
}

