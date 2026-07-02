package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho loại giao dịch điểm
 */
public enum TransactionType {
    EARN("EARN"),
    REDEEM("REDEEM");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TransactionType fromValue(String value) {
        if (value == null) return null;
        for (TransactionType t : TransactionType.values()) {
            if (t.value.equalsIgnoreCase(value)) return t;
        }
        throw new IllegalArgumentException("Invalid transaction type: " + value);
    }
}

