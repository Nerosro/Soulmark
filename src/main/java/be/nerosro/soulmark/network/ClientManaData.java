package be.nerosro.soulmark.network;

import be.nerosro.soulmark.element.Element;
import be.nerosro.soulmark.element.ElementRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Client-side cache for mana data received from the server.
 * Rendering code (HUD overlays, etc.) reads from here.
 */
public final class ClientManaData {

    private static float currentMana;
    private static float maxPool;
    private static float manaBase;
    private static float poolTrinketBonus;
    private static float regenTrinketBonus;
    private static boolean affinityRevealed;
    private static boolean traitsRevealed;
    private static boolean scarsRevealed;
    private static boolean hasExperiencedManaCollapse;
    private static Element affinity;

    private ClientManaData() {}

    public static void update(ManaSyncPayload payload) {
        currentMana = payload.currentMana();
        maxPool = payload.maxPool();
        manaBase = payload.manaBase();
        poolTrinketBonus = payload.poolTrinketBonus();
        regenTrinketBonus = payload.regenTrinketBonus();
        affinityRevealed = payload.affinityRevealed();
        traitsRevealed = payload.traitsRevealed();
        scarsRevealed = payload.scarsRevealed();
        hasExperiencedManaCollapse = payload.hasExperiencedManaCollapse();
        
        Identifier affinityId = payload.affinityId();
        if (affinityId != null) {
            ResourceKey<Element> key = ResourceKey.create(ElementRegistry.ELEMENT_REGISTRY_KEY, affinityId);
            affinity = ElementRegistry.ELEMENT_REGISTRY.getValue(key);
        } else {
            affinity = null;
        }
    }

    public static float getCurrentMana() {
        return currentMana;
    }

    public static float getMaxPool() {
        return maxPool;
    }

    public static float getManaBase() {
        return manaBase;
    }

    public static float getPoolTrinketBonus() {
        return poolTrinketBonus;
    }

    public static float getRegenTrinketBonus() {
        return regenTrinketBonus;
    }

    public static boolean isAffinityRevealed() {
        return affinityRevealed;
    }

    public static boolean isTraitsRevealed() {
        return traitsRevealed;
    }

    public static boolean isScarsRevealed() {
        return scarsRevealed;
    }

    public static boolean hasExperiencedManaCollapse() {
        return hasExperiencedManaCollapse;
    }

    /** Returns the player's affinity Element, or null if not initialized. */
    public static Element getAffinity() {
        return affinity;
    }

    /**
     * Returns current mana as a fraction of max pool (0.0 to 1.0).
     */
    public static float getManaFraction() {
        if (maxPool <= 0) return 0f;
        return currentMana / maxPool;
    }
}


