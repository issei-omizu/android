package com.example.isseiomizu.stopwatch.presentation.view.activity;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.isseiomizu.stopwatch.databinding.ActivityMainBinding;
import com.example.isseiomizu.stopwatch.presentation.viewmodel.basics.BasicsViewModel;
import com.example.isseiomizu.stopwatch.presentation.viewmodel.basics.JavaBasicsViewModel;
import com.example.isseiomizu.stopwatch.R;

import butterknife.BindView;
import io.reactivex.disposables.Disposable;


public class MainActivity extends AppCompatActivity {

//    MyTimerTask timerTask = null;
//    Timer mTimer = null;

    private ActivityMainBinding mBinding;

    private Disposable mDisposable;

    @BindView(R.id.view_seconds)
    TextView mViewSeconds;

    @BindView(R.id.view_minutes)
    TextView mViewMinutes;

    @BindView(R.id.editInterval)
    EditText mEditInterval;

    @BindView(R.id.editSeconds)
    EditText mEditSeconds;

    private BasicsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        ButterKnife.bind(this);

        viewModel = new JavaBasicsViewModel();
        // viewModel = new KotlinBasicsViewModel();

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setViewModel(viewModel);
        mBinding.setHandlers(new MainActivityHandlers());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    public interface BaseHandlers {
        void onClick(View view);
    }

    @FunctionalInterface
    private interface SampleInterface {
        void say(float data);
    }

    // Event
    public class MainActivityHandlers implements BaseHandlers {
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.btnStart:
                    onStartButtonClick(view);
                    break;
                case R.id.btnStop:
                    onStopButtonClick(view);
                    break;
            }
        }

        public void onStartButtonClick(View view) {
            // Keep screen on
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



//            mDisposable = Flowable.interval(1000L, TimeUnit.MILLISECONDS)
//                    // スレッド(thread)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    // 購読(subscribe)
//                    .subscribe(aLong -> {
////                        SampleInterface sampleInterface = this::updateTimeView;
////                        sampleInterface.say(aLong);
//                    });



//        if (mTimer == null) {
//            //タイマーの初期化処理
//            timerTask = new MyTimerTask(this);
//            mTimer = new Timer(true);
//            mTimer.schedule(timerTask, 100, 1000);
//        }
//
//        if (!mEditInterval.getText().toString().isEmpty()) {
//            timerTask.setIntervalTime(Integer.parseInt(mEditInterval.getText().toString()));
//        }
//
//        if (!mEditSeconds.getText().toString().isEmpty()) {
//            timerTask.setSeconds(Integer.parseInt(mEditSeconds.getText().toString()));
//        }

//            Toast.makeText(this, "startが押されました",
//                    Toast.LENGTH_LONG).show();
        }

        public void onStopButtonClick(View view) {
            // Keep screen off
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



            if (mDisposable != null && !mDisposable.isDisposed()) {
                mDisposable.dispose();
            }



//        if (mTimer != null) {
//            mTimer.cancel();
//            mTimer = null;
//        }
//
//        timerTask.stop();

//            Toast.makeText(this, "stopが押されました",
//                    Toast.LENGTH_LONG).show();
        }

//        private void updateTimeView(float data) {
//            //現在のLapTime
//            mViewSeconds.setText(String.format(Locale.US, "%f", data));
//        }
    }
}
