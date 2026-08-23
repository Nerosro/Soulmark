package be.nerosro.soulmark;

import com.mojang.logging.LogUtils;
import be.nerosro.soulmark.capability.SoulmarkAttachments;
import be.nerosro.soulmark.dev.DevEvents;
import be.nerosro.soulmark.element.ElementRegistry;
import be.nerosro.soulmark.element.SoulmarkElements;
import be.nerosro.soulmark.events.SoulmarkEvents;
import be.nerosro.soulmark.network.SoulmarkNetwork;
import be.nerosro.soulmark.skilltree.SkillTreeRegistries;
import be.nerosro.soulmark.traits.TraitRegistries;
import be.nerosro.soulmark.traits.SoulmarkTraits;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

@Mod(SoulMark.MOD_ID)
public class SoulMark {
    public static final String MOD_ID = "soulmark";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SoulMark(IEventBus modEventBus, ModContainer modContainer) {
        // Register custom registries (traits)
        modEventBus.addListener(this::registerRegistries);

        // Register elements
        SoulmarkElements.register(modEventBus);

        // Register Soulmark's own traits
        SoulmarkTraits.register(modEventBus);

        // Register data attachments (mana, affinity, traits)
        SoulmarkAttachments.register(modEventBus);

        // Register network payloads
        modEventBus.addListener(SoulmarkNetwork::register);

        // Register game event listeners
        NeoForge.EVENT_BUS.register(new SoulmarkEvents());
        NeoForge.EVENT_BUS.register(new DevEvents());

        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, SoulmarkConfig.SPEC, "soulmark-common.toml");
    }

    private void registerRegistries(NewRegistryEvent event) {
        event.register(ElementRegistry.ELEMENT_REGISTRY);
        event.register(TraitRegistries.TRAIT_REGISTRY);
        event.register(SkillTreeRegistries.TREE_REGISTRY);
        event.register(SkillTreeRegistries.NODE_REGISTRY);
    }
}
