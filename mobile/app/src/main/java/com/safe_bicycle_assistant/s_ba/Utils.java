package com.safe_bicycle_assistant.s_ba;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static long dateToLong(Date date) {
        return date.getTime();
    }
    public static Date longToDate(long millis) {
        return new Date(millis);
    }
    public static String DateToString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH::mm");
        return format.format(date);
    }
}
