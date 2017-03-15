package com.example.isseiomizu.stopwatch;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // mHandlerを通じてUI Threadへ処理をキューイング
            mHandler.post(new Runnable() {
                public void run() {
                    //実行間隔分を加算処理
                    mLapTime += 1.0d;

                    if (mTimeUp) {
                        if (isRingtone(mLapTime)) {
                            // 停止
                            mRingtone.stop();
                        }
                        if (isInterval(mLapTime)) {
                            mTimeUp = false;
                            mLapTime = 0;
                        }
                        return;
                    }

                    //計算にゆらぎがあるので小数点第1位で丸める
                    BigDecimal bi = new BigDecimal(mLapTime);
                    float outputValue = bi.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();

                    //現在のLapTime
                    mViewSeconds.setText(Float.toString(outputValue));

                    // secondsで設定した時間がすぎたら音を鳴らす
                    if (isTimeUp(mLapTime)) {
                        mIntervalCounter++;
                        mTimeUp = true;

                        // 再生
                        mRingtone.play();
                    }
                }
            });
        }

        private boolean isInterval(float time) {
            boolean result = false;

            int seconds = mSeconds;
            int interval = mIntervalTime;
            if (time > 0) {
                if (time % seconds == interval) {
                    result = true;
                }
            }

            return result;
        }

        private boolean isTimeUp(float time) {
            boolean result = false;

            int seconds = mSeconds;
            if (time > 0) {
                if (time % seconds == 0) {
                    result = true;
                }
            }

            return result;
        }

        private boolean isRingtone(float time) {
            boolean result = false;

            int interval = mSeconds;
            if (time > 0) {
                if (time % interval == 5) {
                    result = true;
                }
            }

            return result;
        }
    }

    MyTimerTask timerTask = null;       // onClickメソッドでインスタンス生成
    Timer mTimer = null;                //onClickメソッドでインスタンス生成
    Handler mHandler = new Handler();   //UI Threadへのpost用ハンドラ
    float mLapTime = 0.0f;
    int mIntervalCounter = 0;
    int mIntervalTime = 0;
    int mSeconds = 0;
    boolean mTimeUp = false;

    Ringtone mRingtone = null;

    @BindView(R.id.view_seconds)
    TextView mViewSeconds;

    @BindView(R.id.editInterval)
    EditText mEditInterval;

    @BindView(R.id.editSeconds)
    EditText mEditSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mRingtone = RingtoneManager.getRingtone(this, uri);
    }

    @OnClick(R.id.btnStart)
    public void start() {
        if (mTimer == null) {
            //タイマーの初期化処理
            timerTask = new MyTimerTask();
            mLapTime = 0.0f;
            mTimer = new Timer(true);
            mTimer.schedule(timerTask, 100, 1000);
        }

        if (!mEditInterval.getText().toString().isEmpty()) {
            mIntervalTime = Integer.parseInt(mEditInterval.getText().toString());
        }

        if (!mEditSeconds.getText().toString().isEmpty()) {
            mSeconds = Integer.parseInt(mEditSeconds.getText().toString());
        }

        Toast.makeText(this, "startが押されました",
                Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.btnStop)
    public void stop() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        //現在のLapTime
        mViewSeconds.setText("0");
        mIntervalCounter = 0;

        Toast.makeText(this, "stopが押されました",
                Toast.LENGTH_LONG).show();
    }
}
