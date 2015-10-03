package com.example.gordonyoon.whentoride.rx;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

import java.io.IOException;
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

    public static rx.Observable<String> getGeocoderObservable(CameraPosition cameraPosition, Context context) {
        StringBuilder builder = new StringBuilder();
        try {
            double latitude = cameraPosition.target.latitude;
            double longitude = cameraPosition.target.longitude;

            Geocoder geocoder = new Geocoder(context);
            List<Address> matches = geocoder.getFromLocation(latitude, longitude, 1);
            if (!matches.isEmpty()) {
                // create a human readable address
                Address bestMatch = matches.get(0);
                for (int i = 0; i <= bestMatch.getMaxAddressLineIndex(); i++) {
                    builder.append(bestMatch.getAddressLine(i));

                    // do not put a comma at the end
                    if (i < bestMatch.getMaxAddressLineIndex()) {
                        builder.append(", ");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rx.Observable.just(builder.toString());
    }
}
