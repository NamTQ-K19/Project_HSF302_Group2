package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho trạng thái của order
 */
public enum OrderStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    PREPARING("preparing"),
    READY("ready"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OrderStatus fromValue(String value) {
        if (value == null) return null;
        for (OrderStatus s : OrderStatus.values()) {
            if (s.value.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid order status: " + value);
    }
}

