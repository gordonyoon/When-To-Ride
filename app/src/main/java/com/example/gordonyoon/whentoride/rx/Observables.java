package com.example.gordonyoon.whentoride.rx;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.example.gordonyoon.whentoride.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

public class Observables {
    public static Observable<CameraPosition> getCameraChangeObservable(GoogleMap map) {
        return Observable.create(new Observable.OnSubscribe<CameraPosition>() {
            @Override
            public void call(final Subscriber<? super CameraPosition> subscriber) {
                map.setOnCameraChangeListener(cameraPosition -> {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(cameraPosition);
                    }
                });
            }
        });
    }

    public static Observable<String> getReverseGeocoderObservable(CameraPosition cameraPosition, Context context) {
        List<Address> matches = new ArrayList<>();
        try {
            double latitude = cameraPosition.target.latitude;
            double longitude = cameraPosition.target.longitude;

            Geocoder geocoder = new Geocoder(context);
            matches = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Observable.from(matches)
                .map(Utils::getAddressText);
    }

    public static Observable<List<Address>> getGeocoderObservable(String query, Context context) {
        List<Address> matches = new ArrayList<>();
        try {
            Geocoder geocoder = new Geocoder(context);
            matches = geocoder.getFromLocationName(query, 20);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Observable.from(matches)
                .filter(address -> address.getMaxAddressLineIndex() == 2)
                .take(10)
                .toList();
    }
}
