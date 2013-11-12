
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
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WidgetProviderBase extends AppWidgetProvider {

    private static final String TAG = WidgetProviderBase.class.getName();

    private final long interval = 60 * 1000; // 更新間隔
    private static final String ACTION_START_MY_ALARM = "jp.blogspot.oretekimemo.ZruvanWidget.ACTION_START_MY_ALARM";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Debug.waitForDebugger();

        super.onUpdate(context, appWidgetManager, appWidgetIds);
        context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
            @SuppressWarnings("unused")
            public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
                setAlarm(context);
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                WidgetProviderBase.this.onReceive(context, intent);
            }
        }, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        setAlarm(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_START_MY_ALARM)) {

            if (ACTION_START_MY_ALARM.equals(intent.getAction())) {
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
     * アラームマネージャで更新間隔を設定する
     */
    private void setAlarm(Context context) {
        Intent alarmIntent = new Intent(context, WidgetProviderBase.class);
        alarmIntent.setAction(ACTION_START_MY_ALARM);
        PendingIntent operation = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long now = System.currentTimeMillis() + 1; // + 1 は確実に未来時刻になるようにする保険
        long oneHourAfter = now + interval - now % (interval);
        am.set(AlarmManager.RTC, oneHourAfter, operation);
    }

    /**
     * Service
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

            updateClock(getBaseContext());
            updateWifiInfo(getBaseContext());
            updateGpsInfo(getBaseContext());
            updateVersionInfo(getBaseContext());

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
     * Broadcast Receiver
     */
    private static BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED) == false) {
                return;
            }

            ComponentName cn = new ComponentName(context, WidgetProviderBase.class);
            RemoteViews   rv = new RemoteViews(context.getPackageName(), R.layout.wedgit_4x2);

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
    private static void updateClock(Context context){
        ComponentName cn = new ComponentName(context, WidgetProviderBase.class);
        RemoteViews   rv = new RemoteViews(context.getPackageName(), R.layout.wedgit_4x2);

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
    private static void updateWifiInfo(Context context) {
        ComponentName cn = new ComponentName(context, WidgetProviderBase.class);
        RemoteViews   rv = new RemoteViews(context.getPackageName(), R.layout.wedgit_4x2);

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
    private static void updateGpsInfo(Context context) {
        ComponentName cn = new ComponentName(context, WidgetProviderBase.class);
        RemoteViews   rv = new RemoteViews(context.getPackageName(), R.layout.wedgit_4x2);

        LocationManager  locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        String bestProvider = locationManager.getBestProvider(criteria, true);

        rv.setTextViewText(R.id.gpsText, bestProvider);

        AppWidgetManager.getInstance(context).updateAppWidget(cn, rv);

        return;
    }

    /**
     * Versions表示の更新
     * @param context
     */
    private static void updateVersionInfo(Context context) {
        ComponentName cn = new ComponentName(context, WidgetProviderBase.class);
        RemoteViews   rv = new RemoteViews(context.getPackageName(), R.layout.wedgit_4x2);

        rv.setTextViewText(R.id.versionText, context.getString(R.string.version_text, Build.VERSION.SDK_INT, Build.VERSION.RELEASE));

        AppWidgetManager.getInstance(context).updateAppWidget(cn, rv);

        return;
    }

}
