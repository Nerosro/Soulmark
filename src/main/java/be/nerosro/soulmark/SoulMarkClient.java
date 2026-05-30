package be.nerosro.soulmark;

import be.nerosro.soulmark.skilltree.SoulmarkInputHandler;
import be.nerosro.soulmark.skilltree.SkillTreeKeybinds;
import be.nerosro.soulmark.ui.RadialMenuOverlay;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = SoulMark.MOD_ID, dist = Dist.CLIENT)
public class SoulMarkClient {

    private static final RadialMenuOverlay RADIAL_OVERLAY = new RadialMenuOverlay();

    public SoulMarkClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        // Register keybinds on the mod bus
        modEventBus.addListener(this::onRegisterKeyMappings);

        // Register GUI layers on the mod bus
        modEventBus.addListener(this::onRegisterGuiLayers);

        // Register client-side game event listeners
        NeoForge.EVENT_BUS.register(new SoulmarkInputHandler());
        NeoForge.EVENT_BUS.register(RADIAL_OVERLAY);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SkillTreeKeybinds.OPEN_SKILL_TREE);
    }

    private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(SoulMark.MOD_ID, "radial_menu"),
                RADIAL_OVERLAY
        );
    }
}
