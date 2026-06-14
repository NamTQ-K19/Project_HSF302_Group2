package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho loại order
 */
public enum OrderType {
    ONLINE("online"),
    COUNTER("counter");

    private final String value;

    OrderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OrderType fromValue(String value) {
        if (value == null) return null;
        for (OrderType t : OrderType.values()) {
            if (t.value.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Invalid order type: " + value);
    }
}

