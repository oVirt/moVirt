package org.ovirt.mobile.movirt.rest.dto.v4;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.List;

public class StorageDomains extends RestEntityWrapperList<StorageDomain> {
    @JsonCreator
    public StorageDomains(@JsonProperty("storage_domain") List<StorageDomain> list) {
        super(list);
    }
}



