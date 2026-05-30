package be.nerosro.soulmark.network;

import be.nerosro.soulmark.SoulMark;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server-to-client payload that syncs the player's mana state for HUD display.
 * Only contains what the client needs to render — regen logic stays server-side.
 */
public record ManaSyncPayload(
        float currentMana,
        float maxPool
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ManaSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SoulMark.MOD_ID, "mana_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ManaSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, ManaSyncPayload::currentMana,
                    ByteBufCodecs.FLOAT, ManaSyncPayload::maxPool,
                    ManaSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
