package org.ovirt.mobile.movirt.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.ovirt.mobile.movirt.model.BaseEntity;
import org.ovirt.mobile.movirt.model.ConnectionInfo;
import org.ovirt.mobile.movirt.model.trigger.Trigger;

/**
 * Class to display notifications.
 * Created by Nika on 21.03.2015.
 */
@EBean(scope = EBean.Scope.Singleton)
public class NotificationHelper {
    private static final String TAG = NotificationHelper.class.getSimpleName();
    @SystemService
    NotificationManager notificationManager;
    @SystemService
    Vibrator vibrator;
    private int notificationCount = 0;

    public <E extends BaseEntity<?>> void showTriggerNotification(
            Trigger<E> trigger, E entity, Context context, PendingIntent resultPendingIntent
    ) {
        Log.d(TAG, "Displaying notification " + notificationCount);
        Notification notification = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(org.ovirt.mobile.movirt.R.drawable.ic_launcher)
                .setContentTitle(trigger.getNotificationType() == Trigger.NotificationType.INFO ? "oVirt event" : ">>> oVirt event <<<")
                .setContentText(trigger.getCondition().getMessage(entity))
                .setContentIntent(resultPendingIntent)
                .build();
        notificationManager.notify(notificationCount++, notification);
        if (trigger.getNotificationType() == Trigger.NotificationType.CRITICAL) {
            vibrator.vibrate(1000);
        }
    }

    public void showConnectionNotification(Context context, PendingIntent resultPendingIntent, ConnectionInfo connectionInfo) {
        Log.d(TAG, "Displaying notification " + notificationCount);
        String successTime = "unknown";
        if (connectionInfo.getLastSuccessful() != null) {
            successTime = connectionInfo.getLastSuccessful().toString();
        }
        Notification notification = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(connectionInfo.getLastAttempt().getTime())
                .setSmallIcon(org.ovirt.mobile.movirt.R.drawable.ic_launcher)
                .setContentTitle("Connection lost!")
                .setContentText("Last successful connection at: " + successTime)
                .setContentIntent(resultPendingIntent)
                .build();
        notificationManager.notify(notificationCount++, notification);
        vibrator.vibrate(1000);
    }
}
