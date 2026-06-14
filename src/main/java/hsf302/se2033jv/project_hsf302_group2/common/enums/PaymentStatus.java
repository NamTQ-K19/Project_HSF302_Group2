package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho trạng thái thanh toán
 */
public enum PaymentStatus {
    PENDING("pending"),
    SUCCESS("success"),
    FAILED("failed"),
    REFUNDED("refunded");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentStatus fromValue(String value) {
        if (value == null) return null;
        for (PaymentStatus s : PaymentStatus.values()) {
            if (s.value.equalsIgnoreCase(value)) return s;
        }
        throw new IllegalArgumentException("Invalid payment status: " + value);
    }
}

