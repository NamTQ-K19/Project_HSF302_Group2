package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho trạng thái thanh toán của deposit
 */
public enum DepositPaymentStatus {
    PENDING("PENDING"),
    PAID("PAID"),
    REFUNDED("REFUNDED"),
    CANCELLED("CANCELLED"),
    FORFEITED("FORFEITED");

    private final String value;

    DepositPaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DepositPaymentStatus fromValue(String value) {
        if (value == null) return null;
        for (DepositPaymentStatus s : DepositPaymentStatus.values()) {
            if (s.value.equalsIgnoreCase(value)) return s;
        }
        throw new IllegalArgumentException("Invalid deposit payment status: " + value);
    }
}

