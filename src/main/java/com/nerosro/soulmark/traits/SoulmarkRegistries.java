package com.nerosro.soulmark.traits;

import com.nerosro.soulmark.SoulMark;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * Holds the custom Soulmark registries.
 * Job mods reference TRAIT_REGISTRY_KEY to register their own traits.
 */
public class SoulmarkRegistries {

    /**
     * The registry key for traits. Job mods use this to create their DeferredRegister.
     */
    public static final ResourceKey<Registry<Trait>> TRAIT_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(SoulMark.MOD_ID, "traits"));

    /**
     * The built trait registry instance.
     */
    public static final Registry<Trait> TRAIT_REGISTRY =
            new RegistryBuilder<>(TRAIT_REGISTRY_KEY).create();
}


