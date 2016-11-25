package org.ovirt.mobile.movirt.ui.dialogs;

import org.ovirt.mobile.movirt.rest.dto.Snapshot;

import java.net.URL;

public interface DialogListener {
    interface NewSnapshotListener {
        void onDialogResult(Snapshot snapshot);
    }

    interface UrlListener {
        void onNewDialogUrl(URL url, boolean startNewChain);
    }
}

