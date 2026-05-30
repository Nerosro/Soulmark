package be.nerosro.soulmark.affinity;

import be.nerosro.soulmark.SoulMark;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Stores the player's affinity data.
 * Rolled once at first spawn and never changes.
 */
public class AffinityData implements ValueIOSerializable {

    private Affinity affinity;
    private boolean initialized;

    public AffinityData() {
        // Default state: uninitialized
    }

    // ── Initialization ───────────────────────────────────────────────────────

    /**
     * Sets the player's affinity. Should only be called once, during first-spawn roll.
     */
    public void setOrigin(Affinity affinity) {
        this.affinity = affinity;
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    // ── Getter ───────────────────────────────────────────────────────────────

    public Affinity getAffinity() {
        return affinity;
    }

    // ── Serialization ────────────────────────────────────────────────────────

    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean("initialized", initialized);
        if (!initialized) return;
        output.putString("affinity", affinity.name());
    }

    @Override
    public void deserialize(ValueInput input) {
        initialized = input.getBooleanOr("initialized", false);
        if (!initialized) return;
        String name = input.getString("affinity").orElse("");
        try {
            affinity = Affinity.valueOf(name);
        } catch (IllegalArgumentException e) {
            SoulMark.LOGGER.warn("Unknown affinity '{}' in saved data, resetting to uninitialized for re-roll", name);
            initialized = false;
        }
    }
}

