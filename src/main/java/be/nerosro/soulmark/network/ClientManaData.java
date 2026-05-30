package be.nerosro.soulmark.network;

/**
 * Client-side cache for mana data received from the server.
 * Rendering code (HUD overlays, etc.) reads from here.
 */
public final class ClientManaData {

    private static float currentMana;
    private static float maxPool;

    private ClientManaData() {}

    public static void update(ManaSyncPayload payload) {
        currentMana = payload.currentMana();
        maxPool = payload.maxPool();
    }

    public static float getCurrentMana() {
        return currentMana;
    }

    public static float getMaxPool() {
        return maxPool;
    }

    /**
     * Returns current mana as a fraction of max pool (0.0 to 1.0).
     */
    public static float getManaFraction() {
        if (maxPool <= 0) return 0f;
        return currentMana / maxPool;
    }
}


