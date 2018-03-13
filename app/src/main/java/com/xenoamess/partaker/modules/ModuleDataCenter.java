package com.xenoamess.partaker.modules;

import android.graphics.Bitmap;

import com.xenoamess.partaker.Widget;
import com.xenoamess.partaker.api.APITask;

/**
 * Created by sfeq on 2018/3/13.
 */

public abstract class ModuleDataCenter {
    //    public abstract APITask buildAPITask();
    public abstract Bitmap processImage(Widget widget);
}
