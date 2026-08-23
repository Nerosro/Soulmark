package be.nerosro.soulmark.capability;

import be.nerosro.soulmark.SoulMark;
import be.nerosro.soulmark.affinity.AffinityData;
import be.nerosro.soulmark.attunement.AttunementData;
import be.nerosro.soulmark.mana.ManaData;
import be.nerosro.soulmark.soulpoint.SoulPointData;
import be.nerosro.soulmark.skilltree.SkillTreeData;
import be.nerosro.soulmark.traits.TraitData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class SoulmarkAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SoulMark.MOD_ID);

    public static final Supplier<AttachmentType<ManaData>> MANA =
            ATTACHMENTS.register("mana", () ->
                    AttachmentType.serializable(ManaData::new)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<AffinityData>> AFFINITY =
            ATTACHMENTS.register("affinity", () ->
                    AttachmentType.serializable(AffinityData::new)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<TraitData>> TRAITS =
            ATTACHMENTS.register("traits", () ->
                    AttachmentType.serializable(TraitData::new)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<AttunementData>> ATTUNEMENT =
            ATTACHMENTS.register("attunement", () ->
                    AttachmentType.serializable(AttunementData::new)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<SkillTreeData>> SKILL_TREE =
            ATTACHMENTS.register("skill_tree", () ->
                    AttachmentType.serializable(SkillTreeData::new)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<SoulPointData>> SOUL_POINTS =
            ATTACHMENTS.register("soul_points", () ->
                    AttachmentType.serializable(SoulPointData::new)
                            .copyOnDeath()
                            .build()
            );

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}
