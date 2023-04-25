package com.safe_bicycle_assistant.s_ba;

import java.util.Date;

public class Utils {
    public static long dateToLong(Date date) {
        return date.getTime();
    }
    public static Date longToDate(long millis) {
        return new Date(millis);
    }
}
