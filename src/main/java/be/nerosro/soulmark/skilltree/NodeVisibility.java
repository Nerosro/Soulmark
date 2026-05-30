package be.nerosro.soulmark.skilltree;

/**
 * Visibility state of a skill node, computed from distance to the nearest unlocked ancestor.
 */
public enum NodeVisibility {
    /** Node is unlocked. Full text and icon visible. */
    READABLE,
    /** Parent is unlocked. Full text visible, can be unlocked. Styled differently from READABLE. */
    UNLOCKABLE,
    /** Grandparent is unlocked. Icon visible, text obfuscated, cannot be unlocked yet. */
    SCRAMBLED,
    /** Far from unlocked. Icon barely visible, no text. */
    TEASED,
    /** Too far from any unlocked node. Completely invisible. */
    INVISIBLE
}

