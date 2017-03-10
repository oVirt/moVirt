package org.ovirt.mobile.movirt.ui;

import android.support.v7.app.AppCompatActivity;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.Receiver;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiver;
import org.ovirt.mobile.movirt.util.message.CreateDialogBroadcastReceiverHelper;

@EActivity
public abstract class BroadcastAwareAppCompatActivity extends AppCompatActivity implements CreateDialogBroadcastReceiver {

    @Receiver(actions = {Broadcasts.ERROR_MESSAGE},
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    public void showErrorDialog(
            @Receiver.Extra(Broadcasts.Extras.ERROR_REASON) String reason,
            @Receiver.Extra(Broadcasts.Extras.REPEATED_MINOR_ERROR) boolean repeatedMinorError) {
        CreateDialogBroadcastReceiverHelper.showErrorDialog(getFragmentManager(), reason, repeatedMinorError);
    }

    @Receiver(actions = {Broadcasts.REST_CA_FAILURE},
            registerAt = Receiver.RegisterAt.OnResumeOnPause)
    public void showCertificateDialog(
            @Receiver.Extra(Broadcasts.Extras.ERROR_REASON) String reason) {
        CreateDialogBroadcastReceiverHelper.showCertificateDialog(getFragmentManager(), reason, true);
    }
}
