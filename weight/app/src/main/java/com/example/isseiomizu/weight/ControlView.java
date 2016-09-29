package com.example.isseiomizu.weight;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.nakama.arraypageradapter.ArrayPagerAdapter;

import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by nakama on 2016/04/01.
 */
public class ControlView extends ScrollView {

    @Bind(R.id.remove_edit)
    public EditText removeEdit;
    @Bind(R.id.add_position_edit)
    public EditText addPositionEdit;

    private ArrayPagerAdapter adapter;
    private ItemCreator creator;

    public ControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.widget_control, this);
        ButterKnife.bind(this);
        this.creator = new StringItemCreator();
    }

    public void setItemCreator(ItemCreator creator) {
        this.creator = creator;
    }

    public void setAdapter(ArrayPagerAdapter adapter) {
        this.adapter = adapter;
    }


    @OnClick({R.id.add_btn, R.id.add_position_btn, R.id.remove_btn, R.id.add_all_btn, R.id.clear_btn})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_btn:
                adapter.add(creator.createItem());
                break;
            case R.id.add_position_btn:
                int addPosition = textAsInt(addPositionEdit);
                if (addPosition != -1) {
                    if (addPosition >= 0 && addPosition <= adapter.getCount()) {
                        adapter.add(addPosition, creator.createItem());
                    } else {
                        Toast.makeText(getContext(), "IndexOutOfBounds:" + addPosition + ",data size is " + adapter.getCount(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Set add position.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.remove_btn:
                int removePosition = textAsInt(removeEdit);
                if (removePosition != -1) {
                    if (removePosition >= 0 && removePosition < adapter.getCount()) {
                        adapter.remove(removePosition);
                    } else {
                        Toast.makeText(getContext(), "IndexOutOfBounds:" + removePosition + ",data size is " + adapter.getCount(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Set remove position.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.add_all_btn:
                adapter.addAll(creator.createItem(), creator.createItem(), creator.createItem());
                break;
            case R.id.clear_btn:
                adapter.clear();
                break;
        }
    }

    private int textAsInt(EditText editText) {
        String str = editText.getText().toString();
        if (!str.equals("")) {
            return Integer.parseInt(str);
        }
        return -1;
    }

    public interface ItemCreator<T> {
        T createItem();
    }

    public static class StringItemCreator implements ItemCreator {
        private int dataIndex = 0;

        @Override
        public Object createItem() {
            return "added:" + (++dataIndex);
        }
    }


    public static class DogItemCreator implements ControlView.ItemCreator<Dog> {

        @Override
        public Dog createItem() {
            Random r = new Random();
            return new Dog(new String[]{"Alfred", "Baron", "Fane"}[r.nextInt(3)],
                    new String[]{"Marshmallow", "Gingerbread", "Eclair"}[r.nextInt(3)],
                    r.nextInt(20));
        }
    }
}
