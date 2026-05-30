package be.nerosro.soulmark.traits;

import be.nerosro.soulmark.SoulMark;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores the player's rolled traits.
 * Traits are stored as registry IDs (Identifiers) so they survive mod changes gracefully.
 * Boost and penalty are lists to support traits like Overmarked that grant extras.
 */
public class TraitData implements ValueIOSerializable {

    private final List<Identifier> boostTraitIds = new ArrayList<>();
    @Nullable
    private Identifier neutralTraitId;
    private final List<Identifier> penaltyTraitIds = new ArrayList<>();
    private boolean initialized;

    public TraitData() {
    }

    // ── Initialization ───────────────────────────────────────────────────────

    public void setTraits(List<Identifier> boosts, @Nullable Identifier neutral,
                          List<Identifier> penalties) {
        this.boostTraitIds.clear();
        this.boostTraitIds.addAll(boosts);
        this.neutralTraitId = neutral;
        this.penaltyTraitIds.clear();
        this.penaltyTraitIds.addAll(penalties);
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public List<Identifier> getBoostTraitIds() {
        return Collections.unmodifiableList(boostTraitIds);
    }

    @Nullable
    public Identifier getNeutralTraitId() {
        return neutralTraitId;
    }

    public List<Identifier> getPenaltyTraitIds() {
        return Collections.unmodifiableList(penaltyTraitIds);
    }

    /**
     * Resolves all boost trait IDs to their Trait definitions.
     * Entries whose mods were removed are skipped.
     */
    public List<Trait> getBoostTraits() {
        return resolveList(boostTraitIds);
    }

    @Nullable
    public Trait getNeutralTrait() {
        return resolve(neutralTraitId);
    }

    /**
     * Resolves all penalty trait IDs to their Trait definitions.
     * Entries whose mods were removed are skipped.
     */
    public List<Trait> getPenaltyTraits() {
        return resolveList(penaltyTraitIds);
    }

    /**
     * Returns all resolved traits (boosts + neutral + penalties) in order.
     */
    public List<Trait> getAllTraits() {
        List<Trait> all = new ArrayList<>();
        all.addAll(getBoostTraits());
        Trait neutral = getNeutralTrait();
        if (neutral != null) all.add(neutral);
        all.addAll(getPenaltyTraits());
        return all;
    }

    @Nullable
    private Trait resolve(@Nullable Identifier id) {
        if (id == null) return null;
        return TraitRegistries.TRAIT_REGISTRY.getValue(id);
    }

    private List<Trait> resolveList(List<Identifier> ids) {
        List<Trait> resolved = new ArrayList<>();
        for (Identifier id : ids) {
            Trait trait = resolve(id);
            if (trait != null) resolved.add(trait);
        }
        return resolved;
    }

    // ── Serialization ────────────────────────────────────────────────────────
    // Format: indexed keys ("boost_0", "boost_1", etc.) with a count prefix.
    // This is simple and correct. Do not change without a migration path for existing saves.

    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean("initialized", initialized);
        if (!initialized) return;

        output.putInt("boostCount", boostTraitIds.size());
        for (int i = 0; i < boostTraitIds.size(); i++) {
            output.putString("boost_" + i, boostTraitIds.get(i).toString());
        }
        if (neutralTraitId != null) output.putString("neutral", neutralTraitId.toString());
        output.putInt("penaltyCount", penaltyTraitIds.size());
        for (int i = 0; i < penaltyTraitIds.size(); i++) {
            output.putString("penalty_" + i, penaltyTraitIds.get(i).toString());
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        initialized = input.getBooleanOr("initialized", false);
        if (!initialized) return;

        // Boost list
        boostTraitIds.clear();
        int boostCount = input.getIntOr("boostCount", 0);
        for (int i = 0; i < boostCount; i++) {
            input.getString("boost_" + i).map(Identifier::parse).ifPresent(boostTraitIds::add);
        }

        neutralTraitId = input.getString("neutral").map(Identifier::parse).orElse(null);

        // Penalty list
        penaltyTraitIds.clear();
        int penaltyCount = input.getIntOr("penaltyCount", 0);
        for (int i = 0; i < penaltyCount; i++) {
            input.getString("penalty_" + i).map(Identifier::parse).ifPresent(penaltyTraitIds::add);
        }

        // Warn once (on load) about any traits that no longer exist in the registry
        warnMissingTraits();
    }

    private void warnMissingTraits() {
        for (Identifier id : boostTraitIds) {
            if (TraitRegistries.TRAIT_REGISTRY.getValue(id) == null) {
                SoulMark.LOGGER.warn("Trait '{}' not found in registry — mod may have been removed", id);
            }
        }
        if (neutralTraitId != null && TraitRegistries.TRAIT_REGISTRY.getValue(neutralTraitId) == null) {
            SoulMark.LOGGER.warn("Trait '{}' not found in registry — mod may have been removed", neutralTraitId);
        }
        for (Identifier id : penaltyTraitIds) {
            if (TraitRegistries.TRAIT_REGISTRY.getValue(id) == null) {
                SoulMark.LOGGER.warn("Trait '{}' not found in registry — mod may have been removed", id);
            }
        }
    }
}
