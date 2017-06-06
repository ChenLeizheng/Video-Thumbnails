package com.self.myapplication.utils;

/**
 * Created by lei on 2017/2/22.
 */
public class Utils {

    public static String formatDuration2HMS(long duration){
        long s = duration/1000;
        long min = s/60;
        long hour = min/60;

        return (hour<10 ? "0"+hour : hour)+":"+(min%60<10 ? "0"+min%60 : min%60)+":"+(s%60<10 ? "0"+s%60 : s%60);
    }
}
