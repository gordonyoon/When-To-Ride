package com.example.gordonyoon.whentoride;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.gordonyoon.whentoride.models.User;
import com.example.gordonyoon.whentoride.uberapi.UberAuthTokenClient;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    @Bind(R.id.web_view) WebView mWebView;
    @BindString(R.string.uber_client_id) String mClientId;
    @BindString(R.string.uber_client_secret) String mClientSecret;
    @BindString(R.string.uber_redirect_uri) String mRedirectUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
                String authorizationCode = HttpUrl.parse(url).queryParameter("code");
                Call<User> call = UberAuthTokenClient.getUberAuthTokenClient().getAuthToken(
                        mClientSecret,
                        mClientId,
                        "authorization_code",
                        authorizationCode,
                        Uri.encode(mRedirectUri));
                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Response<User> response) {
                        if (response.isSuccess()) {
                            Log.i(TAG, "User authenticated!");
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                    }
                });
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }
    }
}

