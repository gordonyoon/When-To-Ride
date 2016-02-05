package com.example.gordonyoon.whentoride;


import android.support.annotation.NonNull;

import com.example.gordonyoon.whentoride.login.LoginActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

    void inject(@NonNull LoginActivity loginActivity);
}
