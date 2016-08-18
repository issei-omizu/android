package com.example.isseiomizu.weight;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.sheets.v4.SheetsScopes;

import com.google.api.services.sheets.v4.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.model.TimeSeries;


public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Google Sheets API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS_READONLY };


    private final int HALF_DAY = 12 * 3600 * 1000;

    private LinearLayout chartlayout;
    private LinearLayout activityLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        chartlayout = (LinearLayout) findViewById(R.id.chart);
//        GraphicalView graphicalView = TimeChartView();
//        chartlayout.removeAllViews();
//        chartlayout.addView(graphicalView);



//        activityLayout = new LinearLayout(this);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//        activityLayout.setLayoutParams(lp);
//        activityLayout.setOrientation(LinearLayout.VERTICAL);
//        activityLayout.setPadding(16, 16, 16, 16);
//
//        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//
//        mCallApiButton = new Button(this);
//        mCallApiButton.setText(BUTTON_TEXT);
//        mCallApiButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCallApiButton.setEnabled(false);
//                mOutputText.setText("");
//                getResultsFromApi();
//                mCallApiButton.setEnabled(true);
//            }
//        });
//        activityLayout.addView(mCallApiButton);
//
        mOutputText = new TextView(this);
//        mOutputText.setLayoutParams(tlp);
//        mOutputText.setPadding(16, 16, 16, 16);
//        mOutputText.setVerticalScrollBarEnabled(true);
//        mOutputText.setMovementMethod(new ScrollingMovementMethod());
//        mOutputText.setText(
//                "Click the \'" + BUTTON_TEXT +"\' button to test the API.");
//        activityLayout.addView(mOutputText);
//
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Sheets API ...");
//
//        setContentView(activityLayout);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());


        getResultsFromApi();

    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<List<Object>>> {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Sheets API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<List<Object>> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * @return List of names and majors
         * @throws IOException
         */
        private List<List<Object>> getDataFromApi() throws IOException {
//            String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
//            String range = "Class Data!A2:E";
            String spreadsheetId = "1CYOcWrQG7VG9wwPmf2VqI2Xqf-YclI04LiUB8Do_v0Q";
            String range = "数値!A2:C";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            // 書き込みテスト！
//            this.mService.spreadsheets().values().

            List<List<Object>> values = response.getValues();
//            if (values != null) {
//                results.add("Name, Major");
//                for (List row : values) {
//                    if (row.size() > 1) {
//                        results.add(row.get(0) + ", " + row.get(1));
//                    }
//                }
//            }
//
//
//
//            return results;
            return values;
        }



        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<List<Object>> output) {
            mProgress.hide();
//            if (output == null || output.size() == 0) {
//                mOutputText.setText("No results returned.");
//            } else {
//                output.add(0, "Data retrieved using the Google Sheets API:");
//                mOutputText.setText(TextUtils.join("\n", output));
//            }

            GraphicalView graphicalView = TimeChartView(output);
            chartlayout.removeAllViews();
            chartlayout.addView(graphicalView);
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

    private GraphicalView TimeChartView(List<List<Object>> output) {
        // (1)グラフデータの準備
        // X軸 日付
        String [] xStrValue={ "2015/12/22","2015/12/21","2015/12/20","2015/12/18" };
        // Y軸 体重
        Double[] yDoubleValue={ 71.2, 71.8, 70.8, 70.4 };
        // X軸 日付
        String [] xStrValueTarget={ "2015/12/22","2015/12/21","2015/12/20","2015/12/19", "2015/12/18"};
        // Y軸 目標体重
        Double[] yDoubleValueTarget={ 70.3, 70.3, 70.3, 70.3, 70.3 };

        List<String> listDate = new ArrayList<>();
        List<Double> listWeight = new ArrayList<>();
        List<Double> listTargetWeight = new ArrayList<>();

        if (output != null) {
            for (int i = output.size() - 1; i >= 0 ; i--) {
                List row = output.get(i);
                if (row.size() > 1) {
                    listDate.add(row.get(0).toString());
                    listWeight.add(Double.parseDouble(row.get(1).toString()));
                    listTargetWeight.add(55.0);
                }
            }
//            for (List row : output) {
//                if (row.size() > 1) {
//                    listDate.add(row.get(0).toString());
//                    listWeight.add(Double.parseDouble(row.get(1).toString()));
//                }
//            }
        }

        xStrValue = listDate.toArray(new String[listDate.size()]);
        xStrValueTarget = listDate.toArray(new String[listDate.size()]);
        yDoubleValue = listWeight.toArray(new Double[listWeight.size()]);
        yDoubleValueTarget = listTargetWeight.toArray(new Double[listWeight.size()]);




        // 日付を文字形式から Date型へ変換
        int DataCount=xStrValue.length;
        Date[] xDateValue = new Date[DataCount];
        for (int i = 0; i < DataCount; i++) {
            xDateValue[i] =String2date(xStrValue[i]);
        }

        int DataCountTarget=xStrValueTarget.length;
        Date[] xDateValueTarget = new Date[DataCountTarget];
        for (int i = 0; i < DataCountTarget; i++) {
            xDateValueTarget[i] =String2date(xStrValueTarget[i]);
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
        renderer.setXAxisMax(xDateValue[0].getTime() + HALF_DAY);    // X軸最大値
        renderer.setYAxisMin(53.0f);                    // Y軸最小値
        renderer.setYAxisMax(60.0f);                    // Y軸最大値
        renderer.setXLabelsAlign(Paint.Align.CENTER);         // X軸ラベル配置位置
        renderer.setYLabelsAlign(Paint.Align.RIGHT);          // Y軸ラベル配置位置
        renderer.setAxesColor(Color.BLACK);            // X軸、Y軸カラー
        renderer.setLabelsColor(Color.BLACK);          // ラベルカラー
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0, Color.BLACK);
        renderer.setXLabels(500);                         // X軸ラベルのおおよその数
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
        GraphicalView mChartView= ChartFactory.getTimeChartView(this, dataset, renderer, "M/d");

        return mChartView;

    }

    /*
 * 日付時刻文字列を Date型に変換
 */
    private Date String2date(String strDate) {
        Date dateDate=null;
        // 日付文字列→date型変換フォーマットを指定して
        SimpleDateFormat sdf1 = new SimpleDateFormat("M/d/yyyy");

        try {
            dateDate = sdf1.parse(strDate);
        }
        catch (ParseException e) {
            Toast.makeText(this, "正しい日付を入力して下さい", Toast.LENGTH_SHORT).show();
        }
        return dateDate;
    }

}
