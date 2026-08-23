package be.nerosro.soulmark.network;

import be.nerosro.soulmark.SoulMark;
import be.nerosro.soulmark.affinity.AffinityData;
import be.nerosro.soulmark.affinity.AffinityUtil;
import be.nerosro.soulmark.attunement.AttunementUtil;
import be.nerosro.soulmark.element.Element;
import be.nerosro.soulmark.element.ElementRegistry;
import be.nerosro.soulmark.element.SoulmarkElements;
import be.nerosro.soulmark.mana.ManaData;
import be.nerosro.soulmark.mana.ManaUtil;
import be.nerosro.soulmark.soulpoint.SoulPointUtil;
import be.nerosro.soulmark.skilltree.SkillTreeUtil;
import be.nerosro.soulmark.traits.TraitUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * Handles registration and sending of all Soulmark network payloads.
 */
public final class SoulmarkNetwork {

    private static final String PROTOCOL_VERSION = "1";

    @Nullable
    private static Function<ServerPlayer, ManaModifiers> manaModifierProvider = null;

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
                    SoulMark.LOGGER.debug("Client received skill tree sync: {} nodes, {} Soul Points",
                        payload.unlockedNodes().size(), payload.availableSoulPoints());
                    ClientSkillTreeData.update(payload);
                }
        );

        // Attunement sync: server → client
        registrar.playToClient(
                AttunementSyncPayload.TYPE,
                AttunementSyncPayload.STREAM_CODEC,
                (payload, context) -> ClientAttunementData.update(payload)
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
     * Allows job mods (e.g., Elemancy) to provide external mana modifiers for accurate stat breakdown.
     * Should be called during mod initialization before any players connect.
     */
    public static void setManaModifierProvider(Function<ServerPlayer, ManaModifiers> provider) {
        manaModifierProvider = provider;
    }

    /**
     * Sends the current mana state to the given player's client.
     */
    public static void syncMana(ServerPlayer player) {
        syncMana(player, false);
    }

    /**
     * Sends the current mana state to the given player's client, with optional Elemancy-specific flags.
     * @param hasExperiencedManaCollapse Elemancy-specific flag for Mana Collapse discovery
     */
    public static void syncMana(ServerPlayer player, boolean hasExperiencedManaCollapse) {
        ManaData mana = ManaUtil.getMana(player);
        boolean affinityRevealed = AffinityUtil.isAffinityRevealed(player);
        boolean traitsRevealed = TraitUtil.isTraitsRevealed(player);
        boolean scarsRevealed = TraitUtil.isScarsRevealed(player);
        Identifier affinityId = resolveAffinityId(player);
        
        ManaModifiers modifiers = manaModifierProvider != null 
                ? manaModifierProvider.apply(player) 
                : ManaModifiers.NONE;
        
        PacketDistributor.sendToPlayer(player, new ManaSyncPayload(
                mana.getCurrentMana(),
                mana.getMaxPool(),
                mana.getOriginMaxPool(),
                modifiers.poolBonus(),
                modifiers.regenBonus(),
                affinityRevealed,
                traitsRevealed,
                scarsRevealed,
                hasExperiencedManaCollapse,
                affinityId
        ));
    }

    private static Identifier resolveAffinityId(ServerPlayer player) {
        AffinityData data = AffinityUtil.getAffinityData(player);
        if (!data.isInitialized()) return null;
        Element element = data.getAffinity();
        if (element == null) return null;
        return ElementRegistry.ELEMENT_REGISTRY.getKey(element);
    }

    /**
     * Sends the current skill tree state to the given player's client.
     */
    public static void syncSkillTree(ServerPlayer player) {
        var treeData = SkillTreeUtil.getTreeData(player);
        int availableSoulPoints = SoulPointUtil.getAvailableSoulPoints(player);
        PacketDistributor.sendToPlayer(player, new SkillTreeSyncPayload(
            treeData.getUnlockedNodes(),
            availableSoulPoints,
            treeData.getDiscoveredTrees()
        ));
    }

    /**
     * Sends the current attunement state to the given player's client.
     */
    public static void syncAttunement(ServerPlayer player) {
        Element element = AttunementUtil.getAttunement(player);
        Identifier elementId = element != null
            ? ElementRegistry.ELEMENT_REGISTRY.getKey(element)
            : ElementRegistry.ELEMENT_REGISTRY.getKey(SoulmarkElements.NONE.get());
        PacketDistributor.sendToPlayer(player, new AttunementSyncPayload(elementId));
    }
}
