package org.ovirt.mobile.movirt.rest;

import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Composes multiple {@link Response} objects and invokes their callbacks in specified order
 */
public class CompositeResponse<T> implements Response<T> {

    private List<Response<T>> responses;

    @SafeVarargs
    public CompositeResponse(Response<T>... responses) {
        this.responses = new ArrayList<>(Arrays.asList(responses));
    }

    @Override
    public void before() {
        for (Response<T> response : responses) {
            if (response != null) {
                response.before();
            }
        }
    }

    @Override
    public void onResponse(T t) throws RemoteException {
        for (Response<T> response : responses) {
            if (response != null) {
                response.onResponse(t);
            }
        }
    }

    @Override
    public void onError() {
        for (Response<T> response : responses) {
            if (response != null) {
                response.onError();
            }
        }
    }

    @Override
    public void after() {
        for (Response<T> response : responses) {
            if (response != null) {
                response.after();
            }
        }
    }

    public void addResponse(Response<T> response) {
        responses.add(response);
    }
}
