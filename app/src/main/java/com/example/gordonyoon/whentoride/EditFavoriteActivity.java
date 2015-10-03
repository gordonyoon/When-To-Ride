package com.example.gordonyoon.whentoride;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EditFavoriteActivity extends FragmentActivity {

    private static final String TAG = "EditFavoriteActivity";

    public static final String EXTRA_SAVED_LOCATION = "location";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapsController mController;
    private Location mSavedLocation;

    private RxBus mBus;

    @Bind(R.id.address) TextView mAddress;

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
        mBus.toObserverable().subscribe(event -> {
            if (event instanceof MapsController.ConnectEvent) {
                // only update after client is connected
                updateLocation();
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
        // add MyLocation layer
        mMap.setMyLocationEnabled(true);

        getCameraChangeObservable()
                .debounce(600, TimeUnit.MILLISECONDS)
                .flatMap(this::getGeocoderObservable)
                .subscribe(mAddress::setText);
    }

    private rx.Observable<CameraPosition> getCameraChangeObservable() {
        return rx.Observable.create(new rx.Observable.OnSubscribe<CameraPosition>() {
            @Override
            public void call(final Subscriber<? super CameraPosition> subscriber) {
                mMap.setOnCameraChangeListener(cameraPosition -> {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(cameraPosition);
                    }
                });
            }
        });
    }

    private rx.Observable<String> getGeocoderObservable(CameraPosition cameraPosition) {
        StringBuilder builder = new StringBuilder();
        try {
            double latitude = cameraPosition.target.latitude;
            double longitude = cameraPosition.target.longitude;

            Geocoder geocoder = new Geocoder(EditFavoriteActivity.this);
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

        // run asynchronously
        return rx.Observable.just(builder.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
