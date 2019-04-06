package com.laregrage.testservice.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.laregrage.testservice.helper.Constants;
import com.laregrage.testservice.item.NotifItem;
import com.laregrage.testservice.util.TestNotification;

import java.util.Calendar;

public class MainService extends Service {
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;
    private boolean mRedelivery;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent)msg.obj);
            stopSelf(msg.arg1);
        }
    }

    public MainService() {
        super();
        mName = "MainService";
    }

    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return mRedelivery ? START_REDELIVER_INTENT : START_STICKY;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onHandleIntent(Intent intent){
        String action = intent.getAction();

        if (action == null) action = "";

        if (!action.isEmpty()){
            Bundle args = intent.getExtras();
            assert args != null;
            NotifItem notifItem = new NotifItem();

            //notifItem =  args.getInt(Constants.Setting.NOTIF_ITEM);
            //2019-04-06 Change object NotifItem variable to int
            int notifId = args.getInt(Constants.Setting.NOTIF_ITEM);

            switch (action){
                case "com.htc.intent.action.QUICKBOOT_POWERON":
                case "android.intent.action.QUICKBOOT_POWERON":
                case "android.intent.action.BOOT_COMPLETED":
                case Constants.Action.CREATE_DAILY:
                    createDailyAlarm();
                    break;
                case Constants.Action.SHOW_NOTIFY:
                    //if (notifItem != null)
                    //2019-04-06 Change notifItem variable to notifId
                    if (notifId > 0)
                    TestNotification.notify(this, notifId);
                    break;
                case Constants.Action.CLOSE_NOTIFY:
                    //if (notifItem != null)
                    //2019-04-06 Change notifItem variable to notifId
                    if (notifId > 0)
                    TestNotification.cancel(this, notifId);
                    break;
            }

            TestReceiver.completeWakefulIntent(intent);
        }
    }

    private void createDailyAlarm(){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TestReceiver.class);
        intent.setAction(Constants.Action.SHOW_NOTIFY);

        //2019-04-06 Remove object NotifItem
        /*
        NotifItem notifItem = new NotifItem();
        notifItem.setId(hour);
        notifItem.setTicker("Test Notification");
        notifItem.setTitle("Test Notification");
        notifItem.setMessage("Test Notification for " + String.valueOf(hour));
        */
        Bundle args = new Bundle();
        args.putInt(Constants.Setting.NOTIF_ITEM, 10002);

        intent.putExtra(Constants.Setting.NOTIF_ITEM, 10002);
        intent.putExtras(args);

        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                60 * 1000, alarmIntent);

        ComponentName receiver = new ComponentName(this, TestReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}
