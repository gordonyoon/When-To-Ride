package com.example.gordonyoon.whentoride.map;


import android.location.Address;

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
}
