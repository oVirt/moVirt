package org.ovirt.mobile.movirt.model.enums;

public enum StorageDomainType {
    DATA,
    ISO,
    IMAGE,
    EXPORT;

    public static StorageDomainType fromString(String type) {
        try {
            return StorageDomainType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            // guess it is this...
            return StorageDomainType.DATA;
        }
    }
}
