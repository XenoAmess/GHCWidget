package com.xenoamess.partaker.modules.github;

import com.xenoamess.partaker.data.CommitsBase;
import com.xenoamess.partaker.data.Day;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Alex on 12/8/14.
 */
public class GitHubCommitsBase extends CommitsBase {

    private ArrayList<Day> days = new ArrayList<Day>();
    private int commitsNumberSum = 0;

    public void addDay(Day day) {
        //Skip the previous year days after a new year.
        if (days.isEmpty() ||
                day.getCalendar().compareTo(days.get(days.size() - 1).getCalendar()) > 0) {
            days.add(day);
            commitsNumberSum += day.getCommitsNumber();
        }
        return;
    }


    public int commitsNumber() {
//        int commitsCounter = 0;
//        for (Day day : days) {
//            commitsCounter += day.getCommitsNumber();
//        }
        return commitsNumberSum;
    }

    public int currentStreak() {
        for (int i = days.size() - 1; i >= 0; i--) {
            if (days.get(i).getCommitsNumber() == 0)
                return days.size() - i - 1;
        }
        return days.size();
    }

    public int getDaysSize() {
        return days.size();
    }


    public ArrayList<Day> getDays() {
        return days;
    }


    public int getNumber1() {
        return commitsNumber();
    }

    public int getNumber2() {
        return currentStreak();
    }


    public boolean ifFirstDayOfMonth(int i) {
        return this.getDays().get(i).getCalendar().get(Calendar.DAY_OF_MONTH) == 1;
    }

    public boolean ifFirstMonthOfYear(int i) {
        return this.getDays().get(i).getCalendar().get(Calendar.MONTH) == 0;
    }

    public String getYearName(int i) {
        return "" + this.getDays().get(i).getCalendar().get(Calendar.YEAR);
    }

    public String getMonthName(int i) {
        return this.getDays().get(i).getMonthName();
    }

    public int getLevel(int i) {
        return this.getDays().get(i).getLevel();
    }


//    public int findStartPos(int startIndex, int weekdayNeed) {
//        while (startIndex < days.size()) {
//            if (days.get(startIndex).getDayOfWeek() == weekdayNeed)
//                return startIndex;
//            startIndex++;
//        }
//        return 0;
//    }
}
