package com.nerosro.soulmark.skilltree;

import com.nerosro.soulmark.SoulMark;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * Custom registries for the skill tree system.
 * Job mods reference these keys to register their trees and nodes.
 */
public class SkillTreeRegistries {

    /**
     * Registry key for skill trees (one per job mod).
     */
    public static final ResourceKey<Registry<SkillTree>> TREE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(SoulMark.MOD_ID, "skill_trees"));

    /**
     * Registry key for skill nodes.
     */
    public static final ResourceKey<Registry<SkillNode>> NODE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(SoulMark.MOD_ID, "skill_nodes"));

    /**
     * The built skill tree registry.
     */
    public static final Registry<SkillTree> TREE_REGISTRY =
            new RegistryBuilder<>(TREE_REGISTRY_KEY).create();

    /**
     * The built skill node registry.
     */
    public static final Registry<SkillNode> NODE_REGISTRY =
            new RegistryBuilder<>(NODE_REGISTRY_KEY).create();
}

