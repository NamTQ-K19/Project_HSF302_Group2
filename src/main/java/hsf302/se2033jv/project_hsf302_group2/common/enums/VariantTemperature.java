package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho nhiệt độ của product variant
 */
public enum VariantTemperature {
    HOT("HOT"),
    COLD("COLD"),
    ROOM("ROOM");

    private final String value;

    VariantTemperature(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static VariantTemperature fromValue(String value) {
        if (value == null) return null;
        for (VariantTemperature temp : VariantTemperature.values()) {
            if (temp.value.equalsIgnoreCase(value)) {
                return temp;
            }
        }
        throw new IllegalArgumentException("Invalid temperature: " + value);
    }
}

