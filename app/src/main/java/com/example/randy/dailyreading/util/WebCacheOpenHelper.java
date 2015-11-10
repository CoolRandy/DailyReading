package com.example.randy.dailyreading.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by randy on 2015/10/17.
 */
public class WebCacheOpenHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "webCache.db";

    public WebCacheOpenHelper(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //如果不存在cache就按照括号中的规则创建数据库，id为主键，newsId即新闻id是唯一的， 然后就是json数据，即新闻内容
        db.execSQL("create table if not exists Cache (id INTEGER primary key autoincrement,newsId INTEGER unique,json text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
