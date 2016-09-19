package com.example.isseiomizu.weight;

/**
 * Created by isseiomizu on 2016/09/12.
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.isseiomizu.weight.models.DayItem;
import com.example.isseiomizu.weight.models.WeekItem;
import com.example.isseiomizu.weight.utils.BusProvider;
import com.example.isseiomizu.weight.utils.Events;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * カスタム PagerAdapter クラス.
 */
public class CustomPagerAdapter extends PagerAdapter {

    /** コンテキスト. */
    private Context mContext;

    /** リスト. */
    private ArrayList<Calendar> mList;

    /** ポジション. */
    private int mPosition;
    private Calendar mCalendar = null;

    private StickyListHeadersListView mListView;
    private StickyAdapter mStickyAdapter;

    /**
     * コンストラクタ.
     */
    public CustomPagerAdapter(Context context, StickyAdapter adapter) {
        mContext = context;
        mList = new ArrayList<Calendar>();
        mStickyAdapter = adapter;
        mPosition = 0;
    }

    /**
     * カレンダーリストにアイテムを追加する.
     * @param item アイテム
     */
    public void add(Calendar item) {
        mList.add(item);
    }

    /**
     * カレンダーリストからアイテムを取得する.
     * @return  カレンダーアイテム
     */
    public Calendar getItem(int position) {
        return mList.get(position);
    }

    /**
     * リストを取得する.
     * @return カレンダーリスト
     */
    public ArrayList<Calendar> getList() {
        return mList;
    }

    /**
     * カレンダーリストをセットする.
     * @param list カレンダーリスト
     */
    public void setList(ArrayList<Calendar> list) {
        mList.addAll(list);
    }

    /**
     * カレンダーリストをクリアする.
     */
    public void clearList() {
        mList.removeAll(mList);
    }

    public int getPosition() { return mPosition; }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        // リストから取得
        Calendar item = mList.get(position);



        // View を生成
        TextView textView = new TextView(mContext);
        textView.setText("Page:" + position + " " + item.getTime().toString());
        textView.setTextSize(30);
//        textView.setTextColor(item);
        textView.setGravity(Gravity.CENTER);
        container.addView(textView);

        return textView;


//        StickyListHeadersListView stickyListHeadersListView = new StickyListHeadersListView(mContext);
//
//        Calendar minDate = Calendar.getInstance();
//        Calendar maxDate = Calendar.getInstance();
//        minDate.setTime(item.getTime());
//        maxDate.setTime(item.getTime());
//        minDate.set(Calendar.DAY_OF_MONTH, 1);
//
//        CalendarManager calendarManager = CalendarManager.getInstance(mContext);
//
//        calendarManager.buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());
//        calendarManager.loadWeights();
//
//        StickyAdapter adapter = new StickyAdapter(mContext, android.R.layout.simple_list_item_1, CalendarManager.getInstance().getWeights());
//        stickyListHeadersListView.setAdapter(adapter);
//
//        container.addView(stickyListHeadersListView);
//
////        BusProvider.getInstance().send(new Events.EventsFetched(item));
//
//        return stickyListHeadersListView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // コンテナから View を削除
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        // リストのアイテム数を返す
        return mList.size();
    }

    @Override
//    public int getItemPosition(Object object) {
//        return mPosition;
//    }
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        // Object 内に View が存在するか判定する
        return view == (TextView) object;
//        return view == (StickyListHeadersListView) object;
    }

}