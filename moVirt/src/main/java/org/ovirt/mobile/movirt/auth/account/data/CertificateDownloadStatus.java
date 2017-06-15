package org.ovirt.mobile.movirt.auth.account.data;

public class CertificateDownloadStatus extends Status {

    public CertificateDownloadStatus(MovirtAccount account, boolean downloadInProgress) {
        super(account, downloadInProgress);
    }
}
