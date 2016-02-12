package com.example.gordonyoon.whentoride.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gordonyoon.whentoride.App;
import com.example.gordonyoon.whentoride.R;
import com.example.gordonyoon.whentoride.models.Favorite;
import com.example.gordonyoon.whentoride.rx.Observables;
import com.example.gordonyoon.whentoride.rx.RxBus;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EditFavoriteActivity extends FragmentActivity {

    private static final String TAG = "EditFavoriteActivity";

    public static final String EXTRA_SELECTED_LOCATION = "selectedLocation";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapsController mController;

    @Inject
    RxBus mBus;
    private CompositeSubscription mSubscriptions = new CompositeSubscription();

    private Realm mRealm;

    // used to save a Favorite
    private double mCurrentLatitude;
    private double mCurrentLongitude;

    @Bind(R.id.current_address) TextView mAddress;

    public static void start(Context context) {
        Intent intent = new Intent(context, EditFavoriteActivity.class);
        context.startActivity(intent);
    }

    @OnClick(R.id.current_address)
    void onAddressClick() {
        MapsSearchActivity.startForResult(this, mAddress.getText().toString());
    }

    @OnClick(R.id.save)
    void onSaveClick() {
        String address = mAddress.getText().toString();
        RealmResults<Favorite> results = mRealm
                .where(Favorite.class)
                .equalTo("address", address)
                .findAll();
        if (results.size() == 0) {
            saveFavorite(address);
        }
        finish();
    }

    private void saveFavorite(String address) {
        GoogleMap.SnapshotReadyCallback callback = bitmap -> {
            // save current map as image
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            // save the favorite into Realm
            mRealm.beginTransaction();
            Favorite favorite = mRealm.createObject(Favorite.class);
            favorite.setAddress(address);
            favorite.setMap(byteArray);
            favorite.setLatitude(mCurrentLatitude);
            favorite.setLongitude(mCurrentLongitude);

            mRealm.commitTransaction();
        };
        mMap.snapshot(callback);
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
        App.get(this).mapComponent().inject(this);
        ButterKnife.bind(this);

//        mBus = new RxBus();
        mRealm = Realm.getDefaultInstance();
        mController = new MapsController(this);
        setUpMapIfNeeded();

        mSubscriptions.add(mBus.toObserverable().subscribe(event -> {
            if (event instanceof MapsController.ConnectEvent) {
                // only update after client is connected
                updateLocation(mController.getLastLocation());
            }
        }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
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

    @Nullable
    private void updateLocation(Location location) {
        if (location != null) {
            updateLocation(location.getLatitude(), location.getLongitude());
        } else {
            Toast.makeText(this, "No location available", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpMap() {
        // add MyLocation layer
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
        mCurrentLatitude = latitude;
        mCurrentLongitude = longitude;
    }
}
