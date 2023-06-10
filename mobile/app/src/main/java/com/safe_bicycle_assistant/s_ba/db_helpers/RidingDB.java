package com.safe_bicycle_assistant.s_ba.db_helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.safe_bicycle_assistant.s_ba.Utils;

import java.util.ArrayList;
import java.util.List;

public class RidingDB extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "s-ba";
    public static final String TABLE_NAME = "RidingLog";
    public final static int TIME = 0;
    public final static int LENGTH = 1;
    public final static int AVERAGE_SPEED = 5;
    public final static int AVERAGE_CADENCE = 6;
    public final static int BICYCLE_NAME = 7;
    public final static int MAP = 4;
    public final static int MAX_SPEED = 2;
    public final static int MAX_CADENCE = 3;
    static final String TABLE_FORMAT = "(time BIGINT, length INT, maxSpeed DECIMAL(4,2), maxCadence DECIMAL(4,2), map TEXT , averageSpeed DECIMAL(4,2), averageCadence DECIMAL(4,2), bicyclename TEXT)";
    public RidingDB(Context context, int version) {
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

    public void insert(long time, int length, double avgSpeed, double avgCadence) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(String.format("INSERT INTO %s(time, length, averageSpeed, averageCadence) VALUES('%d','%d','%f','%f')",TABLE_NAME,time,length,avgSpeed,avgCadence));
        db.close();
    }
//(time BIGINT, length INT, maxSpeed DECIMAL(4,2), maxCadence DECIMAL(4,2), map TEXT , averageSpeed DECIMAL(4,2), averageCadence DECIMAL(4,2))
    public void insert(long time, int length, double avgSpeed, double avgCadence, String map, double maxSpeed, double maxCadence,String bicyclename) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(String.format("INSERT INTO %s(time, length, averageSpeed, averageCadence,maxSpeed,maxCadence,map) VALUES('%d','%d','%f','%f','%f','%f','%s','%s')",TABLE_NAME,time,length,avgSpeed,avgCadence,maxSpeed,maxCadence,map,bicyclename));
        db.close();
    }
    public Cursor getAllDataToCursor(String bicyclename) {
        String query = "select *,1 _id from "+ TABLE_NAME+ " where bicyclename = '" + bicyclename +"' order by time desc";
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(query,null);
        return c;
    }
    public Cursor getDataByIndex(int i,String bicyclename) {
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
            String line = "" + Utils.longToDate(c.getLong(0)) +" "+c.getInt(1) + " " + c.getDouble(2) + " " + c.getDouble(3);
            items.add(line);
        }
        db.close();
        return items;
    }

    public Cursor getAllDataAfterTime(long millis)
    {
        String query = "select *,1 _id  from "+TABLE_NAME+" where time > "+millis+" order by time desc";
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery(query,null);
        return c;
    }

}
