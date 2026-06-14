package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho action_type trong policies
 */
public enum PolicyActionType {
    DISCOUNT("discount"),
    ORDER("order"),
    REVIEW("review");

    private final String value;

    PolicyActionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PolicyActionType fromValue(String value) {
        if (value == null) return null;
        for (PolicyActionType t : PolicyActionType.values()) {
            if (t.value.equalsIgnoreCase(value)) return t;
        }
        throw new IllegalArgumentException("Invalid policy action type: " + value);
    }
}

