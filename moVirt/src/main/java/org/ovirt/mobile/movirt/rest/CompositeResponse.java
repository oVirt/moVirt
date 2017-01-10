package org.ovirt.mobile.movirt.rest;

import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

/**
 * Composes multiple {@link Response} objects and invokes their callbacks in specified order
 */
public class CompositeResponse<T> implements Response<T> {

    private List<Response<T>> responses = new ArrayList<>();

    @SafeVarargs
    public CompositeResponse(Response<T>... responses) {
        for (Response<T> response : responses) {
            if (response != null) {
                this.responses.add(response);
            }
        }
    }

    @Override
    public void before() {
        for (Response<T> response : responses) {
            response.before();
        }
    }

    @Override
    public void onResponse(T t) throws RemoteException {
        for (Response<T> response : responses) {
            response.onResponse(t);
        }
    }

    @Override
    public void onError() {
        for (Response<T> response : responses) {
            response.onError();
        }
    }

    @Override
    public void after() {
        for (Response<T> response : responses) {
            response.after();
        }
    }

    public void addResponse(Response<T> response) {
        if (response != null) {
            responses.add(response);
        }
    }
}
