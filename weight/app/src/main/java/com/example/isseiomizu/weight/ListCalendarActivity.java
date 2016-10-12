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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.isseiomizu.weight.models.DayItem;
import com.example.isseiomizu.weight.models.WeekItem;
import com.nakama.arraypageradapter.ArrayPagerAdapter;
import com.nakama.arraypageradapter.ArrayViewPagerAdapter;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import se.emilsjolander.stickylistheaders.WrapperView;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.nakama.arraypageradapter.ArrayPagerAdapter;

public class ListCalendarActivity extends AppCompatActivity {

    private StickyListHeadersListView mListView;
    private ViewPager mViewPager;
    private CalendarPickerController mCalendarPickerController;
    private CustomPagerAdapter mCustomPagerAdapter;

    private Date mSelectedDate;
    private Calendar mPreviousDate2 = Calendar.getInstance();
    private Calendar mPreviousDate = Calendar.getInstance();
    private Calendar mCurrentDate = Calendar.getInstance();
    private Calendar mNextDate = Calendar.getInstance();
    private Calendar mNextDate2 = Calendar.getInstance();

    private StickyAdapter mAdapter;

    private SQLiteDatabase mDbWeight;

    /** コンテキスト. */
    private Context mContext;

    @Bind(R.id.view_pager)
    ViewPager viewPager;
//    @Bind(R.id.control_view)
//    ControlView controlView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_list_calendar);
        setContentView(R.layout.activity_pager);

        mContext = this;

        ButterKnife.bind(this);

        Intent intent = getIntent();
        String paramSelectedDate = intent.getStringExtra("date");

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");

//        try {
//            mSelectedDate = sdf1.parse(paramSelectedDate);
//
//            // db
//            MyOpenHelper helper = new MyOpenHelper(this);
//            this.mDbWeight = helper.getWritableDatabase();
//
//            // minimum and maximum date of our calendar
//            // 2 month behind, one year ahead, example: March 2015 <-> May 2015 <-> May 2016
//            Calendar minDate = Calendar.getInstance();
//            Calendar maxDate = Calendar.getInstance();
//
//            minDate.setTime(mSelectedDate);
//            maxDate.setTime(mSelectedDate);
//
////            minDate.add(Calendar.MONTH, -1);
//            minDate.set(Calendar.DAY_OF_MONTH, 1);
////            maxDate.add(Calendar.YEAR, 1);
////            maxDate.add(Calendar.MONTH, 1);
////            maxDate.set(Calendar.DAY_OF_MONTH, 1);
//
//            //////// This can be done once in another thread
//            CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
//            calendarManager.buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());
//            calendarManager.loadWeights();
//
////            mListView = (StickyListHeadersListView) findViewById(R.id.sticky_listview);
//
//            // ViewPager を生成
//            mViewPager = (ViewPager) findViewById(R.id.view_pager);
//
//            mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//                @Override
//                public void onPageSelected(int position) {
//                    mViewPager.setCurrentItem(1);
//                }
//
//                @Override
//                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//                }
//
//                @Override
//                public void onPageScrollStateChanged(int state) {
//                    if (state == ViewPager.SCROLL_STATE_SETTLING) {
//                        int page = mViewPager.getCurrentItem();
//                        /**
//                         *  ここにしたい処理を書く。例えば、
//                         *  textView.setText(String.valueOf(page));
//                         *  という感じ
//                         */
//                        if (page == 0) {
////                        mViewPager.removeViewAt(2);
////                        ArrayList<Calendar> list = mCustomPagerAdapter.getList();
////                        for (int i = 0; i < list.size(); i++) {
////                            Calendar cal = list.get(i);
////                            cal.add(Calendar.MONTH, -2);
////                        }
////                        mCustomPagerAdapter.notifyDataSetChanged();
////                        mViewPager.setCurrentItem(1);
//                        } else if (page == 2) {
//                            ArrayList<Calendar> list = mCustomPagerAdapter.getList();
//                            ArrayList<Calendar> newList = new ArrayList<>();
//                            for (int i = 0; i < list.size(); i++) {
////                                Calendar cal = Calendar.getInstance();
////                                cal.setTime(list.get(i).getTime());
////                                cal.add(Calendar.MONTH, 2);
////
////                                newList.add(cal);
//
//                                list.get(i).add(Calendar.MONTH, 2);
//                            }
//
////                            mCustomPagerAdapter.clearList();
////                            mCustomPagerAdapter.setList(newList);
////                            mViewPager.removeAllViews();
//
//
//                            mCustomPagerAdapter.notifyDataSetChanged();
////                            mViewPager.setCurrentItem(1);
//                        }
//                    }
//                }
//            });
//        } catch (ParseException e) {
//            //失敗時の処理…
//        }

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
            minDate.set(Calendar.DAY_OF_MONTH, 1);

