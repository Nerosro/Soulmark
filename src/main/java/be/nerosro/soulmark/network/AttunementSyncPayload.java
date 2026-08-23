package be.nerosro.soulmark.network;

import be.nerosro.soulmark.SoulMark;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Server-to-client payload that syncs the player's attunement state for Tome display.
 * The element identifier is "soulmark:none" when the player is not yet attuned
 * (Identifier's stream codec is not null-safe, so a sentinel is used instead of null).
 */
public record AttunementSyncPayload(
        Identifier elementId
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AttunementSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SoulMark.MOD_ID, "attunement_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AttunementSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC, AttunementSyncPayload::elementId,
                    AttunementSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
