package be.nerosro.soulmark.affinity;

import be.nerosro.soulmark.SoulMark;
import be.nerosro.soulmark.element.Element;
import be.nerosro.soulmark.element.ElementRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Stores the player's affinity data.
 * Rolled once at first spawn and never changes.
 */
public class AffinityData implements ValueIOSerializable {

    private Element affinity;
    private boolean initialized;

    public AffinityData() {
        // Default state: uninitialized
    }

    // ── Initialization ───────────────────────────────────────────────────────

    /**
     * Sets the player's affinity. Should only be called once, during first-spawn roll.
     */
    public void setOrigin(Element affinity) {
        this.affinity = affinity;
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    // ── Getter ───────────────────────────────────────────────────────────────

    public Element getAffinity() {
        return affinity;
    }

    // ── Serialization ────────────────────────────────────────────────────────

    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean("initialized", initialized);
        if (!initialized) return;
        Identifier key = ElementRegistry.ELEMENT_REGISTRY.getKey(affinity);
        if (key != null) {
            output.putString("affinity", key.toString());
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        initialized = input.getBooleanOr("initialized", false);
        if (!initialized) return;
        String name = input.getString("affinity").orElse("");
        if (name.isEmpty()) {
            SoulMark.LOGGER.warn("Empty affinity in saved data, resetting to uninitialized for re-roll");
            initialized = false;
            return;
        }
        ResourceKey<Element> key = ResourceKey.create(ElementRegistry.ELEMENT_REGISTRY_KEY, Identifier.parse(name));
        affinity = ElementRegistry.ELEMENT_REGISTRY.getValue(key);
        if (affinity == null) {
            SoulMark.LOGGER.warn("Unknown element '{}' in saved data, resetting to uninitialized for re-roll", name);
            initialized = false;
        }
    }
}

