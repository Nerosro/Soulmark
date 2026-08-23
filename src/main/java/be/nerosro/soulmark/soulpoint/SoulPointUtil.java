package be.nerosro.soulmark.soulpoint;

import be.nerosro.soulmark.capability.SoulmarkAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Public API for Soul Point initialization and spending.
 */
public final class SoulPointUtil {

    private SoulPointUtil() {}

    public static SoulPointData getData(Player player) {
        return player.getData(SoulmarkAttachments.SOUL_POINTS.get());
    }

    public static void initialize(ServerPlayer player) {
        SoulPointData data = getData(player);
        if (data.isInitialized()) return;
        data.initialize(SoulPointWorldData.get(player.level()).getStartingSoulPoints());
        player.setData(SoulmarkAttachments.SOUL_POINTS.get(), data);
    }

    public static boolean trySpend(Player player, int cost) {
        SoulPointData data = getData(player);
        boolean success = data.trySpend(cost);
        if (success) {
            player.setData(SoulmarkAttachments.SOUL_POINTS.get(), data);
        }
        return success;
    }

    public static void award(Player player, int amount) {
        SoulPointData data = getData(player);
        data.award(amount);
        player.setData(SoulmarkAttachments.SOUL_POINTS.get(), data);
    }

    public static int getAvailableSoulPoints(Player player) {
        return getData(player).getAvailableSoulPoints();
    }
}