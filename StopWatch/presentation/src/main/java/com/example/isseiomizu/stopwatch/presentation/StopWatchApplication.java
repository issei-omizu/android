package com.example.isseiomizu.stopwatch.presentation;

import android.app.Application;

import com.example.isseiomizu.stopwatch.presentation.internal.di.components.ApplicationComponent;
import com.example.isseiomizu.stopwatch.presentation.internal.di.components.DaggerApplicationComponent;
import com.example.isseiomizu.stopwatch.presentation.internal.di.modules.ApplicationModule;

/**
 * Created by isseiomizu on 2017/03/31.
 */

public class StopWatchApplication extends Application {

    private ApplicationComponent applicationComponent;

    @Override public void onCreate() {
        super.onCreate();
        injectApplicationComponent();
    }

    private void injectApplicationComponent() {
        this.applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return this.applicationComponent;
    }

}
