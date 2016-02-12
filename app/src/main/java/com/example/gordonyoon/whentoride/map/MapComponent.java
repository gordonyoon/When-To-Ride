package com.example.gordonyoon.whentoride.map;


import android.support.annotation.NonNull;

import com.example.gordonyoon.whentoride.login.LoginActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = MapModule.class)
public interface MapComponent {

    void inject(@NonNull EditFavoriteActivity editFavoriteActivity);

    void inject(@NonNull MapsController mapsController);
}