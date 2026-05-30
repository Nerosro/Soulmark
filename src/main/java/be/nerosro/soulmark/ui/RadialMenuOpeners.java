package be.nerosro.soulmark.ui;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Registry for radial menu opener mappings.
 * Job mods register which held item opens the radial menu and how to populate it.
 * <p>
 * Example (Elemancy):
 * <pre>
 *   RadialMenuOpeners.register(ElemancyItems.WAND.get(), player -> {
 *       // Build entries from unlocked abilities
 *       return getUnlockedAbilities(player).stream()
 *           .map(node -> new RadialMenuEntry(node.name(), nodeId, color, icon))
 *           .toList();
 *   }, (player, selected) -> {
 *       EquippedSpellData.setActiveSpell(player, selected.nodeId());
 *   });
 * </pre>
 */
public final class RadialMenuOpeners {

    /**
     * A registered radial menu opener with entry provider and selection handler.
     */
    public record OpenerRegistration(
            Function<Player, List<RadialMenuEntry>> entryProvider,
            BiConsumer<Player, RadialMenuEntry> onSelect,
            @Nullable Function<Player, Identifier> equippedProvider
    ) {}

    private static final Map<Item, OpenerRegistration> OPENERS = new LinkedHashMap<>();
    private static final Map<TagKey<Item>, OpenerRegistration> TAG_OPENERS = new LinkedHashMap<>();

    private RadialMenuOpeners() {}

    /**
     * Registers a held item as a radial menu opener.
     *
     * @param item             The item that triggers the radial menu
     * @param entryProvider    Provides the list of entries to show (called when menu opens)
     * @param onSelect         Called when the player selects an entry
     * @param equippedProvider Returns the currently equipped node ID for highlighting, or null
     */
    public static void register(Item item,
                                Function<Player, List<RadialMenuEntry>> entryProvider,
                                BiConsumer<Player, RadialMenuEntry> onSelect,
                                @Nullable Function<Player, Identifier> equippedProvider) {
        OPENERS.put(item, new OpenerRegistration(entryProvider, onSelect, equippedProvider));
    }

    /**
     * Registers a held item as a radial menu opener (without equipped indicator).
     */
    public static void register(Item item,
                                Function<Player, List<RadialMenuEntry>> entryProvider,
                                BiConsumer<Player, RadialMenuEntry> onSelect) {
        register(item, entryProvider, onSelect, null);
    }

    /**
     * Registers an item tag as a radial menu opener.
     * Any item in this tag will trigger the radial menu with the given providers.
     */
    public static void registerTag(TagKey<Item> tag,
                                   Function<Player, List<RadialMenuEntry>> entryProvider,
                                   BiConsumer<Player, RadialMenuEntry> onSelect,
                                   @Nullable Function<Player, Identifier> equippedProvider) {
        TAG_OPENERS.put(tag, new OpenerRegistration(entryProvider, onSelect, equippedProvider));
    }

    /**
     * Returns the opener registration for the given item stack, or null if not registered.
     * Checks exact item matches first, then falls back to tag matches.
     */
    @Nullable
    public static OpenerRegistration getOpenerForItem(ItemStack stack) {
        OpenerRegistration exact = OPENERS.get(stack.getItem());
        if (exact != null) return exact;
        for (var entry : TAG_OPENERS.entrySet()) {
            if (stack.is(entry.getKey())) return entry.getValue();
        }
        return null;
    }
}

