package be.nerosro.soulmark.ui;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * A single entry in a radial menu.
 * Job mods create these from their unlocked ABILITY nodes.
 *
 * @param name     Display name of the entry
 * @param nodeId   The skill node ID this entry represents (nullable for non-node entries)
 * @param color    The ARGB color for this entry's segment (e.g. affinity tint)
 * @param iconChar A single character or short string shown as the icon (e.g. "🔥", "⚡")
 * @param icon     If non-null, a texture path rendered instead of iconChar (e.g. "modid:textures/gui/skills/fireball.png")
 */
public record RadialMenuEntry(
        String name,
        Identifier nodeId,
        int color,
        String iconChar,
        @Nullable Identifier icon
) {
    /**
     * Simple constructor with default white color and no icon.
     */
    public RadialMenuEntry(String name, Identifier nodeId) {
        this(name, nodeId, 0xFFFFFFFF, "●", null);
    }

    /**
     * Constructor with color, default icon char, no texture icon.
     */
    public RadialMenuEntry(String name, Identifier nodeId, int color) {
        this(name, nodeId, color, "●", null);
    }

    /**
     * Constructor with color and icon char, no texture icon.
     */
    public RadialMenuEntry(String name, Identifier nodeId, int color, String iconChar) {
        this(name, nodeId, color, iconChar, null);
    }
}

