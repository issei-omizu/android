package com.example.isseiomizu.weight;

import android.app.ActionBar;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphActivity extends AppCompatActivity {

    private final int HALF_DAY = 12 * 3600 * 1000;

    private LinearLayout chartlayout;
    private LinearLayout chartBodyFatPercentage;

    private SQLiteDatabase mDbWeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_graph);

        // タイトルを非表示にする
        getSupportActionBar().hide();

        // db
        MyOpenHelper helper = new MyOpenHelper(this);
        this.mDbWeight = helper.getWritableDatabase();

        chartlayout = (LinearLayout) findViewById(R.id.chart);
//        GraphicalView graphicalView = TimeChartView();
        GraphicalView graphicalView = chartWeight();
        chartlayout.removeAllViews();
        chartlayout.addView(graphicalView);

        chartBodyFatPercentage = (LinearLayout) findViewById(R.id.chartBodyFatPercentage);
//        GraphicalView graphicalView = TimeChartView();
        GraphicalView gvChartBodyFatPercentage = chartBodyFatPercentage();
        chartBodyFatPercentage.removeAllViews();
        chartBodyFatPercentage.addView(gvChartBodyFatPercentage);
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

        // sqliteから登録している年を取得
        List<String> listYear = new ArrayList<>();
        Cursor c = mDbWeight.query("weight", new String[]{"substr(date, 1, 4) as year"}, null, null, "year", null, "date ASC");
        boolean mov = c.moveToFirst();
        while (mov) {
            listYear.add(c.getString(0));
            // 次のレコードへ
            mov = c.moveToNext();
        }
        c.close();

        String date = "";
        String weight = "";
        String bodyFatPercentage = "";

        List<String> listDate = new ArrayList<>();
        List<Double> listWeight = new ArrayList<>();
        List<Double> listBodyFatPercentage = new ArrayList<>();
        List<Double> listTargetWeight = new ArrayList<>();


        int[] colors = new int[] { Color.BLUE, Color.YELLOW, Color.GREEN, Color.RED };

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        // グラフをrenderする数は、取得する年*2
        int countRenderer = listYear.size() * 2;

        // (2) グラフのタイトル、X軸Y軸ラベル、色等の設定
        renderer = new XYMultipleSeriesRenderer(countRenderer);


        renderer.setPanEnabled(false, false);

        renderer.setChartTitle("体重");     // グラフタイトル
        renderer.setChartTitleTextSize(25);             //
        renderer.setXTitle("日付");                     // X軸タイトル
        renderer.setYTitle("体重(Kg)");                     // Y軸タイトル
        renderer.setAxisTitleTextSize(25);              //
        renderer.setLegendTextSize(25);                 // 凡例　テキストサイズ
        renderer.setPointSize(3f);                      // ポイントマーカーサイズ

//        renderer.setXAxisMin(xDateValue[xDateValue.length - 1].getTime() - HALF_DAY);  // X軸最小値
//        renderer.setXAxisMax(xDateValue[0].getTime() + HALF_DAY);    // X軸最大値
        renderer.setXAxisMin(11);  // X軸最小値
        renderer.setXAxisMax(1231);    // X軸最大値

//        renderer.setYAxisMin(53.0f);                    // Y軸最小値
        renderer.setYAxisMin(53.0f, 0);                    // Y軸最小値
        renderer.setYAxisMax(63.0f, 0);                    // Y軸最大値
        renderer.setXLabelsAlign(Paint.Align.CENTER);         // X軸ラベル配置位置
        renderer.setYLabelsAlign(Paint.Align.RIGHT);          // Y軸ラベル配置位置
        renderer.setAxesColor(Color.BLACK);            // X軸、Y軸カラー
        renderer.setLabelsColor(Color.BLACK);          // ラベルカラー
        renderer.setXLabelsColor(Color.BLACK);


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


//        renderer.setYLabelsColor(0, colors[0]);
//        renderer.setYLabelsColor(1, colors[1]);

