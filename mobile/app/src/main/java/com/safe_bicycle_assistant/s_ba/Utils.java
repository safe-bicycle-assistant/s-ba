package com.safe_bicycle_assistant.s_ba;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }
    public static String bitmap2String(Bitmap b)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);
    }
    public static Bitmap string2Bitmap(String s)
    {
        byte[] encodedBytes = Base64.decode(s,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(encodedBytes,0, encodedBytes.length);
        return bitmap;
    }
}
