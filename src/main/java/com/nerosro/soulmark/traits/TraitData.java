package com.nerosro.soulmark.traits;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jspecify.annotations.Nullable;

/**
 * Stores the player's rolled traits.
 * Traits are stored as registry IDs (Identifiers) so they survive mod changes gracefully.
 */
public class TraitData implements ValueIOSerializable {

    @Nullable
    private Identifier boostTraitId;
    @Nullable
    private Identifier neutralTraitId;
    @Nullable
    private Identifier penaltyTraitId;
    private boolean initialized;

    public TraitData() {
    }

    // ── Initialization ───────────────────────────────────────────────────────

    public void setTraits(@Nullable Identifier boost, @Nullable Identifier neutral,
                          @Nullable Identifier penalty) {
        this.boostTraitId = boost;
        this.neutralTraitId = neutral;
        this.penaltyTraitId = penalty;
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    @Nullable
    public Identifier getBoostTraitId() {
        return boostTraitId;
    }

    @Nullable
    public Identifier getNeutralTraitId() {
        return neutralTraitId;
    }

    @Nullable
    public Identifier getPenaltyTraitId() {
        return penaltyTraitId;
    }

    /**
     * Resolves a trait ID to its Trait definition from the registry.
     * Returns null if the trait no longer exists (e.g. mod removed).
     */
    @Nullable
    public Trait getBoostTrait() {
        return resolve(boostTraitId);
    }

    @Nullable
    public Trait getNeutralTrait() {
        return resolve(neutralTraitId);
    }

    @Nullable
    public Trait getPenaltyTrait() {
        return resolve(penaltyTraitId);
    }

    @Nullable
    private Trait resolve(@Nullable Identifier id) {
        if (id == null) return null;
        return SoulmarkRegistries.TRAIT_REGISTRY.getValue(id);
    }

    // ── Serialization ────────────────────────────────────────────────────────

    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean("initialized", initialized);
        if (!initialized) return;

        if (boostTraitId != null) output.putString("boost", boostTraitId.toString());
        if (neutralTraitId != null) output.putString("neutral", neutralTraitId.toString());
        if (penaltyTraitId != null) output.putString("penalty", penaltyTraitId.toString());
    }

    @Override
    public void deserialize(ValueInput input) {
        initialized = input.getBooleanOr("initialized", false);
        if (!initialized) return;

        boostTraitId = input.getString("boost").map(Identifier::parse).orElse(null);
        neutralTraitId = input.getString("neutral").map(Identifier::parse).orElse(null);
        penaltyTraitId = input.getString("penalty").map(Identifier::parse).orElse(null);
    }
}
