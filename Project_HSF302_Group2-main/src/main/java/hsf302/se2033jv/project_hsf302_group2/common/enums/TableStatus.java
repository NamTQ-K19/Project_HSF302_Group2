package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho status của một bàn/table
 */
public enum TableStatus {
    AVAILABLE("AVAILABLE"),
    OCCUPIED("OCCUPIED"),
    RESERVED("RESERVED"),
    MAINTENANCE("MAINTENANCE");

    private final String value;

    TableStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TableStatus fromValue(String value) {
        if (value == null) return null;
        for (TableStatus status : TableStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid table status: " + value);
    }
}

