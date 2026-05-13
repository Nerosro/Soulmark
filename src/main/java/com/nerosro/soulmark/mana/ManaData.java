package com.nerosro.soulmark.mana;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Stores all mana-related data for a player.
 * Origin values are rolled once on first spawn and never change.
 * Effective values (maxPool, regenRate) can be modified by traits, gear, etc.
 */
public class ManaData implements ValueIOSerializable {

    // Origin values (immutable after first roll)
    private float originMaxPool;
    private float originRegenRate;

    // Current runtime values
    private float currentMana;
    private float maxPool;          // effective max (origin + modifiers)
    private float regenRate;        // effective regen (origin + modifiers)

    // Regen delay tracking
    private int regenDelayTicks;    // configurable base delay before regen starts
    private int ticksSinceLastCast; // ticks elapsed since last mana spend

    private boolean initialized;    // false until first-spawn roll

    // ────────────────────────────────────────────────────────────────────────────
    // Initialization
    // ────────────────────────────────────────────────────────────────────────────

    public ManaData() {
        // Default state: uninitialized
    }

    /**
     * Sets origin values. Should only be called once, during first-spawn roll.
     */
    public void setOrigin(float maxPool, float regenRate, int baseDelayTicks) {
        this.originMaxPool = maxPool;
        this.originRegenRate = regenRate;
        this.maxPool = maxPool;
        this.regenRate = regenRate;
        this.currentMana = maxPool; // start full
        this.regenDelayTicks = baseDelayTicks;
        this.ticksSinceLastCast = baseDelayTicks; // allow immediate regen at spawn
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Mana spending and regeneration
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Attempts to spend the given amount of mana.
     * Returns true if successful, false if insufficient mana.
     */
    public boolean spend(float amount) {
        if (amount <= 0 || currentMana < amount) {
            return false;
        }
        currentMana -= amount;
        ticksSinceLastCast = 0; // reset regen delay
        return true;
    }

    /**
     * Called once per tick to handle mana regeneration.
     */
    public void tick() {
        if (!initialized) return;

        ticksSinceLastCast++;

        if (ticksSinceLastCast >= regenDelayTicks && currentMana < maxPool) {
            float regenPerTick = regenRate / 20.0f;
            currentMana = Math.min(currentMana + regenPerTick, maxPool);
        }
    }

    /**
     * Returns true if the player has at least the given amount of mana.
     */
    public boolean hasEnough(float amount) {
        return currentMana >= amount;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Getters
    // ────────────────────────────────────────────────────────────────────────────

    public float getOriginMaxPool() {
        return originMaxPool;
    }

    public float getOriginRegenRate() {
        return originRegenRate;
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public float getMaxPool() {
        return maxPool;
    }

    public float getRegenRate() {
        return regenRate;
    }

    public int getRegenDelayTicks() {
        return regenDelayTicks;
    }

    public int getTicksSinceLastCast() {
        return ticksSinceLastCast;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Setters for effective values (used by traits, gear, buffs, etc.)
    // ────────────────────────────────────────────────────────────────────────────

    public void setMaxPool(float maxPool) {
        this.maxPool = maxPool;
        if (currentMana > maxPool) {
            currentMana = maxPool;
        }
    }

    public void setRegenRate(float regenRate) {
        this.regenRate = regenRate;
    }

    public void setRegenDelayTicks(int ticks) {
        this.regenDelayTicks = ticks;
    }

    public void setCurrentMana(float mana) {
        this.currentMana = Math.clamp(mana, 0, maxPool);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Serialization (ValueIOSerializable)
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean("initialized", initialized);
        if (!initialized) return;

        output.putFloat("originMaxPool", originMaxPool);
        output.putFloat("originRegenRate", originRegenRate);
        output.putFloat("currentMana", currentMana);
        output.putFloat("maxPool", maxPool);
        output.putFloat("regenRate", regenRate);
        output.putInt("regenDelayTicks", regenDelayTicks);
        output.putInt("ticksSinceLastCast", ticksSinceLastCast);
    }

    @Override
    public void deserialize(ValueInput input) {
        initialized = input.getBooleanOr("initialized", false);
        if (!initialized) return;

        originMaxPool = input.getFloatOr("originMaxPool", 0f);
        originRegenRate = input.getFloatOr("originRegenRate", 0f);
        currentMana = input.getFloatOr("currentMana", 0f);
        maxPool = input.getFloatOr("maxPool", originMaxPool);
        regenRate = input.getFloatOr("regenRate", originRegenRate);
        regenDelayTicks = input.getIntOr("regenDelayTicks", 40);
        ticksSinceLastCast = input.getIntOr("ticksSinceLastCast", 0);
    }
}