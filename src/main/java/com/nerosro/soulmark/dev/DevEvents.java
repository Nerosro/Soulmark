package com.nerosro.soulmark.dev;

import com.nerosro.soulmark.affinity.Affinity;
import com.nerosro.soulmark.affinity.AffinityData;
import com.nerosro.soulmark.affinity.AffinityUtil;
import com.nerosro.soulmark.capability.SoulmarkAttachments;
import com.nerosro.soulmark.mana.ManaData;
import com.nerosro.soulmark.mana.ManaUtil;
import com.nerosro.soulmark.traits.Trait;
import com.nerosro.soulmark.traits.TraitData;
import com.nerosro.soulmark.traits.TraitUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Dev-only debug tools. Creative-mode only.
 * - Paper: print all player stats to chat.
 * - Feather: reroll all origin stats.
 * - Ink Sac: reroll traits only.
 * - Stick: fake cast, spends 10–25 mana randomly.
 */
public class DevEvents {

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.isCreative()) return;

        if (event.getItemStack().is(Items.PAPER)) {
            printManaDebug(player);
        } else if (event.getItemStack().is(Items.FEATHER)) {
            rerollStats(player);
        } else if (event.getItemStack().is(Items.INK_SAC)) {
            rerollTraits(player);
        } else if (event.getItemStack().is(Items.STICK)) {
            fakeCast(player);
        }
    }

    private void printManaDebug(ServerPlayer player) {
        ManaData mana = ManaUtil.getMana(player);

        if (!mana.isInitialized()) {
            player.sendSystemMessage(Component.literal("[Soulmark Debug] Mana not yet initialized.")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        player.sendSystemMessage(Component.literal("═══ Soulmark Debug ═══")
                .withStyle(ChatFormatting.GOLD));

        // Affinity
        Affinity affinity = AffinityUtil.getAffinity(player);
        player.sendSystemMessage(Component.literal("Affinity: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(affinity != null ? affinity.displayName() : "None")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)));

        // Mana
        player.sendSystemMessage(Component.literal("Origin Pool: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1f", mana.getOriginMaxPool()))
                        .withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("Origin Regen: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f /s", mana.getOriginRegenRate()))
                        .withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("Current Mana: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1f / %.1f", mana.getCurrentMana(), mana.getMaxPool()))
                        .withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("Effective Regen: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f /s", mana.getRegenRate()))
                        .withStyle(ChatFormatting.AQUA)));
        player.sendSystemMessage(Component.literal("Regen Delay: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%d ticks (%.1fs)", mana.getRegenDelayTicks(), mana.getRegenDelayTicks() / 20f))
                        .withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("Ticks Since Cast: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(mana.getTicksSinceLastCast()))
                        .withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("Mana %: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1f%%", ManaUtil.getManaFraction(player) * 100f))
                        .withStyle(ChatFormatting.GREEN)));

        // Traits
        TraitData traitData = TraitUtil.getTraitData(player);
        if (traitData.isInitialized()) {
            player.sendSystemMessage(Component.literal("Traits: ").withStyle(ChatFormatting.GRAY)
                    .append(buildTraitLine(traitData)));
        }
    }

    private Component buildTraitLine(TraitData traitData) {

        var builder = Component.empty();
        boolean first = true;

        Trait[] traits = { traitData.getBoostTrait(), traitData.getNeutralTrait(), traitData.getPenaltyTrait() };
        for (Trait trait : traits) {
            if (trait == null) continue;
            if (!first) builder.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            builder.append(Component.literal(trait.name()).withStyle(trait.weight().getColor()));
            first = false;
        }

        if (first) {
            // All trait IDs resolved to null — can happen if the mods that registered them were removed
            builder.append(Component.literal("None").withStyle(ChatFormatting.DARK_GRAY));
        }
        return builder;
    }

    private void rerollStats(ServerPlayer player) {
        ManaData mana = new ManaData();
        player.sendSystemMessage(Component.literal("[Soulmark Debug] Rerolling stats!")
                .withStyle(ChatFormatting.YELLOW));

        ManaUtil.rollOrigin(mana, player.getRandom());
        player.setData(SoulmarkAttachments.MANA.get(), mana);

        player.sendSystemMessage(Component.literal("  New Pool: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1f", mana.getOriginMaxPool()))
                        .withStyle(ChatFormatting.WHITE)));
        player.sendSystemMessage(Component.literal("  New Regen: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.2f /s", mana.getOriginRegenRate()))
                        .withStyle(ChatFormatting.WHITE)));

        AffinityData affinityData = new AffinityData();
        AffinityUtil.rollOrigin(affinityData, player.getRandom());
        player.setData(SoulmarkAttachments.AFFINITY.get(), affinityData);

        player.sendSystemMessage(Component.literal("  New Affinity: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(affinityData.getAffinity().displayName())
                        .withStyle(ChatFormatting.LIGHT_PURPLE)));

        // Traits
        rerollTraits(player);
    }

    private void rerollTraits(ServerPlayer player) {
        TraitData traitData = new TraitData();
        TraitUtil.rollOrigin(traitData, player.getRandom());
        player.setData(SoulmarkAttachments.TRAITS.get(), traitData);

        player.sendSystemMessage(Component.literal("[Soulmark Debug] New Traits: ")
                .withStyle(ChatFormatting.YELLOW)
                .append(buildTraitLine(traitData)));
    }

    private void fakeCast(ServerPlayer player) {
        ManaData mana = ManaUtil.getMana(player);

        if (!mana.isInitialized()) {
            player.sendSystemMessage(Component.literal("[Soulmark Debug] Mana not yet initialized.")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        float cost = 10f + player.getRandom().nextFloat() * 15f; // random 10–25
        boolean success = ManaUtil.trySpend(player, cost);

        if (success) {
            player.sendSystemMessage(Component.literal("[Soulmark Debug] Cast! Spent ")
                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.literal(String.format("%.1f", cost))
                            .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(String.format(" mana. Remaining: %.1f / %.1f",
                            mana.getCurrentMana(), mana.getMaxPool()))
                            .withStyle(ChatFormatting.GRAY)));
        } else {
            player.sendSystemMessage(Component.literal("[Soulmark Debug] Not enough mana! Need ")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal(String.format("%.1f", cost))
                            .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(String.format(", have %.1f", mana.getCurrentMana()))
                            .withStyle(ChatFormatting.GRAY)));
        }
    }
}

