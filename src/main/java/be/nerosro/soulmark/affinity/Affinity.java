package be.nerosro.soulmark.affinity;

import net.minecraft.ChatFormatting;

/**
 * The possible elemental affinities a player can be born with.
 * Job mods interpret what each affinity means in gameplay.
 * <p>
 * Colors are stored as ARGB (0xAARRGGBB). Use {@link #argb()} for full color
 * or {@link #rgb()} when alpha must be stripped (e.g. particle systems).
 */
public enum Affinity {
    // WARNING: oppositeOrdinal depends on declaration order — do not reorder.
    FIRE(1, ChatFormatting.DARK_RED, 0xFFFF4400),
    WATER(0, ChatFormatting.BLUE, 0xFF2288FF),
    EARTH(3, ChatFormatting.DARK_GREEN, 0xFF8B5E3C),
    AIR(2, ChatFormatting.YELLOW, 0xFFCCFF88),
    LIGHT(5, ChatFormatting.WHITE, 0xFFFFFFF0),
    DARK(4, ChatFormatting.DARK_GRAY, 0xFF1A0033);

    private final int oppositeOrdinal;
    private final ChatFormatting chatColor;
    private final int argb;

    Affinity(int oppositeOrdinal, ChatFormatting chatColor, int argb) {
        this.oppositeOrdinal = oppositeOrdinal;
        this.chatColor = chatColor;
        this.argb = argb;
    }

    /**
     * Returns the opposing affinity (e.g. FIRE ↔ WATER, EARTH ↔ AIR, LIGHT ↔ DARK).
     */
    public Affinity opposite() {
        return values()[oppositeOrdinal];
    }

    /**
     * Returns the chat formatting color for use in text components.
     */
    public ChatFormatting chatColor() {
        return chatColor;
    }

    /**
     * Returns the full ARGB color (0xAARRGGBB).
     */
    public int argb() {
        return argb;
    }

    /**
     * Returns the RGB color with alpha stripped (0x00RRGGBB).
     */
    public int rgb() {
        return argb & 0x00FFFFFF;
    }

    /**
     * Returns a display-friendly name for this affinity.
     */
    public String displayName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}

