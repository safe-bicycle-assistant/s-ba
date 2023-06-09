package com.safe_bicycle_assistant.s_ba.db_helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.safe_bicycle_assistant.s_ba.Utils;

import java.util.ArrayList;
import java.util.List;

public class ManagementDB extends SQLiteOpenHelper {
    public static final int BRAKES = 1;
    public static final int TYRES = 2;
    public static final int WHEELS = 4;
    public static final int ALL = 7;
    public static final int TIME = 0;
    public static final int CHANGE = 1;
    public final static int BICYCLE_NAME = 2;
    static final String DATABASE_NAME = "s-ba";
    public static final String TABLE_NAME = "ManagementLog";
    static final String TABLE_FORMAT = "(time BIGINT, change INT, bicyclename TEXT)";
    public ManagementDB(Context context, int version) {
        super(context, DATABASE_NAME,null,version);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s%s",TABLE_NAME,TABLE_FORMAT));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(String.format("DROP TABLE IF EXISTS %s",TABLE_NAME));
        onCreate(sqLiteDatabase);
    }

    public void insert(long time, int changedItems, String bicyclename) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(String.format("INSERT INTO %s VALUES('%d','%d','%s')",TABLE_NAME,time,changedItems,bicyclename));
        db.close();

    }

    public Cursor getAllDataToCursor(String bicyclename) {
        String query = "select *,1 _id from "+ TABLE_NAME + " where bicyclename = '" + bicyclename +"' order by time desc";
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(query,null);
        return c;
    }

    public Cursor getDataByIndex(int i, String bicyclename) {
//        String query = "select * from "+TABLE_NAME+" limit 1 offset "+i;
//        SQLiteDatabase db = getWritableDatabase();
//        Cursor c = db.rawQuery(query,null);
//        return c;

        String query = "select *,1 _id from "+ TABLE_NAME+ " where bicyclename = '" + bicyclename +"' order by time desc";
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(query,null);
        c.moveToFirst();
        while(i > 0 )
        {
            if(c.moveToNext())
                i--;
            else
                return null;
        }
        return c;
    }

    public List<String> getAllData()
    {
        String query = "select * from "+ TABLE_NAME;
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(query,null);
        List<String> items = new ArrayList<>();
        while(c.moveToNext()) {
            String line = "" + Utils.longToDate(c.getLong(0)) +" "+c.getInt(1);
            items.add(line);
        }
        db.close();
        return items;
    }
}
