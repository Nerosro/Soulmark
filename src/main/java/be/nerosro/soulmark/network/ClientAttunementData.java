package be.nerosro.soulmark.network;

import be.nerosro.soulmark.element.Element;
import be.nerosro.soulmark.element.ElementRegistry;
import be.nerosro.soulmark.element.SoulmarkElements;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Client-side cache for attunement data received from the server.
 * UI code (e.g. the Tome's Identity view) should read from here rather than
 * calling AttunementUtil directly, since AttunementUtil reads the (server-authoritative)
 * attachment which is not guaranteed to be present/correct on a remote client.
 */
public final class ClientAttunementData {

    private static Element attunement;

    private ClientAttunementData() {}

    public static void update(AttunementSyncPayload payload) {
        Identifier elementId = payload.elementId();
        ResourceKey<Element> key = ResourceKey.create(ElementRegistry.ELEMENT_REGISTRY_KEY, elementId);
        Element resolved = ElementRegistry.ELEMENT_REGISTRY.getValue(key);
        attunement = (resolved == null || resolved == SoulmarkElements.NONE.get()) ? null : resolved;
    }

    public static boolean isAttuned() {
        return attunement != null;
    }

    public static Element getAttunement() {
        return attunement;
    }
}
