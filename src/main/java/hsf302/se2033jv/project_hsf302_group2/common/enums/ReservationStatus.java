package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho trạng thái reservation
 */
public enum ReservationStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    ARRIVED("arrived"),
    COMPLETED("completed"),
    CANCELLED("cancelled"),
    NO_SHOW("no_show");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ReservationStatus fromValue(String value) {
        if (value == null) return null;
        for (ReservationStatus s : ReservationStatus.values()) {
            if (s.value.equalsIgnoreCase(value)) return s;
        }
        throw new IllegalArgumentException("Invalid reservation status: " + value);
    }
}

