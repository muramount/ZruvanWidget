package jp.blogspot.oretekimemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RemoteViews;
import android.widget.Spinner;

public class WidgetConfigure extends Activity {

    private int mAppWidgetId;
    private AppWidgetManager mWidgetManager;
    private RemoteViews mRemoteViews;

    private static final int[] TEXT_VIEW_ID_LIST = {
        R.id.dateText,
        R.id.timeText,
        R.id.batteryText,
        R.id.wifiLabel,
        R.id.wifiText,
        R.id.gpsLabel,
        R.id.gpsText,
        R.id.versionLabel,
        R.id.versionText
    };

    private Spinner mColorSpinner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_configure);

        setResult(RESULT_CANCELED);

        mWidgetManager = AppWidgetManager.getInstance(this);
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.wedgit_layout_4x2);
        mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mColorSpinner = (Spinner) findViewById(R.id.colorSpinner);

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        } else {
            Intent intent = new Intent(this, WidgetConfigure.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, mAppWidgetId, intent, 0);
            mRemoteViews.setOnClickPendingIntent(R.id.parentLayout, pendingIntent);
            mWidgetManager.updateAppWidget(mAppWidgetId, mRemoteViews);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClickOk(View view) {
        setResult(RESULT_CANCELED);

        mWidgetManager.updateAppWidget(mAppWidgetId, getChangeStyleView());

        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, intent);

        finish();
    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    private RemoteViews getChangeStyleView() {
        int position = mColorSpinner.getSelectedItemPosition();

        for (int id : TEXT_VIEW_ID_LIST) {
            mRemoteViews.setTextColor(id, getColorFromPostion(position));
        }

        mRemoteViews.setInt(R.id.borderText, "setBackgroundColor", getColorFromPostion(position));

        mRemoteViews.setInt(R.id.batteryImage, "setColorFilter", getColorFromPostion(position));

        return mRemoteViews;
    }

    private int getColorFromPostion(int position) {
        int id = 0;
        switch (position) {
            case 0: id = R.color.white; break;
            case 1: id = R.color.black; break;
            case 2: id = R.color.red; break;
            case 3: id = R.color.green; break;
            case 4: id = R.color.blue; break;
            case 5: id = R.color.yellow; break;
            case 6: id = R.color.purple; break;
            case 7: id = R.color.brown; break;
            case 8: id = R.color.pink; break;
            case 9: id = R.color.orange; break;
            case 10: id = R.color.gray; break;
        }
        return getResources().getColor(id);
    }
}
