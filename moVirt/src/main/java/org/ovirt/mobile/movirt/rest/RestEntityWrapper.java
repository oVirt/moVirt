package org.ovirt.mobile.movirt.rest;

import org.ovirt.mobile.movirt.model.OVirtEntity;

interface RestEntityWrapper<E extends OVirtEntity> {
    E toEntity();
}
