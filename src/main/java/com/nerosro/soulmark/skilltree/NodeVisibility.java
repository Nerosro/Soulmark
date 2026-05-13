package com.nerosro.soulmark.skilltree;

/**
 * Visibility state of a skill node, computed from distance to the nearest unlocked ancestor.
 */
public enum NodeVisibility {
    /** Node is unlocked. Full text and icon visible. */
    READABLE,
    /** Parent is unlocked. Icon visible, text obfuscated, can be unlocked. */
    SCRAMBLED,
    /** Grandparent is unlocked. Icon visible, no text, cannot be unlocked. */
    TEASED,
    /** Too far from any unlocked node. Completely invisible. */
    INVISIBLE
}

