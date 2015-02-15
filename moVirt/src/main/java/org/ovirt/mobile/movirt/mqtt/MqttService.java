package org.ovirt.mobile.movirt.mqtt;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Receiver;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.ovirt.mobile.movirt.Broadcasts;
import org.ovirt.mobile.movirt.auth.MovirtAuthenticator;

@EService
public class MqttService extends Service implements MqttCallback {

    public static final String TAG = MqttService.class.getSimpleName();

    private static final String CLIENT_ID = Settings.Secure.ANDROID_ID;
    private static final int KEEPALIVE_SECONDS = 15 * 60;

    private static final int QOS_GUARANTEE = 2;

    private MqttAsyncClient client;
    private volatile boolean connected;

    @Bean
    AlarmPingSender alarmPingSender;

    @Bean
    PushDispatcher pushDispatcher;

    @Bean
    MovirtAuthenticator authenticator;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @AfterInject
    void init() {
        if (!authenticator.useDoctorRest() || TextUtils.isEmpty(authenticator.getDoctorMqttUrl())) {
            return;
        }
        connect();
    }

    private synchronized void connect() {
        Log.i(TAG, "Entering MQTT connect ...");
        if (client != null && client.isConnected()) {
            try {
                client.disconnectForcibly();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        try {
            this.client = new MqttAsyncClient(authenticator.getDoctorMqttUrl(), CLIENT_ID, new MemoryPersistence(), alarmPingSender);
            this.client.setCallback(this);
            this.client.connect(getConnectionOptions(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    try {
                        connected = true;
                        subscribe();
                        sendBroadcast(new Intent(Broadcasts.MQTT_CONNECTED));
                        Log.i(TAG, "MQTT Connection successful!");
                    } catch (MqttException e) {
                        Log.e(TAG, "Error subscribing to messages!", e);
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable e) {
                    Log.e(TAG, "Error connecting to MQTT broker at: " + authenticator.getDoctorMqttUrl(), e);
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Error connecting to MQTT broker at: " + authenticator.getDoctorMqttUrl(), e);
        }
    }

    private synchronized void subscribe() throws MqttException {
        if (!client.isConnected()) {
            return;
        }
        for (String topic : pushDispatcher.getTopics()) {
            client.subscribe(topic, QOS_GUARANTEE);
        }
    }

    private MqttConnectOptions getConnectionOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setKeepAliveInterval(KEEPALIVE_SECONDS);
        return options;
    }

    @Override
    public synchronized void connectionLost(Throwable throwable) {
        Log.i(TAG, "MQTT Connection Lost!");
        connected = false;
        sendBroadcast(new Intent(Broadcasts.MQTT_DISCONNECTED));
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        Log.i(TAG, "Message arrived; " + new String(mqttMessage.getPayload()));
        String message = new String(mqttMessage.getPayload());
        pushDispatcher.pushReceived(topic, message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    @Receiver(actions = ConnectivityManager.CONNECTIVITY_ACTION)
    void connectivityChanged(@Receiver.Extra(ConnectivityManager.EXTRA_NO_CONNECTIVITY) boolean noConnectivity) {
        // try to reconnect if we are not connected and there is at least some connectivity
        if (!noConnectivity && !connected) {
            connect();
        }
    }

    @Receiver(actions = {Broadcasts.REFRESH_TRIGGERED, Broadcasts.IN_SYNC})
    synchronized void refresh() {
        // on explicit refresh try to reconnect
        if (!connected) {
            connect();
        }
    }
}
