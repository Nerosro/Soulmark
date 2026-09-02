package be.nerosro.soulmark.skilltree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.world.entity.player.Player;

/**
 * Registers context-sensitive actions for Soulmark's shared action key.
 */
public final class ContextualKeyActions {

    private static final List<Registration> REGISTRATIONS = new ArrayList<>();

    private ContextualKeyActions() {
    }

    public static void register(Predicate<Player> condition, Consumer<Player> action) {
        REGISTRATIONS.add(new Registration(condition, action));
    }

    public static void tryHandle(Player player) {
        for (Registration registration : REGISTRATIONS) {
            if (registration.condition().test(player)) {
                registration.action().accept(player);
                return;
            }
        }
    }

    private record Registration(Predicate<Player> condition, Consumer<Player> action) {
    }
}