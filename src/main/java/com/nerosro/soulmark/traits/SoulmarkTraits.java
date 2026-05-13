package com.nerosro.soulmark.traits;

import com.nerosro.soulmark.SoulMark;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Traits registered by Soulmark itself.
 * Only framework-level traits that are not owned by any job mod belong here.
 */
public class SoulmarkTraits {

    public static final DeferredRegister<Trait> TRAITS =
            DeferredRegister.create(SoulmarkRegistries.TRAIT_REGISTRY_KEY, SoulMark.MOD_ID);

    // ── Core trait ───────────────────────────────────────────────────────────

    /** Markless — prevents all other traits from appearing. */
    public static final Supplier<Trait> MARKLESS = TRAITS.register("markless",
            () -> new Trait("Markless", "Your soul bears no mark. No other traits are granted.", TraitType.NEUTRAL, TraitWeight.LEGENDARY));


    public static void register(IEventBus modEventBus) {
        TRAITS.register(modEventBus);
    }
}

