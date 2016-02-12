package com.example.gordonyoon.whentoride;


import android.support.annotation.NonNull;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Provides
    @NonNull
    @Singleton
    OkHttpClient provideOkHttpClient() {
        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(new StethoInterceptor());
        return client;
    }
}