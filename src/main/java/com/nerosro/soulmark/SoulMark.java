package com.nerosro.soulmark;

import com.mojang.logging.LogUtils;
import com.nerosro.soulmark.capability.SoulmarkAttachments;
import com.nerosro.soulmark.dev.DevEvents;
import com.nerosro.soulmark.events.SoulmarkEvents;
import com.nerosro.soulmark.skilltree.SkillTreeRegistries;
import com.nerosro.soulmark.traits.SoulmarkRegistries;
import com.nerosro.soulmark.traits.SoulmarkTraits;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SoulMark.MOD_ID)
public class SoulMark {
    public static final String MOD_ID = "soulmark";
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public SoulMark(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register custom registries (traits)
        modEventBus.addListener(this::registerRegistries);

        // Register Soulmark's own traits
        SoulmarkTraits.register(modEventBus);

        // Register data attachments (mana, affinity, traits)
        SoulmarkAttachments.register(modEventBus);

        // Register game event listeners
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new SoulmarkEvents());
        NeoForge.EVENT_BUS.register(new DevEvents());

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, SoulmarkConfig.SPEC, "soulmark-common.toml");
    }

    private void registerRegistries(NewRegistryEvent event) {
        event.register(SoulmarkRegistries.TRAIT_REGISTRY);
        event.register(SkillTreeRegistries.TREE_REGISTRY);
        event.register(SkillTreeRegistries.NODE_REGISTRY);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
