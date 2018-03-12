package com.xenoamess.partaker.modules.github;

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
import com.xenoamess.partaker.api.APITask;
import com.xenoamess.partaker.data.ColorTheme;
import com.xenoamess.partaker.data.CommitsBase;
import com.xenoamess.partaker.modules.ModuleDataCenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sfeq on 2018/3/13.
 */

public class GithubModuleDataCenter extends ModuleDataCenter {
    public static CommitsBase loadData(Widget widget, Context context, String username) {
        int requiredDaySize = widget.getDaySize(context);
        int nowDaySize = 0;

        GitHubAPITask task = null;
        try {
            widget.setStatus(Widget.STATUS_ONLINE);
            ArrayList<String> dataStrings = new ArrayList<String>();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
            Date curDate = new Date(System.currentTimeMillis());
            GitHubHelper.NOW_YEAR = Integer.parseInt(formatter.format(curDate));
            int year = GitHubHelper.NOW_YEAR;

            final String prefDataKeyHead = "offline_data";

            while (requiredDaySize >= 0) {
                task = new GitHubAPITask(widget, context, year);
                String prefDataKey = prefDataKeyHead + "_" + year;
                String data;

                // If the widget have to be updated online, load data and save it to SharedPreferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                if (widget.getOnline() || !prefs.contains(prefDataKey)) {
                    data = task.execute(username).get();
                    if (data != null) {
                        editor.putString(prefDataKey, data);
                        editor.commit();
                    }
                } else data = prefs.getString(prefDataKey, null);
                dataStrings.add(data);

                if (year != GitHubHelper.NOW_YEAR)
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

    public static Bitmap processImage(Widget widget) {
        Context context = widget.getContext();
        CommitsBase base = widget.getBase();
        if (base == null || widget.isOnline()) {
            CommitsBase refreshedBase = GithubModuleDataCenter.loadData(widget, context, widget.getUsername());
            if (refreshedBase != null) {
                base = refreshedBase;
                updateInfoBar(widget, base);
            } else return null;
        }

        Point size = widget.getScreenSize(context);
        //int weeks = 4 * months + 1;
        return widget.createBitmap(base, size);
    }


    public static void updateInfoBar(Widget widget, CommitsBase base) {
        RemoteViews remoteViews = widget.getRemoteViews();
        remoteViews.setTextViewText(R.id.total, String.valueOf(base.commitsNumber()));
        remoteViews.setTextViewText(R.id.totalTextView, widget.getContext().getString(R.string.total));
        int streak = base.currentStreak();
        remoteViews.setTextViewText(R.id.days, String.valueOf(streak));
        if (streak == 1) {
            remoteViews.setTextViewText(R.id.daysTextView, widget.getContext().getString(R.string.day));
        } else
            remoteViews.setTextViewText(R.id.daysTextView, widget.getContext().getString(R.string.days));
    }

}