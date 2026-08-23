package be.nerosro.soulmark.skilltree;

/**
 * A skill node's semantic position within a tree.
 *
 * @param lane  Cross-axis sibling placement in fine-grained layout units
 * @param depth Progression distance from the tree root in full layout columns
 */
public record LayoutPosition(int lane, int depth) {
}