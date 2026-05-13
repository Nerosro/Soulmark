package com.nerosro.soulmark.traits;

/**
 * A trait definition registered by a job mod.
 * Soulmark stores these in its custom registry — job mods provide the content.
 * In-game a trait is: name, description, type, and weight.
 */
public record Trait(
        String name,
        String description,
        TraitType type,
        TraitWeight weight
) {
    /**
     * Returns true if this trait blocks all other traits when rolled (e.g. Markless).
     */
    public boolean isExclusive() {
        return weight == TraitWeight.LEGENDARY && type == TraitType.NEUTRAL && name.equals("Markless");
    }
}

