package org.ovirt.mobile.movirt.ui.vms;

import android.net.Uri;

import org.ovirt.mobile.movirt.model.enums.ConsoleProtocol;
import org.ovirt.mobile.movirt.ui.mvp.AccountPresenter;
import org.ovirt.mobile.movirt.ui.mvp.FinishableProgressBarView;
import org.ovirt.mobile.movirt.ui.mvp.StatusView;

public interface VmDetailContract {

    interface View extends FinishableProgressBarView, StatusView {

        void displayMenu(MenuState menuState);

        void startMigrationActivity(String hostId, String clusterId);

        void startConsoleActivity(Uri uri);
    }

    interface Presenter extends AccountPresenter {
        Presenter setVmId(String id);

        void migrateToDefault();

        void migrateTo(String hostId);

        void stopVm();

        void rebootVm();

        void cancelMigration();

        void startVm();

        void openConsole(ConsoleProtocol protocol);

        void createSnapshot(org.ovirt.mobile.movirt.rest.dto.Snapshot snapshot);

        void beginMigration();
    }
}

