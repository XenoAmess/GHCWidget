package com.xenoamess.partaker.data;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sfeq on 2018/3/13.
 */

public class ModuleManager {
    public static String GITHUB = "github";
    public static String CODEFORCES = "codeforces";

//    public static ArrayList<CharSequence> MODULE_NAMES;
//
//    static {
//        MODULE_NAMES = new ArrayList<CharSequence>();
//        MODULE_NAMES.add(GITHUB);
//        MODULE_NAMES.add(CODEFORCES);
//    }

    public static CharSequence[] getModuleNames() {

        return new CharSequence[]{GITHUB, CODEFORCES};
        //return MODULE_NAMES.toArray(new CharSequence[10]);

//        return new CharSequence[]{ColorTheme.GITHUB, ColorTheme.MODERN, ColorTheme.GRAY, ColorTheme.RED, ColorTheme.BLUE, ColorTheme.PURPLE, ColorTheme.ORANGE, ColorTheme.HALLOWEEN};
    }


}
