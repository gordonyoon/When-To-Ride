package com.example.gordonyoon.whentoride;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.gordonyoon.whentoride.models.Products;
import com.example.gordonyoon.whentoride.uberapi.UberApiService;

import butterknife.BindString;
import butterknife.ButterKnife;
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

        testUberProductsCall();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
