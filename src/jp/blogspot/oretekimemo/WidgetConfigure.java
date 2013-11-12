package jp.blogspot.oretekimemo;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Spinner;

public class WidgetConfigure extends Activity {

    private int mAppWidgetId;

    private Spinner mColorSpinner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configure);

        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        mColorSpinner = (Spinner) findViewById(R.id.colorSpinner);

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

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.wedgit_4x2);
        appWidgetManager.updateAppWidget(mAppWidgetId, changeStyle(rv));

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);

        finish();
    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    private RemoteViews changeStyle(RemoteViews rv) {
        int position = mColorSpinner.getSelectedItemPosition();

        int[] textViewIds = {
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
        for (int id : textViewIds) {
            rv.setTextColor(id, getColorFromPostion(position));
        }

        rv.setInt(R.id.borderText, "setBackgroundColor", getColorFromPostion(position));

        rv.setInt(R.id.batteryImage, "setColorFilter", getColorFromPostion(position));

        return rv;
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
