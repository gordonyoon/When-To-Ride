package com.example.gordonyoon.whentoride.rx;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import com.example.gordonyoon.whentoride.R;
import com.example.gordonyoon.whentoride.map.MapUtils;
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

    public static Observable<String> getReverseGeocoderObservable(Context context, CameraPosition cameraPosition) {
        if (!MapUtils.hasInternetConnection(context)) return Observable.just("");

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
                .map(MapUtils::getAddressText);
    }

    public static Observable<List<Address>> getGeocoderObservable(Context context, String query) {
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

    public static Observable<Address> getAddressObservable(Context context, String query) {
        Address address = null;
        try {
            Geocoder geocoder = new Geocoder(context);
            List<Address> matches = geocoder.getFromLocationName(query, 1);
            address = matches.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Observable.just(address);
    }
}
