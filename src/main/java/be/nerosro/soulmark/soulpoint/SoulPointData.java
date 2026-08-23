package be.nerosro.soulmark.soulpoint;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Stores a player's Soul Point balance.
 */
public class SoulPointData implements ValueIOSerializable {

    private boolean initialized;
    private int availableSoulPoints;
    private int totalSpent;

    public boolean isInitialized() {
        return initialized;
    }

    public void initialize(int startingSoulPoints) {
        if (initialized) return;
        initialized = true;
        availableSoulPoints = startingSoulPoints;
    }

    public boolean trySpend(int cost) {
        if (cost <= 0 || availableSoulPoints < cost) return false;
        availableSoulPoints -= cost;
        totalSpent += cost;
        return true;
    }

    public void award(int amount) {
        if (amount <= 0) return;
        availableSoulPoints += amount;
    }

    public int getAvailableSoulPoints() {
        return availableSoulPoints;
    }

    public int getTotalSpent() {
        return totalSpent;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean("initialized", initialized);
        output.putInt("availableSoulPoints", availableSoulPoints);
        output.putInt("totalSpent", totalSpent);
    }

    @Override
    public void deserialize(ValueInput input) {
        initialized = input.getBooleanOr("initialized", false);
        availableSoulPoints = input.getIntOr("availableSoulPoints", 0);
        totalSpent = input.getIntOr("totalSpent", 0);
    }
}