package be.nerosro.soulmark.soulpoint;

import be.nerosro.soulmark.network.ClientSkillTreeData;
import be.nerosro.soulmark.skilltree.SkillTreePayment;
import net.minecraft.world.entity.player.Player;

/**
 * Skill tree payment implementation for Soul Point gate nodes.
 */
public enum SoulPointPayment implements SkillTreePayment {
    INSTANCE;

    @Override
    public boolean trySpend(Player player, int cost) {
        return SoulPointUtil.trySpend(player, cost);
    }

    @Override
    public int getClientAvailableBalance() {
        return ClientSkillTreeData.getAvailableSoulPoints();
    }

    @Override
    public String displayName() {
        return "Soul Points";
    }
}