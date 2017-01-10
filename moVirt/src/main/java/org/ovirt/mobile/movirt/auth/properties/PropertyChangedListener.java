package org.ovirt.mobile.movirt.auth.properties;

/**
 * Binds {@linkplain AccountProperty Account Property} and type of that property together.
 *
 * @param <E> type should correspond to AccountProperty as described in documentation of {@linkplain AccountProperty Account Property}.
 */
public interface PropertyChangedListener<E> {
    void onPropertyChange(E property);

    AccountProperty getProperty();
}
