package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho policy_type trong policies
 */
public enum PolicyType {
    EARN("earn"),
    REDEEM("redeem");

    private final String value;

    PolicyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PolicyType fromValue(String value) {
        if (value == null) return null;
        for (PolicyType t : PolicyType.values()) {
            if (t.value.equalsIgnoreCase(value)) return t;
        }
        throw new IllegalArgumentException("Invalid policy type: " + value);
    }
}

