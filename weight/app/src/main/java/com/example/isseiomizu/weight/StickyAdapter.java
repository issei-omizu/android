package com.example.isseiomizu.weight;

/**
 * Created by isseiomizu on 2016/09/07.
 */

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class StickyAdapter extends ArrayAdapter<String> implements StickyListHeadersAdapter {

    private LayoutInflater mInflater;

    public StickyAdapter(Context context, int resource, List<String> itemList) {
        super(context, resource, itemList);

        mInflater = LayoutInflater.from(context);
    }

    // Headerのレイアウトを定義します
    @Override
    public View getHeaderView(int i, View view, ViewGroup viewGroup) {
        HeaderViewHolder holder;

        if (view == null) {
            view = mInflater.inflate(R.layout.sticky_header_row, null);
            holder = new HeaderViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (HeaderViewHolder) view.getTag();
        }
        holder.textView.setText("Header: " +getHeaderItem(i));

        return view;
    }

    // 【重要】Headerグループ毎に同じ値を返すようにしましょう
    // ここではItemポジションを5で割った商が同じになるものをグループにします（5個/グループ）
    // getHeaderItemを参考にしましょう
    @Override
    public long getHeaderId(int i) {
        return getHeaderItem(i);
    }

    // getItemにならい、Header版も作る優しさが持てるといいですね
    // 各アイテムのHeaderはポジションを5で割った商です
    // getHeaderIdを参考にしましょう
    public int getHeaderItem(int position) {
        return position / 5;
    }

    // おなじみのgetViewです
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.sticky_item_row, parent, false);
            holder = new ItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ItemViewHolder) convertView.getTag();
        }

        // Adapterに渡されたテキストを入れます
        holder.textView.setText(getItem(position));

        return convertView;
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

        // コンストラクタ内でidバインドを行なうとスッキリします
        public ItemViewHolder(View view) {
            textView = (TextView) view.findViewById(R.id.item_textview);
        }
    }
}