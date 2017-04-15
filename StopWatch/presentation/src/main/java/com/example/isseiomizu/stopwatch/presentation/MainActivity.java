package com.example.isseiomizu.stopwatch.presentation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.isseiomizu.stopwatch.R;

import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


public class MainActivity extends AppCompatActivity {

    MyTimerTask timerTask = null;
    Timer mTimer = null;

    private Disposable disposable;

    @BindView(R.id.view_seconds)
    TextView mViewSeconds;

    @BindView(R.id.view_minutes)
    TextView mViewMinutes;

    @BindView(R.id.editInterval)
    EditText mEditInterval;

    @BindView(R.id.editSeconds)
    EditText mEditSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @OnClick(R.id.btnStart)
    public void start() {
        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



//        disposable = Flowable.interval(1000L, TimeUnit.MILLISECONDS)
//                // スレッド(thread)
//                .observeOn(AndroidSchedulers.mainThread())
//                // 購読(subscribe)
//                .subscribe(aLong -> {
//                    SampleInterface sampleInterface = this::updateTimeView;
//                    sampleInterface.say(aLong);
//                });



        if (mTimer == null) {
            //タイマーの初期化処理
            timerTask = new MyTimerTask(this);
            mTimer = new Timer(true);
            mTimer.schedule(timerTask, 100, 1000);
        }

        if (!mEditInterval.getText().toString().isEmpty()) {
            timerTask.setIntervalTime(Integer.parseInt(mEditInterval.getText().toString()));
        }

        if (!mEditSeconds.getText().toString().isEmpty()) {
            timerTask.setSeconds(Integer.parseInt(mEditSeconds.getText().toString()));
        }

        Toast.makeText(this, "startが押されました",
                Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.btnStop)
    public void stop() {
        // Keep screen off
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }



        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        timerTask.stop();

        Toast.makeText(this, "stopが押されました",
                Toast.LENGTH_LONG).show();
    }

    private void updateTimeView(float data) {
        //現在のLapTime
        mViewSeconds.setText(String.format(Locale.US, "%f", data));
    }

    @FunctionalInterface
    private interface SampleInterface {
        void say(float data);
    }
}
