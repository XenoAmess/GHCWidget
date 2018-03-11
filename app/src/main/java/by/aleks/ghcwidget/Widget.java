package by.aleks.ghcwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import by.aleks.ghcwidget.api.GitHubAPITask;
import by.aleks.ghcwidget.data.ColorTheme;
import by.aleks.ghcwidget.data.CommitsBase;
import by.aleks.ghcwidget.data.Day;

public class Widget extends AppWidgetProvider {
    public static final int MAX_MONTHS = 100;

    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_NOTFOUND = 1;
    public static final int STATUS_ONLINE = 2;

    private static final String TAG = "GHCWidget";
    private RemoteViews remoteViews;
    private CommitsBase base;
    private int status = STATUS_ONLINE;
    private int[] appWidgetIds;
    private boolean resized = false;
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

    public static int NOW_YEAR;

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

        resized = true;
        updateWidget(context);
        setClickIntent(context, appWidgetId);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * Determine appropriate view based on width provided.
     *
     * @param minWidth
     * @param minHeight
     * @return
     */
    private RemoteViews getRemoteViews(Context context, int minWidth,
                                       int minHeight) {
        // First find out rows and columns based on width provided.
        int rows = getCellsForSize(minHeight);
        int columns = getCellsForSize(minWidth);
        if (resized) {
            adjustMonthsNum(context, columns, rows);
            resized = false;
        }
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
        remoteViews = getRemoteViews(context, minWidth, minHeight);

        setPreferences(context);
        Bitmap bitmap = processImage(context);
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
                setClickIntent(context, appWidgetId);
            }
        }
    }


    private void setPreferences(Context context) {
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
        theme = prefs.getString("color_theme", ColorTheme.GITHUB);
//        startOnMonday = prefs.getBoolean("start_on_monday", false);
//        showDaysLabel = prefs.getBoolean("days_labels", true);

        try {
            weeksColumns = Integer.parseInt(prefs.getString("weeks_columns", "30"));
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
    }

    //On click open the preferences activity
    private void setClickIntent(Context context, int appWidgetId) {

        Intent launchActivity = new Intent(context, WidgetPreferenceActivity.class);
        launchActivity.setAction("android.appwidget.action.APPWIDGET_CONFIGURE");
        launchActivity.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchActivity, 0);

        remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        ComponentName thisWidget = new ComponentName(context, Widget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(thisWidget, remoteViews);

    }


//    public static Bitmap getBitmap(String path) {
//        Bitmap bitmap = null;
//        try {
//            URL url = new URL(path);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setConnectTimeout(5000);
//            conn.setRequestMethod("GET");
//            if (conn.getResponseCode() == 200) {
//                InputStream inputStream = conn.getInputStream();
//                bitmap = BitmapFactory.decodeStream(inputStream);
//                return bitmap;
//            }
//            return null;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return bitmap;
//    }

    private void updateInfoBar(CommitsBase base) {
//        remoteViews.setImageViewBitmap(R.id.octocatView, getBitmap("https://avatars.githubusercontent.com/" + username));
        remoteViews.setTextViewText(R.id.total, String.valueOf(base.commitsNumber()));
        remoteViews.setTextViewText(R.id.totalTextView, context.getString(R.string.total));
        int streak = base.currentStreak();
        remoteViews.setTextViewText(R.id.days, String.valueOf(streak));
        if (streak == 1) {
            remoteViews.setTextViewText(R.id.daysTextView, context.getString(R.string.day));
        } else remoteViews.setTextViewText(R.id.daysTextView, context.getString(R.string.days));
    }

    // Load data from GitHub and generate a bitmap with commits.
    private Bitmap processImage(Context context) {

        if (base == null || online) {
            CommitsBase refreshedBase = loadData(context, username);
            if (refreshedBase != null) {
                base = refreshedBase;
                updateInfoBar(base);
            } else return null;
        }

        Point size = getScreenSize(context);
        //int weeks = 4 * months + 1;
        return createBitmap(base, size, theme);
    }


    //Load data from the api using AsyncTask.
    private CommitsBase loadData(Context context, String username) {
        int requiredDaySize = getDaySize(context);
        int nowDaySize = 0;

        GitHubAPITask task = null;
        try {
            status = STATUS_ONLINE;
            ArrayList<String> dataStrings = new ArrayList<String>();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
            Date curDate = new Date(System.currentTimeMillis());
            NOW_YEAR = Integer.parseInt(formatter.format(curDate));
            int year = NOW_YEAR;

            final String prefDataKeyHead = "offline_data";

            while (requiredDaySize >= 0) {
                task = new GitHubAPITask(this, context, year);
                String prefDataKey = prefDataKeyHead + "_" + year;
                String data;

                // If the widget have to be updated online, load data and save it to SharedPreferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                if (online || !prefs.contains(prefDataKey)) {
                    data = task.execute(username).get();
                    if (data != null) {
                        editor.putString(prefDataKey, data);
                        editor.commit();
                    }
                } else data = prefs.getString(prefDataKey, null);
                dataStrings.add(data);

                if (year != NOW_YEAR)
                    requiredDaySize -= (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) ? 366 : 365;
                year--;
            }
            return GitHubAPITask.parseResult(dataStrings);
        } catch (Exception e) {
            if (task != null)
                task.cancel(true);
            return null;
        }
    }

    private Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private int getRowSize(Context context) {
        Point size = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
        float daysLabelSpaceRatio = showDaysLabel ? 0.8f : 0;
        float side = size.x / (weeksColumns + daysLabelSpaceRatio) * (1 - SPACE_RATIO);
        float space = size.x / (weeksColumns + daysLabelSpaceRatio) - side;
        float textSize = side * 0.87f;
        int heightPerRow = (int) (7 * (side + space) + textSize + TEXT_GRAPH_SPACE);
        return size.y / heightPerRow;
    }

    private int getDaySize(Context context) {
        return weeksColumns * getRowSize(context) * 7;
    }


    private Bitmap createBitmap(CommitsBase base, Point size, String theme) {

        float daysLabelSpaceRatio = showDaysLabel ? 0.8f : 0;


        float side = size.x / (weeksColumns + daysLabelSpaceRatio) * (1 - SPACE_RATIO);
        float space = size.x / (weeksColumns + daysLabelSpaceRatio) - side;
        float textSize = side * 0.87f;

        int heightPerRow = (int) (7 * (side + space) + textSize + TEXT_GRAPH_SPACE);

        weeksRows = getRowSize(context);
        int height = heightPerRow * weeksRows;

        ColorTheme colorTheme = new ColorTheme();


        Bitmap bitmap = Bitmap.createBitmap(size.x, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paintText.setTextSize(textSize);
        paintText.setColor(Color.GRAY);


        if (base != null) {

            float initx = 0;
            float inity = 0;
            float x = 0, y;

            // Draw days labels.
            if (showDaysLabel) {
//                y = startOnMonday ? textSize * 2 + TEXT_GRAPH_SPACE : textSize * 2 + TEXT_GRAPH_SPACE + side;
                y = textSize * 2 + TEXT_GRAPH_SPACE + side;
                initx = x = textSize;
            }

            int daysNum = Math.min(base.getDays().size(), weeksColumns * weeksRows * 7);

            y = textSize + TEXT_GRAPH_SPACE;

//            int tmpi = 0;
//            if (startOnMonday) tmpi = 1;
//            int startPos = base.findStartPos(base.getDays().size() - daysNum, tmpi);
            int startPos = base.getDays().size() - daysNum;

            outerloop:
            for (int i = startPos; i < base.getDays().size(); ) {
                x = initx;
                inity = y;

                if (showDaysLabel) {
                    y += 2 * side + space;
//                    if (startOnMonday) {
//                        y -= side + space;
//                    }
                    canvas.drawText(context.getString(R.string.m), 0, y, paintText);
                    canvas.drawText(context.getString(R.string.w), 0, y + 2 * (side + space), paintText);
                    canvas.drawText(context.getString(R.string.f), textSize * 0.1f, y + 4 * (side + space), paintText);
//                    if (startOnMonday)
//                        canvas.drawText(context.getString(R.string.s), textSize * 0.1f, y + 6 * (side + space), paintText);
                    initx = x = textSize;
//                    if (startOnMonday) {
//                        y += side + space;
//                    }
                    y -= 2 * side + space;
                }

                for (int j = i; i < j + weeksColumns * 7; ) {
                    if (i > base.getDays().size()) {
                        break outerloop;
                    }
                    // Set the position and draw a month name.


                    for (int k = i; i < k + 7; i++) {

                        if (i > base.getDays().size()) {
                            break outerloop;
                        }

                        if (base.getDays().get(i).getCalendar().get(Calendar.DAY_OF_MONTH) == 1) {
                            String drawTest;
                            if (base.getDays().get(i).getCalendar().get(Calendar.MONTH) == 0) {
                                drawTest = "" + base.getDays().get(i).getCalendar().get(Calendar.YEAR);
                            } else {
                                drawTest = base.getDays().get(i).getMonthName();
                            }
                            canvas.drawText(drawTest, x, inity - (textSize + TEXT_GRAPH_SPACE) + textSize, paintText);
                        }

                        paint.setColor(colorTheme.getColor(theme, base.getDays().get(i).getLevel()));
                        canvas.drawRect(x, y, x + side, y + side, paint);

                        //

//                        canvas.drawText("" + base.getDays().get(i).getDayOfWeek(), x, y, paintText);

                        //

                        y = y + side + space;
                    }

                    y = inity;
                    x = x + side + space;
                }
                y += heightPerRow;
            }
        }

        return bitmap;

        //return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    }

    private void adjustMonthsNum(Context context, int numColumns, int numRows) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if (numRows > 1) {
            switch (numColumns) {
                case 2:
                    editor.putString("months", "2");
                    break;
                case 3:
                    editor.putString("months", "4");
                    break;
                case 4:
                    editor.putString("months", "5");
                    break;
                case 5:
                    editor.putString("months", "5");
                    break;
                case 6:
                    editor.putString("months", "7");
                    break;
                case 8:
                    editor.putString("months", "9");
                    break;
                default:
                    editor.putString("months", "12");
            }
        } else {
            switch (numColumns) {
                case 2:
                    editor.putString("months", "2");
                    break;
                case 3:
                    editor.putString("months", "5");
                    break;
                case 4:
                    editor.putString("months", "6");
                    break;
                case 5:
                    editor.putString("months", "7");
                    break;
                case 6:
                    editor.putString("months", "11");
                    break;
                default:
                    editor.putString("months", "12");
            }
        }
        editor.commit();
    }

    private void printMessage(String msg) {
        remoteViews.setTextViewText(R.id.total, "");
        remoteViews.setTextViewText(R.id.totalTextView, "");
        remoteViews.setTextViewText(R.id.days, "");
        remoteViews.setTextViewText(R.id.daysTextView, msg);
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