            //////// This can be done once in another thread
            CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
            calendarManager.buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());
            calendarManager.loadWeights();

//            MyPagerAdapter adapter = new MyPagerAdapter(new String[]{"1", "2", "3"});
//            viewPager.setAdapter(adapter);
//            controlView.setAdapter(adapter);

            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
//                    viewPager.setCurrentItem(1);
//                    onPageSelected(position);
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                    String test  = "";
//
//                    StickyListHeadersListView sv0 = (StickyListHeadersListView) viewPager.getChildAt(0);
//                    LinearLayout l0 = null;
//                    ViewGroup.LayoutParams lp0;
//                    if (sv0.getChildCount() >= 2) {
//                        l0 = (LinearLayout) sv0.getChildAt(1);
//                        lp0 = l0.getLayoutParams();
//                        if (l0 != null) {
//                            TextView tv = (TextView) l0.getChildAt(0);
//                            tv.setText(tv.getText() + " BBB");
//                        }
//                    }
//
//                    StickyListHeadersListView sv = null;
//                    if (viewPager.getChildCount() >= 2) {
//                        sv = (StickyListHeadersListView) viewPager.getChildAt(1);
//                    }
//
//                    LinearLayout l;
//                    TextView v;
//                    ViewGroup.LayoutParams lp;
//                    if (sv != null && sv.getChildCount() >= 2) {
//
//                        l = (LinearLayout) sv.getChildAt(1);
//
//                        if (l != null) {
//                            v = (TextView) l.getChildAt(0);
//                            v.setText(v.getText() + " AAAAA");
//
//                            lp = l.getLayoutParams();
//                            lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//
//
////                            sv.removeViewAt(1);
//                        } else {
//
//                        }
//
//                        if (l0 != null) {
////                            sv.addView(l0);
//                        }
//
//                        String ooo = "";
//                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_SETTLING) {
                        int page = viewPager.getCurrentItem();
                        /**
                         *  ここにしたい処理を書く。例えば、
                         *  textView.setText(String.valueOf(page));
                         *  という感じ
                         */
                        if (page == 0) {
                            ArrayPagerAdapter arrayPagerAdapter = (ArrayPagerAdapter) viewPager.getAdapter();

                            ArrayList<Calendar> list = mCustomPagerAdapter.getList();
                            ArrayList<Calendar> newList = new ArrayList<>();
                            for (int i = 0; i < list.size(); i++) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(list.get(i).getTime());
                                cal.add(Calendar.MONTH, -1);

                                newList.add(cal);
                                }

                            mCustomPagerAdapter.clearList();
                            mCustomPagerAdapter.setList(newList);
//                            arrayPagerAdapter.clear();
//                            arrayPagerAdapter.addAll(newList);

                            arrayPagerAdapter.remove(2);
                            arrayPagerAdapter.add(0, newList.get(0));
                        } else if (page == 2) {
                            ArrayPagerAdapter arrayPagerAdapter = (ArrayPagerAdapter) viewPager.getAdapter();

                            ArrayList<Calendar> list = mCustomPagerAdapter.getList();
                            ArrayList<Calendar> newList = new ArrayList<>();
                            for (int i = 0; i < list.size(); i++) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(list.get(i).getTime());
                                cal.add(Calendar.MONTH, 1);

                                newList.add(cal);

//                                list.get(i).add(Calendar.MONTH, 2);
//                                mViewPager.setCurrentItem(1);
                            }

                            mCustomPagerAdapter.clearList();
                            mCustomPagerAdapter.setList(newList);

                            arrayPagerAdapter.remove(0);
                            arrayPagerAdapter.add(2, newList.get(2));

