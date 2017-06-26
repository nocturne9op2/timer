package com.timer.util.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.support.v7.app.NotificationCompat;

import com.timer.util.R;

public class NotificationUtils {
    public static final int NOTIFICATION_ID = 0;

    public static NotificationCompat.Builder buildBase(Context context, PendingIntent pendingIntent) {
        return (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.timer_finished))
                .setContentText(context.getString(R.string.timer_finished))
                .setSmallIcon(android.R.drawable.ic_notification_clear_all)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
    }

    public static void issue(Context context, NotificationCompat.Builder builder) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void remove(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    private NotificationUtils() {
    }
}