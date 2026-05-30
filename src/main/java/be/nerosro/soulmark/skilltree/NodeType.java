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

