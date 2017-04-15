package com.example.isseiomizu.stopwatch.presentation;

import android.content.Context;
import android.media.Ringtone;
import android.os.Handler;

import com.example.isseiomizu.stopwatch.presentation.controller.SoundController;

import java.math.BigDecimal;
import java.util.TimerTask;

/**
 * Created by isseiomizu on 2017/03/21.
 */

public class MyTimerTask extends TimerTask {
    private static final int NOTIFICATION_INTERVAL_SECONDS = 20;

    private Handler mHandler = new Handler();   // UI Threadへのpost用ハンドラ
    private Ringtone mRingtone = null;
    private SoundController mSoundController = null;

    private float mLapTime = 0.0f;
    private int mMinutes = 0;
    private int mIntervalTime = 0;
    private int mSeconds = 0;


    public int getIntervalTime() {
        return mIntervalTime;
    }

    public void setIntervalTime(int mIntervalTime) {
        this.mIntervalTime = mIntervalTime;
    }

    public int getSeconds() {
        return mSeconds;
    }

    public void setSeconds(int mSeconds) {
        this.mSeconds = mSeconds;
    }

    MyTimerTask(Context context) {
        mLapTime = 0.0f;
        mSoundController = new SoundController(context);
    }

    @Override
    public void run() {
        // mHandlerを通じてUI Threadへ処理をキューイング
        mHandler.post(new Runnable() {
            public void run() {
                if (isStartTime()) {
                    mSoundController.playStartSound();
                }

                //実行間隔分を加算処理
                addLapTime();
                updateViewSeconds();

                if (isNotificationIntervalTime()) {
                    mSoundController.playNotificationIntervalSound();
                }

                // secondsで設定した時間がすぎたら音を鳴らす
                if (isPastSeconds()) {
                    mSoundController.playPastSecondsSound();
                    mLapTime = 0;
                }
            }
        });
    }

    public void stop() {
        if (mRingtone != null) {
            // 停止
            mSoundController.forceStop();
        }

        mLapTime = 0;
        mMinutes = 0;

        //現在のLapTime
//        mViewSeconds.setText("0");
//        mViewMinutes.setText("0");
    }

    private void addLapTime() {
        //実行間隔分を加算処理
        mLapTime += 1.0d;
    }

    private void updateViewSeconds () {
        //計算にゆらぎがあるので小数点第1位で丸める
        BigDecimal bi = new BigDecimal(mLapTime);
        float outputValue = bi.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();

        //現在のLapTime
//        mViewSeconds.setText(Float.toString(outputValue));
    }

    private boolean isNotificationIntervalTime() {
        boolean result = false;
        float time = mLapTime;

        if (time > 0) {
            if (time % NOTIFICATION_INTERVAL_SECONDS == 0) {
                result = true;
            }
        }

        return result;
    }

    private boolean isStartTime() {
        boolean result = false;
        float time = mLapTime;

        if (time == 0) {
            result = true;
        }

        return result;
    }

    private boolean isPastSeconds() {
        boolean result = false;
        float time = mLapTime;
        int seconds = mSeconds;

        if (time > 0) {
            if (time % seconds == 0) {
                result = true;
            }
        }

        return result;
    }
}
