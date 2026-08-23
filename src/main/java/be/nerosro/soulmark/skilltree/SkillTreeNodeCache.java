package be.nerosro.soulmark.skilltree;

import be.nerosro.soulmark.network.ClientSkillTreeData;
import be.nerosro.soulmark.network.ClientSkillTreeQuery;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds and holds the client-side render cache for a single skill tree page.
 * Recomputed whenever the active page changes or the client's unlock cache updates.
 */
public final class SkillTreeNodeCache {

    private final List<NodeEntry> entries = new ArrayList<>();
    private final Map<Identifier, NodeEntry> entriesById = new HashMap<>();

    /**
     * Rebuilds the cache for the given tree, computing visibility, layout position,
     * and exclusion state for every node using the client's cached unlock data.
     */
    public void rebuild(Identifier treeId) {
        entries.clear();
        entriesById.clear();
        if (treeId == null) return;

        SkillTree tree = SkillTreeRegistries.TREE_REGISTRY.getValue(treeId);
        boolean horizontal = tree != null && tree.direction() == LayoutDirection.LEFT_RIGHT;

        for (Identifier nodeId : SkillTreeUtil.getAllNodesInTree(treeId)) {
            SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
            if (node == null) continue;

            // Compute visibility from client cache using the shared server/client algorithm
            NodeVisibility visibility = SkillTreeUtil.getVisibility(ClientSkillTreeQuery.INSTANCE, nodeId);
            if (visibility == NodeVisibility.INVISIBLE) continue;

            // Map semantic lane/depth coordinates to screen axes for the tree direction.
            int pixelX;
            int pixelY;
            if (horizontal) {
                pixelX = node.depth() * SkillTreeScreenConstants.Layout.DEPTH_SPACING;
                pixelY = node.lane() * SkillTreeScreenConstants.Layout.LANE_SPACING;
            } else {
                pixelX = node.lane() * SkillTreeScreenConstants.Layout.LANE_SPACING;
                pixelY = node.depth() * SkillTreeScreenConstants.Layout.DEPTH_SPACING;
            }

            boolean excluded = isExcluded(nodeId);
            NodeEntry entry = new NodeEntry(nodeId, node, pixelX, pixelY, visibility, excluded);
            entries.add(entry);
            entriesById.put(nodeId, entry);
        }
    }

    // Note: iterates full node registry per call. Acceptable for current tree sizes (<50 nodes).
    // If multi-mod setups grow to 100+ nodes, consider caching exclusion results during rebuild().
    private boolean isExcluded(Identifier nodeId) {
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        if (node == null || node.exclusionGroup() == null) return false;

        // Check if any other node in the same exclusion group is already unlocked
        for (Map.Entry<ResourceKey<SkillNode>, SkillNode> entry : SkillTreeRegistries.NODE_REGISTRY.entrySet()) {
            SkillNode other = entry.getValue();
            Identifier otherId = entry.getKey().identifier();
            if (otherId.equals(nodeId)) continue;
            if (node.exclusionGroup().equals(other.exclusionGroup())
                    && other.nodeType() == NodeType.SPECIALIZATION
                    && ClientSkillTreeData.isUnlocked(otherId)) {
                return true;
            }
        }

        return false;
    }

    public List<NodeEntry> entries() {
        return entries;
    }

    public @Nullable NodeEntry get(Identifier nodeId) {
        return entriesById.get(nodeId);
    }

    /**
     * Cached render data for a single node: layout position, computed visibility, and
     * exclusion state as of the last {@link #rebuild(Identifier)} call.
     */
    public record NodeEntry(
            Identifier nodeId,
            SkillNode node,
            int pixelX,
            int pixelY,
            NodeVisibility visibility,
            boolean excluded
    ) {}
}
