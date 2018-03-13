package com.xenoamess.partaker.modules;

import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.xenoamess.partaker.R;
import com.xenoamess.partaker.Widget;

/**
 * Created by sfeq on 2018/3/13.
 */

public abstract class ModuleDataCenter {
    //    public abstract APITask buildAPITask();
    public abstract Bitmap processImage(Widget widget);

    public void printMessage(Widget widgetm, String msg) {
        RemoteViews remoteViews = widgetm.getRemoteViews();
        remoteViews.setTextViewText(R.id.total, "");
        remoteViews.setTextViewText(R.id.totalTextView, "");
        remoteViews.setTextViewText(R.id.days, "");
        remoteViews.setTextViewText(R.id.daysTextView, msg);
    }

}
