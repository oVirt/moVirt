package org.ovirt.mobile.movirt.ui.dialogs;

import org.ovirt.mobile.movirt.rest.dto.Snapshot;

public interface DialogListener {
    interface NewSnapshotListener {
        void onDialogResult(Snapshot snapshot);
    }
}

