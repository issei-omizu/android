package com.example.isseiomizu.weight;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class ListCalendarActivity extends AppCompatActivity {

    private StickyListHeadersListView mListView;

    private SQLiteDatabase mDbWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_calendar);

        // db
        MyOpenHelper helper = new MyOpenHelper(this);
        this.mDbWeight = helper.getWritableDatabase();

        mListView = (StickyListHeadersListView) findViewById(R.id.sticky_listview);

    }

    @Override
    protected void onResume() {
        super.onResume();

        StickyAdapter adapter = new StickyAdapter(this, android.R.layout.simple_list_item_1, createSampleArray());
        mListView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        this.mDbWeight.close();
    }

    // Adapterに渡すサンプルListを生成します
    private List<String> createSampleArray() {

        List<String> list = new ArrayList<>();

        String date = "";
        String weight = "";
        String bodyFatPercentage = "";

        // sqliteからデータを全取得
        Cursor c = mDbWeight.query("weight", new String[]{"date", "weight", "body_fat_percentage"}, null, null, null, null, "date DESC");
        boolean mov = c.moveToFirst();

        while (mov) {
            date = c.getString(0);
            weight = c.getString(1);
            bodyFatPercentage = c.getString(2);

            if (weight != null && !weight.isEmpty()) {
                list.add("position: " + date + weight + bodyFatPercentage);
            }

            // 次のレコードへ
            mov = c.moveToNext();
        }

        c.close();

//        for (int i = 0; i < 100; i++) {
//            list.add("position: " + i);
//        }
        return list;
    }

}

