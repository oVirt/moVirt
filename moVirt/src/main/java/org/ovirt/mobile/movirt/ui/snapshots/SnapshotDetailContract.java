package org.ovirt.mobile.movirt.ui.snapshots;

import org.ovirt.mobile.movirt.ui.mvp.AccountPresenter;
import org.ovirt.mobile.movirt.ui.mvp.FinishableProgressBarView;
import org.ovirt.mobile.movirt.ui.mvp.StatusView;

public interface SnapshotDetailContract {

    interface View extends FinishableProgressBarView, StatusView {

        void displayMenu(MenuState menuState);

        void openPreviewDialog();

        void openRestoreDialog();
    }

    interface Presenter extends AccountPresenter {
        Presenter setIds(String snapshotId, String vmId);

        void onPreviewSnapshot();

        void onRestoreSnapshot();

        void previewSnapshot(boolean restoreMemory);

        void restoreSnapshot(boolean restoreMemory);

        void commitSnapshot();

        void undoSnapshot();

        void deleteSnapshot();
    }
}

