package org.ovirt.mobile.movirt.ui;

import org.ovirt.mobile.movirt.rest.dto.Snapshot;

/**
 * Created by suomiy on 2/16/16.
 */
public interface NewSnapshotListener {
    void onDialogResult(Snapshot snapshot);
}
