package org.ovirt.mobile.movirt.rest;

public interface RestEntityWrapper<E> {
    E toEntity(String accountId);
}
