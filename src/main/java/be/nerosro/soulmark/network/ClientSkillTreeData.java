package be.nerosro.soulmark.network;

import be.nerosro.soulmark.skilltree.NodeType;
import be.nerosro.soulmark.skilltree.SkillNode;
import be.nerosro.soulmark.skilltree.SkillTreeRegistries;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Client-side cache for skill tree data received from the server.
 * The skill tree screen reads from here to determine node states.
 */
public final class ClientSkillTreeData {

    private static final Set<Identifier> unlockedNodes = new HashSet<>();
    private static final Set<Identifier> discoveredTrees = new HashSet<>();
    private static int availableSoulPoints = 0;
    private static Runnable changeListener = null;

    private ClientSkillTreeData() {}

    /**
     * Registers a listener that fires whenever the cache is updated (e.g. after an unlock).
     * The skill tree screen uses this to rebuild its node cache without closing/reopening.
     */
    public static void setChangeListener(Runnable listener) {
        changeListener = listener;
    }

    public static void clearChangeListener() {
        changeListener = null;
    }

    public static void update(SkillTreeSyncPayload payload) {
        unlockedNodes.clear();
        unlockedNodes.addAll(payload.unlockedNodes());
        discoveredTrees.clear();
        discoveredTrees.addAll(payload.discoveredTrees());
        availableSoulPoints = payload.availableSoulPoints();
        if (changeListener != null) {
            changeListener.run();
        }
    }

    public static boolean isUnlocked(Identifier nodeId) {
        if (unlockedNodes.contains(nodeId)) return true;
        // Root nodes are implicitly unlocked (except DISCOVERY nodes)
        SkillNode node = SkillTreeRegistries.NODE_REGISTRY.getValue(nodeId);
        return node != null && node.isRoot() && node.nodeType() != NodeType.DISCOVERY;
    }

    public static Set<Identifier> getUnlockedNodes() {
        return Collections.unmodifiableSet(unlockedNodes);
    }

    public static boolean isTreeDiscovered(Identifier treeId) {
        return discoveredTrees.contains(treeId);
    }

    public static Set<Identifier> getDiscoveredTrees() {
        return Collections.unmodifiableSet(discoveredTrees);
    }

    public static int getAvailableSoulPoints() {
        return availableSoulPoints;
    }
}

