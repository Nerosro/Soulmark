package be.nerosro.soulmark.skilltree;

import be.nerosro.soulmark.element.Element;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A single node in a skill tree.
 * Registered by job mods into the node registry.
 *
 * @param name            Display name of the node
 * @param description     What unlocking this node does (shown when readable)
 * @param treeId          Which tree this node belongs to (e.g. "elemancy:main")
 * @param parentIds       The parent node registry IDs (empty = root node)
 * @param parentMode      Whether ALL or ANY parent must be unlocked (default ALL)
 * @param nodeType        The category of this node (determines icon frame shape)
 * @param cost            How many points this node costs to unlock (default 1)
 * @param gridX           Horizontal grid position within the tree layout (0 = center column)
 * @param gridY           Vertical grid position within the tree layout (0 = top row)
 * @param element         The element this node belongs to (NONE = unattuned)
 * @param prerequisites   Extra node IDs that must be unlocked before this node (in addition to parentIds)
 * @param exclusionGroup  If non-null, unlocking this node locks out all other nodes in the same group
 * @param hiddenCondition If non-null, this node is hidden until the condition type is satisfied
 * @param icon            If non-null, a texture path rendered as the node's icon (e.g. "modid:textures/gui/skills/fireball.png")
 * @param soulGate   Whether this exceptional node spends Soul Points instead of its tree's default Job Points
 */
public record SkillNode(
        String name,
        String description,
        Identifier treeId,
        List<Identifier> parentIds,
        ParentMode parentMode,
        NodeType nodeType,
        int cost,
        int gridX,
        int gridY,
        Element element,
        List<Identifier> prerequisites,
        @Nullable String exclusionGroup,
        @Nullable HiddenCondition hiddenCondition,
        @Nullable Identifier icon,
        boolean soulGate
) {
    /**
     * Returns true if this is a root node (no parents).
     */
    public boolean isRoot() {
        return parentIds.isEmpty();
    }

    /**
     * Returns the first parent ID, or null if this is a root node.
     * Convenience for trees that only use single-parent chains.
     */
    public @Nullable Identifier parentId() {
        return parentIds.isEmpty() ? null : parentIds.getFirst();
    }

    /**
     * Builder for creating SkillNode instances with sensible defaults.
     */
    public static class Builder {
        private final String name;
        private final String description;
        private final Identifier treeId;
        private final NodeType nodeType;
        private final Supplier<Element> element;
        private final List<Identifier> parentIds = new ArrayList<>();
        private ParentMode parentMode = ParentMode.ALL;
        private int cost = 1;
        private int gridX = 0;
        private int gridY = 0;
        private List<Identifier> prerequisites = List.of();
        private @Nullable String exclusionGroup;
        private @Nullable HiddenCondition hiddenCondition;
        private @Nullable Identifier icon;
        private boolean soulGate;

        public Builder(String name, String description, Identifier treeId, NodeType nodeType, Supplier<Element> element) {
            this.name = name;
            this.description = description;
            this.treeId = treeId;
            this.nodeType = nodeType;
            this.element = element;
        }

        public Builder parent(Identifier parentId) {
            this.parentIds.add(parentId);
            return this;
        }

        public Builder parents(Identifier... parentIds) {
            this.parentIds.addAll(List.of(parentIds));
            return this;
        }

        public Builder parentMode(ParentMode mode) {
            this.parentMode = mode;
            return this;
        }

        public Builder cost(int cost) {
            this.cost = cost;
            return this;
        }

        public Builder gridPosition(int x, int y) {
            this.gridX = x;
            this.gridY = y;
            return this;
        }

        public Builder prerequisites(Identifier... prerequisites) {
            this.prerequisites = List.of(prerequisites);
            return this;
        }

        public Builder exclusionGroup(String group) {
            this.exclusionGroup = group;
            return this;
        }

        public Builder hiddenCondition(HiddenCondition condition) {
            this.hiddenCondition = condition;
            return this;
        }

        public Builder icon(Identifier icon) {
            this.icon = icon;
            return this;
        }

        /**
         * Marks this node to use Soul Points instead.
         */
        public Builder soulGate() {
            this.soulGate = true;
            return this;
        }

        public SkillNode build() {
            return new SkillNode(name, description, treeId, List.copyOf(parentIds), parentMode, nodeType,
                    cost, gridX, gridY, element.get(), prerequisites, exclusionGroup, hiddenCondition, icon, soulGate);
        }
    }
}
