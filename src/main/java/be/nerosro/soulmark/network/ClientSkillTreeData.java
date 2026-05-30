package be.nerosro.soulmark.network;

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
    private static int availablePoints = 0;
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
        availablePoints = payload.availablePoints();
        if (changeListener != null) {
            changeListener.run();
        }
    }

    public static boolean isUnlocked(Identifier nodeId) {
        return unlockedNodes.contains(nodeId);
    }

    public static Set<Identifier> getUnlockedNodes() {
        return Collections.unmodifiableSet(unlockedNodes);
    }

    public static int getAvailablePoints() {
        return availablePoints;
    }
}

