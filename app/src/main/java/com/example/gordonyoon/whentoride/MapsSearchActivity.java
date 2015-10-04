package com.example.gordonyoon.whentoride;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.gordonyoon.whentoride.rx.Observables;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MapsSearchActivity extends AppCompatActivity {

    private static final String TAG = "MapsSearchActivity";

    @Bind(R.id.search) TextView mSearch;
    @Bind(R.id.search_results) RecyclerView mRecyclerView;

    CompositeSubscription mSubscriptions = new CompositeSubscription();

    public static void start(Context context) {
        Intent intent = new Intent(context, MapsSearchActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_search);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mSubscriptions.add(RxTextView.textChanges(mSearch)
                .debounce(200, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap(s -> Observables.getGeocoderObservable(String.valueOf(s), this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(addresses -> mRecyclerView.setAdapter(new SearchAdapter(addresses))));
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.address_text) TextView mAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View v) {

        }
    }

    class SearchAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<String> mAddresses;

        public SearchAdapter(List<String> addresses) {
            mAddresses = addresses;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_address, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(vh);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder.mAddress != null) {
                holder.mAddress.setText(mAddresses.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mAddresses.size();
        }
    }
}
