package be.nerosro.soulmark.skilltree;

import net.minecraft.resources.Identifier;

/**
 * Structural contract for querying unlock state, satisfied by both the server-side
 * {@link SkillTreeData} and the client-side cache.
 * Allows visibility/distance algorithms in {@link SkillTreeUtil} to be shared between
 * server and client without duplicating logic — any divergence in behavior becomes
 * a compile-time concern at the implementation site rather than a silent runtime bug.
 */
public interface ISkillTreeQuery {

    /**
     * Returns true if the given node is unlocked.
     */
    boolean isUnlocked(Identifier nodeId);
}
