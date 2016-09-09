package com.example.isseiomizu.weight;

import com.example.isseiomizu.weight.models.DayItem;
import com.example.isseiomizu.weight.models.WeekItem;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View.OnFocusChangeListener;
import android.content.res.Configuration;

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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;



public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Google Sheets API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {SheetsScopes.DRIVE, SheetsScopes.SPREADSHEETS};


    private final int HALF_DAY = 12 * 3600 * 1000;

    private RelativeLayout mRlWeight;


    private Button mBtnImport;
    private Button mBtnExport;
    private TextView mTextView;
    private EditText editWeight;
    private EditText editBodyFatPercentage;

    private int mApiMode = 1;
    private List mListWrite = new ArrayList<>();
    private String mRangeWrite = "Sheet3!A2:B2";
    private String mDate = "";

    private Map<String, List<String>> mapWeight = new HashMap<>();

    private SQLiteDatabase mDbWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // minimum and maximum date of our calendar
        // 2 month behind, one year ahead, example: March 2015 <-> May 2015 <-> May 2016
        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();

        minDate.add(Calendar.MONTH, -2);
        minDate.set(Calendar.DAY_OF_MONTH, 1);
        maxDate.add(Calendar.YEAR, 1);

        //////// This can be done once in another thread
        CalendarManager calendarManager = CalendarManager.getInstance(getApplicationContext());
        calendarManager.buildCal(minDate, maxDate, Locale.getDefault(), new DayItem(), new WeekItem());

        // db
        MyOpenHelper helper = new MyOpenHelper(this);
        this.mDbWeight = helper.getWritableDatabase();

        this.mRlWeight = (RelativeLayout) findViewById(R.id.rlWeight);
        this.mRlWeight.setVisibility(View.VISIBLE);

        this.editWeight = (EditText) findViewById(R.id.editWeight);
        this.editBodyFatPercentage = (EditText) findViewById(R.id.editBodyFatPercentage);

        this.mBtnImport = (Button) findViewById(R.id.btnImport);
        this.mBtnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setImportData();
                importFromApi();
            }
        });

        this.mBtnExport = (Button) findViewById(R.id.btnExport);
        this.mBtnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setExportData();
                importFromApi();
            }
        });

        this.editWeight.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    setWriteDate();
//                    putResultsFromApi();
                }
            }
        });

        this.editBodyFatPercentage.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    setWriteDate();
//                    putResultsFromApi();
                }
            }
        });

        final DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
        datePicker.init(2015, 12, 14, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 日付を選択した時に実行される
                mDate = String.format("%04d%02d%02d", year, monthOfYear + 1, dayOfMonth);

                List test = mapWeight.get(mDate);

                editWeight.setText("");
                editBodyFatPercentage.setText("");


                // sqliteからデータ取得
                Cursor c = mDbWeight.query("weight", new String[]{"date", "weight", "body_fat_percentage"}, "date = ?", new String[]{mDate}, null, null, "date DESC");

                boolean mov = c.moveToFirst();

                String weight = "";
                String bodyFatPercentage = "";
                if (mov) {
                    weight = c.getString(1);
                    bodyFatPercentage = c.getString(2);

                    if (weight != null) {
                        editWeight.setText(weight);
                    }

                    if (bodyFatPercentage != null) {
                        editBodyFatPercentage.setText(bodyFatPercentage);
                    }
                }

                c.close();

            }
        });

        mOutputText = new TextView(this);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Sheets API ...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        String orientation = config.orientation == Configuration.ORIENTATION_LANDSCAPE
                ? "横" : "縦";

