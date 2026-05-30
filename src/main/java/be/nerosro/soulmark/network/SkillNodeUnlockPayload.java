package be.nerosro.soulmark.network;

import be.nerosro.soulmark.SoulMark;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client-to-server payload requesting to unlock a skill node.
 * Server validates all prerequisites, points, etc. before unlocking.
 */
public record SkillNodeUnlockPayload(
        Identifier nodeId
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SkillNodeUnlockPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SoulMark.MOD_ID, "skill_node_unlock"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillNodeUnlockPayload> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC,
                    SkillNodeUnlockPayload::nodeId,
                    SkillNodeUnlockPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

