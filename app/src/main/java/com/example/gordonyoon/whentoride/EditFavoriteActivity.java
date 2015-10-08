package com.example.gordonyoon.whentoride;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gordonyoon.whentoride.rx.Observables;
import com.example.gordonyoon.whentoride.rx.RxBus;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EditFavoriteActivity extends FragmentActivity {

    private static final String TAG = "EditFavoriteActivity";

    public static final String EXTRA_SELECTED_LOCATION = "selectedLocation";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapsController mController;

    private RxBus mBus;
    private CompositeSubscription mSubscriptions = new CompositeSubscription();

    @Bind(R.id.current_address) TextView mAddress;

    public static void start(Context context) {
        Intent intent = new Intent(context, EditFavoriteActivity.class);
        context.startActivity(intent);
    }

    @OnClick(R.id.current_address)
    void onAddressClick() {
        MapsSearchActivity.startForResult(this, mAddress.getText().toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                String query = extras.getString(EXTRA_SELECTED_LOCATION);
                Observables.getAddressObservable(this, query)
                        .subscribe(address -> {
                            mBus.toObserverable().subscribe(event -> {
                                if (event instanceof MapsController.ConnectEvent) {
                                    updateLocation(address.getLatitude(), address.getLongitude());
                                }
                            });
                        });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_favorite);
        ButterKnife.bind(this);

        mBus = new RxBus();

        mSubscriptions.add(mBus.toObserverable().subscribe(event -> {
            if (event instanceof MapsController.ConnectEvent) {
                // only update after client is connected
                updateLocation(mController.getLastLocation());
            }
        }));

        mController = new MapsController(this, mBus);

        setUpMapIfNeeded();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
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

    @Nullable
    private void updateLocation(Location location) {
        if (location != null) {
            updateLocation(location.getLatitude(), location.getLongitude());
        } else {
            Toast.makeText(this, "No location available", Toast.LENGTH_SHORT).show();
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

        mSubscriptions.add(Observables.getCameraChangeObservable(mMap)
                .subscribeOn(AndroidSchedulers.mainThread())
                .debounce(600, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .flatMap(cameraPosition -> Observables.getReverseGeocoderObservable(this, cameraPosition))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mAddress::setText));
    }

    private void updateLocation(double latitude, double longitude) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
    }
}
