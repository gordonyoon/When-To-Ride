package com.example.gordonyoon.whentoride.uberapi;


import android.content.Context;
import android.net.Uri;

import com.example.gordonyoon.whentoride.App;
import com.example.gordonyoon.whentoride.models.User;
import com.squareup.okhttp.OkHttpClient;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.POST;
import retrofit.http.Query;

public class UberAuthTokenClient {

    public static UberAuthTokenService getUberAuthTokenClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://login.uber.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(App.okHttpClient)
                .build();

        return retrofit.create(UberAuthTokenService.class);
    }

    public interface UberAuthTokenService {

        @POST("/oauth/token")
        Call<User> getAuthToken(@Query("client_secret") String clientSecret,
                          @Query("client_id") String clientId,
                          @Query("grant_type") String grantType,
                          @Query("code") String code,
                          @Query(value="redirect_uri", encoded = true) String redirectUrl);
    }
}
