package be.nerosro.soulmark.dev;

import be.nerosro.soulmark.affinity.Affinity;
import be.nerosro.soulmark.affinity.AffinityData;
import be.nerosro.soulmark.affinity.AffinityUtil;
import be.nerosro.soulmark.capability.SoulmarkAttachments;
import be.nerosro.soulmark.mana.ManaData;
import be.nerosro.soulmark.mana.ManaUtil;
import be.nerosro.soulmark.traits.Trait;
import be.nerosro.soulmark.traits.TraitData;
import be.nerosro.soulmark.traits.TraitUtil;
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
 */
public class DevEvents {

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (event.getItemStack().is(Items.PAPER)) {
            printManaDebug(player);
        } else if (event.getItemStack().is(Items.FEATHER)) {
            rerollStats(player);
        } else if (event.getItemStack().is(Items.INK_SAC)) {
            rerollTraits(player);
        }
    }

    private void printManaDebug(ServerPlayer player) {
        ManaData mana = ManaUtil.getMana(player);

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

        for (Trait trait : traitData.getAllTraits()) {
            if (!first) builder.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            builder.append(trait.weight().styledName(trait.name()));
            first = false;
        }

        if (first) {
            builder.append(Component.literal("None").withStyle(ChatFormatting.DARK_GRAY));
        }
        return builder;
    }

    private void rerollStats(ServerPlayer player) {
        ManaData mana = new ManaData();
        player.sendSystemMessage(Component.literal("[Soulmark] Rerolling stats!")
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

        player.sendSystemMessage(Component.literal("[Soulmark] New Traits: ")
                .withStyle(ChatFormatting.YELLOW)
                .append(buildTraitLine(traitData)));
    }
}
