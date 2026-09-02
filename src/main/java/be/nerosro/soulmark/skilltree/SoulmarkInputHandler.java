package be.nerosro.soulmark.skilltree;

import be.nerosro.soulmark.ui.RadialMenuOpeners;
import be.nerosro.soulmark.ui.RadialMenuOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Client-side handler for the Soulmark keybind.
 * Main hand tome → skill tree. Off-hand wand → radial ability menu (overlay).
 */
public class SoulmarkInputHandler {

    private boolean actionKeyWasDown;

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        boolean actionKeyIsDown = SkillTreeKeybinds.OPEN_SKILL_TREE.isDown();
        if (actionKeyIsDown || !actionKeyWasDown) {
            actionKeyWasDown = actionKeyIsDown;
            return;
        }
        actionKeyWasDown = false;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // If radial menu is already open, close it (toggle behavior)
        if (RadialMenuOverlay.isActive()) {
            RadialMenuOverlay.confirmAndClose();
            return;
        }

        // Check main hand for skill tree opener (tome)
        Item mainHand = player.getMainHandItem().getItem();
        Identifier tree = SkillTreeOpeners.getTreeForItem(mainHand);
        if (tree != null) {
            mc.setScreen(new SkillTreeScreen(tree));
            return;
        }

        // Check off-hand for radial menu opener (wand)
        ItemStack offHandStack = player.getOffhandItem();
        RadialMenuOpeners.OpenerRegistration radial = RadialMenuOpeners.getOpenerForItem(offHandStack);
        if (radial != null) {
            var entries = radial.entryProvider().apply(player);
            if (entries != null && !entries.isEmpty()) {
                Identifier equipped = radial.equippedProvider() != null
                        ? radial.equippedProvider().apply(player)
                        : null;
                RadialMenuOverlay.open(entries, selected -> radial.onSelect().accept(player, selected), equipped);
                return;
            }
        }

        ContextualKeyActions.tryHandle(player);
    }
}
