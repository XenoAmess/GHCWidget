package com.xenoamess.partaker.api;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import com.xenoamess.partaker.Widget;
import com.xenoamess.partaker.data.CommitsBase;
import com.xenoamess.partaker.data.Day;
import com.xenoamess.partaker.modules.github.GitHubHelper;

public abstract class APITask extends AsyncTask<String, Integer, String> // Username to the input, Progress, Output
{

//    private static final String debugTag = "GHCWiget";
//    private Widget widget;
//    private Context context;
//    private static CommitsBase base = null;
//    private int year;

    public APITask() {
//        this.widget = widget;
//        this.context = context;
//        this.year = year;
    }


    // Call the downloading method in background and load data
    @Override
    protected abstract String doInBackground(String... params);

    public static CommitsBase parseResult(final ArrayList<String> dataStrings) throws ExecutionException, InterruptedException {
        return null;
    }
}
