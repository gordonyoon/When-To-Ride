package com.example.gordonyoon.whentoride;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MapsController implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mNewLocation;
    private RxBus mBus;


    public MapsController(Context context, RxBus bus, @Nullable Location savedLocation) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mBus = bus;

        if (savedLocation != null) {
            mNewLocation = savedLocation;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mNewLocation == null) {
            // if we are creating a new favorite, start at the current location
            mNewLocation = getLastLocation();
        }
        mBus.send(new ConnectEvent());
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        mGoogleApiClient.connect();
    }

    @Nullable
    public Location getLastLocation() {
        return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        mGoogleApiClient.disconnect();
    }

    public class ConnectEvent {}
}
