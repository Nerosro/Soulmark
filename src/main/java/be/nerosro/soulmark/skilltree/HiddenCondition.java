package be.nerosro.soulmark.skilltree;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Describes when a hidden skill node should become visible.
 * <p>
 * Hidden nodes are invisible (not teased, not scrambled) until their condition is met.
 * This supports cross-mod bridge content, mutation rituals, and discovery-driven mechanics.
 * <p>
 * The condition type tells the system what to check. The detail string provides the
 * specifics. Job mods can register custom condition evaluators for their own types.
 *
 * @param type   The kind of condition (see {@link Type})
 * @param detail A condition-specific parameter (e.g. a node ID, mod ID, or tag name)
 */
public record HiddenCondition(
        Type type,
        @Nullable String detail
) {
    /**
     * Built-in condition types for hidden nodes.
     * Job mods can extend visibility by registering custom evaluators via
     * {@link HiddenConditionRegistry}.
     */
    public enum Type {
        /**
         * Visible when a specific node is unlocked.
         * detail = node ID (e.g. "elemancy:deepening_ritual")
         */
        NODE_UNLOCKED,

        /**
         * Visible when a specific mod is loaded.
         * detail = mod ID (e.g. "knights")
         */
        MOD_LOADED,

        /**
         * Always evaluate through a custom registered evaluator.
         * detail = evaluator key registered in HiddenConditionRegistry
         */
        CUSTOM
    }

    // ── Factory methods ──────────────────────────────────────────────────────

    public static HiddenCondition nodeUnlocked(Identifier nodeId) {
        return new HiddenCondition(Type.NODE_UNLOCKED, nodeId.toString());
    }

    public static HiddenCondition modLoaded(String modId) {
        return new HiddenCondition(Type.MOD_LOADED, modId);
    }

    public static HiddenCondition custom(String evaluatorKey) {
        return new HiddenCondition(Type.CUSTOM, evaluatorKey);
    }
}

