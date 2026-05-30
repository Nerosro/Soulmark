package be.nerosro.soulmark.network;

import be.nerosro.soulmark.SoulMark;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

/**
 * Server-to-client payload that syncs the player's skill tree state.
 * Sends the set of unlocked node IDs and available skill points.
 */
public record SkillTreeSyncPayload(
        Set<Identifier> unlockedNodes,
        int availablePoints
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SkillTreeSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(SoulMark.MOD_ID, "skill_tree_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillTreeSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)),
                    SkillTreeSyncPayload::unlockedNodes,
                    ByteBufCodecs.VAR_INT,
                    SkillTreeSyncPayload::availablePoints,
                    SkillTreeSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

