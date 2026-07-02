package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho trạng thái refund của deposit
 */
public enum RefundStatus {
    NONE("NONE"),
    PARTIAL("PARTIAL"),
    FULL("FULL");

    private final String value;

    RefundStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RefundStatus fromValue(String value) {
        if (value == null) return null;
        for (RefundStatus s : RefundStatus.values()) {
            if (s.value.equalsIgnoreCase(value)) return s;
        }
        throw new IllegalArgumentException("Invalid refund status: " + value);
    }
}

