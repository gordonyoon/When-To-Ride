package com.example.gordonyoon.whentoride.rx;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;

public class Observables {
    public static rx.Observable<CameraPosition> getCameraChangeObservable(GoogleMap map) {
        return rx.Observable.create(new rx.Observable.OnSubscribe<CameraPosition>() {
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

    public static rx.Observable<String> getReverseGeocoderObservable(CameraPosition cameraPosition, Context context) {
        List<Address> matches = new ArrayList<>();
        try {
            double latitude = cameraPosition.target.latitude;
            double longitude = cameraPosition.target.longitude;

            Geocoder geocoder = new Geocoder(context);
            matches = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rx.Observable.from(matches)
                .map(Observables::getAddressText);
    }

    public static rx.Observable<String> getGeocoderObservable(String query, Context context) {
        List<Address> matches = new ArrayList<>();
        try {
            Geocoder geocoder = new Geocoder(context);
            matches = geocoder.getFromLocationName(query, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rx.Observable.from(matches)
                .map(Observables::getAddressText);
    }

    private static String getAddressText(Address address) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            builder.append(address.getAddressLine(i));

            // don't put a comma at the end
            if (i < address.getMaxAddressLineIndex()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}
