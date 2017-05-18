package org.ovirt.mobile.movirt.ui;

import android.support.v7.app.AppCompatActivity;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiverHelper;

@EActivity
public abstract class BroadcastAwareAppCompatActivity extends AppCompatActivity {

    @Receiver(actions = {Broadcasts.ERROR_MESSAGE},
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    public void showErrorDialog(
            @Receiver.Extra(Broadcasts.Extras.ERROR_HEADER) String header,
            @Receiver.Extra(Broadcasts.Extras.ERROR_REASON) String reason) {
        CreateDialogBroadcastReceiverHelper.showErrorDialog(getFragmentManager(), header, reason);
    }

    @Receiver(actions = {Broadcasts.REST_CA_FAILURE},
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    public void showCertificateDialog(
            @Receiver.Extra(Broadcasts.Extras.ERROR_ACCOUNT) MovirtAccount account,
            @Receiver.Extra(Broadcasts.Extras.ERROR_REASON) String reason,
            @Receiver.Extra(Broadcasts.Extras.ERROR_API_URL) String apiUrl) {
        CreateDialogBroadcastReceiverHelper.showCertificateDialog(getFragmentManager(), account, reason, apiUrl);
    }
}
