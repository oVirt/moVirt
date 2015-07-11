package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageDomain implements RestEntityWrapper<org.ovirt.mobile.movirt.model.StorageDomain> {
    public String id;
    public String name;
    public String type;
    public String available;
    public String used;

    @Override
    public org.ovirt.mobile.movirt.model.StorageDomain toEntity() {
        org.ovirt.mobile.movirt.model.StorageDomain storageDomain = new org.ovirt.mobile.movirt.model.StorageDomain();
        storageDomain.setId(id);
        storageDomain.setName(name);

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
        return org.ovirt.mobile.movirt.model.StorageDomain.Type.valueOf(type.toUpperCase());
    }

}
