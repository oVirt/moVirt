package org.ovirt.mobile.movirt.util;

import java.util.Comparator;

public class NullPriorityComparator implements Comparator<Comparable> {

    @Override
    public int compare(Comparable o1, Comparable o2) {
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        } else {
            return o2 == null ? 1 : o1.compareTo(o2);
        }
    }
}
