package com.example.gordonyoon.whentoride.map;


import android.support.annotation.NonNull;

import com.example.gordonyoon.whentoride.rx.RxBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MapModule {

    @Provides
    @NonNull
    @Singleton
    RxBus provideRxBus() {
        return new RxBus();
    }
}
