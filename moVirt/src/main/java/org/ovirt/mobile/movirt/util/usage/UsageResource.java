package org.ovirt.mobile.movirt.util.usage;

public interface UsageResource {
    long getValue();

    void setValue(long value);

    void addValue(long value);

    String getReadableValueAsString();

    String getReadableUnitAsString();
}
