package com.wuj10n.mysimplebrowser;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "wuj10n.db";
    private static final int DATABASE_VERSION = 1;



    public MyDataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //创建表
        String CREATE_HISTORY="create table history(" +
                "_id integer primary key autoincrement,"+
                "title," +
                "url)";

        //书签表
        String CREATE_BOOKMARK = "create table bookmark(" +
                "_id integer primary key autoincrement," +
                "title," +
                "url not null unique,"+
                "category)";

        sqLiteDatabase.execSQL(CREATE_HISTORY);
        sqLiteDatabase.execSQL(CREATE_BOOKMARK);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //更新表
        sqLiteDatabase.execSQL("drop table if exists bookmark");
        sqLiteDatabase.execSQL("drop table if exists history");
        onCreate(sqLiteDatabase);

    }
}
