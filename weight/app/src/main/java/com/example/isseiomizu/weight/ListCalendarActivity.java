package com.example.isseiomizu.weight;

import com.example.isseiomizu.weight.utils.BusProvider;
import com.example.isseiomizu.weight.utils.Events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.isseiomizu.weight.models.DayItem;
import com.example.isseiomizu.weight.models.WeekItem;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class ListCalendarActivity extends AppCompatActivity {

    private StickyListHeadersListView mListView;
    private ViewPager mViewPager;
    private CalendarPickerController mCalendarPickerController;

    private Date mSelectedDate;
    private Calendar mPreviousDate = Calendar.getInstance();
    private Calendar mCurrentDate = Calendar.getInstance();
    private Calendar mNextDate = Calendar.getInstance();

    private StickyAdapter mAdapter;

    private SQLiteDatabase mDbWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_calendar);

        Intent intent = getIntent();
        String paramSelectedDate = intent.getStringExtra("date");

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
        try {
            mSelectedDate = sdf1.parse(paramSelectedDate);

            // db
            MyOpenHelper helper = new MyOpenHelper(this);
            this.mDbWeight = helper.getWritableDatabase();

            // minimum and maximum date of our calendar
            // 2 month behind, one year ahead, example: March 2015 <-> May 2015 <-> May 2016
            Calendar minDate = Calendar.getInstance();
            Calendar maxDate = Calendar.getInstance();

            minDate.setTime(mSelectedDate);
            maxDate.setTime(mSelectedDate);

//            minDate.add(Calendar.MONTH, -1);
            minDate.set(Calendar.DAY_OF_MONTH, 1);
//            maxDate.add(Calendar.YEAR, 1);
//            maxDate.add(Calendar.MONTH, 1);
//            maxDate.set(Calendar.DAY_OF_MONTH, 1);

            //////// This can be done once in another thread
            CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
            calendarManager.buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());
            calendarManager.loadWeights();

//            mListView = (StickyListHeadersListView) findViewById(R.id.sticky_listview);

            // ViewPager を生成
            mViewPager = (ViewPager) findViewById(R.id.view_pager);

        } catch (ParseException e) {
            //失敗時の処理…
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

////        StickyAdapter adapter = new StickyAdapter(this, android.R.layout.simple_list_item_1, createSampleArray());
        mAdapter = new StickyAdapter(this, android.R.layout.simple_list_item_1, CalendarManager.getInstance().getWeights());
//        mListView.setAdapter(adapter);

        // カスタム PagerAdapter を生成
        CustomPagerAdapter customPagerAdapter = new CustomPagerAdapter(this, mAdapter);

        mPreviousDate = Calendar.getInstance();
        mCurrentDate = Calendar.getInstance();
        mNextDate = Calendar.getInstance();

        mPreviousDate.setTime(mSelectedDate);
        mPreviousDate.add(Calendar.MONTH, -1);
        mPreviousDate.set(Calendar.DAY_OF_MONTH, 1);

        mCurrentDate.setTime(mSelectedDate);
//        mCurrentDate.add(Calendar.MONTH, 1);
        mCurrentDate.set(Calendar.DAY_OF_MONTH, 1);

        mNextDate.setTime(mSelectedDate);
        mNextDate.add(Calendar.MONTH, 1);
        mNextDate.set(Calendar.DAY_OF_MONTH, 1);




        customPagerAdapter.add(mPreviousDate);
        customPagerAdapter.add(mCurrentDate);
        customPagerAdapter.add(mNextDate);
//        customPagerAdapter.add(Color.BLUE);
//        customPagerAdapter.add(Color.CYAN);
//        customPagerAdapter.add(Color.MAGENTA);
//        customPagerAdapter.add(Color.YELLOW);

        mViewPager.setAdapter(customPagerAdapter);


        BusProvider.getInstance().toObserverable()
                .subscribe(event -> {
                    if (event instanceof Events.DayClickedEvent) {
                        String test = "";
                        mCalendarPickerController.onDaySelected(((Events.DayClickedEvent) event).getDay());
                    } else if (event instanceof Events.EventsFetched) {
                        CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
//                        calendarManager.loadPrevious();
                        calendarManager.loadCal(((Events.EventsFetched) event).getCalendar());
                        mAdapter.notifyDataSetChanged();
                        mViewPager.setCurrentItem(1);
                    } else if (event instanceof Events.EventsPrevious) {
                        CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
//                        calendarManager.loadPrevious();
                        calendarManager.loadCal(mPreviousDate);
                        mAdapter.notifyDataSetChanged();
                        mViewPager.setCurrentItem(1);
                    } else if (event instanceof Events.EventsCurrent) {
                        CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
//                        calendarManager.loadNext();
                        calendarManager.loadCal(mCurrentDate);
                        mAdapter.notifyDataSetChanged();
                        mViewPager.setCurrentItem(1);
                    } else if (event instanceof Events.EventsNext) {
                        CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
//                        calendarManager.loadNext();
                        calendarManager.loadCal(mNextDate);
                        mAdapter.notifyDataSetChanged();
                        mViewPager.setCurrentItem(1);
                    }
                });
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

