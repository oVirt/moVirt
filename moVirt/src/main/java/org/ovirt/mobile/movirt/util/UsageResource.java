package org.ovirt.mobile.movirt.util;

/**
 * Created by suomiy on 5/6/16.
 */
public interface UsageResource {
    long getValue();

    void setValue(long value);

    void addValue(long value);

    String getReadableValueAsString();

    String getReadableUnitAsString();
}
