package be.nerosro.soulmark.skilltree;

import net.minecraft.world.entity.player.Player;

/**
 * Defines how a skill tree debits and displays its point currency.
 * Job mods own implementations for their Job Points.
 */
public interface SkillTreePayment {

    boolean trySpend(Player player, int cost);

    int getClientAvailableBalance();

    String displayName();
}