//        renderer.setYTitle("Hours", 1);
//        renderer.setYAxisAlign(Align.RIGHT, 1);
//        renderer.setYLabelsAlign(Align.LEFT, 1);
//        renderer.setGridColor(colors[0], 0);
//        renderer.setGridColor(colors[1], 1);

        for (int yearCount = 0; yearCount < listYear.size(); yearCount++) {

            // (1)グラフデータの準備
            // X軸 日付
            String[] xStrValue = {"2015/12/22", "2015/12/21", "2015/12/20", "2015/12/18"};
            // Y軸 体重
            Double[] yDoubleValue = {71.2, 71.8, 70.8, 70.4};
            // Y2軸 体脂肪率
            Double[] yDoubleBodyFatPercentage = {71.2, 71.8, 70.8, 70.4};
            // X軸 日付
            String[] xStrValueTarget = {"2015/12/22", "2015/12/21", "2015/12/20", "2015/12/19", "2015/12/18"};
            // Y軸 目標体重
            Double[] yDoubleValueTarget = {70.3, 70.3, 70.3, 70.3, 70.3};


            listDate.clear();
            listWeight.clear();
            listBodyFatPercentage.clear();
            listTargetWeight.clear();



            String searchWord = listYear.get(yearCount) + "%";

            // sqliteからデータを全取得
            c = mDbWeight.query("weight", new String[]{"date", "weight", "body_fat_percentage"}, "date like ? ", new String[]{searchWord}, null, null, "date DESC");
            mov = c.moveToFirst();

            while (mov) {
                date = c.getString(0);
                weight = c.getString(1);
                bodyFatPercentage = c.getString(2);

                if (weight != null && !weight.isEmpty()) {
                    listDate.add(date);
                    listWeight.add(Double.parseDouble(weight));
                    listBodyFatPercentage.add(Double.parseDouble(bodyFatPercentage));
                    listTargetWeight.add(55.0);
                }

                // 次のレコードへ
                mov = c.moveToNext();
            }

            c.close();


            xStrValue = listDate.toArray(new String[listDate.size()]);
            xStrValueTarget = listDate.toArray(new String[listDate.size()]);
            yDoubleValue = listWeight.toArray(new Double[listWeight.size()]);
            yDoubleBodyFatPercentage = listBodyFatPercentage.toArray(new Double[listBodyFatPercentage.size()]);

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

            // (3) データ系列の色、マーク等の設定
            XYSeriesRenderer r1 = new XYSeriesRenderer();
            r1.setColor(colors[yearCount]);
            r1.setPointStyle(PointStyle.CIRCLE);
            r1.setLineWidth(5f);
            r1.setFillPoints(true);
            renderer.addSeriesRenderer(r1);

            XYSeriesRenderer r2 = new XYSeriesRenderer();
            r2.setColor(colors[yearCount]);
            r2.setPointStyle(PointStyle.SQUARE);
            r2.setLineWidth(5f);
            r2.setFillPoints(true);
            renderer.addSeriesRenderer(r2);

            int seriesRendererCount = renderer.getSeriesRendererCount() - 1;
            renderer.setYTitle("体脂肪率", seriesRendererCount);
            renderer.setYAxisAlign(Align.RIGHT, seriesRendererCount);
            renderer.setYLabelsAlign(Align.LEFT, seriesRendererCount);
            renderer.setYLabelsColor(seriesRendererCount, colors[yearCount]);
            renderer.setGridColor(colors[yearCount], seriesRendererCount);
            renderer.setYAxisMax(20, seriesRendererCount);
            renderer.setYAxisMin(0, seriesRendererCount);



            // (4) データ系列　データの設定 (体重)
            SimpleDateFormat sdf = new SimpleDateFormat("Mdd");
            TimeSeries series1 = new TimeSeries("体重");     // データ系列凡例
            XYSeries seriesWeight = new XYSeries(listYear.get(yearCount));     // データ系列凡例
            for (int i = 0; i < DataCount; i++) {
                series1.add(xDateValue[i], yDoubleValue[i]);
                seriesWeight.add(Double.valueOf(sdf.format(xDateValue[i])), yDoubleValue[i]);
            }
//        dataset.addSeries(series1);
            dataset.addSeries(seriesWeight);


            XYSeries seriesBodyFat = new XYSeries("", seriesRendererCount);     // データ系列凡例
            for (int i = 0; i < DataCount; i++) {
                seriesBodyFat.add(Double.valueOf(sdf.format(xDateValue[i])), yDoubleBodyFatPercentage[i]);
            }
            dataset.addSeries(seriesBodyFat);


            // (4) データ系列　データの設定 (目標値)
            TimeSeries seriesTarget = new TimeSeries("目標値");     //
            for (int i = 0; i < DataCountTarget; i++) {
                seriesTarget.add(xDateValueTarget[i], yDoubleValueTarget[i]);
            }
//        dataset.addSeries(seriesTarget);
        }

        // (5)タイムチャートグラフの作成
