package org.ovirt.mobile.movirt.facade.predicates;

import com.android.internal.util.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by suomiy on 2/11/16.
 * <p/>
 * Composes multiple predicates into one as AND combination of them
 */
public class AndPredicate<T> implements Predicate<T> {

    private List<Predicate<T>> predicates;

    @SafeVarargs
    public AndPredicate(Predicate<T>... predicates) {
        this.predicates = new ArrayList<>(Arrays.asList(predicates));
    }

    @Override
    public boolean apply(T t) {
        for (Predicate<T> predicate : predicates) {
            if (!predicate.apply(t)) {
                return false;
            }
        }
        return true;
    }

    public void addPredicate(Predicate<T> predicate) {
        predicates.add(predicate);
    }
}
