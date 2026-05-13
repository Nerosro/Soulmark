package com.nerosro.soulmark.affinity;

/**
 * The possible elemental affinities a player can be born with.
 * Job mods interpret what each affinity means in gameplay.
 */
public enum Affinity {
    FIRE,
    WATER,
    EARTH,
    AIR,
    LIGHT,
    DARK;

    /**
     * Returns a display-friendly name for this affinity.
     */
    public String displayName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}

