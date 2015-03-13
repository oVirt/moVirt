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
import org.ovirt.mobile.movirt.model.trigger.Trigger;

/**
 * Class to display notifications provided by triggers
 * Created by Nika on 21.03.2015.
 */
@EBean(scope = EBean.Scope.Singleton)
public class NotificationDisplayer {
    private static final String TAG = NotificationDisplayer.class.getSimpleName();
    private int notificationCount = 0;

    @SystemService
    NotificationManager notificationManager;

    @SystemService
    Vibrator vibrator;

    public <E extends BaseEntity<?>> void showNotification(
            Trigger<E> trigger, E entity, Context context, PendingIntent resultPendingIntent
    ) {
        Log.d(TAG, "Displaying notification " + notificationCount);
        notificationManager.notify(notificationCount++, new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(org.ovirt.mobile.movirt.R.drawable.ic_launcher)
                .setContentTitle(trigger.getNotificationType() == Trigger.NotificationType.INFO ? "oVirt event" : ">>> oVirt event <<<")
                .setContentText(trigger.getCondition().getMessage(entity))
                .setContentIntent(resultPendingIntent)
                .build());
        if (trigger.getNotificationType() == Trigger.NotificationType.CRITICAL) {
            vibrator.vibrate(1000);
        }
    }
}
