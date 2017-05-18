package org.ovirt.mobile.movirt.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ovirt.mobile.movirt.model.enums.StorageDomainType;
import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.dto.common.HasId;
import org.ovirt.mobile.movirt.util.ObjectUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class StorageDomain implements RestEntityWrapper<org.ovirt.mobile.movirt.model.StorageDomain>, HasId {
    public String id;
    public String name;
    public String type;
    public String available;
    public String used;
    public Storage storage;
    public String storage_format;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Storage {
        public String address;
        public String type;
        public String path;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public org.ovirt.mobile.movirt.model.StorageDomain toEntity(String accountId) {
        org.ovirt.mobile.movirt.model.StorageDomain storageDomain = new org.ovirt.mobile.movirt.model.StorageDomain();
        storageDomain.setIds(accountId, id);
        storageDomain.setName(name);
        storageDomain.setStorageFormat(storage_format);

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

        storageDomain.setType(StorageDomainType.fromString(type));
        storageDomain.setAvailableSize(ObjectUtils.parseLong(available));
        storageDomain.setUsedSize(ObjectUtils.parseLong(used));

        return storageDomain;
    }
}
