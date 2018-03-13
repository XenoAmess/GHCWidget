package com.xenoamess.partaker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;

import java.util.Calendar;

import com.xenoamess.partaker.data.ColorTheme;
import com.xenoamess.partaker.data.CommitsBase;
import com.xenoamess.partaker.data.ModuleManager;
import com.xenoamess.partaker.modules.github.ModuleDataCenter;

public class Widget extends AppWidgetProvider {
    public static final int MAX_MONTHS = 100;

    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_NOTFOUND = 1;
    public static final int STATUS_ONLINE = 2;

    private com.xenoamess.partaker.modules.ModuleDataCenter moduleDataCenter;
    private String moduleName;

    private static final String TAG = "Partaker";
    private RemoteViews remoteViews;
    private CommitsBase base;
    private int status = STATUS_ONLINE;
    private int[] appWidgetIds;

    //    private boolean resized = false;
    private boolean online;
    private Context context;
    public static final String LOAD_DATA_KEY = "load_data";

    //Parameters
    private String username;
    private int months;
    private String theme;
    private boolean startOnMonday;

    private boolean showDaysLabel;

    //below added by XenoAmess
    private int weeksColumns;

    private int weeksRows;

    public static final float SPACE_RATIO = 0.1f;
    public static final int TEXT_GRAPH_SPACE = 7;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (this.appWidgetIds == null)
            this.appWidgetIds = appWidgetIds;
        updateWidget(context);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action != null) {
            if (action.equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE) ||
                    action.equals(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {

                online = intent.getBooleanExtra(LOAD_DATA_KEY, true); //Set the flag of online/caching mode
                AppWidgetManager appWM = AppWidgetManager.getInstance(context);
                if (this.appWidgetIds == null)
                    this.appWidgetIds = appWM.getAppWidgetIds(intent.getComponent());

                updateWidget(context);
            }

            super.onReceive(context, intent);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

//        resized = true;
        printMessage("onAppWidgetOptionsChanged0");
        updateWidget(context);
        setClickIntent(appWidgetId);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        printMessage("onAppWidgetOptionsChanged1");
    }

    /**
     * Determine appropriate view based on width provided.
     *
     * @param minWidth
     * @param minHeight
     * @return
     */
    private RemoteViews getRemoteViews(int minWidth,
                                       int minHeight) {
        // First find out rows and columns based on width provided.
        int rows = getCellsForSize(minHeight);
        int columns = getCellsForSize(minWidth);

//        if (resized) {
//            adjustMonthsNum(context, columns, rows);
//            resized = false;
//        }

        if (rows == 1)
            return new RemoteViews(context.getPackageName(), R.layout.one_row);
        if (columns > 2) {
            return new RemoteViews(context.getPackageName(), R.layout.main);
        } else {
            return new RemoteViews(context.getPackageName(), R.layout.small);
        }
    }

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }

    private void updateWidget(Context context) {
        printMessage("updateWidget0");
        if (this.context == null)
            this.context = context;

        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = mgr.getAppWidgetIds(new ComponentName(context, Widget.class));
        // See the dimensions and
        Bundle options = mgr.getAppWidgetOptions(appWidgetIds[0]);

        // Get min width and height.
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        // Obtain appropriate widget and update it.
        remoteViews = getRemoteViews(minWidth, minHeight);

        setPreferences();
        Bitmap bitmap = processImage();
        if (bitmap != null)
            remoteViews.setImageViewBitmap(R.id.commitsView, bitmap);

        switch (status) {
            case STATUS_OFFLINE:
                printMessage(context.getResources().getString(R.string.loading_error));
                break;
            case STATUS_NOTFOUND:
                printMessage(context.getResources().getString(R.string.not_found));
                break;
        }

        if (appWidgetIds != null) {
            for (int appWidgetId : appWidgetIds) {
                setClickIntent(appWidgetId);
            }
        }
        printMessage("updateWidget1");
    }


    private void setPreferences() {
        printMessage("setPreferences0");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        username = prefs.getString("username", "XenoAmess");
//        try {
//            months = Integer.parseInt(prefs.getString("months", "5"));
//            if (months < 1)
//                months = 1;
//            if (months > MAX_MONTHS)
//                months = MAX_MONTHS;
//        } catch (Exception e) {
//            months = 5;
//        }
        theme = prefs.getString("color_theme", null);
        if (theme == null) {
            prefs.edit().putString("color_theme", ColorTheme.GITHUB).commit();
            theme = ColorTheme.GITHUB;
        }

        moduleName = prefs.getString("module_name", null);
        if (moduleName == null) {
            prefs.edit().putString("module_name", ModuleManager.GITHUB).commit();
            moduleName = ModuleManager.GITHUB;
        }
//        Log.d(TAG, "IANHERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR!    " + moduleName + ".ModuleDataCenter");

        moduleDataCenter = ModuleManager.GetModuleDataCenter(moduleName);
//            moduleDataCenter = (com.xenoamess.partaker.modules.ModuleDataCenter) moduleClass.newInstance();




//        startOnMonday = prefs.getBoolean("start_on_monday", false);
//        showDaysLabel = prefs.getBoolean("days_labels", true);

        try {
            String tmps = prefs.getString("weeks_columns", null);
            if (tmps == null) {
                prefs.edit().putString("weeks_columns", "30").commit();
                tmps = "30";
            }
            weeksColumns = Integer.parseInt(tmps);
            if (weeksColumns < 1)
                weeksColumns = 1;
        } catch (Exception e) {
            weeksColumns = 20;
        }
//        try {
//            weeksRows = Integer.parseInt(prefs.getString("weeks_rows", "1"));
//            if (weeksRows < 1)
//                weeksRows = 1;
//        } catch (Exception e) {
//            weeksRows = 3;
//        }

        Log.d(TAG, "Preferences updated: " + username + " " + theme);
        printMessage("setPreferences1");
    }

    //On click open the preferences activity
    private void setClickIntent(int appWidgetId) {
        printMessage("setClickIntent0");
        Intent launchActivity = new Intent(context, WidgetPreferenceActivity.class);
        launchActivity.setAction("android.appwidget.action.APPWIDGET_CONFIGURE");
        launchActivity.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchActivity, 0);

        remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        ComponentName thisWidget = new ComponentName(context, Widget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, remoteViews);
        printMessage("setClickIntent1");
    }

    // Load data and generate a bitmap with commits.
    private Bitmap processImage() {
        printMessage("processImage0");
        return moduleDataCenter.processImage(this);
//        printMessage("processImage1");
    }

    public Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public int getRowSize(Context context) {
        Point size = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
        float daysLabelSpaceRatio = showDaysLabel ? 0.8f : 0;
        float side = size.x / (weeksColumns + daysLabelSpaceRatio) * (1 - SPACE_RATIO);
        float space = size.x / (weeksColumns + daysLabelSpaceRatio) - side;
        float textSize = side * 0.87f;
        int heightPerRow = (int) (7 * (side + space) + textSize + TEXT_GRAPH_SPACE);
        return size.y / heightPerRow;
    }

    public int getDaySize(Context context) {
        return weeksColumns * getRowSize(context) * 7;
    }


