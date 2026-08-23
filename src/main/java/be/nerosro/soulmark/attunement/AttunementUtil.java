package be.nerosro.soulmark.attunement;

import be.nerosro.soulmark.capability.SoulmarkAttachments;
import be.nerosro.soulmark.element.Element;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * Public utility API for the attunement system.
 * Attunement is conferred by completing an elemental ritual and can be replaced.
 * Job mods should use this class to read and write player attunement.
 */
public final class AttunementUtil {

    private AttunementUtil() {}

    /**
     * Returns the player's AttunementData.
     */
    public static AttunementData getAttunementData(Player player) {
        return player.getData(SoulmarkAttachments.ATTUNEMENT.get());
    }

    /**
     * Returns true if the player has been attuned to an element.
     */
    public static boolean isAttuned(Player player) {
        return getAttunementData(player).isAttuned();
    }

    /**
     * Returns the player's attuned element, or null if not yet attuned.
     */
    @Nullable
    public static Element getAttunement(Player player) {
        return getAttunementData(player).getElement();
    }

    /**
     * Sets the player's attuned element. Can be called multiple times to re-attune.
     * Calling this replaces any existing attunement.
     */
    public static void setAttunement(Player player, Element element) {
        getAttunementData(player).setAttunement(element);
    }

    /**
     * Clears the player's attunement. Dev/testing use only.
     */
    public static void clearAttunement(Player player) {
        getAttunementData(player).clearAttunement();
    }
}
