package com.xenoamess.partaker.modules.codeforces;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.xenoamess.partaker.R;
import com.xenoamess.partaker.Widget;
import com.xenoamess.partaker.data.ColorTheme;
import com.xenoamess.partaker.data.CommitsBase;
import com.xenoamess.partaker.modules.github.GitHubAPITask;
import com.xenoamess.partaker.modules.github.GitHubCommitsBase;
import com.xenoamess.partaker.modules.github.GitHubHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;
import static com.xenoamess.partaker.Widget.SPACE_RATIO;
import static com.xenoamess.partaker.Widget.TEXT_GRAPH_SPACE;

/**
 * Created by sfeq on 2018/3/13.
 */

public class ModuleDataCenter extends com.xenoamess.partaker.modules.ModuleDataCenter {

    public Bitmap processImage(Widget widget) {
        Context context = widget.getContext();
        CommitsBase base = widget.getBase();
        if (base == null || !base.getClass().equals(CodeforcesCommitsBase.class) || widget.isOnline()) {
            CommitsBase refreshedBase = loadData(widget, context, widget.getUsername());
            if (refreshedBase != null) {
                base = refreshedBase;
                updateInfoBar(widget, base);
            } else return null;
        }

        Point size = widget.getScreenSize(context);
        //int weeks = 4 * months + 1;
        return createBitmap(widget, base, size);
    }

    public CommitsBase loadData(Widget widget, Context context, String username) {
        int requiredDaySize = widget.getDaySize(context);
        int nowDaySize = 0;

        CodeforcesAPITask task = null;
        try {
            widget.setStatus(Widget.STATUS_ONLINE);

            task = new CodeforcesAPITask(widget, context);
            String dataString;

            final String prefDataKey = "codeforces_cache";
//            context.openFileInput(prefDataKey);
            // If the widget have to be updated online, load data and save it to SharedPreferences
            if (widget.getOnline() || !new File(prefDataKey).exists()) {
                dataString = task.execute(username).get();
                if (dataString != null) {
                    FileOutputStream fos = context.openFileOutput(prefDataKey, MODE_PRIVATE);
                    fos.write(dataString.getBytes());
                    fos.close();
                }
            } else {
                FileInputStream fis = context.openFileInput(prefDataKey);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                dataString = new String(buffer);
                fis.close();
            }

            return CodeforcesAPITask.parseResult(dataString);
        } catch (Exception e) {
            if (task != null)
                task.cancel(true);
            return null;
        }
    }


    public void updateInfoBar(Widget widget, CommitsBase base) {
        RemoteViews remoteViews = widget.getRemoteViews();

        remoteViews.setTextViewText(R.id.total, String.valueOf(base.getNumber1()));
        remoteViews.setTextViewText(R.id.totalTextView, widget.getContext().getString(R.string.total));
        remoteViews.setTextViewText(R.id.days, String.valueOf(base.getNumber2()));
        remoteViews.setTextViewText(R.id.daysTextView, widget.getContext().getString(R.string.accepted));
    }


    public Bitmap createBitmap(Widget widget, CommitsBase base, Point size) {
//        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);

        int weeksRows;
        int weeksColumns = widget.getWeeksColumns();
        boolean showDaysLabel = widget.isShowDaysLabel();
        Context context = widget.getContext();
        float daysLabelSpaceRatio = showDaysLabel ? 0.8f : 0;


        float side = size.x / (weeksColumns + daysLabelSpaceRatio) * (1 - SPACE_RATIO);
        float space = size.x / (weeksColumns + daysLabelSpaceRatio) - side;
        float textSize = side * 0.87f;

        int heightPerRow = (int) (7 * (side + space) + textSize + TEXT_GRAPH_SPACE);

        weeksRows = widget.getRowSize(context);
        widget.setWeeksRows(weeksRows);


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

            int daysNum = Math.min(base.getDaysSize(), weeksColumns * weeksRows * 7);

            y = textSize + TEXT_GRAPH_SPACE;

//            int tmpi = 0;
//            if (startOnMonday) tmpi = 1;
//            int startPos = base.findStartPos(base.getDays().size() - daysNum, tmpi);
            int startPos = base.getDaysSize() - daysNum;

            outerloop:
            for (int i = startPos; i < base.getDaysSize(); ) {
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
                    if (i > base.getDaysSize()) {
                        break outerloop;
                    }
                    // Set the position and draw a month name.


                    for (int k = i; i < k + 7; i++) {

                        if (i > base.getDaysSize()) {
                            break outerloop;
                        }


                        if (base.ifFirstDayOfMonth(i)) {
                            String drawTest;
                            if (base.ifFirstMonthOfYear(i)) {
                                drawTest = base.getYearName(i);
                            } else {
                                drawTest = base.getMonthName(i);
                            }
                            canvas.drawText(drawTest, x, inity - (textSize + TEXT_GRAPH_SPACE) + textSize, paintText);
                        }

                        paint.setColor(colorTheme.getColor(widget.getTheme(), base.getLevel(i)));
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


}