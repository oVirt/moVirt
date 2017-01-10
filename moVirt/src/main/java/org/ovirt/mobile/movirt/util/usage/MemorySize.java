package org.ovirt.mobile.movirt.util.usage;

import android.support.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class MemorySize implements UsageResource {
    private long value = 0;

    public enum MemoryUnit {
        B(0),
        KiB(1),
        MiB(2),
        GiB(3),
        TiB(4),
        PiB(5),
        EiB(6);

        private final int value;
        private static final Map<Integer, MemoryUnit> lookup = new HashMap<>();

        static {
            for (MemoryUnit d : MemoryUnit.values()) {
                lookup.put(d.getValue(), d);
            }
        }

        MemoryUnit(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static MemoryUnit getEnum(int order) {
            return lookup.get(order);
        }
    }

    /**
     * size initialize to 0
     */
    public MemorySize() {
    }

    /**
     * @param value size in Bytes, truncated to 0 if value < 0
     */
    public MemorySize(long value) {
        setValue(value);
    }

    /**
     * @param value size, truncated to 0 if value < 0
     * @param unit  unit of the size
     */
    public MemorySize(long value, MemoryUnit unit) {
        setValue(value, unit);
    }

    /**
     * @return value in Bytes
     */
    public long getValue() {
        return value;
    }

    /**
     * @param unit unit of the size
     * @return value in units of unit
     */
    public double getValue(MemoryUnit unit) {
        return value / (double) getPowerOf1024(unit.getValue());
    }

    /**
     * @param value size in Bytes, truncated to 0 if value < 0
     */
    public void setValue(long value) {
        if (value > 0) {
            this.value = value;
        }
    }

    /**
     * @param value size, truncated to 0 if value < 0
     * @param unit  unit of the size
     */
    public void setValue(long value, MemoryUnit unit) {
        if (value > 0) {
            this.value = value * getPowerOf1024(unit.getValue());
        }
    }

    /**
     * @param value adds value in Bytes, no operation occurs if value < 0
     */
    public void addValue(long value) {
        if (value > 0) {
            this.value += value;
        }
    }

    /**
     * @param value adds value in units of unit, no operation occurs if value < 0
     * @param unit  unit of the size
     */
    public void addValue(long value, MemoryUnit unit) {
        if (value > 0) {
            this.value += value * getPowerOf1024(unit.getValue());
        }
    }

    /**
     * Returns human readable value
     *
     * @return value converted to highest possible unit, which you can get by calling getReadableUnit()
     */
    public double getReadableValue() {
        return value / (double) getPowerOf1024(getLogarithmOfBase1024(value));
    }

    /**
     * Returns human readable value as string rounded as HALF_EVEN to one decimal place without trailing zeros
     *
     * @return value converted to highest possible unit, which you can get by calling getReadableUnit()
     */
    public String getReadableValueAsString() {
        return getReadableValueAsString(1);
    }

    /**
     * Returns human readable value as string rounded as HALF_EVEN according to precision without trailing zeros
     *
     * @param precision rounding precision of the resulted string
     * @return value converted to highest possible unit, which you can get by calling getReadableUnit()
     */
    public String getReadableValueAsString(int precision) {
        DecimalFormat decimalFormat = getDecimalFormat(precision);
        return decimalFormat.format(getReadableValue());
    }

    /**
     * Returns human readable value as string rounded as HALF_EVEN to one decimal place without trailing zeros
     *
     * @param unit result will be converted to this unit
     * @return value converted to unit
     */
    public String getReadableValueAsString(MemoryUnit unit) {
        return getReadableValueAsString(1, unit);
    }

    /**
     * Returns human readable value as string rounded as HALF_EVEN according to precision without trailing zeros
     *
     * @param precision rounding precision of the resulted string
     * @param unit      result will be converted to this unit
     * @return value converted to unit
     */
    public String getReadableValueAsString(int precision, MemoryUnit unit) {
        DecimalFormat decimalFormat = getDecimalFormat(precision);
        return decimalFormat.format(getValue(unit));
    }

    /**
     * @return highest possible unit for value
     */
    public MemoryUnit getReadableUnit() {
        return MemoryUnit.getEnum((int) getLogarithmOfBase1024(value));
    }

    /**
     * @return highest possible unit for value as a string
     */
    public String getReadableUnitAsString() {
        return getReadableUnit().name();
    }

    /**
     * @return human readable value with a unit
     */
    @Override
    public String toString() {
        return String.format("%s %s", getReadableValueAsString(), getReadableUnitAsString());
    }

    @NonNull
    private DecimalFormat getDecimalFormat(int precision) {
        DecimalFormat decimalFormat = new DecimalFormat("0");
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setMaximumFractionDigits(precision);
        return decimalFormat;
    }

    private long getPowerOf1024(long exponent) {
        return 1l << (10 * exponent);
    }

    /**
     * @param value positive number
     * @return floored logarithm of a value
     */
    private long getLogarithmOfBase1024(long value) {
        return value > 0 ? (long) (Math.log(value) / Math.log(1024d)) : 0;
    }
}
