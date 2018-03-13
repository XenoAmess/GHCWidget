package com.xenoamess.partaker.modules.codeforces;

import com.xenoamess.partaker.data.CommitsBase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TreeMap;

/**
 * Created by sfeq on 2018/3/13.
 */

public class CodeforcesCommitsBase extends CommitsBase {
    TreeMap<Integer, Integer> submitMap, acceptedMap;

    private int submitNumberSum;
    private int acceptedNumberSum;

    public CodeforcesCommitsBase(TreeMap<Integer, Integer> submitMap, TreeMap<Integer, Integer> acceptedMap) {
        this.submitMap = submitMap;
        submitNumberSum = 0;
        if (submitMap != null) {
            for (Integer au : submitMap.values()) {
                submitNumberSum += au;
            }
        }

        this.acceptedMap = acceptedMap;
        if (acceptedMap != null) {
            for (Integer au : acceptedMap.values()) {
                acceptedNumberSum += au;
            }
        }
    }

    public int getSubmitNumberSum() {
        return submitNumberSum;
    }

    public int getAcceptedNumberSum() {
        return acceptedNumberSum;
    }


    public int getNumber1() {
        return getSubmitNumberSum();
    }

    public int getNumber2() {
        return getAcceptedNumberSum();
    }

    public Calendar getCalendar(int i) {
        long timeLong = 1l * i * 86400 * 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeLong);
        return calendar;
    }

    public boolean ifFirstDayOfMonth(int i) {
        return getCalendar(i).get(Calendar.DAY_OF_MONTH) == 1;
    }

    public boolean ifFirstMonthOfYear(int i) {
        return getCalendar(i).get(Calendar.MONTH) == 0;
    }

    public String getYearName(int i) {
        return "" + getCalendar(i).get(Calendar.YEAR);
    }

    public String getMonthName(int i) {
        return new SimpleDateFormat("MMM").format(getCalendar(i).getTime());
    }

    public int getLevel(int i) {
        int res = 0;
        if (!submitMap.containsKey(i)) return res;

        res += submitMap.get(i);
        if (acceptedMap.containsKey(i)) {
            res += acceptedMap.get(i) * 5;
        }
        res /= 12;
        if (res > 4) res = 4;
        return res;
    }

    public int getDaysSize() {
        return (int) (System.currentTimeMillis() / 1000 / 86400);
    }

}
