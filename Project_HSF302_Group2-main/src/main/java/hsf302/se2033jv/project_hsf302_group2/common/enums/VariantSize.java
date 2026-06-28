package hsf302.se2033jv.project_hsf302_group2.common.enums;

/**
 * Enum đại diện cho size của product variant
 */
public enum VariantSize {
    S("S"),
    M("M"),
    L("L"),
    XL("XL");

    private final String value;

    VariantSize(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static VariantSize fromValue(String value) {
        if (value == null) return null;
        for (VariantSize size : VariantSize.values()) {
            if (size.value.equalsIgnoreCase(value)) {
                return size;
            }
        }
        throw new IllegalArgumentException("Invalid size: " + value);
    }
}

