package be.nerosro.soulmark.network;

import be.nerosro.soulmark.SoulMark;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server-to-client payload that syncs the player's mana state for HUD display.
 * Also carries discovery flags (affinity and traits revealed) and the affinity identifier.
 * Includes separate trinket bonuses for accurate stat breakdown display in Tome.
 */
public record ManaSyncPayload(
        float currentMana,
        float maxPool,
        float manaBase,
        float poolTrinketBonus,
        float regenTrinketBonus,
        boolean affinityRevealed,
        boolean traitsRevealed,
        boolean scarsRevealed,
        boolean hasExperiencedManaCollapse,
        Identifier affinityId
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ManaSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SoulMark.MOD_ID, "mana_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ManaSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, ManaSyncPayload::currentMana,
                    ByteBufCodecs.FLOAT, ManaSyncPayload::maxPool,
                    ByteBufCodecs.FLOAT, ManaSyncPayload::manaBase,
                    ByteBufCodecs.FLOAT, ManaSyncPayload::poolTrinketBonus,
                    ByteBufCodecs.FLOAT, ManaSyncPayload::regenTrinketBonus,
                    ByteBufCodecs.BOOL, ManaSyncPayload::affinityRevealed,
                    ByteBufCodecs.BOOL, ManaSyncPayload::traitsRevealed,
                    ByteBufCodecs.BOOL, ManaSyncPayload::scarsRevealed,
                    ByteBufCodecs.BOOL, ManaSyncPayload::hasExperiencedManaCollapse,
                    Identifier.STREAM_CODEC, ManaSyncPayload::affinityId,
                    ManaSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
