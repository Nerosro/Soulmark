package be.nerosro.soulmark.skilltree;

/**
 * The category of a skill node, determines the shape of the icon frame in the UI
 * and influences unlock behavior.
 */
public enum NodeType {
    /** Passive bonuses (filled square indicator) */
    PASSIVE("Passive"),
    /** Active abilities (diamond indicator) */
    ABILITY("Ability"),
    /** Utility skills (square indicator) */
    UTILITY("Utility"),
    /** Recipe unlock nodes (scroll indicator). Learning grants access to a crafting recipe. */
    RECIPE("Recipe"),
    /** Ritual nodes (circle indicator). Triggers a transformative event or ceremony. */
    RITUAL("Ritual"),
    /** Discovery nodes (hidden). Used for gating Tome entries and tracking world discoveries. Never visible in skill tree. */
    DISCOVERY("Discovery"),
    /** Specialization commitment nodes (diamond indicator). Typically triggers exclusion groups. */
    SPECIALIZATION("Specialization"),
    /** End-of-branch capstone nodes (star indicator). Powerful, one per specialization. */
    CAPSTONE("Capstone");

    private final String displayName;

    NodeType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

