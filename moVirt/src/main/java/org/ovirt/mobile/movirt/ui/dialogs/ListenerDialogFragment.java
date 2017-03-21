package org.ovirt.mobile.movirt.ui.dialogs;

import android.app.DialogFragment;

import org.androidannotations.annotations.EFragment;

@EFragment
public abstract class ListenerDialogFragment<E> extends DialogFragment {

    @SuppressWarnings("unchecked")
    public E getListener() {
        try {
            return (E) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement E listener");
        }
    }
}
