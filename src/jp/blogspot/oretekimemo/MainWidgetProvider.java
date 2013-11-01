
package jp.blogspot.oretekimemo;

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
import android.os.BatteryManager;
import android.os.IBinder;
import android.widget.RemoteViews;

public class MainWidgetProvider extends AppWidgetProvider {

    private static final String TAG = MainWidgetProvider.class.getName();

    private final long interval = 60 * 1 * 1000; // 更新間隔
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

}