//        GraphicalView mChartView = ChartFactory.getTimeChartView(this, dataset, renderer, "M/d");
        GraphicalView mChartView = ChartFactory.getLineChartView(this, dataset, renderer);

        return mChartView;

    }

    private GraphicalView chartWeight() {

        // sqliteから登録している年を取得
        List<String> listYear = new ArrayList<>();
        Cursor c = mDbWeight.query("weight", new String[]{"substr(date, 1, 4) as year"}, null, null, "year", null, "date ASC");
        boolean mov = c.moveToFirst();
        while (mov) {
            listYear.add(c.getString(0));
            // 次のレコードへ
            mov = c.moveToNext();
        }
        c.close();

        String date = "";
        String weight = "";
        String bodyFatPercentage = "";

        List<String> listDate = new ArrayList<>();
        List<Double> listWeight = new ArrayList<>();
        List<Double> listBodyFatPercentage = new ArrayList<>();
        List<Double> listTargetWeight = new ArrayList<>();


        int[] colors = new int[] { Color.BLUE, Color.YELLOW, Color.GREEN, Color.RED };

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        // グラフをrenderする数は、取得する年*2
        int countRenderer = listYear.size() * 2;

        // (2) グラフのタイトル、X軸Y軸ラベル、色等の設定
        renderer = new XYMultipleSeriesRenderer();


        renderer.setPanEnabled(false, false);

        renderer.setChartTitle("体重");     // グラフタイトル
        renderer.setChartTitleTextSize(25);             //
        renderer.setXTitle("日付");                     // X軸タイトル
//        renderer.setYTitle("体重(Kg)");                     // Y軸タイトル
        renderer.setAxisTitleTextSize(25);              //
        renderer.setLegendTextSize(25);                 // 凡例　テキストサイズ
        renderer.setPointSize(3f);                      // ポイントマーカーサイズ

//        renderer.setXAxisMin(xDateValue[xDateValue.length - 1].getTime() - HALF_DAY);  // X軸最小値
//        renderer.setXAxisMax(xDateValue[0].getTime() + HALF_DAY);    // X軸最大値

        Calendar calRange = Calendar.getInstance();
        calRange.set(2016, 1, 1);
        calRange.add(Calendar.MONTH, -1);
        renderer.setXAxisMin(calRange.getTimeInMillis());  // X軸最小値
        calRange.set(2016, 12, 31);
        calRange.add(Calendar.MONTH, -1);
        renderer.setXAxisMax(calRange.getTimeInMillis());    // X軸最大値

//        renderer.setYAxisMin(53.0f);                    // Y軸最小値
        renderer.setYAxisMin(53.0f, 0);                    // Y軸最小値
        renderer.setYAxisMax(63.0f, 0);                    // Y軸最大値
        renderer.setXLabelsAlign(Paint.Align.CENTER);         // X軸ラベル配置位置
        renderer.setYLabelsAlign(Paint.Align.RIGHT);          // Y軸ラベル配置位置
        renderer.setAxesColor(Color.BLACK);            // X軸、Y軸カラー
        renderer.setLabelsColor(Color.BLACK);          // ラベルカラー
        renderer.setXLabelsColor(Color.BLACK);


        renderer.setXLabels(12);                         // X軸ラベルのおおよその数
        renderer.setYLabels(6);                         // Y軸ラベルのおおよその数
        renderer.setLabelsTextSize(25);                 // ラベルサイズ
        // グリッド表示
        renderer.setShowGrid(true);
        // グリッド色
        renderer.setGridColor(Color.parseColor("#808080")); // グリッドカラー
        // グラフ描画領域マージン top, left, bottom, right
        renderer.setMargins(new int[]{40, 30, 40, 10});  //
        // 凡例非表示
        // renderer.setShowLegend(false);
        renderer.setMarginsColor(Color.WHITE);
        // マージンを凡例にフィットさせる
        renderer.setFitLegend(true);


//        renderer.setYLabelsColor(0, colors[0]);
//        renderer.setYLabelsColor(1, colors[1]);

//        renderer.setYTitle("Hours", 1);
//        renderer.setYAxisAlign(Align.RIGHT, 1);
//        renderer.setYLabelsAlign(Align.LEFT, 1);
//        renderer.setGridColor(colors[0], 0);
//        renderer.setGridColor(colors[1], 1);

        for (int yearCount = 0; yearCount < listYear.size(); yearCount++) {

            // (1)グラフデータの準備
            // X軸 日付
            String[] xStrValue = {"2015/12/22", "2015/12/21", "2015/12/20", "2015/12/18"};
            // Y軸 体重
            Double[] yDoubleValue = {71.2, 71.8, 70.8, 70.4};
            // Y2軸 体脂肪率
            Double[] yDoubleBodyFatPercentage = {71.2, 71.8, 70.8, 70.4};
            // X軸 日付
            String[] xStrValueTarget = {"2015/12/22", "2015/12/21", "2015/12/20", "2015/12/19", "2015/12/18"};
            // Y軸 目標体重
            Double[] yDoubleValueTarget = {70.3, 70.3, 70.3, 70.3, 70.3};


            listDate.clear();
            listWeight.clear();
            listBodyFatPercentage.clear();
            listTargetWeight.clear();



            String searchWord = listYear.get(yearCount) + "%";

            // sqliteからデータを全取得
            c = mDbWeight.query("weight", new String[]{"date", "weight", "body_fat_percentage"}, "date like ? ", new String[]{searchWord}, null, null, "date DESC");
            mov = c.moveToFirst();

            while (mov) {
                date = c.getString(0);
                weight = c.getString(1);
                bodyFatPercentage = c.getString(2);

                if (weight != null && !weight.isEmpty()) {
                    listDate.add(date);
                    listWeight.add(Double.parseDouble(weight));
                    listBodyFatPercentage.add(Double.parseDouble(bodyFatPercentage));
                    listTargetWeight.add(55.0);
                }

                // 次のレコードへ
                mov = c.moveToNext();
            }

            c.close();


            xStrValue = listDate.toArray(new String[listDate.size()]);
            xStrValueTarget = listDate.toArray(new String[listDate.size()]);
            yDoubleValue = listWeight.toArray(new Double[listWeight.size()]);
            yDoubleBodyFatPercentage = listBodyFatPercentage.toArray(new Double[listBodyFatPercentage.size()]);

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


            // (3) データ系列の色、マーク等の設定
            XYSeriesRenderer r1 = new XYSeriesRenderer();
            r1.setColor(colors[yearCount]);
            r1.setPointStyle(PointStyle.CIRCLE);
            r1.setLineWidth(1f);
            r1.setFillPoints(true);
            renderer.addSeriesRenderer(r1);

            XYSeriesRenderer r2 = new XYSeriesRenderer();
            r2.setColor(colors[yearCount]);
            r2.setPointStyle(PointStyle.SQUARE);
            r2.setLineWidth(1f);
            r2.setFillPoints(true);
            renderer.addSeriesRenderer(r2);


            // (4) データ系列　データの設定 (体重)
            SimpleDateFormat sdf = new SimpleDateFormat("Mdd");
//            XYSeries seriesWeight = new XYSeries(listYear.get(yearCount));     // データ系列凡例
//            for (int i = 0; i < DataCount; i++) {
//                seriesWeight.add(Double.valueOf(sdf.format(xDateValue[i])), yDoubleValue[i]);
//            }
//            dataset.addSeries(seriesWeight);
//
//            // (4) データ系列　データの設定 (目標値)
//            XYSeries seriesTarget = new XYSeries("目標値");     //
//            for (int i = 0; i < DataCountTarget; i++) {
//                seriesTarget.add(Double.valueOf(sdf.format(xDateValue[i])), yDoubleValueTarget[i]);
//            }
//            dataset.addSeries(seriesTarget);

            Calendar cal = Calendar.getInstance();
            TimeSeries seriesWeight = new TimeSeries(listYear.get(yearCount));     // データ系列凡例
            for (int i = 0; i < DataCount; i++) {
                cal.setTime(xDateValue[i]);
                cal.set(2016, cal.get(Calendar.MONTH), cal.get(Calendar.DATE));

                seriesWeight.add(cal.getTime(), yDoubleValue[i]);
            }
            dataset.addSeries(seriesWeight);

            // (4) データ系列　データの設定 (目標値)
            TimeSeries seriesTarget = new TimeSeries("");     // データ系列凡例
            for (int i = 0; i < DataCountTarget; i++) {
                cal.setTime(xDateValue[i]);
                cal.set(2016, cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
                seriesTarget.add(cal.getTime(), yDoubleValueTarget[i]);
            }
            dataset.addSeries(seriesTarget);

        }

        // (5)ライングラフの作成
//        GraphicalView mChartView = ChartFactory.getLineChartView(this, dataset, renderer);
        GraphicalView mChartView = ChartFactory.getTimeChartView(this, dataset, renderer, "M/d");

        return mChartView;

    }

    private GraphicalView chartBodyFatPercentage() {

        // sqliteから登録している年を取得
        List<String> listYear = new ArrayList<>();
        Cursor c = mDbWeight.query("weight", new String[]{"substr(date, 1, 4) as year"}, null, null, "year", null, "date ASC");
        boolean mov = c.moveToFirst();
        while (mov) {
            listYear.add(c.getString(0));
            // 次のレコードへ
            mov = c.moveToNext();
        }
        c.close();

        String date = "";
        String weight = "";
        String bodyFatPercentage = "";

        List<String> listDate = new ArrayList<>();
        List<Double> listWeight = new ArrayList<>();
        List<Double> listBodyFatPercentage = new ArrayList<>();
        List<Double> listTargetWeight = new ArrayList<>();


        int[] colors = new int[] { Color.BLUE, Color.YELLOW, Color.GREEN, Color.RED };

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        // グラフをrenderする数は、取得する年*2
        int countRenderer = listYear.size() * 2;

        // (2) グラフのタイトル、X軸Y軸ラベル、色等の設定
        renderer = new XYMultipleSeriesRenderer();


        renderer.setPanEnabled(false, false);

        renderer.setChartTitle("体脂肪率");     // グラフタイトル
        renderer.setChartTitleTextSize(25);             //
        renderer.setXTitle("日付");                     // X軸タイトル
//        renderer.setYTitle("体脂肪率(%)");                     // Y軸タイトル
        renderer.setAxisTitleTextSize(25);              //
        renderer.setLegendTextSize(25);                 // 凡例　テキストサイズ
        renderer.setPointSize(3f);                      // ポイントマーカーサイズ

        Calendar calRange = Calendar.getInstance();
        calRange.set(2016, 1, 1);
        calRange.add(Calendar.MONTH, -1);
        renderer.setXAxisMin(calRange.getTimeInMillis());  // X軸最小値
        calRange.set(2016, 12, 31);
        calRange.add(Calendar.MONTH, -1);
        renderer.setXAxisMax(calRange.getTimeInMillis());    // X軸最大値

        renderer.setYAxisMin(3.0f, 0);                    // Y軸最小値
        renderer.setYAxisMax(17.0f, 0);                    // Y軸最大値
        renderer.setXLabelsAlign(Paint.Align.CENTER);         // X軸ラベル配置位置
        renderer.setYLabelsAlign(Paint.Align.RIGHT);          // Y軸ラベル配置位置
        renderer.setAxesColor(Color.BLACK);            // X軸、Y軸カラー
        renderer.setLabelsColor(Color.BLACK);          // ラベルカラー
        renderer.setXLabelsColor(Color.BLACK);


        renderer.setXLabels(12);                         // X軸ラベルのおおよその数
        renderer.setYLabels(6);                         // Y軸ラベルのおおよその数
        renderer.setLabelsTextSize(25);                 // ラベルサイズ
        // グリッド表示
        renderer.setShowGrid(true);
        // グリッド色
        renderer.setGridColor(Color.parseColor("#808080")); // グリッドカラー
        // グラフ描画領域マージン top, left, bottom, right
        renderer.setMargins(new int[]{40, 30, 40, 10});  //
        // 凡例非表示
        // renderer.setShowLegend(false);
        renderer.setMarginsColor(Color.WHITE);
        // マージンを凡例にフィットさせる
        renderer.setFitLegend(true);


        for (int yearCount = 0; yearCount < listYear.size(); yearCount++) {

            // (1)グラフデータの準備
            // X軸 日付
            String[] xStrValue = {"2015/12/22", "2015/12/21", "2015/12/20", "2015/12/18"};
            // Y軸 体重
            Double[] yDoubleValue = {71.2, 71.8, 70.8, 70.4};
            // Y2軸 体脂肪率
            Double[] yDoubleBodyFatPercentage = {71.2, 71.8, 70.8, 70.4};
            // X軸 日付
            String[] xStrValueTarget = {"2015/12/22", "2015/12/21", "2015/12/20", "2015/12/19", "2015/12/18"};
            // Y軸 目標体重
            Double[] yDoubleValueTarget = {70.3, 70.3, 70.3, 70.3, 70.3};


            listDate.clear();
            listWeight.clear();
            listBodyFatPercentage.clear();
            listTargetWeight.clear();



            String searchWord = listYear.get(yearCount) + "%";

            // sqliteからデータを全取得
            c = mDbWeight.query("weight", new String[]{"date", "weight", "body_fat_percentage"}, "date like ? ", new String[]{searchWord}, null, null, "date DESC");
            mov = c.moveToFirst();

            while (mov) {
                date = c.getString(0);
                weight = c.getString(1);
                bodyFatPercentage = c.getString(2);

                if (weight != null && !weight.isEmpty()) {
                    listDate.add(date);
                    listWeight.add(Double.parseDouble(weight));
                    listBodyFatPercentage.add(Double.parseDouble(bodyFatPercentage));
                    listTargetWeight.add(6.0);
                }

                // 次のレコードへ
                mov = c.moveToNext();
            }

            c.close();


            xStrValue = listDate.toArray(new String[listDate.size()]);
            xStrValueTarget = listDate.toArray(new String[listDate.size()]);
            yDoubleValue = listBodyFatPercentage.toArray(new Double[listBodyFatPercentage.size()]);

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


            // (3) データ系列の色、マーク等の設定
            XYSeriesRenderer r1 = new XYSeriesRenderer();
            r1.setColor(colors[yearCount]);
            r1.setPointStyle(PointStyle.CIRCLE);
            r1.setLineWidth(1f);
            r1.setFillPoints(true);
            renderer.addSeriesRenderer(r1);

            XYSeriesRenderer r2 = new XYSeriesRenderer();
            r2.setColor(colors[yearCount]);
            r2.setPointStyle(PointStyle.SQUARE);
            r2.setLineWidth(1f);
            r2.setFillPoints(true);
            renderer.addSeriesRenderer(r2);


            // (4) データ系列　データの設定 (体重)
//            SimpleDateFormat sdf = new SimpleDateFormat("Mdd");
//            XYSeries seriesWeight = new XYSeries(listYear.get(yearCount));     // データ系列凡例
//            for (int i = 0; i < DataCount; i++) {
//                seriesWeight.add(Double.valueOf(sdf.format(xDateValue[i])), yDoubleValue[i]);
//            }
//            dataset.addSeries(seriesWeight);
//
//            // (4) データ系列　データの設定 (目標値)
//            XYSeries seriesTarget = new XYSeries("目標値");     //
//            for (int i = 0; i < DataCountTarget; i++) {
//                seriesTarget.add(Double.valueOf(sdf.format(xDateValue[i])), yDoubleValueTarget[i]);
//            }
//            dataset.addSeries(seriesTarget);

            Calendar cal = Calendar.getInstance();
            TimeSeries seriesWeight = new TimeSeries(listYear.get(yearCount));     // データ系列凡例
            for (int i = 0; i < DataCount; i++) {
                cal.setTime(xDateValue[i]);
                cal.set(2016, cal.get(Calendar.MONTH), cal.get(Calendar.DATE));

                seriesWeight.add(cal.getTime(), yDoubleValue[i]);
            }
            dataset.addSeries(seriesWeight);

            // (4) データ系列　データの設定 (目標値)
            TimeSeries seriesTarget = new TimeSeries("");     // データ系列凡例
            for (int i = 0; i < DataCountTarget; i++) {
                cal.setTime(xDateValue[i]);
                cal.set(2016, cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
                seriesTarget.add(cal.getTime(), yDoubleValueTarget[i]);
            }
            dataset.addSeries(seriesTarget);
        }

        // (5)ライングラフの作成
//        GraphicalView mChartView = ChartFactory.getLineChartView(this, dataset, renderer);
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
