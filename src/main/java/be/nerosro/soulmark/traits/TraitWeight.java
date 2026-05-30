package be.nerosro.soulmark.traits;

import be.nerosro.soulmark.SoulmarkConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * The weight/rarity of a trait, determining how often it appears in its pool.
 * Higher rarity = lower chance to roll.
 */
public enum TraitWeight {
    COMMON,
    UNCOMMON,
    RARE,
    LEGENDARY,
    EXOTIC;

    /**
     * Builds a styled display component for a trait name.
     * Common–Rare use simple colored text.
     * Legendary gets bold + star brackets.
     * Exotic gets bold + italic + diamond brackets.
     */
    public MutableComponent styledName(String name) {
        return switch (this) {
            case COMMON -> Component.literal(name).withStyle(ChatFormatting.WHITE);
            case UNCOMMON -> Component.literal(name).withStyle(ChatFormatting.GREEN);
            case RARE -> Component.literal(name).withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD);
            case LEGENDARY -> Component.empty()
                    .append(Component.literal("★ ").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(Component.literal(name).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                    .append(Component.literal(" ★").withStyle(ChatFormatting.LIGHT_PURPLE));
            case EXOTIC -> Component.empty()
                    .append(Component.literal("✦ ").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.ITALIC))
                    .append(Component.literal(" ✦").withStyle(ChatFormatting.YELLOW));
        };
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
