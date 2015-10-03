package com.example.gordonyoon.whentoride;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.okhttp.OkHttpClient;


public class App extends Application {

    public static OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        okHttpClient.networkInterceptors().add(new StethoInterceptor());

        LeakCanary.install(this);
    }
}
