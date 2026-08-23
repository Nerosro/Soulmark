package be.nerosro.soulmark.skilltree;

import com.mojang.blaze3d.platform.InputConstants;
import be.nerosro.soulmark.SoulMark;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

/**
 * Keybind registration for the skill tree.
 */
public final class SkillTreeKeybinds {

    private SkillTreeKeybinds() {}

    private static final KeyMapping.Category SOULMARK_CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(SoulMark.MOD_ID, "keys"));

    public static final KeyMapping OPEN_SKILL_TREE = new KeyMapping(
            "key." + SoulMark.MOD_ID + ".open_skill_tree",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            SOULMARK_CATEGORY
    );
}
