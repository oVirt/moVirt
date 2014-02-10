package org.ovirt.mobile.movirt.rest;

import org.ovirt.mobile.movirt.model.BaseEntity;

interface RestEntityWrapper<E extends BaseEntity> {
    E toEntity();
}
