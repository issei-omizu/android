package com.example.isseiomizu.weight;

/**
 * Created by isseiomizu on 2016/08/25.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyOpenHelper extends SQLiteOpenHelper {
    public MyOpenHelper(Context context) {
        super(context, "WeightDB", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table weight("
                + " date text not null,"
                + " weight text,"
                + " body_fat_percentage text"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DBバージョンアップ時のデータ移行を実装
        if (oldVersion < newVersion) {
            if (oldVersion == 2) {
                db.execSQL("alter table weight add body_temperature text default '36.50'" );
            }
        }
    }
}
