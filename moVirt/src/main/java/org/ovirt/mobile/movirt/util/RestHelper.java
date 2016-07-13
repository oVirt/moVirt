package org.ovirt.mobile.movirt.util;

import android.util.Log;

import org.ovirt.mobile.movirt.rest.RestEntityWrapper;
import org.ovirt.mobile.movirt.rest.RestEntityWrapperList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RestHelper {
    private static final String TAG = RestHelper.class.getSimpleName();

    public static <E, U extends RestEntityWrapper<E>> List<E> mapToEntities(RestEntityWrapperList<U> wrappersList) {
        if (wrappersList == null) {
            return Collections.emptyList();
        }

        List<U> wrappers = wrappersList.getList();

        if (wrappers == null) {
            return Collections.emptyList();
        }

        List<E> entities = new ArrayList<>();
        for (U rest : wrappers) {
            try {
                entities.add(rest.toEntity());
            } catch (Exception e) {
                Log.e(TAG, "Error parsing rest response, ignoring: " + rest.toString() + " error: " + e.getMessage());
            }
        }
        return entities;
    }
}
