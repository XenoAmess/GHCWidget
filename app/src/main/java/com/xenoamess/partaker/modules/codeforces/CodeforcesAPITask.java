package com.xenoamess.partaker.modules.codeforces;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.xenoamess.partaker.Widget;
import com.xenoamess.partaker.data.CommitsBase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class CodeforcesAPITask extends AsyncTask<String, Integer, String> // Username to the input, Progress, Output
{

    private static final String debugTag = "GHCWiget";
    private Widget widget;
    private Context context;
    private static CommitsBase base = null;

    public CodeforcesAPITask(Widget widget, Context context) {
        super();
        this.widget = widget;
        this.context = context;
    }


    // Call the downloading method in background and load data
    @Override
    protected String doInBackground(String... params) {
        String result = null;
        try {
            Log.d(debugTag, "Background:" + Thread.currentThread().getName());
            result = CodeforcesHelper.downloadFromServer(params[0], context);
        } catch (CodeforcesHelper.ApiException e) {
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

    public static CommitsBase parseResult(final String dataString) throws ExecutionException, InterruptedException {

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, CommitsBase> task = new AsyncTask<Void, Void, CommitsBase>() {

            @Override
            protected CodeforcesCommitsBase doInBackground(Void... params) {

                TreeMap<Integer, Integer> submitMap = new TreeMap<Integer, Integer>();
                TreeMap<Integer, Integer> acceptedMap = new TreeMap<Integer, Integer>();


                try {
                    JSONObject dataBody = new JSONObject(dataString);
                    if (dataBody == null) return null;
                    JSONArray resultArray = dataBody.getJSONArray("result");
                    if (resultArray == null) return null;
//                    String formats = "yyyyMMdd";
                    for (int i = resultArray.length() - 1; i > 0; i--) {
                        JSONObject thisCommit = resultArray.getJSONObject(i);
                        int timestamp = (int) (thisCommit.getLong("creationTimeSeconds") / 86400);
                        if (submitMap.containsKey(timestamp)) {
                            submitMap.put(timestamp, submitMap.get(timestamp) + 1);
                        } else {
                            submitMap.put(timestamp, 1);
                        }


                        if (thisCommit.getString("verdict").equals("OK")) {
                            if (acceptedMap.containsKey(timestamp)) {
                                acceptedMap.put(timestamp, acceptedMap.get(timestamp) + 1);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.d(debugTag, "Error in parsing");
                    e.printStackTrace();
                    return null;
                }
                return new CodeforcesCommitsBase(submitMap, acceptedMap);
            }
        };
        return task.execute().get();
    }


}