//        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            this.mRlWeight.setVisibility(View.INVISIBLE);
//        } else {
//            this.mRlWeight.setVisibility(View.VISIBLE);
//        }

        Toast.makeText(this, "Screenが" + orientation + "になりました",
                Toast.LENGTH_LONG).show();
    }

    public void startSubActivity(View view) {
        Intent intent = new Intent(getApplication(), GraphActivity.class);
        startActivity(intent);
    }

    public void startListCalendarActivity(View view) {
        Intent intent = new Intent(getApplication(), ListCalendarActivity.class);
        startActivity(intent);
    }

    private void setWriteDate() {
        String weight = editWeight.getText().toString();
        String bodyFatPercentage = editBodyFatPercentage.getText().toString();

        if (weight.isEmpty()) {
//            weight = "0";
        }

        if (bodyFatPercentage.isEmpty()) {
//            bodyFatPercentage = "0";
        }

        mApiMode = 2;
        mListWrite = new ArrayList<>();
        List listValue = Arrays.asList(weight, bodyFatPercentage);
        mListWrite.add(listValue);

        // 体重データ更新
        List updateWeight = this.mapWeight.get(mDate);
        List values;
        Integer count = this.mapWeight.size() + 1;
        if (updateWeight != null) {
            values = Arrays.asList(updateWeight.get(0).toString(), weight, bodyFatPercentage);
        } else {
            values = Arrays.asList(count.toString(), weight, bodyFatPercentage);
        }

        this.mapWeight.put(mDate, values);

        // 本当に更新された？確認用
        updateWeight = this.mapWeight.get(mDate);


        // sqliteに保存
        Cursor c = this.mDbWeight.query("weight", new String[] {"date", "weight", "body_fat_percentage"}, "date = ?", new String[]{ mDate }, null, null, "date DESC");

        boolean mov = c.moveToFirst();
        c.close();

        if (mov) {
            if (weight.isEmpty() && bodyFatPercentage.isEmpty()) {
                // 体重・体脂肪率両方とも未入力の場合はレコードを削除する
                mDbWeight.delete( "weight", "date=?", new String[] { mDate });
            } else {
                ContentValues updateValues = new ContentValues();
                updateValues.put("weight", weight);
                updateValues.put("body_fat_percentage", bodyFatPercentage);
                this.mDbWeight.update("weight", updateValues, "date=?", new String[] { mDate });
            }
        } else {
            // 体重・体脂肪率どちらか入力されている場合は追加する
            if (!weight.isEmpty() || !bodyFatPercentage.isEmpty()) {
                ContentValues insertValues = new ContentValues();
                insertValues.put("date", mDate);
                insertValues.put("weight", weight);
                insertValues.put("body_fat_percentage", bodyFatPercentage);
                long id = this.mDbWeight.insert("weight", null, insertValues);
                long confirm = id;
            }
        }

    }

    private void setImportData() {
        mApiMode = 3;
    }

    private void setExportData() {
        mApiMode = 4;
    }

    private void importFromApi() {
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

    private void putResultsFromApi() {
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
     * present. The AfterPermissionGrgetAuthTokenanted annotation indicates that this
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
                List<List<Object>> val = new ArrayList<>();
                switch (mApiMode) {
                    case 1:
                        val = getDataFromApi();
                        break;
                    case 2:
                        val = putDataFromApi();
                        break;
                    case 3:
                        val = importDataFromApi();
                        break;
                    case 4:
                        val = exportDataFromApi();
                        break;
                }
                return val;
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private List<List<Object>> exportDataFromApi() throws IOException {
            // sqliteのデータを全取得
            Cursor c = mDbWeight.query("weight", new String[]{"date", "weight", "body_fat_percentage"}, null, null, null, null, "date DESC");
            boolean mov = c.moveToFirst();


            String spreadsheetId = "1CYOcWrQG7VG9wwPmf2VqI2Xqf-YclI04LiUB8Do_v0Q";
            Integer worksheetId = 1144545091;

            // 列削除テスト！
            BatchUpdateSpreadsheetRequest del = new BatchUpdateSpreadsheetRequest();
            DeleteDimensionRequest deleteDimensionRequest = new DeleteDimensionRequest();

            DimensionRange dimensionRange = new DimensionRange();
            dimensionRange.setSheetId(worksheetId);
            dimensionRange.setDimension("COLUMNS");
            dimensionRange.setStartIndex(0);
            dimensionRange.setEndIndex(3);

            deleteDimensionRequest.setRange(dimensionRange);


            List<Request> requests = new ArrayList<>();

            // 列削除
            requests.add(new Request()
                    .setDeleteDimension(deleteDimensionRequest));

            // 列追加
            requests.add(new Request()
                    .setAppendDimension(new AppendDimensionRequest()
                            .setSheetId(worksheetId)
                            .setDimension("COLUMNS")
                            .setLength(3)));

            del.setRequests(requests);
            BatchUpdateSpreadsheetResponse retDel = this.mService.spreadsheets().batchUpdate(spreadsheetId, del).execute();

            requests.clear();

            // 体重データ全登録
            List<CellData> values;
            Integer count = 1;

            String date = "";
            String weight = "";
            String bodyFatPercentage = "";

            while (mov) {
                date = c.getString(0);
                weight = c.getString(1);
                bodyFatPercentage = c.getString(2);

                /**
                 * 体重・体脂肪率が両方ともデータが設定されていない時はデータを
                 * 登録しない。
                 */
                if (weight != null || bodyFatPercentage != null) {
                    values = new ArrayList<>();

                    // date
                    values.add(new CellData()
                            .setUserEnteredValue(new ExtendedValue()
                                    .setStringValue(date)));

                    values.add(new CellData()
                            .setUserEnteredValue(new ExtendedValue()
                                    .setStringValue(weight)));

                    values.add(new CellData()
                            .setUserEnteredValue(new ExtendedValue()
                                    .setStringValue(bodyFatPercentage)));

                    requests.add(new Request()
                            .setUpdateCells(new UpdateCellsRequest()
                                    .setStart(new GridCoordinate()
                                            .setSheetId(worksheetId)
                                            .setRowIndex(count)
                                            .setColumnIndex(0))
                                    .setRows(Arrays.asList(
                                            new RowData().setValues(values)))
                                    .setFields("userEnteredValue,userEnteredFormat.backgroundColor")));

                    count++;
                }

                // 次のレコードへ
                mov = c.moveToNext();
            }

            c.close();

            del.clear();
            del.setRequests(requests);
            retDel = this.mService.spreadsheets().batchUpdate(spreadsheetId, del).execute();

            return null;
        }


        private List<List<Object>> importDataFromApi() throws IOException {
            String spreadsheetId = "1CYOcWrQG7VG9wwPmf2VqI2Xqf-YclI04LiUB8Do_v0Q";
            int rangeStart = 2;
            String range = "weight!A" + rangeStart + ":C";
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = response.getValues();

            /**
             * 取得したデータをsqliteに書き込み
             */
            // weightテーブルのデータを全削除
            mDbWeight.delete( "weight", null, null);

            // 消えたかどうか確認する（テスト用）
            Cursor c = mDbWeight.query("weight", new String[] {"date", "weight", "body_fat_percentage"}, null, null, null, null, "date DESC");
            boolean mov = c.moveToFirst();
            c.close();

            // 取得したデータをweightテーブルに追加
            if (values != null) {
                ContentValues insertValues = new ContentValues();
                for (List row : values) {
                    insertValues.clear();

                    insertValues.put("date", row.get(0).toString());

                    if (row.get(1) != null) {
                        insertValues.put("weight", row.get(1).toString());
                    }
                    /**
                     * 一番最後の列にデータが存在しない（空）ときはデータが取得されない。
                     * 例外エラーを避けるためにデータサイズで判断する。
                     */
                    if (row.size() == 3) {
                        insertValues.put("body_fat_percentage", row.get(2).toString());
                    }

                    long id = mDbWeight.insert("weight", null, insertValues);
                    long confirm = id;
                }

                // 追加されたかどうか確認する（テスト用）
                c = mDbWeight.query("weight", new String[] {"date", "weight", "body_fat_percentage"}, null, null, null, null, "date DESC");
                mov = c.moveToFirst();
                c.close();
            }

            return values;
        }


        private List<List<Object>> putDataFromApi() throws IOException {
            String spreadsheetId = "1CYOcWrQG7VG9wwPmf2VqI2Xqf-YclI04LiUB8Do_v0Q";
            Integer worksheetId = 1144545091;

            // 列削除テスト！
            BatchUpdateSpreadsheetRequest del = new BatchUpdateSpreadsheetRequest();
            DeleteDimensionRequest deleteDimensionRequest = new DeleteDimensionRequest();

            DimensionRange dimensionRange = new DimensionRange();
            dimensionRange.setSheetId(worksheetId);
            dimensionRange.setDimension("COLUMNS");
            dimensionRange.setStartIndex(0);
            dimensionRange.setEndIndex(3);

            deleteDimensionRequest.setRange(dimensionRange);


            List<Request> requests = new ArrayList<>();

            // 列削除
            requests.add(new Request()
                    .setDeleteDimension(deleteDimensionRequest));

            // 列追加
            requests.add(new Request()
                    .setAppendDimension(new AppendDimensionRequest()
                            .setSheetId(worksheetId)
                            .setDimension("COLUMNS")
                            .setLength(3)));

            del.setRequests(requests);
            BatchUpdateSpreadsheetResponse retDel = this.mService.spreadsheets().batchUpdate(spreadsheetId, del).execute();

            requests.clear();

            // 体重データ全登録
            List<CellData> values;
//            values.add(new CellData()
//                    .setUserEnteredValue(new ExtendedValue()
//                            .setNumberValue(Double.valueOf(1)))
//                    .setUserEnteredFormat(new CellFormat()
//                            .setBackgroundColor(new com.google.api.services.sheets.v4.model.Color()
//                                    .setRed(Float.valueOf(1)))));
//            values.add(new CellData()
//                    .setUserEnteredValue(new ExtendedValue()
//                            .setNumberValue(Double.valueOf(2)))
//                    .setUserEnteredFormat(new CellFormat()
//                            .setBackgroundColor(new com.google.api.services.sheets.v4.model.Color()
//                                    .setBlue(Float.valueOf(1)))));
//            values.add(new CellData()
//                    .setUserEnteredValue(new ExtendedValue()
//                            .setNumberValue(Double.valueOf(3)))
//                    .setUserEnteredFormat(new CellFormat()
//                            .setBackgroundColor(new com.google.api.services.sheets.v4.model.Color()
//                                    .setGreen(Float.valueOf(1)))));

            Integer count = 1;

            for(Map.Entry<String, List<String>> e : mapWeight.entrySet()) {
//                System.out.println(e.getKey() + " : " + e.getValue());
                values = new ArrayList<>();

                values.add(new CellData()
                        .setUserEnteredValue(new ExtendedValue()
                                .setStringValue(e.getKey())));
//                                .setNumberValue(Double.valueOf(1))));
//                    .setUserEnteredFormat(new CellFormat()
//                            .setNumberFormat(new NumberFormat()
//                                    .setType("DATE"))));
//                                    .setType("DATE")
//                                    .setPattern("yyyy-mm-dd"))));

                if (e.getValue().size() > 1) {
                    values.add(new CellData()
                            .setUserEnteredValue(new ExtendedValue()
                                    .setNumberValue(Double.valueOf(e.getValue().get(1)))));
                    values.add(new CellData()
                            .setUserEnteredValue(new ExtendedValue()
                                    .setNumberValue(Double.valueOf(e.getValue().get(2)))));
                }

                requests.add(new Request()
                        .setUpdateCells(new UpdateCellsRequest()
                                .setStart(new GridCoordinate()
                                        .setSheetId(worksheetId)
                                        .setRowIndex(count)
                                        .setColumnIndex(0))
                                .setRows(Arrays.asList(
                                        new RowData().setValues(values)))
                                .setFields("userEnteredValue,userEnteredFormat.backgroundColor")));

                count++;
            }

//            UpdateCellsRequest updateWeightData = new UpdateCellsRequest();
//            updateWeightData.setRows(new RowData().setValues(new CellData().set))
//            requests.add(new Request()
//                    .setUpdateCells()


            del.clear();
            del.setRequests(requests);
            retDel = this.mService.spreadsheets().batchUpdate(spreadsheetId, del).execute();



//            // 書き込みテスト！
//            ValueRange content = new ValueRange();
//            content.setRange(mRangeWrite);
//            content.setValues(mListWrite);
//
//            UpdateValuesResponse ret = this.mService.spreadsheets().values().update(spreadsheetId, mRangeWrite, content)
//                    .setValueInputOption("USER_ENTERED")
//                    .execute();

            return getDataFromApi();
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
            int rangeStart = 2;
//            String range = "数値!A" + rangeStart + ":C";
            String range = "weight!A" + rangeStart + ":C";
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = response.getValues();

            // 取得したデータをMapに展開
            List<String> listData;
            Integer count = rangeStart;
            if (values != null) {
                for (List row : values) {
                    listData = new ArrayList<>();
                    listData.add(count.toString());
                    if (row.size() > 1) {
                        listData.add(row.get(1).toString());
                        listData.add(row.get(2).toString());
                    }
                    mapWeight.put(row.get(0).toString(), listData);
                    count++;
                }
            }


//            // 書き込みテスト！
//            String rangeWrite = "Sheet3!A2:B2";
//            ValueRange content = new ValueRange();
//            content.setRange(rangeWrite);
////            content.setMajorDimension("ROWS");
//
//            List listWrite = new ArrayList<>();
//            List listValue = Arrays.asList("55.5", "7.7");
//
//
////            listValue.add(55.5);
////            listValue.add(7.5);
//
//            listWrite.add(listValue);
//
//
//            content.setValues(listWrite);
//
////            content.set("55.5", "7.7");
//            UpdateValuesResponse ret = this.mService.spreadsheets().values().update(spreadsheetId, rangeWrite, content)
//                    .setValueInputOption("USER_ENTERED")
//                    .execute();

//            return results;
            return values;
//            return null;
        }



        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<List<Object>> output) {
            mProgress.hide();
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
}
