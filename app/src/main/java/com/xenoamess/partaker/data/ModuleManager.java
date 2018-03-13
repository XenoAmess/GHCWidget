package com.xenoamess.partaker.data;

import android.graphics.Color;

import com.xenoamess.partaker.modules.ModuleDataCenter;

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

    public static HashMap<String, ModuleDataCenter> ModuleDataCenterMap = new HashMap<String, ModuleDataCenter>();
    static {
        ModuleDataCenterMap.put(GITHUB, new com.xenoamess.partaker.modules.github.ModuleDataCenter());
    }


    public static ModuleDataCenter GetModuleDataCenter(String name) {
        return ModuleDataCenterMap.get(name);
    }

    public static CharSequence[] getModuleNames() {
        return new CharSequence[]{GITHUB, CODEFORCES};
    }


}
