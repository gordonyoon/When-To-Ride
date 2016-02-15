package com.example.gordonyoon.whentoride;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import butterknife.BindString;

public class Utils {

    public static Intent getUberIntent(Context context, String clientId, String latitude, String longitude, String address) {
        // open the Uber application
        String uri;
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);

            // call an Uber and open the application
            uri = new Uri.Builder()
                    .scheme("uber")
                    .authority("")
                    .appendQueryParameter("client_id", clientId)
                    .appendQueryParameter("action", "setPickup")
                    .appendQueryParameter("pickup", "my_location")
                    .appendQueryParameter("dropoff[latitude]", latitude)
                    .appendQueryParameter("dropoff[longitude]", longitude)
                    .appendQueryParameter("dropoff[formatted_address]", address)
                    .appendQueryParameter("product_id", "a1111c8c-c720-46c3-8534-2fcdd730040d")
                    .build()
                    .toString();
        } catch (PackageManager.NameNotFoundException e) {
            // No Uber app! Open mobile website.
            uri = new Uri.Builder()
                    .scheme("https")
                    .authority("m.uber.com")
                    .path("sign-up")
                    .appendQueryParameter("client_id", clientId)
                    .build()
                    .toString();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));
        return intent;
    }
}
