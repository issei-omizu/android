package com.example.isseiomizu.weight;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GraphActivity extends AppCompatActivity {

    private final int HALF_DAY = 12 * 3600 * 1000;

    private LinearLayout chartlayout;

    private SQLiteDatabase mDbWeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // db
        MyOpenHelper helper = new MyOpenHelper(this);
        this.mDbWeight = helper.getWritableDatabase();

        chartlayout = (LinearLayout) findViewById(R.id.chart);
        GraphicalView graphicalView = TimeChartView();
        chartlayout.removeAllViews();
        chartlayout.addView(graphicalView);
    }
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        this.mDbWeight.close();
    }

    private GraphicalView TimeChartView() {
        List<String> listDate = new ArrayList<>();
        List<Double> listWeight = new ArrayList<>();
        List<Double> listTargetWeight = new ArrayList<>();

        String date = "";
        String weight = "";
        String bodyFatPercentage = "";

        // (1)グラフデータの準備
        // X軸 日付
        String[] xStrValue = {"2015/12/22", "2015/12/21", "2015/12/20", "2015/12/18"};
        // Y軸 体重
        Double[] yDoubleValue = {71.2, 71.8, 70.8, 70.4};
        // X軸 日付
        String[] xStrValueTarget = {"2015/12/22", "2015/12/21", "2015/12/20", "2015/12/19", "2015/12/18"};
        // Y軸 目標体重
        Double[] yDoubleValueTarget = {70.3, 70.3, 70.3, 70.3, 70.3};

        // sqliteからデータを全取得
        Cursor c = mDbWeight.query("weight", new String[]{"date", "weight", "body_fat_percentage"}, null, null, null, null, "date DESC");
        boolean mov = c.moveToFirst();

        while (mov) {
            date = c.getString(0);
            weight = c.getString(1);
            bodyFatPercentage = c.getString(2);

            if (weight != null && !weight.isEmpty()) {
                listDate.add(date);
                listWeight.add(Double.parseDouble(weight));
                listTargetWeight.add(55.0);
            }

            // 次のレコードへ
            mov = c.moveToNext();
        }

        c.close();


        xStrValue = listDate.toArray(new String[listDate.size()]);
        xStrValueTarget = listDate.toArray(new String[listDate.size()]);
        yDoubleValue = listWeight.toArray(new Double[listWeight.size()]);
        yDoubleValueTarget = listTargetWeight.toArray(new Double[listTargetWeight.size()]);


        // 日付を文字形式から Date型へ変換
        int DataCount = xStrValue.length;
        Date[] xDateValue = new Date[DataCount];
        for (int i = 0; i < DataCount; i++) {
            xDateValue[i] = String2date(xStrValue[i]);
        }

        int DataCountTarget = xStrValueTarget.length;
        Date[] xDateValueTarget = new Date[DataCountTarget];
        for (int i = 0; i < DataCountTarget; i++) {
            xDateValueTarget[i] = String2date(xStrValueTarget[i]);
        }

        // (2) グラフのタイトル、X軸Y軸ラベル、色等の設定
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setChartTitle("体重");     // グラフタイトル
        renderer.setChartTitleTextSize(25);             //
        renderer.setXTitle("日付");                     // X軸タイトル
        renderer.setYTitle("体重(Kg)");                     // Y軸タイトル
        renderer.setAxisTitleTextSize(25);              //
        renderer.setLegendTextSize(25);                 // 凡例　テキストサイズ
        renderer.setPointSize(3f);                      // ポイントマーカーサイズ
        renderer.setXAxisMin(xDateValue[xDateValue.length - 1].getTime() - HALF_DAY);  // X軸最小値
//        renderer.setXAxisMax(xDateValue[0].getTime() + HALF_DAY);    // X軸最大値
        renderer.setXAxisMax(xDateValue[xDateValue.length - 31].getTime() + HALF_DAY);    // X軸最大値
        renderer.setYAxisMin(53.0f);                    // Y軸最小値
        renderer.setYAxisMax(60.0f);                    // Y軸最大値
        renderer.setXLabelsAlign(Paint.Align.CENTER);         // X軸ラベル配置位置
        renderer.setYLabelsAlign(Paint.Align.RIGHT);          // Y軸ラベル配置位置
        renderer.setAxesColor(Color.BLACK);            // X軸、Y軸カラー
        renderer.setLabelsColor(Color.BLACK);          // ラベルカラー
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0, Color.BLACK);
        renderer.setXLabels(31);                         // X軸ラベルのおおよその数
        renderer.setYLabels(10);                         // Y軸ラベルのおおよその数
        renderer.setLabelsTextSize(25);                 // ラベルサイズ
        // グリッド表示
        renderer.setShowGrid(true);
        // グリッド色
        renderer.setGridColor(Color.parseColor("#808080")); // グリッドカラー
        // グラフ描画領域マージン top, left, bottom, right
        renderer.setMargins(new int[]{40, 100, 15, 40});  //
        // 凡例非表示
        // renderer.setShowLegend(false);
        renderer.setMarginsColor(Color.rgb(128, 128, 128));
        // マージンを凡例にフィットさせる
        renderer.setFitLegend(true);

        // (3) データ系列の色、マーク等の設定
        XYSeriesRenderer r1 = new XYSeriesRenderer();
        r1.setColor(Color.rgb(255, 166, 0));
        r1.setPointStyle(PointStyle.CIRCLE);
        r1.setLineWidth(5f);
        r1.setFillPoints(true);
        renderer.addSeriesRenderer(r1);
        XYSeriesRenderer r2 = new XYSeriesRenderer();
        r2.setColor(Color.rgb(255, 0, 0));
        r2.setPointStyle(PointStyle.SQUARE);
        r2.setLineWidth(5f);
        r2.setFillPoints(true);
        renderer.addSeriesRenderer(r2);


        // (4) データ系列　データの設定 (体重)
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        TimeSeries series1 = new TimeSeries("体重");     // データ系列凡例
        for (int i = 0; i < DataCount; i++) {
            series1.add(xDateValue[i], yDoubleValue[i]);
        }
        dataset.addSeries(series1);

        // (4) データ系列　データの設定 (目標値)
        TimeSeries seriesTarget = new TimeSeries("目標値");     //
        for (int i = 0; i < DataCountTarget; i++) {
            seriesTarget.add(xDateValueTarget[i], yDoubleValueTarget[i]);
        }
        dataset.addSeries(seriesTarget);

        // (5)タイムチャートグラフの作成
        GraphicalView mChartView = ChartFactory.getTimeChartView(this, dataset, renderer, "M/d");

        return mChartView;

    }

    /**
     * 日付時刻文字列を Date型に変換
     *
     * @param strDate
     * @return
     */
    private Date String2date(String strDate) {
        Date dateDate=null;
        // 日付文字列→date型変換フォーマットを指定して
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");

        try {
            dateDate = sdf1.parse(strDate);
        }
        catch (ParseException e) {
            Toast.makeText(this, "正しい日付を入力して下さい", Toast.LENGTH_SHORT).show();
        }
        return dateDate;
    }


}
