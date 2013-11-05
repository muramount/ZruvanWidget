
package jp.blogspot.oretekimemo;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainWidgetProvider extends AppWidgetProvider {

    private static final String TAG = MainWidgetProvider.class.getName();

    private final long interval = 60 * 1000; // 更新間隔
    private static final String ACTION_START_MY_ALARM = "jp.blogspot.oretekimemo.ZruvanWidget.ACTION_START_MY_ALARM";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        /*
         * ホームボタンを押したら、receiveを呼び出す タスクキラーなどで、
         * アプリを強制終了された際、 Widgetも終了してしまうため。
         */
        context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
            @SuppressWarnings("unused")
            public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
                setAlarm(context);
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                MainWidgetProvider.this.onReceive(context, intent);
            }
        }, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        setAlarm(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_START_MY_ALARM)) {

            if (ACTION_START_MY_ALARM.equals(intent.getAction())) {

                updateClock(context);
                updateWifiInfo(context);
                updateGpsInfo(context);

                Intent serviceIntent = new Intent(context, MyService.class);
                context.startService(serviceIntent);
            }
            setAlarm(context);
        }
    }

    // 起動したServiceを停止する(1)
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Intent in = new Intent(context, MyService.class);
        context.stopService(in);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * アラームマネージャをで更新間隔を設定する
     */
    private void setAlarm(Context context) {
        Intent alarmIntent = new Intent(context, MainWidgetProvider.class);
        alarmIntent.setAction(ACTION_START_MY_ALARM);
        PendingIntent operation = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long now = System.currentTimeMillis() + 1; // + 1 は確実に未来時刻になるようにする保険
        long oneHourAfter = now + interval - now % (interval);
        am.set(AlarmManager.RTC, oneHourAfter, operation);
    }

    /**
     * Serviceの内容
     */
    public static class MyService extends Service {
        @Override
        public void onCreate() {
            super.onCreate();
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onStart(Intent intent, int startId) {
            super.onStart(intent, startId);

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(mBroadcastReceiver, filter);
        }

        @Override
        public void onDestroy() {
            unregisterReceiver(mBroadcastReceiver);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

    }

    /**
     * Receiverの内容
     */
    private static BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED) == false) {
                return;
            }

            ComponentName cn = new ComponentName(context, MainWidgetProvider.class);
            RemoteViews   rv = new RemoteViews(context.getPackageName(), R.layout.wedgit_main);

            int status = intent.getIntExtra("status", 0);
            int level  = intent.getIntExtra("level", 0);

            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    rv.setImageViewResource(R.id.batteryImage, R.drawable.battery_chaging);
                    break;
                default:
                    if (level >= 95) {
                        rv.setImageViewResource(R.id.batteryImage, R.drawable.battery_100);
                    } else if (level >= 80) {
                        rv.setImageViewResource(R.id.batteryImage, R.drawable.battery_80);
                    } else if (level >= 60) {
                        rv.setImageViewResource(R.id.batteryImage, R.drawable.battery_60);
                    } else if (level >= 40) {
                        rv.setImageViewResource(R.id.batteryImage, R.drawable.battery_40);
                    } else if (level >= 20) {
                        rv.setImageViewResource(R.id.batteryImage, R.drawable.battery_20);
                    } else {
                        rv.setImageViewResource(R.id.batteryImage, R.drawable.battery_0);
                    }
                    break;
            }

            rv.setTextViewText(R.id.batteryText, level + "%" );
            AppWidgetManager.getInstance(context).updateAppWidget(cn, rv);
        }

    };

    /**
     * 日時表示の更新
     * @param context
     */
    @SuppressLint("SimpleDateFormat")
    private void updateClock(Context context){
        ComponentName cn = new ComponentName(context, MainWidgetProvider.class);
        RemoteViews   rv = new RemoteViews(context.getPackageName(), R.layout.wedgit_main);

        SimpleDateFormat sdf;

        // 現在の時刻を取得
        Date date = new Date();

        // 日付
        sdf = new SimpleDateFormat("yyyy/M/d（E）");
        rv.setTextViewText(R.id.dateText, sdf.format(date));

        // 時間
        sdf = new SimpleDateFormat("kk:mm");
        rv.setTextViewText(R.id.timeText, sdf.format(date));

        AppWidgetManager.getInstance(context).updateAppWidget(cn, rv);

        return;
    }

    /**
     * Wi-Fi表示の更新
     * @param context
     */
    private void updateWifiInfo(Context context) {
        ComponentName cn = new ComponentName(context, MainWidgetProvider.class);
        RemoteViews   rv = new RemoteViews(context.getPackageName(), R.layout.wedgit_main);

        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);

        StringBuffer wifiStr = new StringBuffer();
        if(wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID().replaceAll("\"", "");
            wifiStr.append(ssid);
        } else {
            wifiStr.append("OFF");
        }

        rv.setTextViewText(R.id.wifiText, wifiStr.toString());

        AppWidgetManager.getInstance(context).updateAppWidget(cn, rv);

        return;
    }

    /**
     * GPS表示の更新
     * @param context
     */
    private void updateGpsInfo(Context context) {
        ComponentName cn = new ComponentName(context, MainWidgetProvider.class);
        RemoteViews   rv = new RemoteViews(context.getPackageName(), R.layout.wedgit_main);

        LocationManager  locationManager = (LocationManager)context.getSystemService(context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        String bestProvider = locationManager.getBestProvider(criteria, true);

        rv.setTextViewText(R.id.gpsText, bestProvider);

        AppWidgetManager.getInstance(context).updateAppWidget(cn, rv);

        return;
    }

}
