package be.nerosro.soulmark.element;

/**
 * An elemental type in the world. Registered into the element registry.
 *
 * @param argb The display color in ARGB format (0xAARRGGBB)
 */
public record Element(int argb) {

    /**
     * Returns the RGB color with alpha stripped (0x00RRGGBB).
     * Suitable for particle systems and Style.withColor().
     */
    public int rgb() {
        return argb & 0x00FFFFFF;
    }
}
