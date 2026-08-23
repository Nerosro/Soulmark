package be.nerosro.soulmark.attunement;

import be.nerosro.soulmark.SoulMark;
import be.nerosro.soulmark.element.Element;
import be.nerosro.soulmark.element.ElementRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jspecify.annotations.Nullable;

/**
 * Stores the player's attunement state.
 * Attunement is not innate — it is conferred by completing an elemental ritual.
 * A player may be re-attuned (e.g. mutation rituals), replacing the previous element.
 */
public class AttunementData implements ValueIOSerializable {

    @Nullable
    private Element element;

    public AttunementData() {}

    // ── Mutation ─────────────────────────────────────────────────────────────

    /**
     * Sets the player's attuned element. Can be called multiple times to re-attune.
     */
    public void setAttunement(Element element) {
        this.element = element;
    }

    /**
     * Clears attunement. Intended for dev/testing use only.
     */
    public void clearAttunement() {
        this.element = null;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public boolean isAttuned() {
        return element != null;
    }

    @Nullable
    public Element getElement() {
        return element;
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    @Override
    public void serialize(ValueOutput output) {
        if (element == null) return;
        Identifier key = ElementRegistry.ELEMENT_REGISTRY.getKey(element);
        if (key != null) {
            output.putString("element", key.toString());
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        String raw = input.getString("element").orElse("");
        if (raw.isEmpty()) return;
        ResourceKey<Element> key = ResourceKey.create(ElementRegistry.ELEMENT_REGISTRY_KEY, Identifier.parse(raw));
        element = ElementRegistry.ELEMENT_REGISTRY.getValue(key);
        if (element == null) {
            SoulMark.LOGGER.warn("Unknown attunement element '{}' in saved data, clearing attunement", raw);
        }
    }
}
