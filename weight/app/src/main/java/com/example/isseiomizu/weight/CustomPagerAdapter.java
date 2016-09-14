package com.example.isseiomizu.weight;

/**
 * Created by isseiomizu on 2016/09/12.
 */

import java.util.ArrayList;
import java.util.Calendar;
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

    private StickyListHeadersListView mListView;
    private StickyAdapter mStickyAdapter;

    /**
     * コンストラクタ.
     */
    public CustomPagerAdapter(Context context, StickyAdapter adapter) {
        mContext = context;
        mList = new ArrayList<Calendar>();
        mStickyAdapter = adapter;
    }

    /**
     * リストにアイテムを追加する.
     * @param item アイテム
     */
    public void add(Calendar item) {
        mList.add(item);
    }

    public int getPosition() { return mPosition; }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        // リストから取得
        Calendar item = mList.get(position);

//        Calendar minDate = Calendar.getInstance();
//        Calendar maxDate = Calendar.getInstance();
//
//        minDate.setTime(item.getTime());
//        maxDate.setTime(item.getTime());
//
//        minDate.set(Calendar.DAY_OF_MONTH, 1);
//
//        CalendarManager calendarManager = CalendarManager.getInstance(mContext);
//        calendarManager.buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());
//        calendarManager.loadWeights();
//

//        // View を生成
//        TextView textView = new TextView(mContext);
//        textView.setText("Page:" + position);
//        textView.setTextSize(30);
//        textView.setTextColor(item);
//        textView.setGravity(Gravity.CENTER);

        StickyAdapter adapter = new StickyAdapter(mContext, android.R.layout.simple_list_item_1, CalendarManager.getInstance().getWeights());

        StickyListHeadersListView stickyListHeadersListView = new StickyListHeadersListView(mContext);
        stickyListHeadersListView.setAdapter(adapter);

        // 3つViewがあっtら全て描画済みとする。
        if (container.getChildCount() == 3) {
            // コンテナに追加
//        container.addView(textView);
            // 消す → 追加！！

//            if (position == 0) {
//                BusProvider.getInstance().send(new Events.EventsPrevious());
//            } else if (position == 1) {
//                BusProvider.getInstance().send(new Events.EventsCurrent());
//            } else if (position == 2) {
//                BusProvider.getInstance().send(new Events.EventsNext());
//            }
//            mPosition = position;
            container.addView(stickyListHeadersListView);
            BusProvider.getInstance().send(new Events.EventsFetched(item));
        } else {
            container.addView(stickyListHeadersListView);
            BusProvider.getInstance().send(new Events.EventsFetched(item));
        }


        return stickyListHeadersListView;
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
    public int getItemPosition(Object object) {
        return mPosition;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        // Object 内に View が存在するか判定する
//        return view == (TextView) object;
        return view == (StickyListHeadersListView) object;
    }

}