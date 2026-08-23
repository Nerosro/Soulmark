package be.nerosro.soulmark.network;

/**
 * External mana modifiers provided by job mods (e.g., Elemancy trinkets).
 * Used during mana sync to send accurate stat breakdowns to the client.
 */
public record ManaModifiers(
        float poolBonus,   // additive pool bonus (e.g., +0.08 for 8% boost)
        float regenBonus   // additive regen bonus (e.g., +0.15 for 15% boost)
) {
    public static final ManaModifiers NONE = new ManaModifiers(0f, 0f);
}
