package com.laregrage.testservice.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.laregrage.testservice.R;
import com.laregrage.testservice.helper.Constants;
import com.laregrage.testservice.item.NotifItem;
import com.laregrage.testservice.service.TestReceiver;

public class TestNotification {
    public static void notify(final Context context, int notifId) {
        final Resources res = context.getResources();

        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.example_picture);

        NotifItem notifItem = new NotifItem();
        notifItem.setId(notifId);
        notifItem.setTicker("Test Notification");
        notifItem.setTitle("Test Notification");
        notifItem.setMessage("Test Notification");

        final String ticker = notifItem.getTicker();
        final String title = notifItem.getTitle();
        final String text = notifItem.getMessage();

        Intent intentReceiver = new Intent(context, TestReceiver.class);
        intentReceiver.putExtra(Constants.Setting.NOTIF_ITEM, notifId);
        intentReceiver.setAction(Constants.Action.CLOSE_NOTIFY);

        PendingIntent closeNotifyIntent = PendingIntent.getBroadcast(context,
                notifId, intentReceiver, PendingIntent.FLAG_UPDATE_CURRENT);

        long[] pattern = null;
        NotificationManager notificationManager = createNotificationChannel(context);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.Default.APP)
                .setDefaults(NotificationCompat.FLAG_AUTO_CANCEL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_stat_test)
                .setContentTitle(title)
                .setContentText(text)
                .setLargeIcon(picture)
                .setTicker(ticker)
                .setVibrate(pattern)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close", closeNotifyIntent)
        ;

        notificationManager.notify(notifId, builder.build());
    }

    private static NotificationManager createNotificationChannel(Context context) {
        NotificationManager notificationManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager = context.getSystemService(NotificationManager.class);
        } else {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            String description = context.getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.Default.APP, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        return notificationManager;
    }

    public static void cancel(final Context context, int notifId) {
        final NotificationManager nm = createNotificationChannel(context);
        nm.cancel(notifId);
    }
}
