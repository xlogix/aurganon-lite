package com.aurganonlite.android.helpers;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyAppWebViewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (Uri.parse(url).getHost().endsWith("aurganon.com")) {
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        view.getContext().startActivity(intent);
        return true;
    }
}