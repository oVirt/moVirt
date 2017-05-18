package org.ovirt.mobile.movirt.ui.auth.connectionsettings;

import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.exception.SmallMistakeException;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.exception.WrongApiPathException;
import org.ovirt.mobile.movirt.ui.auth.connectionsettings.exception.WrongArgumentException;
import org.ovirt.mobile.movirt.ui.mvp.AccountPresenter;
import org.ovirt.mobile.movirt.ui.mvp.FinishableView;

public interface ConnectionSettingsContract {

    interface View extends FinishableView {
        void setPasswordVisibility(boolean visibility);

        void displayUserName(String userName);

        void displayPassword(String password);

        void displayApiUrl(String apiUrl);

        void displayTitle(String title);

        void showLoginInProgress(boolean loginInProgress);

        void startCertificateManagementActivity(MovirtAccount account, String apiUrl);
    }

    interface Presenter extends AccountPresenter {
        void togglePasswordVisibility();

        void sanitizeAndCheckLoginInfo(LoginInfo loginInfo) throws WrongArgumentException, SmallMistakeException, WrongApiPathException;

        void login(LoginInfo loginInfo);

        void onCertificateManagementClicked(String apiUrl);
    }
}

