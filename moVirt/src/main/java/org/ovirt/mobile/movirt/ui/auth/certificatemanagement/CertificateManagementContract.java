package org.ovirt.mobile.movirt.ui.auth.certificatemanagement;

import android.net.Uri;

import org.ovirt.mobile.movirt.auth.properties.property.Cert;
import org.ovirt.mobile.movirt.auth.properties.property.CertHandlingStrategy;
import org.ovirt.mobile.movirt.auth.properties.property.CertLocation;
import org.ovirt.mobile.movirt.ui.mvp.AccountPresenter;
import org.ovirt.mobile.movirt.ui.mvp.FinishableView;

import java.net.URL;

public interface CertificateManagementContract {

    interface View extends FinishableView {

        void displayTitle(String title);

        void showCertificateDownloadInProgress(boolean inProgress);

        void selectCertHandlingStrategy(CertHandlingStrategy certHandlingStrategy);

        void selectCertLocationType(CertLocation certLocation);

        void showChangeCertificateLocationDeleteDialog(CertLocation certLocation);

        void showDownloadButton(CertLocation certLocation, boolean showDownloadButtons, boolean hasCerts);

        void showHostnames(String hostnames);

        void showCustomCertInnerDetailVisibility(boolean visible);

        void showCerts(Cert[] certs);

        void showImportFromFileCustomCaDialog();

        void showDownloadCustomCaDialog(String url, boolean startNewChain);

        void finish();
    }

    interface Presenter extends AccountPresenter {

        void onCertHandlingStrategyChanged(CertHandlingStrategy certHandlingStrategy);

        void onValidHostnamesChanged(String hostnames);

        void downloadDefaultCertificate();

        void downloadCustomCertificate();

        void importCustomCertificateFromFile(Uri file);

        void downloadCertificate(URL[] urls, boolean startNewChain);

        void onCertificateLocationChangeAttempt(CertLocation certLocation);

        void toggleCustomCertificateLocation(CertLocation certLocation);
    }
}

