package com.example.gordonyoon.whentoride;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gordonyoon.whentoride.map.EditFavoriteActivity;
import com.example.gordonyoon.whentoride.models.Favorite;
import com.example.gordonyoon.whentoride.models.User;

import org.solovyev.android.views.llm.LinearLayoutManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class FavoritesActivity extends AppCompatActivity {

    public static final String EXTRA_USER = "user";

    private User mUser;
    Realm mRealm;

    @Bind(R.id.favorites) RecyclerView mRecyclerView;

    public static void start(Context context, User user) {
        Intent intent = new Intent(context, FavoritesActivity.class);
        intent.putExtra(EXTRA_USER, user);
        context.startActivity(intent);
    }

    @OnClick(R.id.add_fab)
    void addNewFavorite() {
        EditFavoriteActivity.start(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        ButterKnife.bind(this);

        mRealm = Realm.getDefaultInstance();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // for logging in with Uber
        if (getIntent().getExtras() != null) {
            mUser = getIntent().getExtras().getParcelable(EXTRA_USER);
        }

        populateAdapterWithFavorites();
    }

    private void populateAdapterWithFavorites() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.favorite_address) TextView mAddress;
        @Bind(R.id.favorite_map) ImageView mMap;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View v) {
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
