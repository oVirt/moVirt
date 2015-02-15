package org.ovirt.mobile.movirt.mqtt;

import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.ReceiverAction;
import org.androidannotations.api.support.content.AbstractBroadcastReceiver;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.sync.SyncAdapter;
import org.ovirt.mobile.movirt.sync.SimpleResponse;
import org.springframework.util.StringUtils;

@EReceiver
public class MqttReceiver extends AbstractBroadcastReceiver {

    public static final String TAG = MqttReceiver.class.getSimpleName();

    @Bean
    SyncAdapter syncAdapter;

    @ReceiverAction(Broadcasts.VMS_UPDATED)
    void vmUpdated(@ReceiverAction.Extra(Broadcasts.Extras.ID) String id,
                   @ReceiverAction.Extra(Broadcasts.Extras.CHANGED_FIELDS) String[] fields) {
        syncAdapter.syncVm(id, new SimpleResponse<Vm>());
        Log.d(TAG, "Syncing VM " + id + " based on push notification. Changed fields: " +
                StringUtils.arrayToDelimitedString(fields, ", "));
    }
}
