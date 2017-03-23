package com.example.hoanbk.photogallery;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by hoanbk on 3/23/2017.
 */

public class PhotoPageFragment extends Visiblefragment {
    private String mUrl;
    private WebView mWebView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mUrl = getActivity().getIntent().getData().toString();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_page, container, false);

        mWebView = (WebView)v.findViewById(R.id.webView);

        // Enable JavaScript
        mWebView.getSettings().setJavaScriptEnabled(true);
        //
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // return true: <-> "Do not handle this URL, i am handling it myself"
                // return false <-> "Go ahead and load this URL, WebView, I am not do anything with it"
                return false;
            }
        });
        // load url
        mWebView.loadUrl(mUrl);

        final ProgressBar progressBar = (ProgressBar)v.findViewById(R.id.progressBar);
        progressBar.setMax(100);

        final TextView titleTextView = (TextView)v.findViewById(R.id.titleTextView);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                titleTextView.setText(title);
            }
        });

        return v;
    }
}
