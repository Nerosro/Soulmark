package com.nerosro.soulmark;

import com.nerosro.soulmark.skilltree.SkillTreeClientEvents;
import com.nerosro.soulmark.skilltree.SkillTreeKeybinds;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = SoulMark.MOD_ID, dist = Dist.CLIENT)
public class SoulMarkClient {
    public SoulMarkClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        // Register keybinds on the mod bus
        modEventBus.addListener(this::onRegisterKeyMappings);
        modEventBus.addListener(this::onClientSetup);

        // Register client-side game event listeners
        NeoForge.EVENT_BUS.register(new SkillTreeClientEvents());
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SkillTreeKeybinds.OPEN_SKILL_TREE);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        SoulMark.LOGGER.info("HELLO FROM CLIENT SETUP");
        SoulMark.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
