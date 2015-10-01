package com.example.gordonyoon.whentoride;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class EditFavoriteActivity extends FragmentActivity {

    private static final String TAG = "EditFavoriteActivity";

    public static final String EXTRA_SAVED_LOCATION = "location";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapsController mController;
    private Location mSavedLocation;

    private RxBus mBus;

    public static void start(Context context) {
        Intent intent = new Intent(context, EditFavoriteActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, Location savedLocation) {
        Intent intent = new Intent(context, EditFavoriteActivity.class);
        intent.putExtra(EXTRA_SAVED_LOCATION, savedLocation);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_favorite);
        ButterKnife.bind(this);

        if (getIntent().getExtras() != null) {
            mSavedLocation = (Location)getIntent().getExtras().get(EXTRA_SAVED_LOCATION);
        }

        mBus = new RxBus();
        mBus.toObserverable().subscribe(new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof MapsController.ConnectEvent) {
                    // only update after client is connected
                    updateLocation();
                }
            }
        });

        mController = new MapsController(this, mBus, mSavedLocation);

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mController.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mController.isConnected()) {
            mController.disconnect();
        }
    }

    private void updateLocation() {
        if (mSavedLocation == null) {
            Location l = mController.getLastLocation();
            if (l != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()), 15));
            } else {
                Toast.makeText(this, "No location available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);

        getCameraChangeObservable()
                .debounce(2000, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<CameraPosition, Observable<String>>() {
                    @Override
                    public Observable<String> call(CameraPosition cameraPosition) {
                        return getGeocoderObservable(cameraPosition);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.i(TAG, "Address: " + s);
                    }
                });
    }

    private rx.Observable<CameraPosition> getCameraChangeObservable() {
        return rx.Observable.create(new rx.Observable.OnSubscribe<CameraPosition>() {
            @Override
            public void call(final Subscriber<? super CameraPosition> subscriber) {
                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(cameraPosition);
                        }
                    }
                });
            }
        });
    }

    private rx.Observable<String> getGeocoderObservable(CameraPosition cameraPosition) {
        try {
            double latitude = cameraPosition.target.latitude;
            double longitude = cameraPosition.target.longitude;

            Geocoder geocoder = new Geocoder(EditFavoriteActivity.this);
            List<Address> matches = geocoder.getFromLocation(latitude, longitude, 1);
            if (!matches.isEmpty()) {
                // create a human readable address
                Address bestMatch = matches.get(0);
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i <= bestMatch.getMaxAddressLineIndex(); i++) {
                    builder.append(bestMatch.getAddressLine(i));

                    // do not put a comma at the end
                    if (i < bestMatch.getMaxAddressLineIndex()) {
                        builder.append(", ");
                    }
                }

                // run asynchronously
                return rx.Observable.just(builder.toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rx.Observable.just(null);
    }
}