//                            arrayPagerAdapter.clear();
//                            arrayPagerAdapter.addAll(newList);

//                            mViewPager.removeAllViews();


//                            mCustomPagerAdapter.notifyDataSetChanged();
//                            mViewPager.setCurrentItem(1);

                        }
                    }
                }
            });

        } catch (Exception e) {
            String status = e.toString();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

////        StickyAdapter adapter = new StickyAdapter(this, android.R.layout.simple_list_item_1, createSampleArray());
        mAdapter = new StickyAdapter(this, android.R.layout.simple_list_item_1, CalendarManager.getInstance().getWeights());
//        mListView.setAdapter(adapter);

        // カスタム PagerAdapter を生成
        mCustomPagerAdapter = new CustomPagerAdapter(this, mAdapter);

        mPreviousDate2 = Calendar.getInstance();
        mPreviousDate = Calendar.getInstance();
        mCurrentDate = Calendar.getInstance();
        mNextDate = Calendar.getInstance();
        mNextDate2 = Calendar.getInstance();

        mPreviousDate2.setTime(mSelectedDate);
        mPreviousDate2.add(Calendar.MONTH, -2);
        mPreviousDate2.set(Calendar.DAY_OF_MONTH, 1);

        mPreviousDate.setTime(mSelectedDate);
        mPreviousDate.add(Calendar.MONTH, -1);
        mPreviousDate.set(Calendar.DAY_OF_MONTH, 1);

        mCurrentDate.setTime(mSelectedDate);
//        mCurrentDate.add(Calendar.MONTH, 1);
        mCurrentDate.set(Calendar.DAY_OF_MONTH, 1);

        mNextDate.setTime(mSelectedDate);
        mNextDate.add(Calendar.MONTH, 1);
        mNextDate.set(Calendar.DAY_OF_MONTH, 1);

        mNextDate2.setTime(mSelectedDate);
        mNextDate2.add(Calendar.MONTH, 2);
        mNextDate2.set(Calendar.DAY_OF_MONTH, 1);

//        mCustomPagerAdapter.add(mPreviousDate2);
        mCustomPagerAdapter.add(mPreviousDate);
        mCustomPagerAdapter.add(mCurrentDate);
        mCustomPagerAdapter.add(mNextDate);
//        mCustomPagerAdapter.add(mNextDate2);

//        mViewPager.setAdapter(mCustomPagerAdapter);
//        mViewPager.setCurrentItem(1);


        MyPagerAdapter adapter = new MyPagerAdapter(new Calendar[]{
                mPreviousDate,
                mCurrentDate,
                mNextDate
        });
        viewPager.setAdapter(adapter);
//        controlView.setAdapter(adapter);
        viewPager.setCurrentItem(1);


        BusProvider.getInstance().toObserverable()
                .subscribe(event -> {
                    if (event instanceof Events.DayClickedEvent) {
                        String test = "";
                        mCalendarPickerController.onDaySelected(((Events.DayClickedEvent) event).getDay());
                    } else if (event instanceof Events.EventsFetched) {
//                        CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
////                        calendarManager.loadPrevious();
//                        calendarManager.loadCal(((Events.EventsFetched) event).getCalendar());
//                        mAdapter.notifyDataSetChanged();
                        mViewPager.setCurrentItem(1);
                    } else if (event instanceof Events.EventsPrevious) {
//                        CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
////                        calendarManager.loadPrevious();
//                        calendarManager.loadCal(mPreviousDate);
//                        mAdapter.notifyDataSetChanged();
////                        mViewPager.setCurrentItem(1);
                        mPreviousDate.add(Calendar.MONTH, -1);
                        mPreviousDate.set(Calendar.DAY_OF_MONTH, 1);

                        mCurrentDate.add(Calendar.MONTH, -1);
                        mCurrentDate.set(Calendar.DAY_OF_MONTH, 1);

                        mNextDate.add(Calendar.MONTH, -11);
                        mNextDate.set(Calendar.DAY_OF_MONTH, 1);

                        mViewPager.removeAllViews();
                        mCustomPagerAdapter.add(mPreviousDate);
                        mCustomPagerAdapter.add(mCurrentDate);
                        mCustomPagerAdapter.add(mNextDate);
                        mCustomPagerAdapter.notifyDataSetChanged();
                    } else if (event instanceof Events.EventsCurrent) {
                        CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
//                        calendarManager.loadNext();
                        calendarManager.loadCal(mCurrentDate);
                        mAdapter.notifyDataSetChanged();
                        mViewPager.setCurrentItem(1);
                    } else if (event instanceof Events.EventsNext) {
//                        CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
////                        calendarManager.loadNext();
//                        calendarManager.loadCal(mNextDate);
//                        mAdapter.notifyDataSetChanged();
////                        mViewPager.setCurrentItem(1);

                        mPreviousDate.add(Calendar.MONTH, 1);
                        mPreviousDate.set(Calendar.DAY_OF_MONTH, 1);

                        mCurrentDate.add(Calendar.MONTH, 1);
                        mCurrentDate.set(Calendar.DAY_OF_MONTH, 1);

                        mNextDate.add(Calendar.MONTH, 1);
                        mNextDate.set(Calendar.DAY_OF_MONTH, 1);

                        mViewPager.removeAllViews();
                        mCustomPagerAdapter.add(mPreviousDate);
                        mCustomPagerAdapter.add(mCurrentDate);
                        mCustomPagerAdapter.add(mNextDate);
                        mCustomPagerAdapter.notifyDataSetChanged();
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        this.mDbWeight.close();
    }

    private class MyPagerAdapter extends ArrayViewPagerAdapter<Calendar> {
        public MyPagerAdapter(Calendar[] data) {
            super(data);
        }

        @Override
        public View getView(LayoutInflater inflater, ViewGroup container, Calendar item, int position) {
////            View v = inflater.inflate(R.layout.item_text_page, container, false);
////            ((TextView) v.findViewById(R.id.item_txt)).setText(item);
//
//
//            ArrayList<Calendar> list = mCustomPagerAdapter.getList();
//
//            StickyListHeadersListView stickyListHeadersListView = new StickyListHeadersListView(mContext);
//
//            Calendar minDate = Calendar.getInstance();
//            Calendar maxDate = Calendar.getInstance();
//            minDate.setTime(list.get(position).getTime());
//            maxDate.setTime(list.get(position).getTime());
//            minDate.set(Calendar.DAY_OF_MONTH, 1);
//
//            CalendarManager calendarManager = CalendarManager.getInstance(mContext);
//
//            calendarManager.buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());
//            calendarManager.loadWeights();
//
//            StickyAdapter adapter = new StickyAdapter(mContext, android.R.layout.simple_list_item_1, CalendarManager.getInstance().getWeights());
//            stickyListHeadersListView.setAdapter(adapter);
//
//            View v = stickyListHeadersListView;



            ArrayList<Calendar> list = mCustomPagerAdapter.getList();

            Calendar minDate = Calendar.getInstance();
            Calendar maxDate = Calendar.getInstance();
            minDate.setTime(list.get(position).getTime());
            maxDate.setTime(list.get(position).getTime());
            minDate.set(Calendar.DAY_OF_MONTH, 1);

            CalendarManager calendarManager = CalendarManager.getInstance(mContext);

            calendarManager.buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());
            calendarManager.loadWeights();

            // View を生成
            LinearLayout linearLayout= new LinearLayout(ListCalendarActivity.this);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            );
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setBackgroundColor(Color.GREEN);


            // header
            View header = inflater.inflate(R.layout.sticky_header_row, null);
            WeightListAdapter.HeaderViewHolder holder = new WeightListAdapter.HeaderViewHolder(header);
            header.setTag(holder);

            Calendar cal = Calendar.getInstance();
            cal.setTime(CalendarManager.getInstance().getWeights().get(position).getDate());

            holder.textView.setText(cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH) + 1));

            linearLayout.addView(header);


            // View を生成
            ListView listView = new ListView(ListCalendarActivity.this);
            ViewGroup.LayoutParams lp2 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp2.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            lp2.width = FrameLayout.LayoutParams.WRAP_CONTENT;
            listView.setLayoutParams(lp2);

            WeightListAdapter adapter = new WeightListAdapter(ListCalendarActivity.this);
            adapter.setTweetList(CalendarManager.getInstance().getWeights());
            listView.setAdapter(adapter);

            linearLayout.addView(listView);

            View v = linearLayout;
            return v;
        }

    }
}

