package com.example.isseiomizu.weight;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.isseiomizu.weight.models.IWeightItem;
import com.example.isseiomizu.weight.models.WeightItem;
import com.google.api.client.util.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by isseiomizu on 2016/11/15.
 */

public class SqliteController {

    private static SqliteController mInstance;

    private Context mContext;
    private static SQLiteDatabase mDbWeight;

    public SqliteController(Context context) {
        this.mContext = context;
    }

    public static SqliteController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SqliteController(context);
        }

        // db
        if (mDbWeight == null) {
            MyOpenHelper helper = new MyOpenHelper(context);
            mDbWeight = helper.getWritableDatabase();
        }

        return mInstance;
    }


    public IWeightItem searchWeightByDate (String searchDate)  {
        IWeightItem item = new WeightItem();

        String date = "";
        String weight = "";
        String bodyFatPercentage = "";
        String bodyTemperature = "";

        // sqliteからデータを全取得
        Cursor c = mDbWeight.query("weight", new String[]{"date", "weight", "body_fat_percentage", "body_temperature"}, "date = ?", new String[]{searchDate}, null, null, "date DESC");
        boolean mov = c.moveToFirst();

        if (mov) {
            date = c.getString(0);
            weight = c.getString(1);
            bodyFatPercentage = c.getString(2);
            bodyTemperature = c.getString(3);

            item.setDate(String2date(date));
            item.setWeight(weight);
            item.setBodyFatPercentage(bodyFatPercentage);
            item.setBodyTemperature(bodyTemperature);
        } else {
            item = null;
        }
        c.close();

        return item;
    }

    public List<IWeightItem> getAllWeight ()  {
        List<IWeightItem> listWeight = new ArrayList<>();

        String date = "";
        String weight = "";
        String bodyFatPercentage = "";
        String bodyTemperature = "";

        // sqliteからデータを全取得
        Cursor c = mDbWeight.query("weight", new String[]{"date", "weight", "body_fat_percentage", "body_temperature"}, null, null, null, null, "date DESC");
        boolean mov = c.moveToFirst();

        while (mov) {
            IWeightItem item = new WeightItem();

            date = c.getString(0);
            weight = c.getString(1);
            bodyFatPercentage = c.getString(2);
            bodyTemperature = c.getString(3);

            item.setDate(String2date(date));
            item.setWeight(weight);
            item.setBodyFatPercentage(bodyFatPercentage);
            item.setBodyTemperature(bodyTemperature);

            listWeight.add(item);

            // 次のレコードへ
            mov = c.moveToNext();
        }
        c.close();

        return listWeight;
    }

    public boolean writeWeight (IWeightItem weightItem) {
        String date = date2String(weightItem.getDate());
        String weight = weightItem.getWeight();
        String bodyFatPercentage = weightItem.getBodyFatPercentage();
        String bodyTemperature = weightItem.getBodyTemperature();

        // sqliteに保存
        Cursor c = mDbWeight.query("weight", new String[] {"date", "weight", "body_fat_percentage", "body_temperature"}, "date = ?", new String[]{ date }, null, null, "date DESC");

        boolean mov = c.moveToFirst();
        c.close();

        if (mov) {
            if (weightItem.getWeight().isEmpty() && weightItem.getBodyFatPercentage().isEmpty()) {
                // 体重・体脂肪率両方とも未入力の場合はレコードを削除する
                mDbWeight.delete( "weight", "date=?", new String[] { date });
            } else {
                ContentValues updateValues = new ContentValues();
                updateValues.put("weight", weight);
                updateValues.put("body_fat_percentage", bodyFatPercentage);
                updateValues.put("body_temperature", bodyTemperature);
                mDbWeight.update("weight", updateValues, "date=?", new String[] { date });
            }
        } else {
            // 体重・体脂肪率どちらか入力されている場合は追加する
            if (!weight.isEmpty() || !bodyFatPercentage.isEmpty()) {
                ContentValues insertValues = new ContentValues();
                insertValues.put("date", date);
                insertValues.put("weight", weight);
                insertValues.put("body_fat_percentage", bodyFatPercentage);
                insertValues.put("body_temperature", bodyTemperature);
                long id = mDbWeight.insert("weight", null, insertValues);
                long confirm = id;
            }
        }

        return true;
    }

    /**
     * 日付時刻文字列を Date型に変換
     *
     * @param strDate
     * @return
     */
    public Date String2date(String strDate) {
        Date dateDate=null;
        // 日付文字列→date型変換フォーマットを指定して
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");

        try {
            dateDate = sdf1.parse(strDate);
        }
        catch (ParseException e) {
            dateDate = Calendar.getInstance().getTime();
        }
        return dateDate;
    }

    /**
     * 日付時刻文字列を Date型に変換
     *
     * @param date
     * @return
     */
    public String date2String(Date date) {
        String dateDate = "";

        // 日付文字列→date型変換フォーマットを指定して
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
        dateDate = sdf1.format(date);

        return dateDate;
    }

}
