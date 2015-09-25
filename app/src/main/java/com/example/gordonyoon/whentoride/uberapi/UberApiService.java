package com.example.gordonyoon.whentoride.uberapi;

import com.example.gordonyoon.whentoride.models.Products;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface UberApiService {

    @GET("/v1/products")
    public Call<Products> getProducts(@Query("server_token") String serverToken,
                                @Query("latitude") String latitude,
                                @Query("longitude") String longitude);
}
