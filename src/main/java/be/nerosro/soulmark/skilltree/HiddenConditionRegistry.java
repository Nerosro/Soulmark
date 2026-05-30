package be.nerosro.soulmark.skilltree;

import be.nerosro.soulmark.SoulMark;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for custom hidden-condition evaluators.
 * <p>
 * Job mods can register their own condition logic here. When a node has a
 * {@link HiddenCondition} of type CUSTOM, this registry is consulted to
 * determine whether the condition is met.
 */
public final class HiddenConditionRegistry {

    /**
     * Functional interface for evaluating a custom hidden condition.
     */
    @FunctionalInterface
    public interface Evaluator {
        /**
         * Returns true if the hidden condition is met and the node should become visible.
         */
        boolean test(Player player, String detail);
    }

    private static final Map<String, Evaluator> EVALUATORS = new HashMap<>();

    private HiddenConditionRegistry() {}

    /**
     * Registers a custom hidden-condition evaluator.
     * The key should be namespaced (e.g. "elemancy:has_deepened").
     */
    public static void register(String key, Evaluator evaluator) {
        if (EVALUATORS.containsKey(key)) {
            SoulMark.LOGGER.warn("HiddenConditionRegistry: overwriting existing evaluator for key '{}'", key);
        }
        EVALUATORS.put(key, evaluator);
    }

    /**
     * Returns the evaluator for the given key, or null if none is registered.
     */
    public static Evaluator get(String key) {
        return EVALUATORS.get(key);
    }
}

