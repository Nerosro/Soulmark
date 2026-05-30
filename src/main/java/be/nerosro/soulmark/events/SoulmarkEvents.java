package be.nerosro.soulmark.events;

import be.nerosro.soulmark.SoulMark;
import be.nerosro.soulmark.affinity.AffinityData;
import be.nerosro.soulmark.affinity.AffinityUtil;
import be.nerosro.soulmark.capability.SoulmarkAttachments;
import be.nerosro.soulmark.mana.ManaData;
import be.nerosro.soulmark.mana.ManaUtil;
import be.nerosro.soulmark.network.SoulmarkNetwork;
import be.nerosro.soulmark.skilltree.SkillTreeUtil;
import be.nerosro.soulmark.traits.TraitData;
import be.nerosro.soulmark.traits.TraitUtil;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Central event listeners for all Soulmark player systems.
 * Handles initialization, ticking, and lifecycle sync.
 */
public class SoulmarkEvents {

    // ── First-spawn initialization ───────────────────────────────────────────

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Mana
        ManaData mana = ManaUtil.getMana(player);
        if (!mana.isInitialized()) {
            SoulMark.LOGGER.info("Rolling origin mana for player '{}'", player.getName().getString());
            ManaUtil.rollOrigin(mana, player.getRandom());
            player.setData(SoulmarkAttachments.MANA.get(), mana);
        }

        // Affinity
        AffinityData affinity = AffinityUtil.getAffinityData(player);
        if (!affinity.isInitialized()) {
            SoulMark.LOGGER.info("Rolling origin affinity for player '{}'", player.getName().getString());
            AffinityUtil.rollOrigin(affinity, player.getRandom());
            player.setData(SoulmarkAttachments.AFFINITY.get(), affinity);
        }

        // Traits
        TraitData traits = TraitUtil.getTraitData(player);
        if (!traits.isInitialized()) {
            SoulMark.LOGGER.info("Rolling origin traits for player '{}'", player.getName().getString());
            TraitUtil.rollOrigin(traits, player.getRandom());
            player.setData(SoulmarkAttachments.TRAITS.get(), traits);
        }

        // Skill Tree — no initialization needed; empty state is valid

        // Sync to client
        SoulmarkNetwork.syncMana(player);
        SoulmarkNetwork.syncSkillTree(player);
    }

    // ── Per-tick updates ─────────────────────────────────────────────────────

    private static final int SYNC_INTERVAL_TICKS = 4;

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Mana regen
        ManaData mana = ManaUtil.getMana(player);
        if (mana.isInitialized()) {
            mana.tick();
            if (mana.isDirty() && player.tickCount % SYNC_INTERVAL_TICKS == 0) {
                SoulmarkNetwork.syncMana(player);
                mana.clearDirty();
            }
        }
    }

    // ── Respawn sync ─────────────────────────────────────────────────────────

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Restore mana to 50% of effective max on respawn
        ManaData mana = ManaUtil.getMana(player);
        if (mana.isInitialized()) {
            mana.setCurrentMana(mana.getMaxPool() * 0.5f);
        }

        // Re-set attachments to trigger client sync
        player.setData(SoulmarkAttachments.MANA.get(), mana);
        player.setData(SoulmarkAttachments.SKILL_TREE.get(), SkillTreeUtil.getTreeData(player));

        // Sync to client after respawn
        SoulmarkNetwork.syncMana(player);
        SoulmarkNetwork.syncSkillTree(player);
    }
}

