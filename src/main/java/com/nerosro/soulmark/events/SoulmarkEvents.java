package com.nerosro.soulmark.events;

import com.nerosro.soulmark.affinity.AffinityData;
import com.nerosro.soulmark.affinity.AffinityUtil;
import com.nerosro.soulmark.capability.SoulmarkAttachments;
import com.nerosro.soulmark.mana.ManaData;
import com.nerosro.soulmark.mana.ManaUtil;
import com.nerosro.soulmark.skilltree.SkillTreeUtil;
import com.nerosro.soulmark.traits.TraitData;
import com.nerosro.soulmark.traits.TraitUtil;
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
            ManaUtil.rollOrigin(mana, player.getRandom());
            player.setData(SoulmarkAttachments.MANA.get(), mana);
        }

        // Affinity
        AffinityData affinity = AffinityUtil.getAffinityData(player);
        if (!affinity.isInitialized()) {
            AffinityUtil.rollOrigin(affinity, player.getRandom());
            player.setData(SoulmarkAttachments.AFFINITY.get(), affinity);
        }

        // Traits
        TraitData traits = TraitUtil.getTraitData(player);
        if (!traits.isInitialized()) {
            TraitUtil.rollOrigin(traits, player.getRandom());
            player.setData(SoulmarkAttachments.TRAITS.get(), traits);
        }

        // Skill Tree — no initialization needed; empty state is valid
    }

    // ── Per-tick updates ─────────────────────────────────────────────────────

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Mana regen
        ManaData mana = ManaUtil.getMana(player);
        if (mana.isInitialized()) {
            mana.tick();
        }
    }

    // ── Respawn sync ─────────────────────────────────────────────────────────

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Re-set attachments to trigger client sync
        player.setData(SoulmarkAttachments.MANA.get(), ManaUtil.getMana(player));
        player.setData(SoulmarkAttachments.AFFINITY.get(), AffinityUtil.getAffinityData(player));
        player.setData(SoulmarkAttachments.TRAITS.get(), TraitUtil.getTraitData(player));
        player.setData(SoulmarkAttachments.SKILL_TREE.get(), SkillTreeUtil.getTreeData(player));
    }
}

