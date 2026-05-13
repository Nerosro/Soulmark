package com.nerosro.soulmark.skilltree;

/**
 * A skill tree definition, registered by job mods.
 * Each tree represents one "page" in the skill book.
 *
 * @param name The display name of this tree (e.g. "Elemancy", "Knights")
 */
public record SkillTree(
        String name
) {
}

