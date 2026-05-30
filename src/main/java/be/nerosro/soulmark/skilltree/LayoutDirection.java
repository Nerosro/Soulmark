package be.nerosro.soulmark.skilltree;

/**
 * Controls how a skill tree's grid coordinates map to screen space.
 */
public enum LayoutDirection {
    /** gridX = horizontal, gridY = vertical (root at top, children below). */
    TOP_DOWN,
    /** gridX = vertical, gridY = horizontal (root at left, children to the right). */
    LEFT_RIGHT
}
