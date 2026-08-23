package be.nerosro.soulmark.element;

import be.nerosro.soulmark.SoulMark;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * Custom registry for elements.
 * Soulmark seeds the 6 base elements + NONE; job mods can register additional elements.
 */
public class ElementRegistry {

    public static final ResourceKey<Registry<Element>> ELEMENT_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(SoulMark.MOD_ID, "elements"));

    public static final Registry<Element> ELEMENT_REGISTRY =
            new RegistryBuilder<>(ELEMENT_REGISTRY_KEY).create();
}
