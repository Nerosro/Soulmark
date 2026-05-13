package com.nerosro.soulmark.traits;

import com.nerosro.soulmark.SoulmarkConfig;
import net.minecraft.ChatFormatting;

/**
 * The weight/rarity of a trait, determining how often it appears in its pool.
 * Higher rarity = lower chance to roll.
 */
public enum TraitWeight {
    COMMON(ChatFormatting.WHITE),
    UNCOMMON(ChatFormatting.GREEN),
    RARE(ChatFormatting.BLUE),
    LEGENDARY(ChatFormatting.DARK_PURPLE),
    EXOTIC(ChatFormatting.GOLD);

    private final ChatFormatting color;

    TraitWeight(ChatFormatting color) {
        this.color = color;
    }

    public ChatFormatting getColor() {
        return color;
    }

    /**
     * Returns the configured roll weight for this rarity tier.
     */
    public int getConfiguredWeight() {
        return switch (this) {
            case COMMON -> SoulmarkConfig.TRAIT_WEIGHT_COMMON.get();
            case UNCOMMON -> SoulmarkConfig.TRAIT_WEIGHT_UNCOMMON.get();
            case RARE -> SoulmarkConfig.TRAIT_WEIGHT_RARE.get();
            case LEGENDARY -> SoulmarkConfig.TRAIT_WEIGHT_LEGENDARY.get();
            case EXOTIC -> SoulmarkConfig.TRAIT_WEIGHT_EXOTIC.get();
        };
    }
}
