package be.nerosro.soulmark.skilltree;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.InputEvent;

/**
 * Raw keybind action checks that distinguish a physical press from held-key repeats.
 */
public final class KeybindInput {

    private KeybindInput() {
    }

    public static boolean isInitialPress(InputEvent.Key event, KeyMapping keybind) {
        return isInitialPress(event) && keybind.matches(event.getKeyEvent());
    }

    public static boolean isRelease(InputEvent.Key event, KeyMapping keybind) {
        return event.getAction() == GLFW.GLFW_RELEASE && keybind.matches(event.getKeyEvent());
    }

    public static boolean isInitialPress(InputEvent.Key event) {
        return event.getAction() == GLFW.GLFW_PRESS;
    }
}