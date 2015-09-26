package com.example.gordonyoon.whentoride;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.gordonyoon.whentoride.models.Products;
import com.example.gordonyoon.whentoride.uberapi.UberApiService;

import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @BindString(R.string.uber_server_token) String mServerToken;

    public static final String mockLatitude = "37.775818";
    public static final String mockLongitude = "-122.418028";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.login_button)
    void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void testUberProductsCall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.uber.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UberApiService service = retrofit.create(UberApiService.class);
        Call<Products> products = service.getProducts(mServerToken, mockLatitude, mockLongitude);
        products.enqueue(new Callback<Products>() {
            @Override
            public void onResponse(Response<Products> response) {
                Log.i(TAG, "Uber Api returned Products!");
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }
}
