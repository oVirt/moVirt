package org.ovirt.mobile.movirt.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.InboxStyle;
import android.util.Log;
import android.util.Pair;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.data.MovirtAccount;
import org.ovirt.mobile.movirt.model.ConnectionInfo;
import org.ovirt.mobile.movirt.model.base.BaseEntity;
import org.ovirt.mobile.movirt.model.trigger.Trigger;

import java.util.List;

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
    @Bean
    AccountRxStore rxStore;

    private int notificationCount = 0;
    private static final int maxDisplayedNotifications = 7; // InboxStyle allows 7
    private static final int vibrationDuration = 1000;

    private <E extends BaseEntity<?>> void showTriggerNotification(
            Trigger<E> trigger, E entity, Context context, PendingIntent resultPendingIntent
    ) {
        String title = trigger.getNotificationType() == Trigger.NotificationType.CRITICAL ? ">>> oVirt event <<<" : "oVirt event";
        Notification notification = prepareNotification(context, resultPendingIntent, System.currentTimeMillis(), title)
                .setContentText(trigger.getCondition().getMessage(context, entity))
                .build();
        notificationManager.notify(notificationCount++, notification);
        if (trigger.getNotificationType() == Trigger.NotificationType.CRITICAL) {
            vibrator.vibrate(vibrationDuration);
        }
    }

    public <E extends BaseEntity<?>> void showTriggersNotification(
            List<Pair<E, Trigger<E>>> entitiesAndTriggers, Context context, PendingIntent resultPendingIntent
    ) {
        Log.d(TAG, "Displaying notification " + notificationCount);
        if (entitiesAndTriggers.size() == 1) { // one entity displays in full format
            Pair<E, Trigger<E>> entityAndTrigger = entitiesAndTriggers.get(0);
            showTriggerNotification(entityAndTrigger.second, entityAndTrigger.first, context, resultPendingIntent);
            return;
        }

        boolean critical = false;
        InboxStyle style = new NotificationCompat.InboxStyle();

        for (int i = 0; i < entitiesAndTriggers.size(); i++) {
            Pair<E, Trigger<E>> pair = entitiesAndTriggers.get(i);

            if (!critical && pair.second.getNotificationType() == Trigger.NotificationType.CRITICAL) {
                critical = true;
            }

            if (i < maxDisplayedNotifications) {
                style.addLine(pair.second.getCondition().getMessage(context, pair.first));
            }
        }

        if (entitiesAndTriggers.size() > maxDisplayedNotifications) {
            style.addLine("."); // dummy line to show dots
            style.setSummaryText("+ " + (entitiesAndTriggers.size() - maxDisplayedNotifications) + " more");
        }

        Notification notification = prepareNotification(context, resultPendingIntent, System.currentTimeMillis(), critical ? ">>> oVirt event <<<" : "oVirt event")
                .setStyle(style)
                .build();
        notificationManager.notify(notificationCount++, notification);
        if (critical) {
            vibrator.vibrate(vibrationDuration);
        }
    }

    public void showConnectionNotification(Context context,
                                           PendingIntent resultPendingIntent,
                                           ConnectionInfo connectionInfo) {
        MovirtAccount account = rxStore.getAllAccountsWrapped().getAccountById(connectionInfo.getAccountId());
        String location = account == null ? "" : " to " + account.getName();

        Log.d(TAG, "Displaying notification " + notificationCount);
        String shortMsg = "Check your settings/server";
        String bigMsg = shortMsg + "\nLast successful connection at: " +
                connectionInfo.getLastSuccessfulWithTimeZone(context);

        Notification notification = prepareNotification(context, resultPendingIntent, connectionInfo.getLastAttempt(), "Connection lost" + location + "!")
                .setContentText(shortMsg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigMsg))
                .build();
        notificationManager.notify(notificationCount++, notification);
        vibrator.vibrate(vibrationDuration);
    }

    private Builder prepareNotification(Context context, PendingIntent resultPendingIntent, long when, String title) {
        return new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(when)
                .setSmallIcon(org.ovirt.mobile.movirt.R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent);
    }
}
