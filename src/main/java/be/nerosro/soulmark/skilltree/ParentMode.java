package be.nerosro.soulmark.skilltree;

/**
 * Determines how multiple parents gate a node's unlock.
 */
public enum ParentMode {
    /** All parents must be unlocked before this node becomes unlockable. */
    ALL,
    /** Any single parent being unlocked is sufficient. */
    ANY
}
