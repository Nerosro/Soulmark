package be.nerosro.soulmark.skilltree;

/**
 * A skill tree definition, registered by job mods.
 * Each tree represents one "page" in the skill book.
 *
 * @param name        The display name of this tree (e.g. "Elemancy", "Knights")
 * @param description A short description shown at the top of the tree page
 * @param jobTag      The job tag that identifies which mod/job this tree belongs to
 *                    (e.g. "elemancy", "knights"). Used for per-tree spend tracking
 *                    and cross-mod queries.
 * @param direction   Layout direction for rendering (TOP_DOWN or LEFT_RIGHT)
 * @param defaultPayment Payment implementation used by ordinary nodes in this tree
 */
public record SkillTree(
        String name,
        String description,
        String jobTag,
        LayoutDirection direction,
        SkillTreePayment defaultPayment
) {}

