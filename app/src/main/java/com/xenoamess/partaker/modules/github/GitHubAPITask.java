package com.xenoamess.partaker.modules.github;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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

public class GitHubAPITask extends AsyncTask<String, Integer, String> // Username to the input, Progress, Output
{

    private static final String debugTag = "GHCWiget";
    private Widget widget;
    private Context context;
    private static CommitsBase base = null;
    private int year;

    public GitHubAPITask(Widget widget, Context context, int year) {
        super();
        this.widget = widget;
        this.context = context;
        this.year = year;
    }


    // Call the downloading method in background and load data
    @Override
    protected String doInBackground(String... params) {
        String result = null;
        try {
            Log.d(debugTag, "Background:" + Thread.currentThread().getName());
            result = GitHubHelper.downloadFromServer(params[0], context, this.year);
        } catch (GitHubHelper.ApiException e) {
            Log.d(debugTag, "Loading failed");
            e.getMessage();
            widget.setStatus(Widget.STATUS_OFFLINE);
            return null;
        }
        if (result.equals("invalid_response")) {
            widget.setStatus(Widget.STATUS_NOTFOUND);
            return null;
        }

        return result;
    }

    public static CommitsBase parseResult(final ArrayList<String> dataStrings) throws ExecutionException, InterruptedException {

        AsyncTask<Void, Void, CommitsBase> task = new AsyncTask<Void, Void, CommitsBase>() {

            @Override
            protected CommitsBase doInBackground(Void... params) {


                GitHubCommitsBase base = new GitHubCommitsBase();

                try {
                    for (int i = dataStrings.size() - 1; i >= 0; i--) {
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        factory.setNamespaceAware(true);
                        XmlPullParser xpp = factory.newPullParser();

                        xpp.setInput(new StringReader(dataStrings.get(i)));
                        int eventType = xpp.getEventType();

                        boolean firstTagSkipped = false;
                        SimpleDateFormat textFormat = new SimpleDateFormat("yyyy-MM-dd");

                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            switch (eventType) {
                                case XmlPullParser.START_DOCUMENT: {
                                    break;
                                }
                                case XmlPullParser.START_TAG: {
                                    if (xpp.getName().equals("g")) {
                                        if (!firstTagSkipped) {
                                            firstTagSkipped = true;
                                            eventType = xpp.next();
                                            break;
                                        } else {
//                                            base.newWeek();
                                            eventType = xpp.next();
                                            break;
                                        }
                                    }

                                    if (xpp.getName().equals("rect")) {
                                        Date date = textFormat.parse(xpp.getAttributeValue(null, "data-date"));
                                        int commits = Integer.valueOf(xpp.getAttributeValue(null, "data-count"));
                                        String color = xpp.getAttributeValue(null, "fill");
                                        int dayOfWeek = Integer.valueOf(xpp.getAttributeValue(null, "y"));
                                        dayOfWeek /= 12;
                                        Day day = new Day(date, commits, color, dayOfWeek);
                                        base.addDay(day);
                                        eventType = xpp.next();
                                        break;
                                    }
                                }
                            }

                            eventType = xpp.next();
                        }
                    }
                } catch (Exception e) {
                    Log.d(debugTag, "Error in parsing");
                    e.printStackTrace();
                    return null;
                }
                return base;
            }
        };

        return task.execute().get();
    }


}
