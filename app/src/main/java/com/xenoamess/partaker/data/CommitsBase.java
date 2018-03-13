package com.xenoamess.partaker.data;

import java.util.ArrayList;

/**
 * Created by Alex on 12/8/14.
 */
public abstract class CommitsBase {
    public abstract int getDaysSize();

    public abstract int getNumber1();

    public abstract int getNumber2();

    public abstract boolean ifFirstDayOfMonth(int i);

    public abstract boolean ifFirstMonthOfYear(int i);

    public abstract String getYearName(int i);

    public abstract String getMonthName(int i);

    public abstract int getLevel(int i);

}
