package be.nerosro.soulmark.traits;

import be.nerosro.soulmark.SoulMark;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Traits registered by Soulmark itself.
 * Only framework-level traits that are not owned by any job mod belong here.
 */
public class SoulmarkTraits {

    public static final DeferredRegister<Trait> TRAITS =
            DeferredRegister.create(TraitRegistries.TRAIT_REGISTRY_KEY, SoulMark.MOD_ID);

    // ── Core traits ──────────────────────────────────────────────────────────

    /** Markless — prevents all other traits from appearing. */
    public static final Supplier<Trait> MARKLESS = TRAITS.register("markless",
            () -> new Trait("Markless", "Your soul bears no mark. No other traits are granted.", TraitType.NEUTRAL, TraitWeight.EXOTIC, 0f));

    /** Overmarked — adds an additional boost and penalty trait. */
    public static final Supplier<Trait> OVERMARKED = TRAITS.register("overmarked",
            () -> new Trait("Overmarked", "Your soul burns with excess. An additional boost and penalty trait are granted.", TraitType.NEUTRAL, TraitWeight.EXOTIC, 0f));


    public static void register(IEventBus modEventBus) {
        TRAITS.register(modEventBus);
    }
}

