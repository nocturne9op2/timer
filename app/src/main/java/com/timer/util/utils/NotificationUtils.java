package com.timer.util.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.support.v7.app.NotificationCompat;

import com.timer.util.R;

public class NotificationUtils {
    public static NotificationCompat.Builder buildBase(Context context, PendingIntent pendingIntent) {
        return (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(context.getString(R.string.timer_finished))
                .setAutoCancel(true)
                .setContentText(context.getString(R.string.timer_finished))
                .setSmallIcon(android.R.drawable.ic_notification_clear_all)
                .setContentIntent(pendingIntent);
    }

    public static void issue(Context context, NotificationCompat.Builder builder) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    private NotificationUtils() {
    }
}