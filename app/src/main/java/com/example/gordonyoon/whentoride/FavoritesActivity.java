package com.example.gordonyoon.whentoride;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gordonyoon.whentoride.map.EditFavoriteActivity;
import com.example.gordonyoon.whentoride.models.Favorite;

import org.solovyev.android.views.llm.LinearLayoutManager;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class FavoritesActivity extends AppCompatActivity {

    private static final String TAG = "FavoritesActivity";

    Realm mRealm;

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.favorites) RecyclerView mRecyclerView;
    @BindBool(R.bool.orientation_landscape) boolean mIsLandscape;
    @BindString(R.string.uber_client_id) String mClientId;

    @OnClick(R.id.add_fab)
    void addNewFavorite() {
        EditFavoriteActivity.start(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        ButterKnife.bind(this);

        mToolbar.setTitle(getString(R.string.app_name));

        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(() -> mRecyclerView.getAdapter().notifyDataSetChanged());

        if (mIsLandscape) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        populateFavorites();
    }

    private void populateFavorites() {
        RealmResults<Favorite> favorites = mRealm
                .where(Favorite.class)
                .findAll();
        mRecyclerView.setAdapter(new FavoritesAdapter(favorites));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.favorite_address) TextView mAddress;
        @Bind(R.id.favorite_map) ImageView mMap;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext().getApplicationContext();
            callUber(context, mAddress.getText().toString());
        }

        private void callUber(Context context, String address) {
            // open the Uber application
            String uri;
            try {
                PackageManager pm = context.getPackageManager();
                pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);

                Favorite favorite = mRealm
                        .where(Favorite.class)
                        .equalTo("address", address)
                        .findFirst();
                String latitude = String.valueOf(favorite.getLatitude());
                String longitude = String.valueOf(favorite.getLongitude());

                // call an Uber and open the application
                uri = new Uri.Builder()
                        .scheme("uber")
                        .authority("")
                        .appendQueryParameter("client_id", mClientId)
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
                        .appendQueryParameter("client_id", mClientId)
                        .build()
                        .toString();
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        }

        @OnClick(R.id.delete_favorite)
        void deleteFavorite() {
            mRealm.beginTransaction();
            RealmResults<Favorite> result = mRealm.where(Favorite.class)
                    .equalTo("address", mAddress.getText().toString())
                    .findAll();
            result.remove(0);
            mRealm.commitTransaction();
        }
    }

    class FavoritesAdapter extends RecyclerView.Adapter<ViewHolder> {
        private RealmResults<Favorite> mFavorites;

        public FavoritesAdapter(RealmResults<Favorite> favorites) {
            mFavorites = favorites;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_favorite, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(vh);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mAddress.setText(mFavorites.get(position).getAddress());

            // convert stored image from byte array to bitmap
            byte[] byteArray = mFavorites.get(position).getMap();
            Bitmap map = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            holder.mMap.setImageBitmap(map);
        }

        @Override
        public int getItemCount() {
            return mFavorites.size();
        }
    }
}
