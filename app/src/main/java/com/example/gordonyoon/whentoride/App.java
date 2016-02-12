package com.example.gordonyoon.whentoride;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.example.gordonyoon.whentoride.map.DaggerMapComponent;
import com.example.gordonyoon.whentoride.map.MapComponent;
import com.example.gordonyoon.whentoride.map.MapModule;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class App extends Application {

    private AppComponent appComponent;
    private MapComponent mapComponent;

    @NonNull
    public static App get(@NonNull Context context) {
        return (App)context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        appComponent = createAppComponent();
        mapComponent = createMapComponent();

        // setup Realm in the application
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);

        // enable inspection of Realm via Stetho
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());
    }

    @NonNull
    private AppComponent createAppComponent() {
        return DaggerAppComponent
                .builder()
                .appModule(new AppModule())
                .build();
    }

    @NonNull
    private MapComponent createMapComponent() {
        return DaggerMapComponent
                .builder()
                .mapModule(new MapModule())
                .build();
    }

    @NonNull
    public AppComponent appComponent() {
        return appComponent;
    }

    @NonNull
    public MapComponent mapComponent() { return mapComponent; }
}
