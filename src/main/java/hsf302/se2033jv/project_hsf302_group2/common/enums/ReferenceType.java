package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho loại tham chiếu của điểm (order, review)
 */
public enum ReferenceType {
    ORDER("order"),
    REVIEW("review");

    private final String value;

    ReferenceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ReferenceType fromValue(String value) {
        if (value == null) return null;
        for (ReferenceType r : ReferenceType.values()) {
            if (r.value.equalsIgnoreCase(value)) return r;
        }
        throw new IllegalArgumentException("Invalid reference type: " + value);
    }
}

