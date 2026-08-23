package be.nerosro.soulmark.element;

import be.nerosro.soulmark.SoulMark;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registers the 6 base elements and NONE.
 * Base elements have opposites: FIRE↔WATER, EARTH↔AIR, LIGHT↔DARK.
 */
public class SoulmarkElements {

    private static final DeferredRegister<Element> ELEMENTS =
            DeferredRegister.create(ElementRegistry.ELEMENT_REGISTRY_KEY, SoulMark.MOD_ID);

    // ── Registered entries ───────────────────────────────────────────────────

    public static final Supplier<Element> FIRE = ELEMENTS.register("fire",
            () -> new Element(0xFFFF4400));

    public static final Supplier<Element> WATER = ELEMENTS.register("water",
            () -> new Element(0xFF2288FF));

    public static final Supplier<Element> EARTH = ELEMENTS.register("earth",
            () -> new Element(0xFF8B5E3C));

    public static final Supplier<Element> AIR = ELEMENTS.register("air",
            () -> new Element(0xFFCCFF88));

    public static final Supplier<Element> LIGHT = ELEMENTS.register("light",
            () -> new Element(0xFFFFD85C));

    public static final Supplier<Element> DARK = ELEMENTS.register("dark",
            () -> new Element(0xFF4A1F6B));

    public static final Supplier<Element> NONE = ELEMENTS.register("none",
            () -> new Element(0xFFB0B0B0));

    /**
     * The 6 base elements (excludes NONE). Used for affinity assignment.
     * Only call after registries have frozen.
     */
    public static List<Element> baseElements() {
        return List.of(FIRE.get(), WATER.get(), EARTH.get(), AIR.get(), LIGHT.get(), DARK.get());
    }

    /**
     * Returns the opposite element, or null if the given element has no opposite.
     * Only call after registries have frozen.
     */
    public static Element getOpposite(Element element) {
        return opposites().get(element);
    }

    private static Map<Element, Element> opposites;

    private static Map<Element, Element> opposites() {
        if (opposites == null) {
            opposites = Map.of(
                    FIRE.get(), WATER.get(),
                    WATER.get(), FIRE.get(),
                    EARTH.get(), AIR.get(),
                    AIR.get(), EARTH.get(),
                    LIGHT.get(), DARK.get(),
                    DARK.get(), LIGHT.get()
            );
        }
        return opposites;
    }

    // ── Registration ─────────────────────────────────────────────────────────

    public static void register(IEventBus modEventBus) {
        ELEMENTS.register(modEventBus);
    }
}
