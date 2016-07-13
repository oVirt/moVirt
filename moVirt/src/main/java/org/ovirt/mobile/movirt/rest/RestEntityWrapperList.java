package org.ovirt.mobile.movirt.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by suomiy on 6/21/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class RestEntityWrapperList<E extends RestEntityWrapper> {
    private List<E> list;

    public RestEntityWrapperList(List<E> list) {
        this.list = list;
    }

    public List<E> getList() {
        return list;
    }
}
