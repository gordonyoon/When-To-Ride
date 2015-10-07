package com.example.gordonyoon.whentoride;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.gordonyoon.whentoride.rx.Observables;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.solovyev.android.views.llm.DividerItemDecoration;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MapsSearchActivity extends AppCompatActivity {

    private static final String TAG = "MapsSearchActivity";

    private static final String EXTRA_CURR_LOC = "currentLocation";

    @Bind(R.id.search) EditText mSearch;
    @Bind(R.id.search_results) RecyclerView mRecyclerView;

    CompositeSubscription mSubscriptions = new CompositeSubscription();

    public static void start(Context context, String currentLocation) {
        Intent intent = new Intent(context, MapsSearchActivity.class);
        intent.putExtra(EXTRA_CURR_LOC, currentLocation);
        context.startActivity(intent);
    }

    @OnClick(R.id.search_close_button)
    void clearText() {
        mSearch.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_search);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSearch.setText(extras.getString(EXTRA_CURR_LOC));
            mSearch.selectAll();
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, null));

        mSubscriptions.add(RxTextView.textChanges(mSearch)
                .debounce(200, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap(s -> Observables.getGeocoderObservable(String.valueOf(s), this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(addresses -> mRecyclerView.setAdapter(new SearchAdapter(addresses))));
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.address_text1) TextView mAddress1;
        @Bind(R.id.address_text2) TextView mAddress2;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View v) {

        }
    }

    class SearchAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<Address> mAddresses;

        public SearchAdapter(List<Address> addresses) {
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
            holder.mAddress1.setText(Utils.getAddressText1(mAddresses.get(position)));
            holder.mAddress2.setText(Utils.getAddressText2(mAddresses.get(position)));
        }

        @Override
        public int getItemCount() {
            return mAddresses.size();
        }
    }
}
