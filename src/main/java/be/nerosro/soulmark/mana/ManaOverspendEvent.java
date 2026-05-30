package be.nerosro.soulmark.mana;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

/**
 * Fired when a player attempts to spend more mana than they have.
 * <p>
 * By default, the spend will fail. A job mod (e.g. Elemancy) can allow
 * overspending by calling {@link #allowOverspend()} — this lets the spend
 * succeed and drives mana into the negative (depth).
 * <p>
 * The listening mod is responsible for tracking and resolving the debt
 * (e.g. mana scars, penalties, etc.).
 */
public class ManaOverspendEvent extends Event {

    private final Player player;
    private final float cost;
    private final float currentMana;
    private final float deficit;
    private boolean overspendAllowed;

    public ManaOverspendEvent(Player player, float cost, float currentMana) {
        this.player = player;
        this.cost = cost;
        this.currentMana = currentMana;
        this.deficit = cost - currentMana;
    }

    public Player getPlayer() {
        return player;
    }

    /** The total mana cost requested. */
    public float getCost() {
        return cost;
    }

    /** The player's mana at the time of the spend attempt. */
    public float getCurrentMana() {
        return currentMana;
    }

    /** How much mana the player is short by (always positive). */
    public float getDeficit() {
        return deficit;
    }

    /** Returns true if a mod has allowed this overspend. */
    public boolean isOverspendAllowed() {
        return overspendAllowed;
    }

    /**
     * Call this to allow the spend to succeed despite insufficient mana.
     * The player's mana will go negative by the deficit amount.
     */
    public void allowOverspend() {
        this.overspendAllowed = true;
    }
}

