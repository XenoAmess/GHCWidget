package com.xenoamess.partaker.modules;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;

import com.xenoamess.partaker.R;
import com.xenoamess.partaker.Widget;
import com.xenoamess.partaker.api.APITask;

import java.io.InputStream;

/**
 * Created by sfeq on 2018/3/13.
 */

public abstract class ModuleDataCenter {
    //    public abstract APITask buildAPITask();
    public abstract Bitmap processImage(Widget widget);


}
