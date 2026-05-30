package be.nerosro.soulmark.traits;

/**
 * A trait definition registered by a job mod.
 * Soulmark stores these in its custom registry — job mods provide the content.
 * In-game a trait is: name, description, type, weight, and value.
 */
public record Trait(
        String name,
        String description,
        TraitType type,
        TraitWeight weight,
        float value // Numeric effect magnitude (e.g. 0.05 = 5%). 0 for traits with no numeric effect.
                    // Future: if traits need multiple values, consider Map<String, Float> modifiers instead.
) {
    /**
     * Returns true if this is the Markless trait — blocks all other traits when rolled.
     */
    public boolean isMarkless() {
        return this == SoulmarkTraits.MARKLESS.get();
    }

    /**
     * Returns true if this trait grants an additional boost and penalty (e.g. Overmarked).
     */
    public boolean isOvermarked() {
        return this == SoulmarkTraits.OVERMARKED.get();
    }
}

