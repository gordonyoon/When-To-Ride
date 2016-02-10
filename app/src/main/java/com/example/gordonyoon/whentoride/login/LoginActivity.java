package com.example.gordonyoon.whentoride.login;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.gordonyoon.whentoride.App;
import com.example.gordonyoon.whentoride.FavoritesActivity;
import com.example.gordonyoon.whentoride.R;
import com.example.gordonyoon.whentoride.models.User;
import com.example.gordonyoon.whentoride.uberapi.UberAuthTokenClient;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    @Inject OkHttpClient okHttpClient;

    @Bind(R.id.web_view) WebView mWebView;
    @BindString(R.string.uber_client_id) String mClientId;
    @BindString(R.string.uber_client_secret) String mClientSecret;
    @BindString(R.string.uber_redirect_uri) String mRedirectUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        App.get(this).appComponent().inject(this);
        ButterKnife.bind(this);

        mWebView.setWebViewClient(new UberWebViewClient());
        mWebView.loadUrl(buildUrl().toString());
    }

    private HttpUrl buildUrl() {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("login.uber.com")
                .addPathSegment("oauth")
                .addPathSegment("v2")
                .addPathSegment("authorize")
                .addQueryParameter("response_type", "code")
                .addQueryParameter("client_id", mClientId)
                .addEncodedQueryParameter("redirect_uri", Uri.encode(mRedirectUri))
                .build();
    }

    private class UberWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(mRedirectUri)) {
                getAuthTokenObservable(HttpUrl.parse(url).queryParameter("code"))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<User>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "onCompleted ");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "onError ");
                            }

                            @Override
                            public void onNext(User user) {
                                Log.d(TAG, "onNext ");
                                FavoritesActivity.start(LoginActivity.this, user);
                            }
                        });
                return true;
            }
            return false;
        }

        private Observable<User> getAuthTokenObservable(String authorizationCode) {
            return UberAuthTokenClient.getUberAuthTokenClient(okHttpClient)
                    .getAuthToken(mClientSecret, mClientId, "authorization_code",
                            authorizationCode, Uri.encode(mRedirectUri));
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }
    }
}

