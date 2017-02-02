package com.example.isseiomizu.weight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.example.isseiomizu.weight.models.IWeightItem;
import com.example.isseiomizu.weight.models.WeightItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by isseiomizu on 2016/10/07.
 */

public class WeightListAdapter extends BaseAdapter {


    Context context;
    LayoutInflater layoutInflater = null;
    List<IWeightItem> tweetList;

    private LayoutInflater mInflater;
    private SqliteController mSqliteController;

    public WeightListAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater = LayoutInflater.from(context);
        this.mSqliteController = SqliteController.getInstance(context);
    }

    public void setTweetList(List<IWeightItem> tweetList) {
        this.tweetList = tweetList;
    }

    @Override
    public int getCount() {
        return tweetList.size();
    }

    @Override
    public Object getItem(int position) {
        return tweetList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return tweetList.get(position).getDate().getTime();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WeightListAdapter.ItemViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.sticky_item_row, parent, false);
            holder = new WeightListAdapter.ItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (WeightListAdapter.ItemViewHolder) convertView.getTag();
        }

        SimpleDateFormat sdf1 = new SimpleDateFormat("d");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");

        // Adapterに渡されたテキストを入れます
        holder.textView.setText(sdf1.format(tweetList.get(position).getDate()));
        holder.itemRowWeight.setText(tweetList.get(position).getWeight());
        holder.itemRowBodyFatPercentage.setText(tweetList.get(position).getBodyFatPercentage());

        return convertView;
    }

    private void setWriteDate(View convertView) {
        WeightListAdapter.ItemViewHolder holder;
        holder = (WeightListAdapter.ItemViewHolder) convertView.getTag();

        String weight = holder.itemRowWeight.getText().toString();
        String bodyFatPercentage = holder.itemRowBodyFatPercentage.getText().toString();

        if (weight.isEmpty()) {
//            weight = "0";
        }

        if (bodyFatPercentage.isEmpty()) {
//            bodyFatPercentage = "0";
        }

        // 体重データ更新
        // sqliteに保存
        IWeightItem weightItem = new WeightItem();
//        weightItem.setDate(this.mSqliteController.String2date(date));
        weightItem.setWeight(weight);
        weightItem.setBodyFatPercentage(bodyFatPercentage);
        this.mSqliteController.writeWeight(weightItem);
    }

    public static class HeaderViewHolder {
        TextView textView;

        // コンストラクタ内でidバインドを行なうとスッキリします
        public HeaderViewHolder(View view) {
            textView = (TextView) view.findViewById(R.id.header_textview);
        }
    }

    public static class ItemViewHolder {
        TextView textView;
        TextView itemRowWeight;
        TextView itemRowBodyFatPercentage;

        // コンストラクタ内でidバインドを行なうとスッキリします
        public ItemViewHolder(View view) {
            textView = (TextView) view.findViewById(R.id.item_textview);
            itemRowWeight = (TextView) view.findViewById(R.id.itemRowWeight);
            itemRowBodyFatPercentage = (TextView) view.findViewById(R.id.itemRowBodyFatPercentage);

        }
    }
}

