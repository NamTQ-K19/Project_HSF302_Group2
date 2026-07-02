package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho trạng thái của một item trong order
 */
public enum OrderItemStatus {
    PENDING("PENDING"),
    PREPARING("PREPARING"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED");

    private final String value;

    OrderItemStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OrderItemStatus fromValue(String value) {
        if (value == null) return null;
        for (OrderItemStatus s : OrderItemStatus.values()) {
            if (s.value.equalsIgnoreCase(value)) return s;
        }
        throw new IllegalArgumentException("Invalid order item status: " + value);
    }
}

