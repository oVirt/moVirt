package org.ovirt.mobile.movirt.util.usage;

public class Percentage implements UsageResource {
    private static final String unit = "%";
    private long value = 0;

    /**
     * percentage initialized to 0
     */
    public Percentage() {
    }

    /**
     * @param value truncated to 0 if value < 0
     */
    public Percentage(long value) {
        setValue(value);
    }

    /**
     * @return value
     */
    public long getValue() {
        return value;
    }

    /**
     * @param value truncated to 0 if value < 0
     */
    public void setValue(long value) {
        if (value > 0) {
            this.value = value;
        }
    }

    /**
     * @param value adds value, no operation occurs if value < 0
     */
    public void addValue(long value) {
        if (value > 0) {
            this.value += value;
        }
    }

    /**
     * @return string representation of value of this percentage
     */
    public String getReadableValueAsString() {
        return Long.toString(value);
    }

    /**
     * @return unit %
     */
    public String getReadableUnitAsString() {
        return unit;
    }

    @Override
    public String toString() {
        return getReadableValueAsString() + unit;
    }
}
