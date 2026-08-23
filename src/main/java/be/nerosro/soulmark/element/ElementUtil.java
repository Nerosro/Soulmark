package be.nerosro.soulmark.element;

import net.minecraft.resources.Identifier;

/**
 * Helper methods for working with elements.
 */
public final class ElementUtil {

    private ElementUtil() {}

    /**
     * Returns the display name for an element (e.g. "Fire" from "soulmark:fire").
     * Capitalizes the path from the identifier.
     * Returns empty string if identifier is null.
     */
    public static String getDisplayName(Identifier elementId) {
        if (elementId == null) return "";
        String path = elementId.getPath();
        if (path.isEmpty()) return "";
        return path.substring(0, 1).toUpperCase() + path.substring(1);
    }
}
