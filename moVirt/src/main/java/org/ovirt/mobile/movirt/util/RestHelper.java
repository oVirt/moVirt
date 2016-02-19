package org.ovirt.mobile.movirt.util;

import org.ovirt.mobile.movirt.rest.RestEntityWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RestHelper {
    public static <E, R extends RestEntityWrapper<E>> List<E> mapToEntities(List<R> wrappers) {
        if (wrappers == null) {
            return Collections.emptyList();
        }

        List<E> entities = new ArrayList<>();
        for (R rest : wrappers) {
            entities.add(rest.toEntity());
        }
        return entities;
    }
}
