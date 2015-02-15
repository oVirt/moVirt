package org.ovirt.mobile.movirt.mqtt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;

@EBean
public class AlarmPingSender implements MqttPingSender {

    public static final String TAG = AlarmPingSender.class.getSimpleName();

    private static final String PING = "org.ovirt.mobile.movirt.PING";

    @RootContext
    Context context;

    @SystemService
    AlarmManager alarmManager;

    private BroadcastReceiver alarmReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Sending MQTT Ping Request");
            clientComms.checkForActivity();
        }
    };

    private ClientComms clientComms;
    private PendingIntent pendingIntent;

    @Override
    public void init(ClientComms clientComms) {
        this.clientComms = clientComms;
    }

    @Override
    public void start() {
        try {
            context.registerReceiver(alarmReceiver, new IntentFilter(PING));
            pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(PING), PendingIntent.FLAG_UPDATE_CURRENT);
            schedule(clientComms.getKeepAlive());
        } catch (Exception e) {
            // ignore exceptions in case of race when connection was lost before start() was called
        }
    }

    @Override
    public void stop() {
        try {
            alarmManager.cancel(pendingIntent);
            context.unregisterReceiver(alarmReceiver);
        } catch (Exception e) {
            // ignore unregister exceptions
        }
    }

    @Override
    public void schedule(long delay) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
    }
}
