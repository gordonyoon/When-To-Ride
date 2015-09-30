package com.example.gordonyoon.whentoride;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import butterknife.ButterKnife;
import rx.functions.Action1;

public class EditFavoriteActivity extends FragmentActivity {

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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
    }
}
