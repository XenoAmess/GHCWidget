package by.aleks.ghcwidget.data;

import java.util.ArrayList;

/**
 * Created by Alex on 12/8/14.
 */
public class CommitsBase {

    private ArrayList<Day> days = new ArrayList<>();
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

    public ArrayList<Day> getDays() {
        return days;
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
