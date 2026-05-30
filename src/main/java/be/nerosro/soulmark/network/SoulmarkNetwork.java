package be.nerosro.soulmark.network;

import be.nerosro.soulmark.SoulMark;
import be.nerosro.soulmark.mana.ManaData;
import be.nerosro.soulmark.mana.ManaUtil;
import be.nerosro.soulmark.skilltree.SkillPointUtil;
import be.nerosro.soulmark.skilltree.SkillTreeUtil;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Handles registration and sending of all Soulmark network payloads.
 */
public final class SoulmarkNetwork {

    private static final String PROTOCOL_VERSION = "1";

    private SoulmarkNetwork() {}

    /**
     * Registers all payload types. Called from the mod bus.
     */
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // Mana sync: server → client (handlers already run on main thread via PayloadRegistrar default)
        registrar.playToClient(
                ManaSyncPayload.TYPE,
                ManaSyncPayload.STREAM_CODEC,
                (payload, context) -> ClientManaData.update(payload)
        );

        // Skill tree sync: server → client
        registrar.playToClient(
                SkillTreeSyncPayload.TYPE,
                SkillTreeSyncPayload.STREAM_CODEC,
                (payload, context) -> {
                    SoulMark.LOGGER.debug("Client received skill tree sync: {} nodes, {} points",
                            payload.unlockedNodes().size(), payload.availablePoints());
                    ClientSkillTreeData.update(payload);
                }
        );

        // Skill node unlock request: client → server
        registrar.playToServer(
                SkillNodeUnlockPayload.TYPE,
                SkillNodeUnlockPayload.STREAM_CODEC,
                (payload, context) -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    SkillTreeUtil.UnlockResult result = SkillTreeUtil.tryUnlockDetailed(player, payload.nodeId());
                    if (result == SkillTreeUtil.UnlockResult.SUCCESS) {
                        SoulMark.LOGGER.debug("Player {} unlocked node {}", player.getName().getString(), payload.nodeId());
                        syncSkillTree(player);
                    } else {
                        SoulMark.LOGGER.debug("Player {} failed to unlock node {}: {}", player.getName().getString(), payload.nodeId(), result);
                    }
                }
        );
    }

    /**
     * Sends the current mana state to the given player's client.
     */
    public static void syncMana(ServerPlayer player) {
        ManaData mana = ManaUtil.getMana(player);
        PacketDistributor.sendToPlayer(player, new ManaSyncPayload(
                mana.getCurrentMana(),
                mana.getMaxPool()
        ));
    }

    /**
     * Sends the current skill tree state to the given player's client.
     */
    public static void syncSkillTree(ServerPlayer player) {
        var treeData = SkillTreeUtil.getTreeData(player);
        int available = SkillPointUtil.getAvailablePoints(player);
        PacketDistributor.sendToPlayer(player, new SkillTreeSyncPayload(
                treeData.getUnlockedNodes(),
                available
        ));
    }
}
