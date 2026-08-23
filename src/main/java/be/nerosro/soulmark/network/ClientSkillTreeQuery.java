package be.nerosro.soulmark.network;

import be.nerosro.soulmark.skilltree.ISkillTreeQuery;
import net.minecraft.resources.Identifier;

/**
 * Adapts the static {@link ClientSkillTreeData} cache to {@link ISkillTreeQuery},
 * allowing client-side screens to reuse the shared visibility/distance algorithms
 * in {@code SkillTreeUtil} instead of maintaining a parallel implementation.
 */
public final class ClientSkillTreeQuery implements ISkillTreeQuery {

    public static final ClientSkillTreeQuery INSTANCE = new ClientSkillTreeQuery();

    private ClientSkillTreeQuery() {}

    @Override
    public boolean isUnlocked(Identifier nodeId) {
        return ClientSkillTreeData.isUnlocked(nodeId);
    }
}
