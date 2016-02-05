package com.example.gordonyoon.whentoride;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.squareup.leakcanary.LeakCanary;


public class App extends Application {

    private AppComponent appComponent;

    @NonNull
    public static App get(@NonNull Context context) {
        return (App)context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        appComponent = createAppComponent();
    }

    @NonNull
    private AppComponent createAppComponent() {
        return DaggerAppComponent
                .builder()
                .appModule(new AppModule(this))
                .build();
    }

    @NonNull
    public AppComponent appComponent() {
        return appComponent;
    }
}
