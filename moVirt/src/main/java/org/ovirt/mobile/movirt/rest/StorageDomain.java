package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageDomain implements RestEntityWrapper<org.ovirt.mobile.movirt.model.StorageDomain> {
    public String id;
    public String name;
    public String type;
    public String available;
    public String used;
    public Status status;
    public Storage storage;
    public String storage_format;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Storage {
        public String address;
        public String type;
        public String path;
    }

    @Override
    public org.ovirt.mobile.movirt.model.StorageDomain toEntity() {
        org.ovirt.mobile.movirt.model.StorageDomain storageDomain = new org.ovirt.mobile.movirt.model.StorageDomain();
        storageDomain.setId(id);
        storageDomain.setName(name);
        storageDomain.setStorageFormat(storage_format);

        if (status != null && status.state != null) {
            storageDomain.setStatus(mapStatus(status));
        } else {
            storageDomain.setStatus(org.ovirt.mobile.movirt.model.StorageDomain.Status.ACTIVE);
        }

        if (storage != null) {
            if (storage.address != null) {
                storageDomain.setStorageAddress(storage.address);
            }
            if (storage.type != null) {
                storageDomain.setStorageType(storage.type);
            }
            if (storage.path != null) {
                storageDomain.setStoragePath(storage.path);
            }
        }

        storageDomain.setType(mapType(type));
        try {
            storageDomain.setAvailableSizeMb(Long.parseLong(available) / (1024 * 1024));
        } catch (Exception e) {
            storageDomain.setAvailableSizeMb(-1);
        }
        try {
            storageDomain.setUsedSizeMb(Long.parseLong(used) / (1024 * 1024));
        } catch (Exception e) {
            storageDomain.setUsedSizeMb(-1);
        }

        return storageDomain;
    }

    private static org.ovirt.mobile.movirt.model.StorageDomain.Type mapType(String type) {
        try {
            return org.ovirt.mobile.movirt.model.StorageDomain.Type.valueOf(type.toUpperCase());
        } catch (Exception e) {
            // guess it is this...
            return org.ovirt.mobile.movirt.model.StorageDomain.Type.DATA;
        }

    }

    private static org.ovirt.mobile.movirt.model.StorageDomain.Status mapStatus(Status status) {
        try {
            return org.ovirt.mobile.movirt.model.StorageDomain.Status.valueOf(status.state.toUpperCase());
        } catch (Exception e) {
            return org.ovirt.mobile.movirt.model.StorageDomain.Status.UNKNOWN;
        }
    }

}