//    private void adjustMonthsNum(Context context, int numColumns, int numRows) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = prefs.edit();
//        if (numRows > 1) {
//            switch (numColumns) {
//                case 2:
//                    editor.putString("months", "2");
//                    break;
//                case 3:
//                    editor.putString("months", "4");
//                    break;
//                case 4:
//                    editor.putString("months", "5");
//                    break;
//                case 5:
//                    editor.putString("months", "5");
//                    break;
//                case 6:
//                    editor.putString("months", "7");
//                    break;
//                case 8:
//                    editor.putString("months", "9");
//                    break;
//                default:
//                    editor.putString("months", "12");
//            }
//        } else {
//            switch (numColumns) {
//                case 2:
//                    editor.putString("months", "2");
//                    break;
//                case 3:
//                    editor.putString("months", "5");
//                    break;
//                case 4:
//                    editor.putString("months", "6");
//                    break;
//                case 5:
//                    editor.putString("months", "7");
//                    break;
//                case 6:
//                    editor.putString("months", "11");
//                    break;
//                default:
//                    editor.putString("months", "12");
//            }
//        }
//        editor.commit();
//    }

    public void printMessage(String msg) {
        printMessage("printMessage0");
        remoteViews.setTextViewText(R.id.total, "");
        remoteViews.setTextViewText(R.id.totalTextView, "");
        remoteViews.setTextViewText(R.id.days, "");
        remoteViews.setTextViewText(R.id.daysTextView, msg);
        printMessage("processImage1");
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean getOnline() {
        return online;
    }

    public CommitsBase getBase() {
        return base;
    }

    public boolean isOnline() {
        return online;
    }

    public RemoteViews getRemoteViews() {
        return remoteViews;
    }

    public String getUsername() {
        return username;
    }

    public Context getContext() {
        return context;
    }

    public boolean isShowDaysLabel() {
        return showDaysLabel;
    }

    public int getWeeksColumns() {
        return weeksColumns;
    }

    public void setWeeksRows(int weeksRows) {
        this.weeksRows = weeksRows;
    }

    public int getWeeksRows() {
        return weeksRows;
    }

    public String getTheme() {
        return theme;
    }


}
