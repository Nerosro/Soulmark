package be.nerosro.soulmark;

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
            .defineInRange("mana.minPool", 85.0, 50.0, 200.0);

    public static final ModConfigSpec.DoubleValue MANA_MAX_POOL = BUILDER
            .comment("Maximum mana pool a player can roll at origin")
            .defineInRange("mana.maxPool", 115.0, 50.0, 200.0);

    public static final ModConfigSpec.DoubleValue MANA_MIN_REGEN = BUILDER
            .comment("Minimum mana regen (per second) a player can roll at origin")
            .defineInRange("mana.minRegen", 1.0, 0.5, 10.0);

    public static final ModConfigSpec.DoubleValue MANA_MAX_REGEN = BUILDER
            .comment("Maximum mana regen (per second) a player can roll at origin")
            .defineInRange("mana.maxRegen", 5.0, 0.5, 10.0);

    public static final ModConfigSpec.IntValue MANA_BASE_DELAY_TICKS = BUILDER
            .comment("Base delay (in ticks) before mana regen begins after casting. 20 ticks = 1 second")
            .defineInRange("mana.baseDelayTicks", 40, 10, 200);

    // ── Traits ───────────────────────────────────────────────────────────────

    public static final ModConfigSpec.IntValue TRAIT_WEIGHT_COMMON = BUILDER
            .comment("Relative weight for Common traits in the roll pool")
            .defineInRange("traits.weights.common", 60, 1, 200);

    public static final ModConfigSpec.IntValue TRAIT_WEIGHT_UNCOMMON = BUILDER
            .comment("Relative weight for Uncommon traits in the roll pool")
            .defineInRange("traits.weights.uncommon", 30, 1, 200);

    public static final ModConfigSpec.IntValue TRAIT_WEIGHT_RARE = BUILDER
            .comment("Relative weight for Rare traits in the roll pool")
            .defineInRange("traits.weights.rare", 15, 1, 200);

    public static final ModConfigSpec.IntValue TRAIT_WEIGHT_LEGENDARY = BUILDER
            .comment("Relative weight for Legendary traits in the roll pool")
            .defineInRange("traits.weights.legendary", 5, 1, 200);

    public static final ModConfigSpec.IntValue TRAIT_WEIGHT_EXOTIC = BUILDER
            .comment("Relative weight for Exotic traits in the roll pool")
            .defineInRange("traits.weights.exotic", 1, 1, 200);

    // ── Soul Points ──────────────────────────────────────────────────────────

    public static final ModConfigSpec.IntValue SOUL_POINT_STARTING_BALANCE = BUILDER
            .comment("Soul Points assigned to each player in a newly created world")
            .defineInRange("soulPoints.startingBalance", 1, 0, 100);

    public static final ModConfigSpec SPEC = BUILDER.build();
}

