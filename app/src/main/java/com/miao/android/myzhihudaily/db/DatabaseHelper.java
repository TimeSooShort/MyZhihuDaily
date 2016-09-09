package com.miao.android.myzhihudaily.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/8/28.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table if not exists LatestPosts(id integer primary key," +
                "title text not null,type integer not null,img_url text not null," +
                "date integer not null)");
        sqLiteDatabase.execSQL("create table if not exists Contents(id integer primary key," +
                "date integer not null,content text not null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVision) {

        switch (oldVersion) {
            case 1:
                sqLiteDatabase.execSQL("create table if not exists Contents(id integer primary key," +
                        "date integer not null,content text not null)");
        }
    }
}
