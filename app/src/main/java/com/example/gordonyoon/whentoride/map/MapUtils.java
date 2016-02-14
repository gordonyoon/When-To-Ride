package com.example.gordonyoon.whentoride.map;


import android.content.Context;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MapUtils {

    public static String getAddressText(Address address) {
        return getAddressText1(address) + ", " + getAddressText2(address);
    }

    public static String getAddressText1(Address address) {
        return address.getAddressLine(0);
    }

    public static String getAddressText2(Address address) {
        return address.getAddressLine(1) + ", " + address.getAddressLine(2);
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
